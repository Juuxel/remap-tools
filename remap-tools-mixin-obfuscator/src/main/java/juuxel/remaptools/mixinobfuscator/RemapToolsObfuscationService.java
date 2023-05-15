package juuxel.remaptools.mixinobfuscator;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingUtil;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.spongepowered.tools.obfuscation.interfaces.IMixinAnnotationProcessor;
import org.spongepowered.tools.obfuscation.service.IObfuscationService;
import org.spongepowered.tools.obfuscation.service.ObfuscationTypeDescriptor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public final class RemapToolsObfuscationService implements IObfuscationService {
    public static final String MAPPING_FILE_OPTION = "mappingFile";
    public static final String MAPPING_FALLBACK_SRC_NS_OPTION = "mappingNamespaces.defaultSource";
    public static final String MAPPING_FALLBACK_DST_NS_OPTION = "mappingNamespaces.defaultTarget";
    static final BiMap<String, String> SPECIAL_NAMES = ImmutableBiMap.of("named:srg", "searge");

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of(MAPPING_FILE_OPTION, MAPPING_FALLBACK_SRC_NS_OPTION, MAPPING_FALLBACK_DST_NS_OPTION);
    }

    @Override
    public Collection<ObfuscationTypeDescriptor> getObfuscationTypes(IMixinAnnotationProcessor ap) {
        var mappingFilePath = ap.getOption(MAPPING_FILE_OPTION);
        if (mappingFilePath == null) throw new IllegalArgumentException("Missing required option " + MAPPING_FILE_OPTION);

        var mappingFile = Path.of(mappingFilePath);
        var tree = new MemoryMappingTree();

        try {
            MappingVisitor visitor = tree;

            // Set up namespace replacements
            Map<String, String> replacements = new HashMap<>();
            var fallbackSrc = ap.getOption(MAPPING_FALLBACK_SRC_NS_OPTION);
            if (fallbackSrc != null) replacements.put(MappingUtil.NS_SOURCE_FALLBACK, fallbackSrc);
            var fallbackDst = ap.getOption(MAPPING_FALLBACK_DST_NS_OPTION);
            if (fallbackDst != null) replacements.put(MappingUtil.NS_TARGET_FALLBACK, fallbackSrc);

            if (!replacements.isEmpty()) {
                visitor = new MappingNsRenamer(visitor, replacements);
            }

            MappingReader.read(mappingFile, visitor);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read mappings", e);
        }

        Set<String> namespaces = new HashSet<>(tree.getDstNamespaces());
        namespaces.add(tree.getSrcNamespace());

        ImmutableList.Builder<ObfuscationTypeDescriptor> descriptors = ImmutableList.builder();
        forEachPair(namespaces, (from, to) -> {
            String mainName = from + ":" + to;
            String[] names = SPECIAL_NAMES.containsKey(mainName)
                ? new String[] { mainName, SPECIAL_NAMES.get(mainName) }
                : new String[] { mainName };

            for (String name : names) {
                descriptors.add(new ObfuscationTypeDescriptor(
                    name,
                    /* inputFileArgName */ MAPPING_FILE_OPTION,
                    /* outFileArgName */  null,
                    RemapToolsObfuscationEnvironment.class
                ));
            }
        });
        return descriptors.build();
    }

    private static void forEachPair(Set<String> strings, BiConsumer<String, String> consumer) {
        for (String a : strings) {
            for (String b : strings) {
                if (a.equals(b)) continue;
                consumer.accept(a, b);
            }
        }
    }
}
