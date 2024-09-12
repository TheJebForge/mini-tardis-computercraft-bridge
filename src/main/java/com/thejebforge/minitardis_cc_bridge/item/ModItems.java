package com.thejebforge.minitardis_cc_bridge.item;

import com.thejebforge.minitardis_cc_bridge.MiniTardisComputerCraftBridge;
import com.thejebforge.minitardis_cc_bridge.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static void load() {
        ModBlocks.ITEM_BLOCKS.forEach(block -> {
            Identifier id = Registries.BLOCK.getId(block);
            Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        });


        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            ModBlocks.ITEM_BLOCKS.forEach(entries::add);
        });
    }

    private static <T extends Item> T register(String path, T item) {
        return Registry.register(Registries.ITEM, MiniTardisComputerCraftBridge.id("peripheral"), item);
    }
}
