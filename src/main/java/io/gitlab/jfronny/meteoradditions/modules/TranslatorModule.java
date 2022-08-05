package io.gitlab.jfronny.meteoradditions.modules;

import io.gitlab.jfronny.commons.throwable.Coerce;
import io.gitlab.jfronny.googlechat.GoogleChat;
import io.gitlab.jfronny.googlechat.GoogleChatConfig;
import io.gitlab.jfronny.meteoradditions.util.LanguageSetting;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.resource.language.*;

public class TranslatorModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<?> serverLanguage = sgGeneral.add(Coerce.pinF(GoogleChat.TRANSLATE_SERVICE, svc -> new LanguageSetting.Builder<>(svc)
            .name("server-language")
            .description(I18n.translate("google-chat.jfconfig.serverLanguage.tooltip"))
            .defaultValue(svc.parseLang("auto"))
            .onChanged(language -> GoogleChatConfig.serverLanguage = language.getIdentifier())
            .build())
    );

    private final Setting<?> clientLanguage = sgGeneral.add(Coerce.pinF(GoogleChat.TRANSLATE_SERVICE, svc -> new LanguageSetting.Builder<>(svc)
            .name("client-language")
            .description(I18n.translate("google-chat.jfconfig.clientLanguage.tooltip"))
            .defaultValue(svc.parseLang("auto"))
            .onChanged(language -> GoogleChatConfig.clientLanguage = language.getIdentifier())
            .build())
    );

    private final Setting<Boolean> translationTooltip = sgGeneral.add(new BoolSetting.Builder()
            .name("translation-tooltip")
            .description(I18n.translate("google-chat.jfconfig.translationTooltip.tooltip"))
            .defaultValue(false)
            .onChanged(value -> GoogleChatConfig.translationTooltip = value)
            .build()
    );

    private final Setting<Boolean> desugar = sgGeneral.add(new BoolSetting.Builder()
            .name("desugar")
            .description(I18n.translate("google-chat.jfconfig.desugar.tooltip"))
            .defaultValue(false)
            .onChanged(value -> GoogleChatConfig.desugar = value)
            .build()
    );

    public TranslatorModule() {
        super(Categories.Misc, "translater", "Automatically translates chat messages");
        GoogleChatConfig.enabled = isActive();
        ((Setting<Object>)serverLanguage).set(GoogleChat.TRANSLATE_SERVICE.parseLang(GoogleChatConfig.serverLanguage));
        ((Setting<Object>)clientLanguage).set(GoogleChat.TRANSLATE_SERVICE.parseLang(GoogleChatConfig.clientLanguage));
        translationTooltip.set(GoogleChatConfig.translationTooltip);
        desugar.set(GoogleChatConfig.desugar);
    }

    @Override
    public void onActivate() {
        GoogleChatConfig.enabled = true;
    }

    @Override
    public void onDeactivate() {
        GoogleChatConfig.enabled = false;
    }
}
