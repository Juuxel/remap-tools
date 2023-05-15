package juuxel.remaptools.mixinobfuscator;

import juuxel.remaptools.MappingTreeRemapper;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.objectweb.asm.commons.Remapper;

import java.io.File;
import java.io.IOException;

final class MappingTreeMappingProvider extends RemapperMappingProvider {
    private final String from;
    private final String to;
    private boolean empty;
    private Remapper remapper;

    MappingTreeMappingProvider(String from, String to) {
        this.from = from;
        this.to = to;
    }

    @Override
    protected Remapper getRemapper() {
        return remapper;
    }

    @Override
    public void clear() {
        remapper = null;
    }

    @Override
    public boolean isEmpty() {
        return remapper == null || empty;
    }

    @Override
    public void read(File input) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree();
        // TODO: Namespace replacement
        MappingReader.read(input.toPath(), tree);
        empty = tree.getClasses().isEmpty();
        remapper = MappingTreeRemapper.builder(tree)
            .namespaces(from, to)
            .completeNamespaces()
            .build();
    }
}
