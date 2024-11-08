package io.gitlab.jfronny.meteoradditions.mixin;

import io.gitlab.jfronny.meteoradditions.util.IModule;
import meteordevelopment.meteorclient.systems.modules.Module;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Module.class)
public class ModuleMixin implements IModule {
    @Unique private String[] keywords;

    @Override
    @Unique
    public void meteorAdditions$setKeywords(String... keywords) {
        this.keywords = keywords;
    }

    @Override
    @Unique
    public String meteorAdditions$getQueryString() {
        return ((Module) (Object) this).name + (keywords == null || keywords.length == 0 ? "" : "-" + String.join("-", keywords));
    }
}
