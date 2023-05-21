/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.remaptools.refmap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.objectweb.asm.commons.Remapper;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A mixin reference mapper.
 *
 * @param mappings the default mappings (mixin → input → output)
 * @param data all mappings (environment → mixin → input → output)
 */
public record Refmap(
    Map<String, Map<String, RefmapEntry>> mappings,
    Map<String, Map<String, Map<String, RefmapEntry>>> data
) {
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(RefmapEntry.class, new RefmapEntryAdapter())
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    /**
     * Creates a refmap from a JSON object.
     *
     * @param json the JSON object
     * @return the read refmap
     * @throws com.google.gson.JsonParseException if the JSON is invalid for a refmap
     */
    public static Refmap fromJson(JsonObject json) {
        return GSON.fromJson(json, Refmap.class);
    }

    /**
     * Reads a refmap from a reader.
     *
     * @param reader the reader
     * @return the read refmap
     * @throws com.google.gson.JsonParseException if the JSON could not be read or is invalid for a refmap
     */
    public static Refmap read(Reader reader) {
        return GSON.fromJson(reader, Refmap.class);
    }

    /**
     * {@return a JSON object representing this refmap}
     */
    public JsonObject toJson() {
        return (JsonObject) GSON.toJsonTree(this);
    }

    /**
     * Writes this refmap to an {@link Appendable}.
     *
     * @param out the appendable to write to
     */
    public void write(Appendable out) throws IOException {
        GSON.toJson(this, out);
    }

    /**
     * Renames a mixin environment between two names.
     * If the original name is not present, returns this refmap.
     *
     * @param from the source environment name
     * @param to   the target environment name
     * @return a refmap where the {@code from} environment has been replaced with {@code to}
     */
    public Refmap renameEnvironment(String from, String to) {
        if (!data.containsKey(from)) {
            return this;
        }

        Map<String, Map<String, Map<String, RefmapEntry>>> newData = new HashMap<>(data);
        var value = newData.remove(from);
        newData.put(to, value);
        return new Refmap(mappings, Collections.unmodifiableMap(newData));
    }

    /**
     * Remaps all names in an environment in {@link #data} with a remapper.
     *
     * @param environment the environment to remap
     * @param remapper    the remapper
     * @return the remapped refmap
     */
    public Refmap remap(String environment, Remapper remapper) {
        return new Refmap(
            mappings,
            transformMap(data, (currentEnvironment, mixinsToEntries) -> {
                if (environment.equals(currentEnvironment)) {
                    return transformMap(mixinsToEntries,
                        (mixin, entries) -> transformMap(entries, (key, entry) -> entry.remap(remapper)));
                }

                return mixinsToEntries;
            })
        );
    }

    /**
     * Remaps all names in the default environment in {@link #mappings} with a remapper.
     *
     * @param remapper the remapper
     * @return the remapped refmap
     */
    public Refmap remapDefault(Remapper remapper) {
        return new Refmap(
            transformMap(mappings,
                (mixin, entries) -> transformMap(entries, (key, entry) -> entry.remap(remapper))),
            data
        );
    }

    private static <K, V, W> Map<K, W> transformMap(Map<K, V> map, BiFunction<K, V, W> mapper) {
        return map.entrySet().stream()
            .map(entry -> Map.entry(entry.getKey(), mapper.apply(entry.getKey(), entry.getValue())))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
