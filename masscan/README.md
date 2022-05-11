# What is this?
Masscan is a program for port scanning large numbers of internet-connected devices.
Several people have made scripts to scan for minecraft servers, some examples are
[this](https://github.com/Footsiefat/Minecraft-Server-Scanner),
[this](https://github.com/Zerogoki00/minescanner),
[this](https://github.com/ObscenityIB/creeper) or
[this](https://blog.bithole.dev/mcmap.html)

This specific script is intended to scan only the ranges of specific hosting providers to reduce exposure,
you can, however, use any of the above scripts if it generates a list of servers where the first space-denominated
column is the server IP. You can then import that list using the `Load IPs` button added by MeteorAdditions.

## Sidenote
You can also utilize shodan.io if you have an API key or are OK with non-automatic imports
An example of a service built around that is [this](https://randmc.rodney.io/)