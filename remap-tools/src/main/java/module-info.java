/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
