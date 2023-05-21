package juuxel.remaptools.gradle;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.jetbrains.annotations.ApiStatus;

/**
 * A spec for defining {@linkplain MappingConfiguration mapping configurations} based on
 * {@linkplain MappingFile mapping files}.
 */
@ApiStatus.NonExtendable
public interface FileMappingConfiguration extends MappingConfiguration {
    /**
     * {@return the mapping files in this configuration}
     */
    @Input
    ListProperty<MappingFile> getMappingFiles();

    /**
     * Creates, configures and adds a new mapping file to this configuration.
     * @param action the configuration action
     */
    void mappingFile(Action<? super MappingFile> action);

    /**
     * Creates, configures and adds a new mapping file to this configuration.
     * @param action the configuration action
     */
    default void mappingFile(@DelegatesTo(value = MappingFile.class, strategy = Closure.DELEGATE_FIRST) Closure<?> action) {
        mappingFile(configuration -> {
            action.setDelegate(configuration);
            action.call(configuration);
        });
    }
}
