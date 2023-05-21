/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.gradle.task;

import juuxel.remaptools.gradle.MappingConfiguration;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.bundling.Jar;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Creates a jar that is then remapped between mapping set namespaces.
 */
public class RemappingJar extends Jar {
    private final ConfigurableFileCollection remapClasspath = getProject().getObjects().fileCollection();
    private final Property<MappingConfiguration> mappings = getProject().getObjects().property(MappingConfiguration.class);
    private final Property<String> sourceNamespace = getProject().getObjects().property(String.class);
    private final Property<String> targetNamespace = getProject().getObjects().property(String.class);

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
        Files.copy(input, archive, StandardCopyOption.REPLACE_EXISTING);
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
        } finally {
            Files.deleteIfExists(mappings);
            Files.deleteIfExists(input);
        }
    }
}
