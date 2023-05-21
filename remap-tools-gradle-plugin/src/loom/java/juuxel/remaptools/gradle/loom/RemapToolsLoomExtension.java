/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.gradle.loom;

import juuxel.remaptools.gradle.MappingConfiguration;
import net.fabricmc.loom.api.mappings.layered.spec.LayeredMappingSpecBuilder;
import org.gradle.api.Action;
import org.jetbrains.annotations.ApiStatus;

/**
 * The {@value #NAME} extension.
 */
@ApiStatus.NonExtendable
public interface RemapToolsLoomExtension {
    /**
     * The name of this extension ({@value}).
     */
    String NAME = "remapToolsLoom";

    /**
     * Creates and configures a layered mapping configuration.
     *
     * @param action the configuration action
     * @return the created mapping configuration
     */
    MappingConfiguration layered(Action<LayeredMappingSpecBuilder> action);
}
