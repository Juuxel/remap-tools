/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.gradle.task;

import juuxel.remaptools.gradle.MappingConfiguration;
import juuxel.remaptools.refmap.Refmap;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.FileSystemReference;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.bundling.Jar;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

/**
 * Creates a jar that is then remapped between mapping set namespaces.
 */
public class RemappingJar extends Jar {
    private final ConfigurableFileCollection remapClasspath = getProject().getObjects().fileCollection();
    private final Property<MappingConfiguration> mappings = getProject().getObjects().property(MappingConfiguration.class);
    private final Property<String> sourceNamespace = getProject().getObjects().property(String.class);
    private final Property<String> targetNamespace = getProject().getObjects().property(String.class);
    private final SetProperty<String> refmaps = getProject().getObjects().setProperty(String.class);
    private final SetProperty<String> refmapEnvironments = getProject().getObjects().setProperty(String.class);
    private final Property<Boolean> remapRefmapMainMappings = getProject().getObjects().property(Boolean.class).convention(true);

    public RemappingJar() {
        getInputs().property("mappings", getMappings().map(MappingConfiguration::asTaskInput));
    }

    /**
     * {@return the classpath of the classes to be remapped}
     */
    @Classpath
    public ConfigurableFileCollection getRemapClasspath() {
        return remapClasspath;
    }

    /**
     * {@return the mapping configuration of this remapping task}
     * It is used to provide a mapping set for remapping.
     */
    @Internal
    public Property<MappingConfiguration> getMappings() {
        return mappings;
    }

    /**
     * {@return the source namespace for remapping}
     */
    @Input
    public Property<String> getSourceNamespace() {
        return sourceNamespace;
    }

    /**
     * {@return the target namespace for remapping}
     */
    @Input
    public Property<String> getTargetNamespace() {
        return targetNamespace;
    }

    /**
     * {@return a set of all Mixin refmap file paths to remap}
     */
    @Input
    public SetProperty<String> getRefmaps() {
        return refmaps;
    }

    /**
     * {@return the Mixin refmap environments to remap}
     * An environment is a key in the {@code data} map.
     */
    @Input
    @Optional
    public SetProperty<String> getRefmapEnvironments() {
        return refmapEnvironments;
    }

    /**
     * {@return whether the {@code mappings} section in Mixin refmaps should be remapped}
     * Defaults to {@code true}.
     */
    @Input
    @Optional
    public Property<Boolean> getRemapRefmapMainMappings() {
        return remapRefmapMainMappings;
    }

    @Override
    protected void copy() {
        super.copy();

        try {
            remap();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void remap() throws IOException {
        Path input = Files.createTempFile(getTemporaryDir().toPath(), "input", ".jar");
        Path archive = getArchiveFile().get().getAsFile().toPath();
        Files.copy(archive, input, StandardCopyOption.REPLACE_EXISTING);
        Files.delete(archive);

        mappings.finalizeValue();
        sourceNamespace.finalizeValue();
        targetNamespace.finalizeValue();
        var fromM = sourceNamespace.get();
        var toM = targetNamespace.get();
        MemoryMappingTree mappingTree = mappings.get().readMappings();

        if (!fromM.equals(mappingTree.getSrcNamespace())) {
            MemoryMappingTree newTree = new MemoryMappingTree();
            mappingTree.accept(new MappingSourceNsSwitch(newTree, fromM));
            mappingTree = newTree;
        }

        Path mappings = Files.createTempFile(getTemporaryDir().toPath(), "mappings", ".tiny");
        try (var writer = MappingWriter.create(mappings, MappingFormat.TINY_2)) {
            mappingTree.accept(new MappingDstNsReorder(writer, toM));
        }

        try {
            TinyRemapper remapper = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(mappings, fromM, toM))
                .build();

            Path[] classpath = getRemapClasspath()
                .getFiles()
                .stream()
                .map(File::toPath)
                .toArray(Path[]::new);

            try (var outputConsumer = new OutputConsumerPath.Builder(archive).build()) {
                outputConsumer.addNonClassFiles(input);

                remapper.readInputs(input);
                remapper.readClassPath(classpath);

                remapper.apply(outputConsumer);
            } finally {
                remapper.finish();
            }

            getRefmaps().finalizeValue();
            getRefmapEnvironments().finalizeValue();
            getRemapRefmapMainMappings().finalizeValue();
            var refmaps = getRefmaps().get();
            if (!refmaps.isEmpty()) remapRefmaps(archive, refmaps, remapper);
        } finally {
            Files.deleteIfExists(mappings);
            Files.deleteIfExists(input);
        }
    }

    private void remapRefmaps(Path archive, Set<String> refmaps, TinyRemapper remapper) throws IOException {
        var environments = getRefmapEnvironments().get();
        var remapMainMappings = getRemapRefmapMainMappings().getOrElse(false);

        if (environments.isEmpty() && !remapMainMappings) {
            // Remapping not enabled.
            return;
        }

        try (var fs = FileSystemReference.openJar(archive, false)) {
            var missing = new HashSet<String>();
            for (var refmap : refmaps) {
                var refmapPath = fs.getPath(refmap);
                if (Files.notExists(refmapPath)) {
                    missing.add(refmap);
                    continue;
                }

                Refmap refmapObj;

                try (Reader reader = Files.newBufferedReader(refmapPath)) {
                    refmapObj = Refmap.read(reader);
                }

                for (var environment : environments) {
                    refmapObj = refmapObj.remap(environment, remapper.getEnvironment().getRemapper());
                }

                if (remapMainMappings) {
                    refmapObj = refmapObj.remapDefault(remapper.getEnvironment().getRemapper());
                }

                try (Writer writer = Files.newBufferedWriter(refmapPath)) {
                    refmapObj.write(writer);
                }
            }
        }
    }
}
