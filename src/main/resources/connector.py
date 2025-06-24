# SPDX-License-Identifier: MIT
# Copyright (c) 2020 Henrik Blidh
# Copyright (c) 2022-2023 The Pybricks Authors
# Copyright (c) 2025 JuniorJacki

"""
Example program for computer-to-hub communication, modified to accept commands with args from stdin and receive Commands.
Requires Pybricks firmware >= 3.3.0.
"""

import asyncio
import sys
import os
from contextlib import suppress
from bleak import BleakScanner, BleakClient
import time
import struct


PYBRICKS_COMMAND_EVENT_CHAR_UUID = "c5f50002-8280-46da-89f4-6d8051e4aeef"
HUB_NAME = "Hub 10"

# Argumente aus Kommandozeile lesen
if len(sys.argv) > 2:
    HUB_NAME = sys.argv[1]
    PYBRICKS_COMMAND_EVENT_CHAR_UUID = sys.argv[2]



async def main():
    main_task = asyncio.current_task()

    def handle_disconnect(_):
        print("hwd") # Hub Was Disconnected
        if not main_task.done():
            main_task.cancel()


    def handle_rx(_, data: bytearray):
        if data[0] == 0x01:  # "write stdout" event (0x01)
            payload = data[1:]
            #print(f"Received at {time.time()}: {payload.decode()}", flush=True)
            print(f"{payload.decode()}", flush=True)


    # Find the hub
    device = await BleakScanner.find_device_by_name(HUB_NAME)
    if device is None:
        print(f"cnf", flush=True) # Could Not Find
        return

    async with BleakClient(device, handle_disconnect) as client:
        await client.start_notify(PYBRICKS_COMMAND_EVENT_CHAR_UUID, handle_rx)

        async def send(data):
            #print(f"Sent: {time.time()} {data}", flush=True)
            await client.write_gatt_char(
                PYBRICKS_COMMAND_EVENT_CHAR_UUID,
                b"\x06" + data.encode() if isinstance(data, str) else b"\x06" + data,
                response=True
            )
            await asyncio.sleep(0)


        async def send_command(command_id, payload=b""):
            """Send a command to the hub."""
            data = bytes([command_id]) + payload
            #print(f"Sent at {time.time()}: Command ID {command_id}, Payload {payload}", flush=True)
            await client.write_gatt_char(
                PYBRICKS_COMMAND_EVENT_CHAR_UUID,
                data,
                response=True
            )
            await asyncio.sleep(0)

        await send_command(0x01)
        print(f"crdy",flush=True)

        # Read commands from stdin
        while True:
            command = sys.stdin.readline().strip()
            if not command:  # Handle EOF or empty input
                continue
            if command == "bye::":
                await send(command)
                break
            await send(command)

if __name__ == "__main__":
    with suppress(asyncio.CancelledError):
        asyncio.run(main())
