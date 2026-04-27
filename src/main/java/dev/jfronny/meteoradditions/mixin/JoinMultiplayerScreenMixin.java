package dev.jfronny.meteoradditions.mixin;

import dev.jfronny.meteoradditions.gui.servers.ServerManagerScreen;
import meteordevelopment.meteorclient.gui.GuiThemes;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {
    protected JoinMultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        addRenderableWidget(Button.builder(Component.translatable("meteor-additions.servers.button"), button -> {
                    minecraft.setScreen(new ServerManagerScreen(GuiThemes.get(), (JoinMultiplayerScreen) (Object) this));
                })
                .pos(this.width - 75 - 3 - 75 - 2 - 75 - 2, 3)
                .size(75, 20)
                .build());
    }
}
