# Mini Tardis ComputerCraft Bridge
### Bridges Mini Tardis and CC:Tweaked <br>Automate your TARDIS!

# About
Adds `TARDIS Computer Interface` block that acts as a peripheral for ComputerCraft, it can be crafted as following:

![image](https://github.com/user-attachments/assets/ce23905a-d4a7-4de9-beee-18c2124b94bb)

(Any wired modem will do)

# How To Use
- Place the block inside of Mini Tardis and connect it to a computer like you normally would
- Wrap the peripheral using `peripheral.wrap(<side>)` or `peripheral.find("minitardis_bridge")`
- Refer to Method Reference as you script your TARDIS

## You can also use scripts I wrote that automate TARDIS
- `scripts/tardisServer.lua` - put this script on a computer inside of the TARDIS, rename it to `startup.lua`, 
change modem channels found on top of the file if there's multiple people playing.
Computer must have TARDIS Computer Interface next to it and optionally ender modem if you want to use remote
- `scripts/tardisRemote.lua` - put this script on a pocket computer with ender modem, change channels if needed in the file
- `scripts/gpsHost.lua` - build a GPS constellation with wired modems and ender modems, 
put info about your modems on top of the file and set which dimension the constellation is in. Rename script to `startup.lua`

# Method Reference
- `isLinked()` returns boolean, tells if Computer Interface is within TARDIS

All following methods will error if Computer Interface is not inside of a TARDIS
- `getState()` checks TARDIS state and returns one of following strings:
  - `booting_up`, TARDIS is currently booting up
  - `crashed`, TARDIS has crashed and needs to be rebooted
  - `crashing`, TARDIS is in process of crashing
  - `disabled`, TARDIS is currently powered off
  - `drifting`, TARDIS is drifting between dimensions
  - `flying`, TARDIS is flying to the destination
  - `landed`, TARDIS is stationary
  - `landing`, TARDIS is currently landing
  - `refueling`, TARDIS is refueling
  - `searching_for_landing`, TARDIS is about to start landing
  - `suspended_flight`, energy conduits are locked in middle of flight
  - `taking_off`, TARDIS is taking off
- `boot()` returns true if successful, boots TARDIS up from `disabled` state
- `shutdown()` returns true if successful, shuts TARDIS down from `landed` or `crashed` states
- `refuel(boolean)` returns true if successful, sets refueling state
- `getFuel()` returns number 0 to 1000, tells how much fuel TARDIS has
- `getStability()` returns number 0 to 1000, tells how much stability TARDIS has
- `getAvailableWorlds()` returns an array of strings, tells which dimensions TARDIS is able to travel to
- `getCurrentPos()` returns array of 3 numbers as XYZ, errors if TARDIS is not landed, current position of TARDIS
- `getCurrentFacing()` returns one of following strings, errors if TARDIS is not landed, which way TARDIS is currently facing:
  - `north`
  - `east`
  - `west`
  - `south`
- `getCurrentWorld()` returns string, world TARDIS is currently in
- `getDestinationPos()` returns array of 3 numbers as XYZ, errors if destination is unknown, current destination of TARDIS
- `getDestinationFacing()` returns compass direction string, errors if destination is unknown, which way TARDIS should face at the destination
- `getDestinationWorld()` returns string, errors if destination is unknown, world TARDIS will try to fly to
- `resetDestination()` returns true if successful, resets destination back to current TARDIS location
- `setDestinationPos(x, y, z)` returns true if successful, errors if destination is unreachable, sets destination to provided coordinates
- `setDestinationFacing(compass_direction)` returns true if successful, sets which way TARDIS will face at the destination
- `setDestinationWorld(world_id)` returns true if successful, errors if TARDIS can't go in that world or it's not yet discovered with Dimensions app, sets destination world
- `isDestinationLocked()` returns boolean, tells if destination is locked
- `setDestinationLocked(boolean)` returns true if successful, sets lock state of destination lock
- `areConduitsUnlocked()` returns boolean, tells if energy conduits are unlocked
- `setConduitsUnlocked(boolean)` returns true if successful, locks or unlocks energy conduits
- `handbrake(boolean)` returns true if successful, sets handbrake state
- `getErrorOffsets()` returns array of offsets, can only be called during `flying` state, errors otherwise, each offset is an array of 2 elements. 
  - First element of an offset is north/south offset, 1 means TARDIS needs to be nudged south, -1 means TARDIS needs to be nudged north
  - Second element of an offset is west/east offset, 1 means TARDIS needs to be nudged east, -1 means TARDIS needs to be nudged west
  - Return example: `{ {0,0}, {1,-1}, {1,0}, {0,-1} }`
- `getCoordinateScale()` returns number 0 to 3, tells the value of Localization Scale Interpreter
- `setCoordinateScale(number 0 to 3)` returns true if successful, sets value of Localization Scale Interpreter, also used to select which offset will be nudged during `flying` state
- `nudgeDestination(direction)` returns true if successful, nudges destination in provided direction, used for adjusting offsets during `flying` state. Direction can be one of following strings:
  - `north`
  - `east`
  - `west`
  - `south`
  - `up`
  - `down`
- `getTotalDriftingPhases()` returns number, can only be called during `drifting` state, errors otherwise, tells how many drifting phases are there
- `getDriftingPhasesComplete()` returns number, can only be called during `drifting` state, errors otherwise, tells how many drifting phases have been completed
- `isDriftingPhaseReady()` returns boolean, can only be called during `drifting` state, errors otherwise, is true when TARDIS is ready for phase shift, call `handbrake(true)` to phase shift
