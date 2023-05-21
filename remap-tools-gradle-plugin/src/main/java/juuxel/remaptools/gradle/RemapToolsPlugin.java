/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.gradle;

import juuxel.remaptools.gradle.internal.RemapToolsExtensionInternal;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * The plugin {@code io.github.juuxel.remap-tools}.
 */
public final class RemapToolsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getExtensions().create(RemapToolsExtension.class, RemapToolsExtension.NAME, RemapToolsExtensionInternal.class);
    }
}
