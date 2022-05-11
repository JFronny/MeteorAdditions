#!/bin/python

# Based on: https://github.com/Footsiefat/Minecraft-Server-Scanner

import os
import sys
import threading

import requests
from mcstatus import MinecraftServer

from cidr import merge, is_ipv4
from masscan import PortScanner, PortScannerError

hostingProviders = ["nitrado.net"]
ranges = []

for hostingProvider in hostingProviders:
    response = requests.post(f"https://ipinfo.io/products/ranges-api?value={hostingProvider}")
    ranges.extend(filter(is_ipv4, response.json()["data"]["ranges"]))

ranges = merge(ranges)

print(f"Identified {len(ranges)} ranges: ", ranges)

try:
    mas = PortScanner()
except PortScannerError:
    print("masscan binary not found", sys.exc_info()[0])
    sys.exit(1)
except:
    print("Unexpected error:", sys.exc_info()[0])
    sys.exit(1)

print("Using masscan ", mas.masscan_version)
print("WARNING: Masscan will now be started. This may take a while")
mas.scan(hosts=','.join(ranges), ports="25565", arguments="--max-rate 1000", sudo=True)
print("masscan command line:", mas.command_line)
print('maascan scanstats: ', mas.scanstats)

threads = 20
searchterm = ""
outputfile = "scan.txt"
hosts = mas.all_hosts

open(outputfile, "a+").close()

if len(hosts) < int(threads):
    threads = len(hosts)

split = list([hosts[i::threads] for i in range(threads)])


class ThreadImpl(threading.Thread):
    def __init__(self, thread_id, name):
        threading.Thread.__init__(self)
        self.thread_id = thread_id
        self.name = name

    def run(self):
        print(f"Starting Thread {self.name}")
        for ip in split[self.thread_id]:
            try:
                server = MinecraftServer(ip, 25565)
                status = server.status()
            except:
                print(f"Failed to get status of: {ip}")
            else:
                print(f"Found server: {ip} {status.players.online} {status.description} {status.version.name}")
                if searchterm in status.version.name:
                    with open(outputfile) as f:
                        if ip not in f.read():
                            text_file = open(outputfile, "a")
                            text_file.write(f"{ip} {status.players.online} {status.description} {status.version.name}")
                            text_file.write(os.linesep)
                            text_file.close()
        print(f"Exiting Thread {self.name}")


for x in range(threads):
    thread = ThreadImpl(x, str(x)).start()
