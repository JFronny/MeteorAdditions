package io.gitlab.jfronny.meteoradditions.util;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.settings.Setting;

public interface IDefaultSettingsWidgetFactory {
    void additions$putFactory(Class<?> klazz, Object factory);
    GuiTheme additions$getTheme();
    void additions$reset(WContainer c, Setting<?> setting, Runnable action);
}
