package com.thejebforge.minitardis_cc_bridge.util;

import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

public class Utils {
    public static String worldRegistryToString(RegistryKey<World> world) {
        return world.getValue().toString();
    }

    public static Optional<RegistryKey<World>> stringToWorldRegistry(String world) {
        Identifier id = Identifier.tryParse(world);
        if (id == null) return Optional.empty();
        return Optional.of(RegistryKey.of(RegistryKeys.WORLD, id));
    }
}
