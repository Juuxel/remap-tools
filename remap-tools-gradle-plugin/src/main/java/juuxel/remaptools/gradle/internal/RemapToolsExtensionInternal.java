package juuxel.remaptools.gradle.internal;

import juuxel.remaptools.gradle.RemapToolsExtension;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.resources.TextResource;

import javax.inject.Inject;

public abstract class RemapToolsExtensionInternal implements RemapToolsExtension {
    @Inject
    protected abstract Project getProject();

    @Override
    public Provider<TextResource> mojangMappings(String minecraftVersion) {
        return getProject().provider(() -> {
            // TODO: getProject().getResources().getText().fromUri();
            return null;
        });
    }
}
