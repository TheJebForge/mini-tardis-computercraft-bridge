package com.thejebforge.minitardis_cc_bridge.cc;

import com.thejebforge.minitardis_cc_bridge.block.ModBlocks;
import dan200.computercraft.api.peripheral.PeripheralLookup;

public class ComputerCraftProxy {
    public static void register() {
        PeripheralLookup.get().registerForBlocks(
                (world, pos, state, blockEntity, context) -> new Peripheral(world),
                ModBlocks.PERIPHERAL
        );
    }
}
