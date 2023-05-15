package juuxel.remaptools.gradle;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.jetbrains.annotations.ApiStatus;

import javax.inject.Inject;

/**
 * Settings for remapping artifacts between JVM mapping sets.
 *
 * <p>New instances of this class can be obtained using an
 * {@linkplain org.gradle.api.model.ObjectFactory object factory}.
 */
public abstract class RemapConfiguration {
    private final ObjectFactory factory;
    private final ProviderFactory providerFactory;

    @ApiStatus.Internal
    @Inject
    public RemapConfiguration(ObjectFactory factory, ProviderFactory providerFactory) {
        this.factory = factory;
        this.providerFactory = providerFactory;
    }

    public abstract ListProperty<MappingFileConfiguration> getMappingFiles();
    public abstract Property<String> getSourceNamespace();
    public abstract Property<String> getTargetNamespace();

    public void addMappingFile(Action<? super MappingFileConfiguration> action) {
        getMappingFiles().add(providerFactory.provider(() -> {
            MappingFileConfiguration configuration = factory.newInstance(MappingFileConfiguration.class);
            action.execute(configuration);
            return configuration;
        }));
    }

    public void addMappingFile(@DelegatesTo(value = MappingFileConfiguration.class, strategy = Closure.DELEGATE_FIRST) Closure<?> action) {
        addMappingFile(configuration -> {
            action.setDelegate(configuration);
            action.call(configuration);
        });
    }
}
