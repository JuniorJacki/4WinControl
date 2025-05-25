# SPDX-License-Identifier: MIT
# Copyright (c) 2020 Henrik Blidh
# Copyright (c) 2022-2023 The Pybricks Authors

"""
Example program for computer-to-hub communication, modified to accept commands from stdin.
Requires Pybricks firmware >= 3.3.0.
"""

import asyncio
import sys
from contextlib import suppress
from bleak import BleakScanner, BleakClient

PYBRICKS_COMMAND_EVENT_CHAR_UUID = "c5f50002-8280-46da-89f4-6d8051e4aeef"
HUB_NAME = "Hub 10"


async def main():
    main_task = asyncio.current_task()

    def handle_disconnect(_):
        print("Hub was disconnected.")
        if not main_task.done():
            main_task.cancel()

    ready_event = asyncio.Event()

    def handle_rx(_, data: bytearray):
        if data[0] == 0x01:  # "write stdout" event (0x01)
            payload = data[1:]
            if payload == b"rdy":
                ready_event.set()
            else:
                print(f"Received: {payload.decode()}", flush=True)

    # Find the hub
    device = await BleakScanner.find_device_by_name(HUB_NAME)
    if device is None:
        print(f"Could not find hub with name: {HUB_NAME}", flush=True)
        return

    async with BleakClient(device, handle_disconnect) as client:
        await client.start_notify(PYBRICKS_COMMAND_EVENT_CHAR_UUID, handle_rx)

        async def send(data):
            await ready_event.wait()
            ready_event.clear()
            await client.write_gatt_char(
                PYBRICKS_COMMAND_EVENT_CHAR_UUID,
                b"\x06" + data.encode() if isinstance(data, str) else b"\x06" + data,
                response=True
            )
            print(f"Sent: {data}", flush=True)

        print("Ready for commands. Send 'fwd', 'rev', or 'bye' via stdin.", flush=True)

        # Read commands from stdin
        while True:
            command = sys.stdin.readline().strip()
            if not command:  # Handle EOF or empty input
                break
            if command == "bye":
                await send(command)
                break
            await send(command)
            print(".", flush=True)

if __name__ == "__main__":
    with suppress(asyncio.CancelledError):
        asyncio.run(main())