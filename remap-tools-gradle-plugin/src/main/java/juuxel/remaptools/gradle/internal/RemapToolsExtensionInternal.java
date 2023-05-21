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
