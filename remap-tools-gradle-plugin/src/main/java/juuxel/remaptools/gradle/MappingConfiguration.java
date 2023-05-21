/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.gradle;

import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.IOException;

/**
 * A mapping configuration that can produce a mapping tree.
 */
public interface MappingConfiguration {
    /**
     * Reads mappings as defined by this mapping configuration.
     *
     * @return the mappings read as a {@link MemoryMappingTree}
     */
    MemoryMappingTree readMappings() throws IOException;

    /**
     * {@return an object that uniquely represents this mapping configuration as a task input}
     */
    Object asTaskInput();
}
