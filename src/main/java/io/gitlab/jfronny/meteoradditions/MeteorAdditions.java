package io.gitlab.jfronny.meteoradditions;

import io.gitlab.jfronny.meteoradditions.modules.GiveCommand;
import io.gitlab.jfronny.meteoradditions.modules.SpawnItemsModule;
import minegame159.meteorclient.MeteorAddon;
import minegame159.meteorclient.systems.commands.Commands;
import minegame159.meteorclient.systems.modules.Modules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MeteorAdditions extends MeteorAddon {
    public static final Logger LOG = LogManager.getLogger();
    @Override
    public void onInitialize() {
        Modules reg = Modules.get();
        reg.add(new SpawnItemsModule());
        Commands.get().add(new GiveCommand());
    }
}
