local modem = peripheral.find("modem")
local running = true
-- Customize your channels here
local tardisChannel = 22
local modemChannel = 23

local termW, termH = term.getSize()

if not modem then
    printError("Modem is required to connect to TARDIS")
    return
end

modem.open(modemChannel)

print("TARDIS Remote\nType 'rhelp' for commands\n")

-- Batteries
function string:split(sep)
    local sep, fields = sep or ":", {}
    local pattern = string.format("([^%s]+)", sep)
    self:gsub(pattern, function(c) fields[#fields+1] = c end)
    return fields
end

function locateDimension(_timeout)
    local myChannel = 65000

    local toCloseChannel = false
    if not modem.isOpen(myChannel) then
        toCloseChannel = true
        modem.open(myChannel)
    end

    modem.transmit(gps.CHANNEL_GPS, myChannel, "DIMENSION")

    local dimension = nil
    local timeout = os.startTimer(_nTimeout or 2)
    while true do
        local event = { os.pullEvent() }
        if event[1] == "modem_message" then
            local modemSide, channel, replyChannel, message, distance = event[2], event[3], event[4], event[5], event[6]

            if channel == myChannel and replyChannel == gps.CHANNEL_GPS and distance and type(message) == "string" then
                dimension = message
                break
            end
        elseif event[1] == "timer" then
            local timer = event[2]
            if timer == timeout then
                break
            end
        end
    end

    if toCloseChannel then
        modem.close(myChannel)
    end

    return dimension
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

-- Client side commands
function locateAndSet(resp)
    local x, y, z = gps.locate()
    local dimension = locateDimension()

    if not x or not y or not z or not dimension then
        resp("Unable to determine remote's location")
        return
    end

    x = math.floor(x)
    z = math.floor(z)

    modem.transmit(tardisChannel, modemChannel, "destw " .. dimension)
    modem.transmit(tardisChannel, modemChannel, "destp " .. x .. " " .. y .. " " .. z)
end

local helpText = [[-- TARDIS Remote --
  * here - use gps to set TARDIS destination to your location
  * exit - exit remote
  * help - view server commands
]]

function executeCommand(command, resp)
    local args = command:split(" ")

    if #args <= 0 then
        return
    end

    local cmd = args[1]:lower()

    local ok, status = pcall(function()
        if cmd == "rhelp" then
            resp(helpText)
        elseif cmd == "exit" then
            term.clear()
            term.setCursorPos(1, 1)
            running = false
            return
        elseif cmd == "here" then
            locateAndSet(resp)
        else
            modem.transmit(tardisChannel, modemChannel, command)
        end
    end)

    if not ok then
        resp("ERROR! " .. status)
    end
end

-- User Input
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
    executeCommand(currentPrompt, print)

    currentPrompt = ""
    printPrompt()
end

function receiveMessage(message)
    clearCurrentLine()
    print(message)
    redrawCurrentPrompt()
end

printPrompt()

while running do
    local event = {os.pullEvent()}

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

    if event[1] == "modem_message" and event[3] == modemChannel then
        receiveMessage(tostring(event[5]))
    end
end