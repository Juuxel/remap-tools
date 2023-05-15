module io.github.juuxel.remaptools.mixinobfuscator {
    requires io.github.juuxel.remaptools;
    requires net.fabricmc.mappingio;
    requires org.spongepowered.mixin;
    requires com.google.common;
    requires static org.jetbrains.annotations;

    provides org.spongepowered.tools.obfuscation.service.IObfuscationService
        with juuxel.remaptools.mixinobfuscator.RemapToolsObfuscationService;
    opens juuxel.remaptools.mixinobfuscator to org.spongepowered.mixin;
}
