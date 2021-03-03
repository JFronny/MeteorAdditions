package io.gitlab.jfronny.meteoradditions.servers;

import minegame159.meteorclient.gui.screens.WindowScreen;
import minegame159.meteorclient.gui.widgets.WButton;
import minegame159.meteorclient.gui.widgets.WTable;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

public class ServerManagerScreen extends WindowScreen {
    public ServerManagerScreen(MultiplayerScreen multiplayerScreen) {
        super("Manage Servers", true);
        this.parent = multiplayerScreen;
        WTable table = add(new WTable()).getWidget();
        table.add(new WButton("Find Servers")).fillX().expandX().getWidget().action = () -> {
            ServerFinderScreen screen = new ServerFinderScreen(multiplayerScreen);
            screen.parent = this;
            client.openScreen(screen);
        };
        table.row();
        table.add(new WButton("Clean Up")).fillX().expandX().getWidget().action = () -> {
            CleanUpScreen screen = new CleanUpScreen(multiplayerScreen);
            screen.parent = this;
            client.openScreen(screen);
        };
    }
}
