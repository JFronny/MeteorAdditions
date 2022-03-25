package io.gitlab.jfronny.meteoradditions.util;

import io.gitlab.jfronny.libjf.translate.api.Language;
import io.gitlab.jfronny.libjf.translate.api.TranslateService;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LanguageSetting<TService extends TranslateService<TLang>, TLang extends Language> extends Setting<TLang> {
    private final List<TLang> values;
    private final List<String> suggestions;

    public LanguageSetting(String name, String description, TLang defaultValue, Consumer<TLang> onChanged, Consumer<Setting<TLang>> onModuleActivated, IVisible visible, TService service) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        values = service.getAvailableLanguages();
        suggestions = Stream.concat(values.stream().map(Language::getIdentifier), values.stream().map(Language::getDisplayName)).collect(Collectors.toList());
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
}
