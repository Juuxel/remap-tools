package juuxel.remaptools.mixinobfuscator;

import org.spongepowered.tools.obfuscation.ObfuscationEnvironment;
import org.spongepowered.tools.obfuscation.ObfuscationType;
import org.spongepowered.tools.obfuscation.mapping.IMappingProvider;
import org.spongepowered.tools.obfuscation.mapping.IMappingWriter;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;

public class RemapToolsObfuscationEnvironment extends ObfuscationEnvironment {
    protected RemapToolsObfuscationEnvironment(ObfuscationType type) {
        super(type);
    }

    @Override
    protected IMappingProvider getMappingProvider(Messager messager, Filer filer) {
        String key = RemapToolsObfuscationService.SPECIAL_NAMES.inverse()
            .getOrDefault(type.getKey(), type.getKey());
        String[] namespaces = key.split(":", 2);
        return new MappingTreeMappingProvider(namespaces[0], namespaces[1]);
    }

    @Override
    protected IMappingWriter getMappingWriter(Messager messager, Filer filer) {
        return null;
    }
}
