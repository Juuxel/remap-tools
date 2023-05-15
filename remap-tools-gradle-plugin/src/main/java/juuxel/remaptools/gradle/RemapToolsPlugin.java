package juuxel.remaptools.gradle;

import juuxel.remaptools.gradle.internal.RemapToolsExtensionInternal;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RemapToolsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getExtensions().create(RemapToolsExtension.class, "remapTools", RemapToolsExtensionInternal.class);
    }
}
