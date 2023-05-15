/**
 * A set of tools for remapping files between different JVM obfuscation mapping sets.
 */
module io.github.juuxel.remaptools {
    exports juuxel.remaptools;
    exports juuxel.remaptools.refmap;

    requires transitive org.objectweb.asm.commons;
    requires com.google.gson;
    requires static net.fabricmc.mappingio; // only for some types
    requires static org.jetbrains.annotations;
}
