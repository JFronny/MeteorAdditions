package io.gitlab.jfronny.meteoradditions.modules;

import io.gitlab.jfronny.meteoradditions.util.IModule;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.util.HashMap;
import java.util.Map;

public class SearchKeywords {
    private static final Map<String, String[]> KEYWORDS = new HashMap<>();
    private static final Map<String, String[]> KEYWORDS_OPTIONAL = new HashMap<>();

    private static void register(String to, String... keywords) {
        KEYWORDS.put(to, keywords);
    }

    private static void registerOptional(String to, String... keywords) {
        KEYWORDS_OPTIONAL.put(to, keywords);
    }

    public static void configure() {
        register("no-render", "anti", "blind", "wobble");
        register("nametags", "health");
        register("light-overlay", "mob", "spawn");
        register("esp", "item", "mob");
        register("block-esp", "search", "base", "cave", "finder");
        register("speed-mine", "fast", "break");
        register("ghost-hand", "no", "clip");
        register("fast-use", "place");
        register("auto-clicker", "mine", "break");
        register("velocity", "no", "move", "push");
        register("jesus", "dolphin", "snow", "shoe");
        register("flight", "creative", "fly");
        register("fast-climb", "ladder");
        register("auto-jump", "bunndy", "hop");
        register("elytra-fly", "extra");
        registerOptional("discord-presence", "rpc");
        register("better-chat", "fancy");
        register("auto-log", "leave");
        register("kill-aura", "click", "multi");

        Modules modules = Modules.get();
        KEYWORDS.forEach((name, keywords) -> ((IModule) modules.get(name)).meteorAdditions$setKeywords(keywords));
        KEYWORDS_OPTIONAL.forEach((name, keywords) -> {
            Module module = modules.get(name);
            if (module != null) ((IModule) module).meteorAdditions$setKeywords(keywords);
        });
    }
}
