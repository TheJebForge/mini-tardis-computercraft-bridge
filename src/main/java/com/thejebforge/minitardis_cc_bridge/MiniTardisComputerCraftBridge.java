package com.thejebforge.minitardis_cc_bridge;

import com.thejebforge.minitardis_cc_bridge.block.ModBlocks;
import com.thejebforge.minitardis_cc_bridge.cc.ComputerCraftProxy;
import com.thejebforge.minitardis_cc_bridge.item.ModItems;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniTardisComputerCraftBridge implements ModInitializer {
	public static final String MOD_ID = "mini_tardis_computercraft_bridge";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModBlocks.load();
		ModItems.load();
		ComputerCraftProxy.register();
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}