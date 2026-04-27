package dev.jfronny.meteoradditions.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.jfronny.meteoradditions.util.IModule;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Modules.class)
public class ModulesMixin {
    @ModifyArg(
            method = "searchTitles(Ljava/lang/String;)Ljava/util/List;",
            at = @At(value = "INVOKE", target = "Lmeteordevelopment/meteorclient/utils/Utils;searchLevenshteinDefault(Ljava/lang/String;Ljava/lang/String;Z)I"),
            index = 0
    )
    String modifySearchTitlesArg(String text, @Local(name = "module") Module module) {
        return text.equals(module.title) ? ((IModule) module).meteorAdditions$getQueryString() : text;
    }
}
