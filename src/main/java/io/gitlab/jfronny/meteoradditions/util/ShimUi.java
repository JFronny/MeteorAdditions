package io.gitlab.jfronny.meteoradditions.util;

import io.gitlab.jfronny.commons.ref.R;
import io.gitlab.jfronny.libjf.config.api.v2.*;
import io.gitlab.jfronny.libjf.config.api.v2.type.Type;
import io.gitlab.jfronny.meteoradditions.MeteorAdditions;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.*;
import meteordevelopment.meteorclient.gui.widgets.input.*;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

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
    private final Naming naming;

    private ShimUi(GuiTheme theme, WVerticalList target, ConfigCategory parent, Naming naming) {
        this.theme = theme;
        this.table = theme.table();
        this.target = target;
        this.parent = Objects.requireNonNull(parent);
        this.naming = naming;
    }

    public static class ShimUiScreen extends WindowScreen {
        private final ConfigInstance config;
        private final Naming naming;
        private Runnable onSave = R::nop;

        public ShimUiScreen(Screen parent, GuiTheme theme, ConfigInstance config, Naming naming) {
            super(theme, translate(naming.name()));
            this.parent = parent;
            this.config = config;
            this.naming = naming;
        }

        public void onSave(Runnable runnable) {
            Runnable old = onSave;
            onSave = () -> {
                old.run();
                runnable.run();
            };
        }

        @Override
        public void initWidgets() {
            generate(this, config, naming, theme, window);
        }

        @Override
        protected void onClosed() {
            onSave.run();
        }
    }

    public static void generate(Screen self, ConfigInstance config, Naming naming, GuiTheme theme, WVerticalList target) {
        try {
            new ShimUi(theme, target, config, naming).generate(self);
        } catch (IllegalAccessException e) {
            MeteorAdditions.LOG.error("Could not shim UI", e);
        }
    }

    private void generate(Screen self) throws IllegalAccessException {
        if (!parent.getPresets().isEmpty()) {
            WSection presets = target.add(theme.section(translate("meteor-additions.presets"), false)).expandX().widget();
            for (Map.Entry<String, Runnable> entry : parent.getPresets().entrySet()) {
                presets.add(theme.button(translate(entry.getKey()))).widget().action = () -> {
                    entry.getValue().run();
                    parent.getRoot().write();
                    target.clear();
                    try {
                        generate(self);
                    } catch (IllegalAccessException e) {
                        MeteorAdditions.LOG.error("Could not generate shim UI", e);
                        target.add(theme.label("Could not generate"));
                    }
                };
            }
        }
        for (var entry : parent.getCategories().entrySet()) {
            var categoryNaming = naming.category(entry.getKey());
            WSection section = target.add(theme.section(translate(categoryNaming.name()), false)).expandX().widget();
            new ShimUi(theme, section, entry.getValue(), categoryNaming).generate(self);
        }
        target.add(table).expandX();
        for (EntryInfo<?> entry : parent.getEntries()) entry(entry, naming.entry(entry.getName()));
        if (!parent.getReferencedConfigs().isEmpty()) {
            WSection referenced = target.add(theme.section(translate("meteor-additions.referenced"), false)).expandX().widget();
            for (ConfigInstance config : parent.getReferencedConfigs()) {
                var referencedNaming = naming.referenced(config);
                String name = translate(referencedNaming.name());
                referenced.add(theme.button(name)).widget().action = () -> {
                    ShimUiScreen screen = new ShimUiScreen(self, theme, config, referencedNaming);
                    MinecraftClient mc = MinecraftClient.getInstance();
                    screen.parent = mc.currentScreen;
                    mc.setScreen(screen);
                };
            }
        }
    }

    private static final Text enabledText = Text.translatableWithFallback("google-chat.jfconfig.enabled", "enabled");
    private void entry(EntryInfo<?> entry, Naming.Entry naming) throws IllegalAccessException {
        if (enabledText.equals(naming.name())) return;
        Type type = entry.getValueType();
        WWidget widget = null;
        if (type.isInt()) {
            widget = add((EntryInfo<Integer>) entry, naming,
                    theme.intEdit((int) entry.getValue(), (int) entry.getMinValue(), (int) entry.getMaxValue(), false),
                    (s, r) -> s.action = r,
                    WIntEdit::get,
                    WIntEdit::set);
        } else if (type.isLong()) {
            // Unsupported
        } else if (type.isFloat()) {
            widget = add((EntryInfo<Float>) entry, naming,
                    theme.doubleEdit((float) entry.getValue(), entry.getMinValue(), entry.getMaxValue()),
                    (s, r) -> s.action = r,
                    s -> (float) s.get(),
                    (s, v) -> s.set(v));
        } else if (type.isDouble()) {
            widget = add((EntryInfo<Double>) entry, naming,
                    theme.doubleEdit((double) entry.getValue(), entry.getMinValue(), entry.getMaxValue()),
                    (s, r) -> s.action = r,
                    WDoubleEdit::get,
                    WDoubleEdit::set);
        } else if (type.isString()) {
            widget = add((EntryInfo<String>) entry, naming,
                    theme.textBox(Objects.requireNonNullElse((String) entry.getValue(), "")),
                    (s, r) -> s.action = r,
                    WTextBox::get,
                    WTextBox::set);
        } else if (type.isBool()) {
            widget = add((EntryInfo<Boolean>) entry, naming,
                    theme.checkbox((boolean) entry.getValue()),
                    (s, r) -> s.action = r,
                    s -> s.checked,
                    (s, v) -> s.checked = v);
        } else if (type.isEnum()) {
            widget = tEnum(entry, naming, type.asEnum());
        }

        if (widget == null) table.add(theme.label(entry.getName() + " (unsupported)"));
        table.row();
    }

    private <T> WWidget tEnum(EntryInfo<T> entry, Naming.Entry naming, Type.TEnum<T> type) throws IllegalAccessException {
        return add(entry, naming,
                theme.dropdown(type.options(), entry.getValue()),
                (s, r) -> s.action = r,
                WDropdown::get,
                WDropdown::set);
    }

    private <T extends WWidget, V> T add(EntryInfo<V> entry, Naming.Entry naming, T setting, BiConsumer<T, Runnable> setAction, Function<T, V> get, BiConsumer<T, V> set) {
        setAction.accept(setting, () -> {
            try {
                entry.setValue(get.apply(setting));
                set.accept(setting, entry.getValue());
                parent.getRoot().write();
            } catch (IllegalAccessException e) {
                MeteorAdditions.LOG.error("Could not set setting", e);
            }
        });
        setting.tooltip = translate(naming.tooltip());
        table.add(theme.label(translate(naming.name())));
        var x = table.add(setting).expandCellX();
        if (!(setting instanceof WCheckbox)) x.expandX();
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

    private static String translate(String key) {
        return I18n.hasTranslation(key) ? I18n.translate(key) : key;
    }

    private static String translate(@Nullable Text text) {
        return text == null ? null : text.getString();
    }
}
