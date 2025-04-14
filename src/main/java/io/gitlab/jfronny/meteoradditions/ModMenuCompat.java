package io.gitlab.jfronny.meteoradditions;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.ModulesScreen;
import net.minecraft.client.gui.screen.Screen;

import java.lang.reflect.Field;
import java.util.Map;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return null;
    }

    public static Screen getMeteorScreen(Screen parent) { // see also ModMenuIntegration in Meteor AME
        GuiTheme theme = GuiThemes.get();
        ModulesScreen screen = new ModulesScreen(theme);
        screen.addDirect(theme.topBar()).top().centerX();
        screen.parent = parent;
        return screen;
    }

    static {
        // MeteorClient provides its own default screen, but that one isn't implemented right
        // (doesn't have the top bar and doesn't respect the parent screen)
        try {
            Field field = ModMenu.class.getDeclaredField("configScreenFactories");
            field.setAccessible(true);
            Map<String, ConfigScreenFactory<?>> factories = (Map<String, ConfigScreenFactory<?>>) field.get(null);
            factories.put("meteor-client", ModMenuCompat::getMeteorScreen);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            MeteorAdditions.LOG.error("Failed to inject MeteorClient config screen", e);
        }
    }
}
