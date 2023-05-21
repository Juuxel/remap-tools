package juuxel.remaptools.gradle;

import org.gradle.api.provider.Property;

/**
 * Settings for remapping artifacts between JVM mapping sets.
 *
 * <p>New instances of this class can be obtained using an
 * {@linkplain org.gradle.api.model.ObjectFactory object factory}.
 */
public abstract class RemapConfiguration {
    /**
     * {@return the mapping configuration of this remap configuration}
     * It is used to provide a mapping set for remapping.
     */
    public abstract Property<MappingConfiguration> getMappings();

    /**
     * {@return the source namespace for remapping}
     */
    public abstract Property<String> getSourceNamespace();

    /**
     * {@return the target namespace for remapping}
     */
    public abstract Property<String> getTargetNamespace();
}
