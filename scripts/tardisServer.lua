local tardis = peripheral.find("minitardis_bridge")
local modem = peripheral.find("modem")

-- Customize your channels here
local modemChannel = 22
local remoteChannel = 23

local didRemoteCauseFly = false

local termW, termH = term.getSize()

if not tardis then
    printError("TARDIS Computer Interface is not found")
    return
end

term.clear()
term.setCursorPos(1, 1)
print("Automated TARDIS Computer\nType 'help' for commands\n")

-- Batteries
function string:split(sep)
    local sep, fields = sep or ":", {}
    local pattern = string.format("([^%s]+)", sep)
    self:gsub(pattern, function(c) fields[#fields+1] = c end)
    return fields
end

function unwrapError(runnable, onError)
    local result = { pcall(runnable) }

    if result[1] then
        table.remove(result, 1)
        return table.unpack(result)
    else
        table.remove(result, 1)
        return onError(table.unpack(result))
    end
end

-- Drawing
function printPrompt()
    write("> ")
    term.setCursorBlink(true)
end

function fill(c, x, y, w, h)
    for cX = x, x + w - 1 do
        for cY = y, y + h - 1 do
            term.setCursorPos(cX, cY)
            term.write(c)
        end
    end
end

function clearCurrentLine()
    local x, y = term.getCursorPos()
    local w, h = term.getSize()

    fill(" ", 1, y, w, 1)
    term.setCursorPos(1, y)
end

-- Talking to remote
function tellRemote(message)
    modem.transmit(remoteChannel, modemChannel, message)
end

-- Flying process
local state = tardis.getState()
local lastState
local driftRequired = false

function updateState()
    lastState = state
    state = tardis.getState()
end

function stateChanged()
    return state ~= lastState
end

function startFlight()
    local destination = unwrapError(function()
        return tardis.getDestinationWorld()
    end, function()
        return "unknown"
    end)

    driftRequired = tardis.getCurrentWorld() ~= destination

    if state == "refueling" then
        tardis.refuel(false)
        updateState()
    end

    tardis.setConduitsUnlocked(true)
    tardis.setDestinationLocked(true)
    tardis.handbrake(true)
end

function performDriftIfNeeded()
    if state == "flying" and stateChanged() and driftRequired then
        if didRemoteCauseFly then
            tellRemote("TARDIS is drifting to another dimension")
        end

        driftRequired = false
        tardis.setDestinationLocked(false)
        tardis.handbrake(false)
        sleep(0.01)
        updateState()
    end
end

function driftIfNeeded()
    if state == "drifting" and tardis.isDriftingPhaseReady() then
        tardis.handbrake(true)
    end
end

function fixErrorOffsetsIfNeeded()
    if state == "flying" and stateChanged() then
        if didRemoteCauseFly then
            tellRemote("TARDIS is flying")
        end

        sleep(0.01)

        for i, v in ipairs(tardis.getErrorOffsets()) do
            tardis.setCoordinateScale(i - 1)

            sleep(0.01)

            local c1 = v[1] > 0 and "south"
                or v[1] < 0 and "north"
                or nil
            local c2 = v[2] > 0 and "east"
                or v[2] < 0 and "west"
                or nil

            if c1 ~= nil then
                tardis.nudgeDestination(c1)
            end

            if c2 ~= nil then
                tardis.nudgeDestination(c2)
            end

            sleep(0.01)
        end

        sleep(0.1)

        tardis.handbrake(false)

        if didRemoteCauseFly then
            tellRemote("TARDIS is about to land")
        end

        sleep(0.01)
        updateState()
    end
end

function lockConduitsIfLanded()
    if state == "landed" and tardis.areConduitsUnlocked() then
        tardis.setConduitsUnlocked(false)
        didRemoteCauseFly = false
    end
end

function refuelIfLanded()
    if state == "landed" and stateChanged(state) and tardis.getFuel() < 1000 then
        tardis.refuel(true)
        updateState()
    end
end

-- Commands
local helpText = [[-- TARDIS --
  * help - you're using this right now
  * loc - TARDIS location
  * dest - TARDIS destination
  * status - TARDIS status
  * destp x y z - set TARDIS destination position
  * destw world_id - set TARDIS destination world
  * destf compass_direction - set TARDIS destination facing
  * worlds - list available worlds
  * fly - take off]]

function executeCommand(command, resp, remote)
    local args = command:split(" ")

    if #args <= 0 then
        return
    end

    local cmd = args[1]:lower()

    local ok, status = pcall(function()
        if cmd == "help" then
            resp(helpText)
        elseif cmd == "loc" then
            local position = unwrapError(function()
                local pos = tardis.getCurrentPos()
                return pos[1] .. " " .. pos[2] .. " " .. pos[3]
            end, function()
                return "Unknown"
            end)

            local facing = unwrapError(function()
                return tardis.getCurrentFacing()
            end, function()
                return "Unknown"
            end)

            local world = tardis.getCurrentWorld()

            resp("-- Current Location --\nPosition: " .. position .. "\nFacing: " .. facing .. "\nWorld: " .. world)
        elseif cmd == "dest" then
            local position = unwrapError(function()
                local pos = tardis.getDestinationPos()
                return pos[1] .. " " .. pos[2] .. " " .. pos[3]
            end, function()
                return "Unknown"
            end)

            local facing = unwrapError(function()
                return tardis.getDestinationFacing()
            end, function()
                return "Unknown"
            end)

            local world = tardis.getDestinationWorld()

            resp("-- Destination --\nPosition: " .. position .. "\nFacing: " .. facing .. "\nWorld: " .. world)
        elseif cmd == "status" then
            resp("Fuel: " .. tardis.getFuel() .. "\nStability: " .. tardis.getStability() .. "\nState: " .. tardis.getState())
        elseif cmd == "destp" then
            local x, y, z = tonumber(args[2]), tonumber(args[3]), tonumber(args[4])

            if not x or not y or not z then
                error("Invalid position", 2)
            end

            if tardis.setDestinationPos(x, y, z) then
                resp("Destination position set")
            end
        elseif cmd == "destf" then
            if tardis.setDestinationFacing(args[2]) then
                resp("Destination facing set")
            end
        elseif cmd == "destw" then
            if tardis.setDestinationWorld(args[2]) then
                resp("Destination world set")
            end
        elseif cmd == "worlds" then
            local worlds = tardis.getAvailableWorlds()
            local response = "-- Available Worlds --"

            for _, v in pairs(worlds) do
                response = response .. "\n" .. v
            end

            resp(response)
        elseif cmd == "fly" then
            startFlight()
            resp("TARDIS is taking off")

            if remote then
                didRemoteCauseFly = true
            end
        else
            error("Unknown command", 2)
        end
    end)

    if not ok then
        resp("ERROR! " .. status)
    end
end

-- Wireless
if modem then
    print("Listening for wireless commands")
    modem.open(modemChannel)
end

-- User Input through terminal
local currentPrompt = ""

function redrawCurrentPrompt(char)
    local stringLen = currentPrompt:len()
    local screenLen = stringLen + 3

    if screenLen <= termW and char then
        write(char)
    else
        clearCurrentLine()
        printPrompt()

        local start = math.max(0, stringLen - (termW - 4))
        write(currentPrompt:sub(start, stringLen))
    end
end

function processInput(char)
    currentPrompt = currentPrompt .. char
    redrawCurrentPrompt()
end

function processBackspace()
    currentPrompt = currentPrompt:sub(1, currentPrompt:len() - 1)
    redrawCurrentPrompt()
end

function processReturn()
    print()
    executeCommand(currentPrompt, print, false)

    currentPrompt = ""
    printPrompt()
end

printPrompt()

-- Tick
function onTick()
    -- Flying functions
    updateState()

    performDriftIfNeeded()
    driftIfNeeded()
    fixErrorOffsetsIfNeeded()
    lockConduitsIfLanded()
    refuelIfLanded()
end

-- Event loop
local interval = 0.2
local timerID = os.startTimer(interval)

while true do
    local event = {os.pullEvent()}

    -- Tick handling
    if event[1] == "timer" and event[2] == timerID then
        onTick()
        timerID = os.startTimer(interval)
    end

    -- User Input
    if event[1] == "char" then
        processInput(event[2])
    end

    if event[1] == "key" then
        if event[2] == keys.enter then
            processReturn()
        elseif event[2] == keys.backspace then
            processBackspace()
        end
    end

    -- Remote commands
    if event[1] == "modem_message" then
        executeCommand(tostring(event[5]), tellRemote, true)
    end
end
