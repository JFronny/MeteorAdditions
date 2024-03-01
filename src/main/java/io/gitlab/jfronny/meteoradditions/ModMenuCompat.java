package io.gitlab.jfronny.meteoradditions;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.gitlab.jfronny.commons.data.MutCollection;
import io.gitlab.jfronny.libjf.config.api.v2.ConfigHolder;
import io.gitlab.jfronny.meteoradditions.util.ShimUi;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Map;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, ConfigScreenFactory<?>> m = MutCollection.mapOf("meteor-client", s -> {
            GuiTheme theme = GuiThemes.get();
            TabScreen screen = Tabs.get().get(0).createScreen(theme);
            screen.addDirect(theme.topBar()).top().centerX();
            return screen;
        });
        if (!FabricLoader.getInstance().isModLoaded("libjf-config-ui-tiny-v1")) {
            ConfigHolder.getInstance().getRegistered().forEach((key, config) -> {
                m.put(key, s -> new ShimUi.ShimUiScreen(GuiThemes.get(), config));
            });
        }
        return m;
    }
}
