/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.gradle.loom.internal;

import juuxel.remaptools.gradle.MappingConfiguration;
import juuxel.remaptools.gradle.loom.RemapToolsLoomExtension;
import net.fabricmc.loom.api.mappings.layered.spec.LayeredMappingSpecBuilder;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingSpecBuilderImpl;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class RemapToolsLoomExtensionInternal implements RemapToolsLoomExtension {
    @Inject
    protected abstract Project getProject();

    @Override
    public MappingConfiguration layered(Action<LayeredMappingSpecBuilder> action) {
        var builder = new LayeredMappingSpecBuilderImpl();
        action.execute(builder);
        var configuration = getProject().getObjects().newInstance(LayeredMappingConfiguration.class);
        configuration.getSpec().set(builder.build());
        return configuration;
    }
}
