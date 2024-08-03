package io.gitlab.jfronny.meteoradditions.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.registry.*;

import java.util.Optional;
import java.util.stream.Stream;

public class CustomWrapperLookup implements RegistryWrapper.WrapperLookup {
    @Override
    public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
        return getRegistryManager().stream().flatMap(DynamicRegistryManager::streamAllRegistryKeys);
    }

    @Override
    public <T> Optional<RegistryWrapper.Impl<T>> getOptionalWrapper(RegistryKey<? extends Registry<? extends T>> registryRef) {
        return getRegistryManager().flatMap(s -> s.getOptionalWrapper(registryRef));
    }

    private Optional<DynamicRegistryManager> getRegistryManager() {
        return Optional.ofNullable(MinecraftClient.getInstance().player)
                .map(Entity::getRegistryManager);
    }
}
