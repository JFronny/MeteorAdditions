package io.gitlab.jfronny.meteoradditions;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import minegame159.meteorclient.gui.GuiTheme;
import minegame159.meteorclient.gui.GuiThemes;
import minegame159.meteorclient.gui.tabs.Tab;
import minegame159.meteorclient.gui.tabs.TabScreen;
import minegame159.meteorclient.gui.tabs.Tabs;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return ImmutableMap.of("meteor-client", (ConfigScreenFactory<?>) s -> {
            Tab tab = Tabs.get().get(0);
            GuiTheme theme = GuiThemes.get();
            try {
                TabScreen screen = (TabScreen)tab.getClass().getMethod("createScreen", GuiTheme.class)
                        .invoke(tab, theme);
                screen.addDirect(theme.topBar()).top().centerX();
                return screen;
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
