package io.gitlab.jfronny.meteoradditions.mixin;

import io.gitlab.jfronny.meteoradditions.util.IModule;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Modules.class)
public class ModulesMixin {
    @Redirect(
            method = "searchTitles(Ljava/lang/String;)Ljava/util/Set;",
            at = @At(value = "FIELD", target = "Lmeteordevelopment/meteorclient/systems/modules/Module;title:Ljava/lang/String;"),
            remap = false
    )
    private String modifySearchTitlesArg(Module instance) {
        return ((IModule) instance).getQueryString();
    }
}
