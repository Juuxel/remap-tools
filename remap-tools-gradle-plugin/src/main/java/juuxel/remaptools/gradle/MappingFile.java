/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
public abstract class MappingFile {
    @ApiStatus.Internal
    public MappingFile() {
        // Matches the Fabric toolchain for Proguard files.
        // We don't support Enigma directories, so official/intermediary -> named should be rare.
        getDefaultSourceNamespace().convention("named");
        getDefaultTargetNamespace().convention("official");

        getMergeNamespace().convention("official");
    }

    /**
     * {@return the input mapping text}
     */
    public abstract Property<TextResource> getMappings();

    /**
     * {@return the source namespace to be used if absent from the file}
     */
    public abstract Property<String> getDefaultSourceNamespace();
    /**
     * {@return the target namespace to be used if absent from the file}
     */
    public abstract Property<String> getDefaultTargetNamespace();

    /**
     * Returns the namespace to use when merging this mapping file into
     * a {@linkplain FileMappingConfiguration file mapping configuration}.
     *
     * <p>When merging, an entry in the current configuration mapping tree will be considered matching to an entry
     * in this mapping file if they share their name and descriptor in this namespace.
     *
     * @return the merge namespace
     */
    public abstract Property<String> getMergeNamespace();
}
