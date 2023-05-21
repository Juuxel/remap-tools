/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.gradle.task;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import juuxel.remaptools.gradle.RemapConfiguration;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
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
    private final Property<RemapConfiguration> remapConfiguration = getProject().getObjects().property(RemapConfiguration.class);

    /**
     * {@return the classpath of the classes to be remapped}
     */
    @Classpath
    public ConfigurableFileCollection getRemapClasspath() {
        return remapClasspath;
    }

    /**
     * {@return the remap configuration for remapping}
     */
    @Input
    public Property<RemapConfiguration> getRemapConfiguration() {
        return remapConfiguration;
    }

    /**
     * Creates, configures and sets the {@linkplain #getRemapConfiguration() remap configuration}.
     * @param action the configuration action
     */
    public void remapConfiguration(Action<? super RemapConfiguration> action) {
        remapConfiguration.set(getProject().provider(() -> {
            RemapConfiguration rc = getProject().getObjects().newInstance(RemapConfiguration.class);
            action.execute(rc);
            return rc;
        }));
    }

    /**
     * Creates, configures and sets the {@linkplain #getRemapConfiguration() remap configuration}.
     * @param action the configuration action
     */
    public void remapConfiguration(@DelegatesTo(value = RemapConfiguration.class, strategy = Closure.DELEGATE_FIRST) Closure<?> action) {
        remapConfiguration(rc -> {
            action.setDelegate(rc);
            action.call(rc);
        });
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

        getRemapConfiguration().finalizeValue();
        RemapConfiguration rc = getRemapConfiguration().get();
        var fromM = rc.getSourceNamespace().get();
        var toM = rc.getTargetNamespace().get();
        MemoryMappingTree mappingTree = rc.getMappings().get().readMappings();

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
