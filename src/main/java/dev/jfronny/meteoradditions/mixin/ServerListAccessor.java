package dev.jfronny.meteoradditions.mixin;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ServerList.class)
public interface ServerListAccessor {
    @Accessor("serverList")
    List<ServerData> getServers();
}
