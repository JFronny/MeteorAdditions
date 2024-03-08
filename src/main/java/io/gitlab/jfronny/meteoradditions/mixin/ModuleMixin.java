package io.gitlab.jfronny.meteoradditions.mixin;

import io.gitlab.jfronny.meteoradditions.util.IModule;
import meteordevelopment.meteorclient.systems.modules.Module;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Module.class)
public class ModuleMixin implements IModule {
    @Shadow @Final public String name;
    @Unique private String[] keywords;

    @Override
    public void setKeywords(String... keywords) {
        this.keywords = keywords;
    }

    @Override
    public String getQueryString() {
        return name + (keywords == null || keywords.length == 0 ? "" : "-" + String.join("-", keywords));
    }
}
