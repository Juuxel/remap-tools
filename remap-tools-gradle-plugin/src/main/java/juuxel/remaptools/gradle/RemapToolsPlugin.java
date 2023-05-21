package juuxel.remaptools.gradle;

import juuxel.remaptools.gradle.internal.RemapToolsExtensionInternal;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * The plugin {@code io.github.juuxel.remap-tools}.
 */
public final class RemapToolsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getExtensions().create(RemapToolsExtension.class, RemapToolsExtension.NAME, RemapToolsExtensionInternal.class);
    }
}
