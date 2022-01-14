package io.gitlab.jfronny.meteoradditions;

import io.gitlab.jfronny.meteoradditions.modules.AdditionsItemGroup;
import io.gitlab.jfronny.meteoradditions.modules.AdditionsItemGroupCommand;
import io.gitlab.jfronny.meteoradditions.modules.AutoExtinguish;
import io.gitlab.jfronny.meteoradditions.modules.SpawnItems;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

public class MeteorAdditions extends MeteorAddon {
    public static final Logger LOG = LogManager.getLogger();
    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.create(new Identifier("meteor-additions", "general"))
            .icon(() -> new ItemStack(Items.TNT))
            .appendItems(AdditionsItemGroup::register)
            .build();

    @Override
    public void onInitialize() {
        MeteorClient.EVENT_BUS.registerLambdaFactory("io.gitlab.jfronny.meteoradditions", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        Modules reg = Modules.get();
        reg.add(new SpawnItems());
        reg.add(new AutoExtinguish());
        Commands.get().add(new AdditionsItemGroupCommand());
    }
}
