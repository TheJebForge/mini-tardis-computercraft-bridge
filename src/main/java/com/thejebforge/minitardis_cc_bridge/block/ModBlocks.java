package com.thejebforge.minitardis_cc_bridge.block;

import com.google.common.collect.ImmutableList;
import com.thejebforge.minitardis_cc_bridge.MiniTardisComputerCraftBridge;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.List;

public class ModBlocks {
    public static final PeripheralBlock PERIPHERAL = register(
            "peripheral",
            new PeripheralBlock(AbstractBlock.Settings.create()
                    .requiresTool()
                    .strength(2.0f, 6.0f)));

    public static final List<? extends Block> ITEM_BLOCKS;
    static {
        var builder = ImmutableList.<Block>builder();
        builder.add(PERIPHERAL);
        ITEM_BLOCKS = builder.build();
    }

    public static void load() {

    }

    private static <T extends Block> T register(String path, T block) {
        return Registry.register(Registries.BLOCK, MiniTardisComputerCraftBridge.id(path), block);
    }
}
