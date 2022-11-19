package io.gitlab.jfronny.meteoradditions.util;

import io.gitlab.jfronny.libjf.translate.api.Language;
import io.gitlab.jfronny.libjf.translate.api.TranslateService;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class LanguageSetting<TService extends TranslateService<TLang>, TLang extends Language> extends Setting<TLang> {
    private final List<TLang> values;
    private final List<String> suggestions;

    public LanguageSetting(String name, String description, TLang defaultValue, Consumer<TLang> onChanged, Consumer<Setting<TLang>> onModuleActivated, IVisible visible, TService service) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        values = service.getAvailableLanguages();
        suggestions = Stream.concat(values.stream().map(Language::getIdentifier), values.stream().map(Language::getDisplayName)).toList();
    }

    @Override
    protected TLang parseImpl(String str) {
        for (TLang possible : values) {
            if (str.equalsIgnoreCase(possible.getIdentifier())) return possible;
            if (str.equalsIgnoreCase(possible.getDisplayName())) return possible;
        }

        return null;
    }

    @Override
    protected boolean isValueValid(TLang value) {
        return true;
    }

    @Override
    public List<String> getSuggestions() {
        return suggestions;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        tag.putString("value", get().getIdentifier());

        return tag;
    }

    @Override
    protected TLang load(NbtCompound tag) {
        parse(tag.getString("value"));

        return get();
    }

    public Object[] getValues() {
        return values.toArray();
    }

    public static class Builder<TService extends TranslateService<TLang>, TLang extends Language> extends SettingBuilder<Builder<TService, TLang>, TLang, LanguageSetting<TService, TLang>> {
        private final TService service;

        public Builder(TService service) {
            super(null);
            this.service = service;
        }

        @Override
        public LanguageSetting<TService, TLang> build() {
            return new LanguageSetting<>(name, description, defaultValue, onChanged, onModuleActivated, visible, service);
        }
    }

    public static SettingsWidgetFactory.Factory widget(GuiTheme theme) {
        return (table, setting) -> widget(theme, table, (LanguageSetting<?, ?>) setting);
    }

    private static <TService extends TranslateService<TLang>, TLang extends Language> void widget(GuiTheme theme, WTable table, LanguageSetting<TService, TLang> setting) {
        WDropdown<Object> dropdown = table.add(theme.dropdown(setting.getValues(), setting.get())).expandCellX().widget();
        dropdown.action = () -> setting.set((TLang)dropdown.get());

        // Reset button
        table.add(theme.button(GuiRenderer.RESET)).widget().action = () -> {
            setting.reset();
            dropdown.set(setting.get());
        };
    }
}
