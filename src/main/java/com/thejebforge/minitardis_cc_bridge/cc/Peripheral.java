package com.thejebforge.minitardis_cc_bridge.cc;

import com.thejebforge.minitardis_cc_bridge.util.Utils;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.component.PartialTardisLocation;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisLocation;
import dev.enjarai.minitardis.component.flight.BootingUpState;
import dev.enjarai.minitardis.component.flight.DisabledState;
import dev.enjarai.minitardis.component.flight.DriftingState;
import dev.enjarai.minitardis.component.flight.FlyingState;
import dev.enjarai.minitardis.component.screen.app.DimensionsApp;
import dev.enjarai.minitardis.component.screen.app.ScreenApp;
import dev.enjarai.minitardis.component.screen.app.ScreenAppTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Peripheral implements IPeripheral, TardisAware {
    private final World world;

    public Peripheral(World world) {
        this.world = world;
    }

    @LuaFunction
    public final boolean isLinked() {
        return getTardis(world).isPresent();
    }

    private Tardis getTardisWithException() throws LuaException {
        return getTardis(world).orElseThrow(() -> new LuaException("Peripheral is not located inside of a TARDIS"));
    }

    // Info and power
    @LuaFunction
    public final String getState() throws LuaException {
        return getTardisWithException().getState().id().getPath();
    }

    @LuaFunction
    public final int getFuel() throws LuaException {
        return getTardisWithException().getFuel();
    }

    @LuaFunction
    public final int getStability() throws LuaException {
        return getTardisWithException().getStability();
    }

    @LuaFunction
    public final boolean boot() throws LuaException {
        return getTardisWithException().suggestStateTransition(new BootingUpState());
    }

    @LuaFunction
    public final boolean shutdown() throws LuaException {
        return getTardisWithException().suggestStateTransition(new DisabledState());
    }


    // Controls
    @LuaFunction
    public final boolean isDestinationLocked() throws LuaException {
        return getTardisWithException().getControls().isDestinationLocked();
    }

    @LuaFunction
    public final boolean setDestinationLocked(IArguments arguments) throws LuaException {
        return getTardisWithException().getControls().setDestinationLocked(arguments.getBoolean(0), false);
    }

    @LuaFunction
    public final boolean areConduitsUnlocked() throws LuaException {
        return getTardisWithException().getControls().areEnergyConduitsUnlocked();
    }

    @LuaFunction
    public final boolean setConduitsUnlocked(IArguments arguments) throws LuaException {
        return getTardisWithException().getControls().setEnergyConduits(arguments.getBoolean(0));
    }

    @LuaFunction
    public final boolean handbrake(IArguments arguments) throws LuaException {
        return getTardisWithException().getControls().handbrake(arguments.getBoolean(0));
    }

    @LuaFunction
    public final int getCoordinateScale() throws LuaException {
        return getTardisWithException().getControls().getScaleState();
    }

    @LuaFunction
    public final boolean setCoordinateScale(IArguments arguments) throws LuaException {
        int power = arguments.getInt(0);

        if (power < 0 || power > 3)
            throw new LuaException("Invalid coordinate scale, range of 0 to 3 is allowed");

        return getTardisWithException().getControls()
                .updateCoordinateScale((int) Math.pow(10, arguments.getInt(0)));
    }

    @LuaFunction
    public final boolean refuel(IArguments arguments) throws LuaException {
        return getTardisWithException().getControls().refuelToggle(arguments.getBoolean(0));
    }

    @LuaFunction
    public final boolean nudgeDestination(IArguments arguments) throws LuaException {
        Direction direction = Direction.byName(arguments.getString(0));

        if (direction == null)
            throw new LuaException("Invalid direction");

        return getTardisWithException().getControls().nudgeDestination(direction);
    }


    // Position
    @LuaFunction
    public final Integer[] getCurrentPos() throws LuaException {
        TardisLocation tardisLocation = getTardisWithException().getCurrentLocation().left()
                .orElseThrow(() -> new LuaException("Current position of TARDIS is unknown"));

        return new Integer[] {tardisLocation.pos().getX(), tardisLocation.pos().getY(), tardisLocation.pos().getZ()};
    }

    @LuaFunction
    public final String getCurrentFacing() throws LuaException {
        TardisLocation tardisLocation = getTardisWithException().getCurrentLocation().left()
                .orElseThrow(() -> new LuaException("Current position of TARDIS is unknown"));

        return tardisLocation.facing().asString();
    }

    @LuaFunction
    public final String getCurrentWorld() throws LuaException {
        RegistryKey<World> world = getTardisWithException().getCurrentLocation()
                .map(
                        location -> location.worldKey(),
                        partialLocation -> partialLocation.worldKey()
                );

        return Utils.worldRegistryToString(world);
    }

    // Destination
    @LuaFunction
    public final Integer[] getDestinationPos() throws LuaException {
        TardisLocation tardisLocation = getTardisWithException().getDestination()
                .orElseThrow(() -> new LuaException("Destination of TARDIS is unknown"));

        return new Integer[] {tardisLocation.pos().getX(), tardisLocation.pos().getY(), tardisLocation.pos().getZ()};
    }

    @LuaFunction
    public final String getDestinationFacing() throws LuaException {
        TardisLocation tardisLocation = getTardisWithException().getDestination()
                .orElseThrow(() -> new LuaException("Destination of TARDIS is unknown"));

        return tardisLocation.facing().asString();
    }

    @LuaFunction
    public final String getDestinationWorld() throws LuaException {
        TardisLocation tardisLocation = getTardisWithException().getDestination()
                .orElseThrow(() -> new LuaException("Destination of TARDIS is unknown"));

        return Utils.worldRegistryToString(tardisLocation.worldKey());
    }

    private List<RegistryKey<World>> availableWorldRegistryKeys(Tardis tardis) {
        Optional<ScreenApp> dimensions = tardis.getControls().getScreenApp(ScreenAppTypes.DIMENSIONS);
        if(dimensions.isEmpty() || !(dimensions.get() instanceof DimensionsApp dimensionsApp))
            return List.of(tardis.getCurrentLocation()
                    .map(TardisLocation::worldKey, PartialTardisLocation::worldKey));

        return dimensionsApp.accessibleDimensions;
    }

    @LuaFunction
    public final String[] getAvailableWorlds() throws LuaException {
        Tardis tardis = getTardisWithException();

        return availableWorldRegistryKeys(tardis).stream()
                .map(Utils::worldRegistryToString)
                .toArray(String[]::new);
    }

    @LuaFunction
    public final boolean setDestinationWorld(IArguments arguments) throws LuaException {
        Tardis tardis = getTardisWithException();

        Optional<RegistryKey<World>> possibleTarget = Utils.stringToWorldRegistry(arguments.getString(0));
        if (possibleTarget.isEmpty())
            throw new LuaException("Invalid destination world");

        RegistryKey<World> target = possibleTarget.get();
        List<RegistryKey<World>> availableWorlds = availableWorldRegistryKeys(tardis);

        if(!availableWorlds.contains(target))
            throw new LuaException("Destination world is not available");

        return tardis.getControls().moveDestinationToDimension(target);
    }

    @LuaFunction
    public final boolean setDestinationFacing(IArguments arguments) throws LuaException {
        Direction direction = Direction.byName(arguments.getString(0));

        if(direction == null || direction == Direction.DOWN || direction == Direction.UP)
            throw new LuaException("Invalid destination facing");

        return getTardisWithException().getControls().rotateDestination(direction);
    }

    @LuaFunction
    public final boolean setDestinationPos(IArguments arguments) throws LuaException {
        Tardis tardis = getTardisWithException();

        BlockPos targetPos = new BlockPos(arguments.getInt(0), arguments.getInt(1), arguments.getInt(2));

        TardisLocation targetLocation = tardis.getDestination()
                .map(tardisLocation -> tardisLocation.with(targetPos))
                .orElseGet(() -> tardis.getCurrentLocation().map(
                        tardisLocation -> tardisLocation.with(targetPos),
                        partialTardisLocation -> new TardisLocation(
                                partialTardisLocation.worldKey(),
                                targetPos,
                                Direction.NORTH
                        )
                ));

        World targetWorld = targetLocation.getWorld(tardis.getServer());
        if (!targetWorld.isInBuildLimit(targetLocation.pos()))
            throw new LuaException("Target location is not in build limit");

        while (!tardis.canSnapDestinationTo(targetLocation)) {
            targetLocation = targetLocation.with(
                    targetLocation.pos().offset(Direction.DOWN)
            );

            if (!targetWorld.isInBuildLimit(targetLocation.pos()))
                throw new LuaException("Can't find a landing spot at provided target location");
        }

        return tardis.setDestination(targetLocation, false);
    }

    @LuaFunction
    public final boolean resetDestination() throws LuaException {
        return getTardisWithException().getControls().resetDestination();
    }

    // Flight info
    @LuaFunction
    public List<List<Integer>> getErrorOffsets() throws LuaException {
        Tardis tardis = getTardisWithException();

        Optional<FlyingState> flyingState = tardis.getState(FlyingState.class);
        if (flyingState.isEmpty())
            throw new LuaException("TARDIS is not in flying state");
        FlyingState state = flyingState.get();

        List<List<Integer>> offsets = new ArrayList<>();

        for (int i = 0; i < state.offsets.length; i += 2) {
            offsets.add(List.of(state.offsets[i], state.offsets[i + 1]));
        }

        return offsets;
    }

    @LuaFunction
    public int getTotalDriftingPhases() throws LuaException {
        Tardis tardis = getTardisWithException();

        Optional<DriftingState> driftingState = tardis.getState(DriftingState.class);
        if (driftingState.isEmpty())
            throw new LuaException("TARDIS is not in flying state");
        DriftingState state = driftingState.get();

        return state.phaseCount;
    }

    @LuaFunction
    public int getDriftingPhasesComplete() throws LuaException {
        Tardis tardis = getTardisWithException();

        Optional<DriftingState> driftingState = tardis.getState(DriftingState.class);
        if (driftingState.isEmpty())
            throw new LuaException("TARDIS is not in flying state");
        DriftingState state = driftingState.get();

        return state.phasesComplete;
    }

    @LuaFunction
    public boolean isDriftingPhaseReady() throws LuaException {
        Tardis tardis = getTardisWithException();

        Optional<DriftingState> driftingState = tardis.getState(DriftingState.class);
        if (driftingState.isEmpty())
            throw new LuaException("TARDIS is not in flying state");
        DriftingState state = driftingState.get();

        return state.phaseTicks >= state.phaseLength;
    }


    @Override
    public String getType() {
        return "minitardis_bridge";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return this == other;
    }
}
