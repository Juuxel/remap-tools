/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools;

import net.fabricmc.mappingio.adapter.MappingNsCompleter;
import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.commons.Remapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;

/**
 * A remapper that wraps {@link MappingTreeView}.
 */
public final class MappingTreeRemapper extends Remapper {
    private final MappingTreeView tree;
    private final int from;
    private final int to;

    private MappingTreeRemapper(MappingTreeView tree, String from, String to) {
        this.tree = Objects.requireNonNull(tree, "tree");
        this.from = getNamespaceId(tree, Objects.requireNonNull(from, "from"));
        this.to = getNamespaceId(tree, Objects.requireNonNull(to, "to"));
    }

    private static int getNamespaceId(MappingTreeView tree, String namespace) {
        int id = tree.getNamespaceId(namespace);

        if (id < MappingTreeView.MIN_NAMESPACE_ID) {
            throw new IllegalArgumentException(
                "Namespace '" + namespace +
                    "' not present in mapping tree. Available: src: " + tree.getSrcNamespace() +
                    ", dst: " + tree.getDstNamespaces());
        }

        return id;
    }

    private String getNameOrDefault(MappingTreeView.ElementMappingView element, String defaultValue) {
        var targetName = element.getName(to);
        return targetName != null ? targetName : defaultValue;
    }

    @Override
    public String map(String internalName) {
        return tree.mapClassName(internalName, from, to);
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        var ownerMapping = tree.getClass(owner, from);
        if (ownerMapping == null) return name;

        var mapping = ownerMapping.getMethod(name, descriptor, from);
        return mapping != null ? getNameOrDefault(mapping, name) : name;
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        var ownerMapping = tree.getClass(owner, from);
        if (ownerMapping == null) return name;

        var mapping = ownerMapping.getField(name, descriptor, from);
        return mapping != null ? getNameOrDefault(mapping, name) : name;
    }

    @Override
    public String mapRecordComponentName(String owner, String name, String descriptor) {
        return mapFieldName(owner, name, descriptor);
    }

    /**
     * Creates a builder for a {@code MappingTreeRemapper}.
     *
     * @param tree the backing mapping tree
     * @return the builder
     */
    public static Builder builder(MappingTreeView tree) {
        return new Builder(Objects.requireNonNull(tree, "tree"));
    }

    /**
     * A builder for a {@link MappingTreeRemapper}.
     */
    public static final class Builder {
        private final MappingTreeView tree;
        private @Nullable String from;
        private @Nullable String to;
        private boolean completeNamespaces = false;
        private @Nullable String completionNamespace;

        private Builder(MappingTreeView tree) {
            this.tree = tree;
        }

        /**
         * Sets the namespaces of this remapper.
         *
         * @param from the input namespace
         * @param to   the output namespace
         * @return this builder
         */
        public Builder namespaces(String from, String to) {
            this.from = from;
            this.to = to;
            return this;
        }

        /**
         * Enables namespace completion. When this remapper is {@linkplain #build built},
         * the missing names in the input namespace will be filled in with source names.
         *
         * @return this builder
         */
        public Builder completeNamespaces() {
            this.completeNamespaces = true;
            return this;
        }

        /**
         * Enables namespace completion. When this remapper is {@linkplain #build built},
         * the missing names in the input namespace will be filled in with names from the specified namespace.
         *
         * @param completionNamespace the namespace to use for filling
         * @return this builder
         */
        public Builder completeNamespaces(String completionNamespace) {
            this.completeNamespaces = true;
            this.completionNamespace = Objects.requireNonNull(completionNamespace, "completion namespace");
            return this;
        }

        /**
         * Builds this remapper.
         *
         * @return the built remapper
         */
        public MappingTreeRemapper build() {
            Objects.requireNonNull(from, "missing input namespace");
            Objects.requireNonNull(to, "missing output namespace");
            MappingTreeView tree;

            if (completeNamespaces && !from.equals(this.tree.getSrcNamespace())) {
                MemoryMappingTree mmt = new MemoryMappingTree();

                try {
                    String inputNs = this.completionNamespace != null ? this.completionNamespace : this.tree.getSrcNamespace();
                    this.tree.accept(new MappingNsCompleter(mmt, Map.of(inputNs, from)));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }

                tree = mmt;
            } else {
                tree = this.tree;
            }

            return new MappingTreeRemapper(tree, from, to);
        }
    }
}
