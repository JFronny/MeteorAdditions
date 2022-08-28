package io.gitlab.jfronny.meteoradditions;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;

import java.util.Map;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return ImmutableMap.of("meteor-client", (ConfigScreenFactory<?>) s -> {
            GuiTheme theme = GuiThemes.get();
            TabScreen screen = Tabs.get().get(0).createScreen(theme);
            screen.addDirect(theme.topBar()).top().centerX();
            return screen;
        });
    }
}
