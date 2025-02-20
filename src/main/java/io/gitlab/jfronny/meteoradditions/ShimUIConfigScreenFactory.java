package io.gitlab.jfronny.meteoradditions;

import io.gitlab.jfronny.libjf.config.api.v2.ConfigInstance;
import io.gitlab.jfronny.libjf.config.api.v2.Naming;
import io.gitlab.jfronny.libjf.config.api.v2.ui.ConfigScreenFactory;
import io.gitlab.jfronny.meteoradditions.util.ShimUi;
import meteordevelopment.meteorclient.gui.GuiThemes;
import net.minecraft.client.gui.screen.Screen;

public class ShimUIConfigScreenFactory implements ConfigScreenFactory<ShimUi.ShimUiScreen, ShimUIConfigScreenFactory.Built> {
    @Override
    public Built create(ConfigInstance config, Naming naming, Screen screen) {
        return new Built(new ShimUi.ShimUiScreen(screen, GuiThemes.get(), config, naming));
    }

    @Override
    public int getPriority() {
        return -5;
    }

    public static class Built implements ConfigScreenFactory.Built<ShimUi.ShimUiScreen> {
        private final ShimUi.ShimUiScreen screen;

        public Built(ShimUi.ShimUiScreen screen) {
            this.screen = screen;
        }

        @Override
        public ShimUi.ShimUiScreen get() {
            return screen;
        }

        @Override
        public void onSave(Runnable runnable) {
            screen.onSave(runnable);
        }
    }
}
