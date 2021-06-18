package io.gitlab.jfronny.meteoradditions.gui.servers;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

public class ServerManagerScreen extends WindowScreen {
    public ServerManagerScreen(GuiTheme theme, MultiplayerScreen multiplayerScreen) {
        super(theme, "Manage Servers");
        this.parent = multiplayerScreen;
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();
        addButton(l, "Find Servers", () -> new ServerFinderScreen(theme, multiplayerScreen, this));
        addButton(l, "Clean Up", () -> new CleanUpScreen(theme, multiplayerScreen, this));
    }

    private void addButton(WContainer c, String text, IGetter<Screen> action) {
        WButton button = c.add(theme.button(text)).expandX().widget();
        button.action = () -> client.openScreen(action.get());
    }
}
