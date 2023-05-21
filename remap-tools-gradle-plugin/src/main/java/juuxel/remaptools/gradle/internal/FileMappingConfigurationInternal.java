/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.gradle.internal;

import juuxel.remaptools.gradle.FileMappingConfiguration;
import juuxel.remaptools.gradle.MappingFile;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingUtil;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.resources.TextResource;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public abstract class FileMappingConfigurationInternal implements FileMappingConfiguration {
    @Inject
    protected abstract ObjectFactory getObjectFactory();
    @Inject
    protected abstract ProviderFactory getProviderFactory();

    public void mappingFile(Action<? super MappingFile> action) {
        getMappingFiles().add(getProviderFactory().provider(() -> {
            MappingFile configuration = getObjectFactory().newInstance(MappingFile.class);
            action.execute(configuration);
            return configuration;
        }));
    }

    @Override
    public MemoryMappingTree readMappings() throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();
        String currentNamespace = null;

        for (MappingFile mappingFile : getMappingFiles().get()) {
            var mergeNamespace = mappingFile.getMergeNamespace().get();
            if (currentNamespace == null || !currentNamespace.equals(mergeNamespace)) {
                currentNamespace = mergeNamespace;

                MemoryMappingTree newTree = new MemoryMappingTree();
                tree.accept(new MappingSourceNsSwitch(newTree, mergeNamespace));
                tree = newTree;
            }

            readIntoVisitor(tree, mappingFile);
        }

        return tree;
    }

    private static void readIntoVisitor(MappingVisitor visitor, MappingFile mappingFile) throws IOException {
        TextResource mappings = mappingFile.getMappings().get();
        MappingFormat format;

        try (Reader reader = mappings.asReader()) {
            format = MappingReader.detectFormat(reader);
        }

        MappingVisitor realVisitor = visitor;

        if (!format.hasNamespaces) {
            realVisitor = new MappingNsRenamer(realVisitor, Map.of(
                MappingUtil.NS_SOURCE_FALLBACK, mappingFile.getDefaultSourceNamespace().get(),
                MappingUtil.NS_TARGET_FALLBACK, mappingFile.getDefaultTargetNamespace().get()
            ));
        }

        try (Reader reader = mappings.asReader()) {
            MappingReader.read(reader, format, realVisitor);
        }
    }
}
