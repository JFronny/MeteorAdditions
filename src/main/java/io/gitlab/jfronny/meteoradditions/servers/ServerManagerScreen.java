package io.gitlab.jfronny.meteoradditions.servers;

import minegame159.meteorclient.gui.GuiTheme;
import minegame159.meteorclient.gui.WindowScreen;
import minegame159.meteorclient.gui.widgets.pressable.WButton;
import minegame159.meteorclient.gui.widgets.containers.WTable;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

public class ServerManagerScreen extends WindowScreen {
    public ServerManagerScreen(GuiTheme theme, MultiplayerScreen multiplayerScreen) {
        super(theme, "Manage Servers");
        this.parent = multiplayerScreen;
        WTable table = add(new WTable()).widget();
        table.add(theme.button("Find Servers")).expandX().widget().action = () -> {
            ServerFinderScreen screen = new ServerFinderScreen(theme, multiplayerScreen);
            client.openScreen(screen);
        };
        table.row();
        table.add(theme.button("Clean Up")).expandX().widget().action = () -> {
            CleanUpScreen screen = new CleanUpScreen(theme, multiplayerScreen);
            client.openScreen(screen);
        };
    }
}
