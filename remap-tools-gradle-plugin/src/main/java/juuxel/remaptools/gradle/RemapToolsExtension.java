package juuxel.remaptools.gradle;

import org.gradle.api.provider.Provider;
import org.gradle.api.resources.TextResource;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface RemapToolsExtension {
    Provider<TextResource> mojangMappings(String minecraftVersion);
}
