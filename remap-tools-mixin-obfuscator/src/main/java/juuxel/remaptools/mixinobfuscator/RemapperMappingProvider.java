package juuxel.remaptools.mixinobfuscator;

import juuxel.remaptools.refmap.RefmapEntry;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.commons.Remapper;
import org.spongepowered.asm.obfuscation.mapping.common.MappingField;
import org.spongepowered.asm.obfuscation.mapping.common.MappingMethod;
import org.spongepowered.tools.obfuscation.mapping.IMappingProvider;

abstract class RemapperMappingProvider implements IMappingProvider {
    protected abstract Remapper getRemapper();

    @Override
    public @Nullable MappingMethod getMethodMapping(MappingMethod method) {
        var entry = new RefmapEntry.MethodEntry(method.getOwner(), method.getSimpleName(), method.getDesc());
        var newEntry = entry.remap(getRemapper());
        if (entry.equals(newEntry)) return null;
        return new MappingMethod(newEntry.owner(), newEntry.name(), newEntry.descriptor());
    }

    @Override
    public @Nullable MappingField getFieldMapping(MappingField field) {
        var entry = new RefmapEntry.FieldEntry(field.getOwner(), field.getSimpleName(), field.getDesc());
        var newEntry = entry.remap(getRemapper());
        if (entry.equals(newEntry)) return null;
        return new MappingField(newEntry.owner(), newEntry.name(), newEntry.descriptor());
    }

    @Override
    public @Nullable String getClassMapping(String className) {
        var entry = new RefmapEntry.ClassEntry(className);
        var newEntry = entry.remap(getRemapper());
        if (entry.equals(newEntry)) return null;
        return newEntry.name();
    }

    @Override
    public @Nullable String getPackageMapping(String packageName) {
        var newName = getRemapper().mapPackageName(packageName);
        return !packageName.equals(newName) ? newName : null;
    }
}
