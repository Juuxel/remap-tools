package juuxel.remaptools.gradle.internal;

import juuxel.remaptools.gradle.MappingFileConfiguration;
import juuxel.remaptools.gradle.RemapConfiguration;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingUtil;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.gradle.api.resources.TextResource;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Map;

public final class RemapConfigurationHelper {
    public static MemoryMappingTree readMappings(RemapConfiguration configuration) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();
        String currentNamespace = null;

        for (MappingFileConfiguration mappingConfiguration : configuration.getMappingFiles().get()) {
            var mergeNamespace = mappingConfiguration.getMergeNamespace().get();
            if (currentNamespace == null || !currentNamespace.equals(mergeNamespace)) {
                currentNamespace = mergeNamespace;

                MemoryMappingTree newTree = new MemoryMappingTree();
                tree.accept(new MappingSourceNsSwitch(newTree, mergeNamespace));
                tree = newTree;
            }

            readIntoVisitor(tree, mappingConfiguration);
        }

        var sourceNamespace = configuration.getSourceNamespace().get();
        if (!sourceNamespace.equals(currentNamespace)) {
            MemoryMappingTree newTree = new MemoryMappingTree();
            tree.accept(new MappingSourceNsSwitch(newTree, sourceNamespace));
            tree = newTree;
        }

        return tree;
    }

    public static void readIntoVisitor(MappingVisitor visitor, MappingFileConfiguration configuration) throws IOException {
        TextResource mappings = configuration.getMappings().get();
        MappingFormat format;

        try (Reader reader = mappings.asReader()) {
            format = MappingReader.detectFormat(reader);
        }

        MappingVisitor realVisitor = visitor;

        if (!format.hasNamespaces) {
            realVisitor = new MappingNsRenamer(realVisitor, Map.of(
                MappingUtil.NS_SOURCE_FALLBACK, configuration.getDefaultSourceNamespace().get(),
                MappingUtil.NS_TARGET_FALLBACK, configuration.getDefaultTargetNamespace().get()
            ));
        }

        try (Reader reader = mappings.asReader()) {
            MappingReader.read(reader, format, realVisitor);
        }
    }
}
