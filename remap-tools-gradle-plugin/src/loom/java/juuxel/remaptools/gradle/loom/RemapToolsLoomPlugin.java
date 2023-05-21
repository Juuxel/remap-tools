package juuxel.remaptools.gradle.loom;

import juuxel.remaptools.gradle.loom.internal.RemapToolsLoomExtensionInternal;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * The plugin {@code io.github.juuxel.remap-tools.loom}.
 */
public final class RemapToolsLoomPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getExtensions().create(RemapToolsLoomExtension.class, RemapToolsLoomExtension.NAME, RemapToolsLoomExtensionInternal.class);
    }
}
