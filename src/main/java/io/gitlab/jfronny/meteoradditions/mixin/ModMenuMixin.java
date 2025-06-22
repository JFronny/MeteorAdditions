package io.gitlab.jfronny.meteoradditions.mixin;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import io.gitlab.jfronny.meteoradditions.ModMenuCompat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModMenu.class)
public class ModMenuMixin {
    @Shadow(remap = false) @Final private static Map<String, ConfigScreenFactory<?>> configScreenFactories;

    @Inject(method = "onInitializeClient()V", at = @At("TAIL"), remap = false)
    private void postInit(CallbackInfo ci) {
        // Forcefully overwrite the default config screen factory for Meteor Client
        configScreenFactories.put("meteor-client", ModMenuCompat::getMeteorScreen);
    }
}
