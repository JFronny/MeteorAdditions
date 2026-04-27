package dev.jfronny.meteoradditions;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.ModulesScreen;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuCompat::getMeteorScreen;
    }

    // Injected via ModMenuMixin
    // see also ModMenuIntegration in Meteor AME
    public static Screen getMeteorScreen(Screen parent) {
        GuiTheme theme = GuiThemes.get();
        ModulesScreen screen = new ModulesScreen(theme);
        screen.addDirect(theme.topBar()).top().centerX();
        screen.parent = parent;
        return screen;
    }
}
