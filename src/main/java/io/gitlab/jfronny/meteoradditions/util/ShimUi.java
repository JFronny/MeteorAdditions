package io.gitlab.jfronny.meteoradditions.util;

import io.gitlab.jfronny.libjf.config.api.v1.*;
import io.gitlab.jfronny.libjf.config.api.v1.type.Type;
import io.gitlab.jfronny.meteoradditions.MeteorAdditions;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.*;
import meteordevelopment.meteorclient.gui.widgets.input.*;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class ShimUi {
    private final GuiTheme theme;
    private final WTable table;
    private final WVerticalList target;
    private final ConfigCategory parent;

    private ShimUi(GuiTheme theme, WVerticalList target, ConfigCategory parent) {
        this.theme = theme;
        this.table = theme.table();
        this.target = target;
        this.parent = Objects.requireNonNull(parent);
    }

    public static void generate(ConfigInstance config, GuiTheme theme, WVerticalList target) {
        try {
            new ShimUi(theme, target, config).generate();
        } catch (IllegalAccessException e) {
            MeteorAdditions.LOG.error("Could not shim UI", e);
        }
    }

    private void generate() throws IllegalAccessException {
        WSection presets = target.add(theme.section("meteor-additions.presets", false)).expandX().widget();
        for (Map.Entry<String, Runnable> entry : parent.getPresets().entrySet()) {
            presets.add(theme.button(parent.getTranslationPrefix() + entry.getKey())).widget().action = () -> {
                entry.getValue().run();
                target.clear();
                try {
                    generate();
                } catch (IllegalAccessException e) {
                    MeteorAdditions.LOG.error("Could not generate shim UI", e);
                    target.add(theme.label("Could not generate"));
                }
            };
        }
        for (var entry : parent.getCategories().entrySet()) {
            WSection section = target.add(theme.section(entry.getValue().getTranslationPrefix() + "title", false)).expandX().widget();
            new ShimUi(theme, section, entry.getValue()).generate();
        }
        target.add(table).expandX();
        for (EntryInfo<?> entry : parent.getEntries()) entry(entry);
        //TODO referenced configs
    }

    private void entry(EntryInfo<?> entry) throws IllegalAccessException {
        Type type = entry.getValueType();
        WWidget widget = null;
        if (type.isInt()) {
            widget = this.add((EntryInfo<Integer>) entry,
                    theme.intEdit((int) entry.getValue(), (int) entry.getMinValue(), (int) entry.getMaxValue(), false),
                    (s, r) -> s.action = r,
                    WIntEdit::get,
                    WIntEdit::set);
        } else if (type.isLong()) {
            // Unsupported
        } else if (type.isFloat()) {
            widget = add((EntryInfo<Float>) entry,
                    theme.doubleEdit((float) entry.getValue(), entry.getMinValue(), entry.getMaxValue()),
                    (s, r) -> s.action = r,
                    s -> (float) s.get(),
                    (s, v) -> s.set(v));
        } else if (type.isDouble()) {
            widget = add((EntryInfo<Double>) entry,
                    theme.doubleEdit((double) entry.getValue(), entry.getMinValue(), entry.getMaxValue()),
                    (s, r) -> s.action = r,
                    WDoubleEdit::get,
                    WDoubleEdit::set);
        } else if (type.isString()) {
            widget = add((EntryInfo<String>) entry,
                    theme.textBox((String) entry.getValue()),
                    (s, r) -> s.action = r,
                    WTextBox::get,
                    WTextBox::set);
        } else if (type.isBool()) {
            widget = add((EntryInfo<Boolean>) entry,
                    theme.checkbox((boolean) entry.getValue()),
                    (s, r) -> s.action = r,
                    s -> s.checked,
                    (s, v) -> s.checked = v);
        } else if (type.isEnum()) {
            widget = tEnum(entry, type.asEnum());
        }

        if (widget == null) table.add(theme.label(entry.getName() + " (unsupported)"));
        table.row();
    }

    private <T> WWidget tEnum(EntryInfo<T> entry, Type.TEnum<T> type) throws IllegalAccessException {
        return add(entry,
                theme.dropdown(type.options(), entry.getValue()),
                (s, r) -> s.action = r,
                WDropdown::get,
                WDropdown::set);
    }

    private <T extends WWidget, V> T add(EntryInfo<V> entry, T setting, BiConsumer<T, Runnable> setAction, Function<T, V> get, BiConsumer<T, V> set) {
        setAction.accept(setting, () -> {
            try {
                entry.setValue(get.apply(setting));
                set.accept(setting, entry.getValue());
                parent.getRoot().write();
            } catch (IllegalAccessException e) {
                MeteorAdditions.LOG.error("Could not set setting", e);
            }
        });
        setting.tooltip = parent.getTranslationPrefix() + entry.getName() + ".tooltip";
        table.add(theme.label(parent.getTranslationPrefix() + entry.getName()));
        table.add(setting).expandCellX();
        table.add(theme.button(GuiRenderer.RESET)).widget().action = () -> {
            try {
                entry.reset();
                set.accept(setting, entry.getValue());
                parent.getRoot().write();
            } catch (IllegalAccessException e) {
                MeteorAdditions.LOG.error("Could not reset setting", e);
            }
        };
        return setting;
    }
}
