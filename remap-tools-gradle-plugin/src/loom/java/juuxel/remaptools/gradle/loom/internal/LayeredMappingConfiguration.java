/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.gradle.loom.internal;

import juuxel.remaptools.gradle.MappingConfiguration;
import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.configuration.providers.mappings.GradleMappingContext;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingSpec;
import net.fabricmc.loom.configuration.providers.mappings.LayeredMappingsProcessor;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

import javax.inject.Inject;
import java.io.IOException;

public abstract class LayeredMappingConfiguration implements MappingConfiguration {
    public LayeredMappingConfiguration() {
        getVersion().convention(getSpec().map(LayeredMappingSpec::getVersion));
    }

    @Inject
    protected abstract Project getProject();

    @Internal
    public abstract Property<LayeredMappingSpec> getSpec();

    @Input
    public abstract Property<String> getVersion();

    @Override
    public MemoryMappingTree readMappings() throws IOException {
        var spec = getSpec().get();
        MappingContext context = new GradleMappingContext(getProject(), spec.getVersion().replace("+", "_").replace(".", "_"));
        LayeredMappingsProcessor processor = new LayeredMappingsProcessor(spec);
        return processor.getMappings(processor.resolveLayers(context));
    }
}
