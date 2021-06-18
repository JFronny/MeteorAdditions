package io.gitlab.jfronny.meteoradditions;

import io.gitlab.jfronny.meteoradditions.modules.AutoExtinguish;
import io.gitlab.jfronny.meteoradditions.modules.GiveCommand;
import io.gitlab.jfronny.meteoradditions.modules.SpawnItems;
import meteordevelopment.meteorclient.MeteorAddon;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

public class MeteorAdditions extends MeteorAddon {
    public static final Logger LOG = LogManager.getLogger();
    @Override
    public void onInitialize() {
        MeteorClient.EVENT_BUS.registerLambdaFactory("io.gitlab.jfronny.meteoradditions", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        Modules reg = Modules.get();
        reg.add(new SpawnItems());
        reg.add(new AutoExtinguish());
        Commands.get().add(new GiveCommand());
    }
}
