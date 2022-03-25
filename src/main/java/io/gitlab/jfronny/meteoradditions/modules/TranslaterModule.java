package io.gitlab.jfronny.meteoradditions.modules;

import io.gitlab.jfronny.googlechat.GoogleChat;
import io.gitlab.jfronny.googlechat.GoogleChatConfig;
import io.gitlab.jfronny.libjf.translate.api.Language;
import io.gitlab.jfronny.libjf.translate.api.TranslateService;
import io.gitlab.jfronny.meteoradditions.util.LanguageSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.function.Function;

public class TranslaterModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<?> serverLanguage = sgGeneral.add(genericsHack(GoogleChat.TRANSLATE_SERVICE, svc -> new LanguageSetting.Builder<>(svc)
            .name("server-language")
            .description("The language of the server used in translations. Auto-detect will disable translating your own messages")
            .defaultValue(svc.parseLang("auto"))
            .onChanged(language -> GoogleChatConfig.serverLanguage = language.getIdentifier())
            .build())
    );

    private final Setting<?> clientLanguage = sgGeneral.add(genericsHack(GoogleChat.TRANSLATE_SERVICE, svc -> new LanguageSetting.Builder<>(svc)
            .name("client-language")
            .description("Your own language used in translations. Auto-detect will disable translating messages by other server members")
            .defaultValue(svc.parseLang("auto"))
            .onChanged(language -> GoogleChatConfig.clientLanguage = language.getIdentifier())
            .build())
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

    private <TRes, TLang extends Language> TRes genericsHack(TranslateService<TLang> service, Function<TranslateService<TLang>, TRes> func) {
        return func.apply(service);
    }
}
