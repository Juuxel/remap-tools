/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.gradle.loom;

import juuxel.remaptools.gradle.RemapToolsPlugin;
import juuxel.remaptools.gradle.loom.internal.RemapToolsLoomExtensionInternal;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * The plugin {@code io.github.juuxel.remap-tools.loom}.
 */
public final class RemapToolsLoomPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getPlugins().apply(RemapToolsPlugin.class);
        target.getExtensions().create(RemapToolsLoomExtension.class, RemapToolsLoomExtension.NAME, RemapToolsLoomExtensionInternal.class);
    }
}
