package io.gitlab.jfronny.meteoradditions.mixin;

import io.gitlab.jfronny.meteoradditions.mixininterface.IMultiplayerScreen;
import io.gitlab.jfronny.meteoradditions.gui.servers.ServerManagerScreen;
import minegame159.meteorclient.gui.GuiThemes;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen implements IMultiplayerScreen {
    @Shadow protected MultiplayerServerListWidget serverListWidget;

    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        addButton(new ButtonWidget(this.width - 75 - 3 - 75 - 2 - 75 - 2, 3, 75, 20, new LiteralText("Servers"), button -> {
            client.openScreen(new ServerManagerScreen(GuiThemes.get(), (MultiplayerScreen) (Object) this));
        }));
    }

    @Override
    public MultiplayerServerListWidget getServerListWidget() {
        return serverListWidget;
    }
}
