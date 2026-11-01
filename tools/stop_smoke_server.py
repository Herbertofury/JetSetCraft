#!/usr/bin/env python3
"""Authenticate to the CI server's loopback RCON and request a normal Minecraft stop."""
from __future__ import annotations

import argparse
import os
import socket
import struct
import sys
import time


def receive_exact(connection: socket.socket, length: int) -> bytes:
    data = bytearray()
    while len(data) < length:
        chunk = connection.recv(length - len(data))
        if not chunk:
            raise EOFError("RCON connection closed")
        data.extend(chunk)
    return bytes(data)


def receive_packet(connection: socket.socket) -> tuple[int, int]:
    length, = struct.unpack("<i", receive_exact(connection, 4))
    if not 10 <= length <= 4096:
        raise ValueError("Invalid RCON response length")
    packet = receive_exact(connection, length)
    if packet[-2:] != b"\0\0":
        raise ValueError("Invalid RCON response terminator")
    return struct.unpack("<ii", packet[:8])


def send_packet(connection: socket.socket, request_id: int, packet_type: int, body: str) -> None:
    packet = struct.pack("<ii", request_id, packet_type) + body.encode("ascii") + b"\0\0"
    connection.sendall(struct.pack("<i", len(packet)) + packet)


def stop_server(password: str, port: int) -> None:
    # Minecraft can print Done just before its RCON listener starts accepting connections.
    deadline = time.monotonic() + 10.0
    while True:
        try:
            connection = socket.create_connection(("127.0.0.1", port), timeout=5.0)
            break
        except OSError:
            if time.monotonic() >= deadline:
                raise
            time.sleep(0.25)
    with connection:
        send_packet(connection, 1, 3, password)
        for _ in range(2):
            request_id, packet_type = receive_packet(connection)
            if request_id != 1:
                raise ValueError("RCON authentication was rejected")
            if packet_type == 2:
                break
            if packet_type != 0:
                raise ValueError("Unexpected RCON authentication response")
        else:
            raise ValueError("Missing RCON authentication response")
        send_packet(connection, 2, 2, "stop")
        # A normal stop can close RCON before its reply arrives. The workflow still
        # requires the saved-world marker, launcher exit and successful exit status.
        try:
            if receive_packet(connection) != (2, 0):
                raise ValueError("Unexpected RCON stop response")
        except (EOFError, ConnectionResetError, socket.timeout):
            pass
    print("JETSETCRAFT_SMOKE_RCON_STOP_SENT authenticated loopback stop command")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--port", type=int, default=25575)
    args = parser.parse_args()
    password = os.environ.get("JETSETCRAFT_SMOKE_RCON_PASSWORD", "")
    if not password or not password.isascii() or "\0" in password or not 1 <= args.port <= 65535:
        parser.error("A valid port and nonempty ASCII JETSETCRAFT_SMOKE_RCON_PASSWORD are required")
    try:
        stop_server(password, args.port)
    except (OSError, EOFError, ValueError) as error:
        print(f"RCON smoke shutdown failed: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
