package juuxel.remaptools.gradle;

import org.gradle.api.Action;
import org.jetbrains.annotations.ApiStatus;

/**
 * The {@value #NAME} extension.
 */
@ApiStatus.NonExtendable
public interface RemapToolsExtension {
    /**
     * The name of this extension ({@value}).
     */
    String NAME = "remapTools";

    /**
     * Creates a new {@linkplain MappingConfiguration mapping configuration} based on
     * {@linkplain MappingFile mapping files}.
     *
     * @param action the configuration action
     * @return the created configuration
     */
    MappingConfiguration fileMappings(Action<? super FileMappingConfiguration> action);
}
