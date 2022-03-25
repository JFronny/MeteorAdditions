package io.gitlab.jfronny.meteoradditions.util;

import io.gitlab.jfronny.libjf.translate.api.Language;
import io.gitlab.jfronny.libjf.translate.api.TranslateService;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;

public class DefaultSettingsWidgetFactoryProxy extends DefaultSettingsWidgetFactory {
    public DefaultSettingsWidgetFactoryProxy(GuiTheme theme) {
        super(theme);
    }

    public static void injectFactories(IDefaultSettingsWidgetFactory factory) {
        factory.additions$putFactory(LanguageSetting.class, (Factory)(table, setting) -> languageW(factory, table, (LanguageSetting<?, ?>) setting));
    }

    private static <TService extends TranslateService<TLang>, TLang extends Language> void languageW(IDefaultSettingsWidgetFactory factory, WTable table, LanguageSetting<TService, TLang> setting) {
        WDropdown<Object> dropdown = table.add(factory.additions$getTheme().dropdown(setting.getValues(), setting.get())).expandCellX().widget();
        dropdown.action = () -> setting.set((TLang)dropdown.get());

        factory.additions$reset(table, setting, () -> dropdown.set(setting.get()));
    }
}
