/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.gradle.internal;

import juuxel.remaptools.gradle.FileMappingConfiguration;
import juuxel.remaptools.gradle.MappingConfiguration;
import juuxel.remaptools.gradle.RemapToolsExtension;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class RemapToolsExtensionInternal implements RemapToolsExtension {
    @Inject
    protected abstract ObjectFactory getObjectFactory();

    @Override
    public MappingConfiguration fileMappings(Action<? super FileMappingConfiguration> action) {
        var configuration = getObjectFactory().newInstance(FileMappingConfigurationInternal.class);
        action.execute(configuration);
        return configuration;
    }
}
