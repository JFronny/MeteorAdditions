package io.gitlab.jfronny.meteoradditions.modules;

import io.gitlab.jfronny.googlechat.GoogleChatConfig;
import io.gitlab.jfronny.libjf.config.api.v1.ConfigInstance;
import io.gitlab.jfronny.meteoradditions.util.ShimUi;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class TranslatorModule extends Module {
    public TranslatorModule() {
        super(Categories.Misc, "translater", "Automatically translates chat messages");
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList table = theme.verticalList();
        ShimUi.generate(ConfigInstance.get("google-chat"), theme, table);
        return table;
    }

    @Override
    public void onActivate() {
        GoogleChatConfig.enabled = true;
    }

    @Override
    public void onDeactivate() {
        GoogleChatConfig.enabled = false;
    }
}
