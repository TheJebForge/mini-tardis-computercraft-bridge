-- Put your modems into this table, key is name of the peripheral, value is a table of XYZ
local modemLocations = {
    ["modem_9"] = { 283, 72, 391 },
    ["modem_10"] = { 287, 75, 395 },
    ["modem_11"] = { 279, 75, 395 },
    ["modem_12"] = { 283, 72, 399 }
}
-- Put id of your dimension here
local dimension = "minecraft:overworld"

-- Open modems
local modems = {}

for i, v in pairs(modemLocations) do
    local modem = peripheral.wrap(i)
    modem.open(gps.CHANNEL_GPS)
    modems[i] = modem
end

print("Listening...")

while true do
    local _, modemSide, channel, replyChannel, message, distance = os.pullEvent("modem_message")

    local modem = modems[modemSide]

    if modem and channel == gps.CHANNEL_GPS and distance then
        local loc = modemLocations[modemSide]

        if message == "PING" then
            local payload = { loc[1], loc[2], loc[3] }
            modem.transmit(replyChannel, channel, payload)
            print("Served PING on " .. modemSide)
        elseif message == "DIMENSION" then
            modem.transmit(replyChannel, channel, dimension)
            print("Served DIMENSION on " .. modemSide)
        end
    end
end
