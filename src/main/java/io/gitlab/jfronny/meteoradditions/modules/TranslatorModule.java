package io.gitlab.jfronny.meteoradditions.modules;

import io.gitlab.jfronny.googlechat.GoogleChatConfig;
import io.gitlab.jfronny.googlechat.JFC_GoogleChatConfig;
import io.gitlab.jfronny.libjf.config.api.v2.ConfigInstance;
import io.gitlab.jfronny.libjf.config.api.v2.Naming;
import io.gitlab.jfronny.meteoradditions.util.ShimUi;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.nbt.NbtCompound;

public class TranslatorModule extends Module {
    public TranslatorModule() {
        super(Categories.Misc, "translator", "Automatically translates chat messages");
    }

    @Override
    public Module fromTag(NbtCompound tag) {
        Module tm = super.fromTag(tag);
        GoogleChatConfig.General.enabled = tm.isActive(); // Sync initial state
        JFC_GoogleChatConfig.write();
        return tm;
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList table = theme.verticalList();
        ShimUi.generate(MeteorClient.mc.currentScreen, ConfigInstance.get("google-chat"), Naming.get("google-chat"), theme, table);
        return table;
    }

    @Override
    public void onActivate() {
        GoogleChatConfig.General.enabled = true;
        JFC_GoogleChatConfig.write();
    }

    @Override
    public void onDeactivate() {
        GoogleChatConfig.General.enabled = false;
        JFC_GoogleChatConfig.write();
    }
}
