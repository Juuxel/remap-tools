package juuxel.remaptools.gradle;

import org.gradle.api.provider.Property;
import org.gradle.api.resources.TextResource;
import org.jetbrains.annotations.ApiStatus;

/**
 * A mapping file together with its settings.
 *
 * <p>New instances of this class can be obtained using an
 * {@linkplain org.gradle.api.model.ObjectFactory object factory}.
 */
public abstract class MappingFileConfiguration {
    @ApiStatus.Internal
    public MappingFileConfiguration() {
        // Matches the Fabric toolchain for Proguard files.
        // We don't support Enigma directories, so official/intermediary -> named should be rare.
        getDefaultSourceNamespace().convention("named");
        getDefaultTargetNamespace().convention("official");

        getMergeNamespace().convention("official");
    }

    public abstract Property<TextResource> getMappings();
    public abstract Property<String> getDefaultSourceNamespace();
    public abstract Property<String> getDefaultTargetNamespace();
    public abstract Property<String> getMergeNamespace();
}
