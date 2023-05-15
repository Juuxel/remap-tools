package juuxel.remaptools.refmap;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.regex.Pattern;

final class RefmapEntryAdapter extends TypeAdapter<RefmapEntry> {
    private static final Pattern FIELD_PATTERN = Pattern.compile("(L[^;];)?(.+):(.+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("(L[^;];)?(.+)(\\(.*\\).+)");

    @Override
    public void write(JsonWriter out, RefmapEntry value) throws IOException {
        String str = value.visit(new RefmapEntry.Visitor<>() {
            private String writeClassDesc(@Nullable String name) {
                return name != null ? "L" + name + ";" : "";
            }

            @Override
            public String visitClass(RefmapEntry.ClassEntry entry) {
                return writeClassDesc(entry.name());
            }

            @Override
            public String visitMethod(RefmapEntry.MethodEntry entry) {
                return writeClassDesc(entry.owner()) + entry.name() + entry.descriptor();
            }

            @Override
            public String visitField(RefmapEntry.FieldEntry entry) {
                return writeClassDesc(entry.owner()) + entry.name() + ":" + entry.descriptor();
            }
        });
        out.value(str);
    }

    @Override
    public RefmapEntry read(JsonReader in) throws IOException {
        String input = in.nextString();

        if (input.contains(":")) {
            var matcher = FIELD_PATTERN.matcher(input);
            if (matcher.matches()) {
                return new RefmapEntry.FieldEntry(matcher.group(1), matcher.group(2), matcher.group(3));
            }
        }

        if (input.contains("(")) {
            var matcher = METHOD_PATTERN.matcher(input);
            if (matcher.matches()) {
                return new RefmapEntry.MethodEntry(matcher.group(1), matcher.group(2), matcher.group(3));
            }
        }

        return new RefmapEntry.ClassEntry(input);
    }
}
