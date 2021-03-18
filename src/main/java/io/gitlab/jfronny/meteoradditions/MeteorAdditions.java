package io.gitlab.jfronny.meteoradditions;

import io.gitlab.jfronny.meteoradditions.modules.*;
import minegame159.meteorclient.MeteorAddon;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Modules;
import net.minecraft.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MeteorAdditions extends MeteorAddon {
    public static final Logger LOG = LogManager.getLogger();
    public static final Category category = new Category("Motor-", Items.BEDROCK.getDefaultStack());
    @Override
    public void onInitialize() {
        Modules reg = Modules.get();
        reg.add(new KillPotion());
        reg.add(new TrollPotion());
        reg.add(new CrashChest());
        reg.add(new SpawnItems());
        reg.add(new Sword32k());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(category);
    }
}
