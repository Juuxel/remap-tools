package juuxel.remaptools.refmap;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.commons.Remapper;

import java.util.Objects;

/**
 * Entries in a {@linkplain Refmap mixin refmap}.
 */
public sealed interface RefmapEntry {
    /**
     * Remaps this refmap entry using a remapper.
     *
     * <p>This method is allowed to return the original entry if it doesn't change under remapping.
     *
     * @param remapper the remapper
     * @return the remapped entry
     */
    RefmapEntry remap(Remapper remapper);

    /**
     * Visits this entry with the visitor.
     *
     * @param visitor the visitor
     * @return the return value of visiting this entry
     * @param <R> the return type of the visitor
     */
    <R> R visit(Visitor<R> visitor);

    /**
     * A class in a refmap.
     *
     * @param name the internal name of the specified class
     */
    record ClassEntry(String name) implements RefmapEntry {
        public ClassEntry {
            Objects.requireNonNull(name, "name");
        }

        @Override
        public ClassEntry remap(Remapper remapper) {
            var newName = remapper.map(name);
            return !name.equals(newName) ? new ClassEntry(newName) : this;
        }

        @Override
        public <R> R visit(Visitor<R> visitor) {
            return visitor.visitClass(this);
        }
    }

    /**
     * A method in a refmap.
     *
     * @param owner      the internal name of the containing class of the method, or {@code null} if not specified
     * @param name       the name of the method
     * @param descriptor the descriptor of the method
     */
    record MethodEntry(@Nullable String owner, String name, String descriptor) implements RefmapEntry {
        public MethodEntry {
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(descriptor, "descriptor");
        }

        @Override
        public MethodEntry remap(Remapper remapper) {
            var newOwner = owner != null ? remapper.map(owner) : null;
            var newName = remapper.mapMethodName(owner, name, descriptor);
            var newDesc = remapper.mapMethodDesc(descriptor);
            return !Objects.equals(owner, newOwner) || !name.equals(newName) || !descriptor.equals(newDesc)
                ? new MethodEntry(newOwner, newName, newDesc) : this;
        }

        @Override
        public <R> R visit(Visitor<R> visitor) {
            return visitor.visitMethod(this);
        }
    }

    /**
     * A field in a refmap.
     *
     * @param owner      the internal name of the containing class of the field, or {@code null} if not specified
     * @param name       the name of the field
     * @param descriptor the descriptor of the field
     */
    record FieldEntry(@Nullable String owner, String name, String descriptor) implements RefmapEntry {
        public FieldEntry {
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(descriptor, "descriptor");
        }

        @Override
        public FieldEntry remap(Remapper remapper) {
            var newOwner = owner != null ? remapper.map(owner) : null;
            var newName = remapper.mapFieldName(owner, name, descriptor);
            var newDesc = remapper.mapDesc(descriptor);
            return !Objects.equals(owner, newOwner) || !name.equals(newName) || !descriptor.equals(newDesc)
                ? new FieldEntry(newOwner, newName, newDesc) : this;
        }

        @Override
        public <R> R visit(Visitor<R> visitor) {
            return visitor.visitField(this);
        }
    }

    /**
     * A visitor for {@link RefmapEntry}.
     * @param <R> the return value of visiting an entry
     */
    interface Visitor<R> {
        /**
         * Visits a class entry.
         *
         * @param entry the visited entry
         * @return an arbitrary value
         */
        R visitClass(ClassEntry entry);

        /**
         * Visits a method entry.
         *
         * @param entry the visited entry
         * @return an arbitrary value
         */
        R visitMethod(MethodEntry entry);

        /**
         * Visits a field entry.
         *
         * @param entry the visited entry
         * @return an arbitrary value
         */
        R visitField(FieldEntry entry);
    }
}
