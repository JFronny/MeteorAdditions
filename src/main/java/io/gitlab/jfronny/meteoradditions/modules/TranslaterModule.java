package io.gitlab.jfronny.meteoradditions.modules;

import io.gitlab.jfronny.googlechat.GoogleChatConfig;
import io.gitlab.jfronny.libjf.translate.impl.google.Language;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class TranslaterModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Language> serverLanguage = sgGeneral.add(new EnumSetting.Builder<Language>()
            .name("server-language")
            .description("The language of the server used in translations. Auto-detect will disable translating your own messages")
            .defaultValue(Language.AUTO_DETECT)
            .onChanged(language -> GoogleChatConfig.serverLanguage = language.id)
            .build()
    );

    private final Setting<Language> clientLanguage = sgGeneral.add(new EnumSetting.Builder<Language>()
            .name("client-language")
            .description("Your own language used in translations. Auto-detect will disable translating messages by other server members")
            .defaultValue(Language.ENGLISH)
            .onChanged(language -> GoogleChatConfig.clientLanguage = language.id)
            .build()
    );

    public TranslaterModule() {
        super(Categories.Misc, "translater", "Automatically translates chat messages");
        GoogleChatConfig.enabled = isActive();
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
