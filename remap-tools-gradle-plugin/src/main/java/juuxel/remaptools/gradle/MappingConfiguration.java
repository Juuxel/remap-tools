package juuxel.remaptools.gradle;

import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.IOException;

/**
 * A mapping configuration that can produce a mapping tree.
 *
 * <p>Implementing types should be Gradle managed types or serializable,
 * i.e. usable as task inputs.
 */
public interface MappingConfiguration {
    /**
     * Reads mappings as defined by this mapping configuration.
     *
     * @return the mappings read as a {@link MemoryMappingTree}
     */
    MemoryMappingTree readMappings() throws IOException;
}
