#!/usr/bin/env python3
"""Verify a built Liberty JAR in an already prepared, isolated official Forge installation."""
from __future__ import annotations

import argparse
import contextlib
import hashlib
import io
import json
import os
from pathlib import Path
import re
import shutil
import signal
import socket
import struct
import subprocess
import time
import zipfile

from stop_smoke_server import receive_exact, send_packet, stop_server

ROOT = Path(__file__).resolve().parents[1]
PROVIDER_STATUS = ("JETSETCRAFT_LIBERTY_PROVIDER_STATUS tacz=true pack=true native_guard=true "
                   "mcdivers_installed=true mcdivers_ready=true home_guard=true")
FATAL = re.compile(r"ModLoadingException|MixinApplyError|InvalidMixinException|NoSuchMethodError|"
                   r"NoSuchFieldError|VerifyError|Failed to complete lifecycle event|"
                   r"Exception in thread \"main\"|Encountered an unexpected exception")


def sha256(path: Path) -> str:
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


def query(password: str, port: int, command: str) -> str:
    def packet(connection: socket.socket) -> tuple[int, int, str]:
        length, = struct.unpack("<i", receive_exact(connection, 4))
        require(10 <= length <= 65_536, "Invalid RCON response length")
        value = receive_exact(connection, length)
        require(value[-2:] == b"\0\0", "Invalid RCON response terminator")
        request, kind = struct.unpack("<ii", value[:8])
        return request, kind, value[8:-2].decode("utf-8", errors="replace")

    deadline = time.monotonic() + 10
    while True:
        try:
            connection = socket.create_connection(("127.0.0.1", port), timeout=5)
            break
        except OSError:
            if time.monotonic() >= deadline:
                raise
            time.sleep(.25)
    with connection:
        send_packet(connection, 1, 3, password)
        for _ in range(2):
            request, kind, _ = packet(connection)
            require(request == 1, "RCON authentication rejected")
            if kind == 2:
                break
            require(kind == 0, "Unexpected RCON authentication response")
        else:
            raise ValueError("Missing RCON authentication response")
        send_packet(connection, 2, 2, command)
        request, kind, text = packet(connection)
        require((request, kind) == (2, 0), "Unexpected RCON command response")
        return text


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--instance", type=Path, required=True, help="Prepared disposable Forge instance; never a player's save")
    parser.add_argument("--jar", type=Path, required=True, help="Final reobfuscated JetSetCraft build artifact")
    parser.add_argument("--sha256", required=True, help="Expected final artifact SHA-256 supplied by the build")
    parser.add_argument("--output", type=Path, required=True, help="Directory for sanitized logs and JSON evidence")
    parser.add_argument("--timeout", type=int, default=240)
    args = parser.parse_args()
    instance, artifact, output = args.instance.resolve(), args.jar.resolve(), args.output.resolve()
    require(re.fullmatch(r"[0-9a-f]{64}", args.sha256) is not None, "Expected SHA-256 must be lowercase hexadecimal")
    require(30 <= args.timeout <= 600, "Use a startup timeout between 30 and 600 seconds")
    require(instance != ROOT / "run" and instance != ROOT, "Use a separately installed disposable instance, not a Gradle/sourceSet run directory")
    require((instance / "run.sh").is_file(), "The official Forge installer must complete first")
    prepared = json.loads((instance / "prepared.json").read_text())
    require(prepared.get("forge_install_complete") is True, "Missing successful official Forge installation receipt")
    require(sha256(instance / prepared["installer"]["file"]) == prepared["installer"]["sha256"], "Forge installer hash changed")
    for item in prepared["dependencies"]:
        path = instance / "mods" / item["file"]
        require(path.stat().st_size == item["bytes"] and sha256(path) == item["sha256"], "Original provider JAR changed: " + item["file"])
    pack = prepared["pack"]
    require(sha256(instance / "tacz" / pack["file"]) == pack["sha256"], "Official full gun pack hash changed")
    require(artifact.name.startswith("jetsetcraft-1.20.1-") and artifact.suffix == ".jar"
            and not artifact.name.endswith("-sources.jar"), "Expected a built Minecraft 1.20.1 JetSetCraft JAR")
    require(sha256(artifact) == args.sha256, "Final JetSetCraft artifact hash does not match the build")
    with zipfile.ZipFile(artifact) as jar:
        names = jar.namelist()
        manifest = jar.read("META-INF/MANIFEST.MF").decode()
        require("MixinConfigs: jetsetcraft.liberty.mixins.json" in manifest, "The packaged JAR has no Liberty native guard manifest entry")
        require("jetsetcraft.liberty.mixins.json" in names, "The packaged native guard configuration is missing")
        require(not any(name.startswith(("com/tacz/", "com/shiroaki193/", "assets/helldiver2/", "assets/zeta/",
                                         "data/helldiver2/", "data/zeta/")) for name in names),
                "Upstream provider code or gun-pack assets were embedded in the release")
        metadata = jar.read("META-INF/mods.toml").decode()
        version = re.search(r'^version\s*=\s*"([^"]+)"', metadata, re.M)
    properties = dict(line.split("=", 1) for line in (instance / "server.properties").read_text().splitlines()
                      if "=" in line and not line.startswith("#"))
    require(properties.get("server-ip") == "127.0.0.1" and properties.get("enable-rcon") == "true",
            "The disposable smoke server must bind to loopback with authenticated RCON")
    require("eula=true" in (instance / "eula.txt").read_text(), "This tool does not accept new EULA terms; prepare the authorized instance first")
    password, port = properties.get("rcon.password", ""), int(properties.get("rcon.port", "0"))
    require(bool(password) and password.isascii() and 1 <= port <= 65_535, "Missing private RCON configuration")
    destination = instance / "mods" / artifact.name
    others = [path for path in (instance / "mods").glob("jetsetcraft-*.jar") if path != destination]
    require(not others, "Remove the prior disposable JetSetCraft test JAR explicitly before running a new version")
    shutil.copyfile(artifact, destination)
    require(sha256(destination) == args.sha256, "Copied build artifact changed")
    started = time.monotonic()
    run_label = time.strftime("installed-%Y%m%d-%H%M%S", time.gmtime())
    log_path = instance / (run_label + ".log")
    stop_path = instance / (run_label + "-rcon.log")
    receipt = {
        "kind": "installed-package-dedicated-server", "launcher": "official Forge run.sh", "sourceSets": False,
        "mod": {"file": artifact.name, "version": version.group(1) if version else "see manifest",
                "sha256": args.sha256, "bytes": artifact.stat().st_size},
        "installer": prepared["installer"], "runtime_provider_dependencies": prepared["dependencies"], "pack": pack,
        "upstream_content_embedded": False, "status": "FAIL", "ready": False, "native_provider_guard": False,
        "rcon_stopped": False, "all_dimensions_saved": False, "home_contact_guard": False,
        "scope": "The exact built and reobfuscated mod runs in a separately installed official Forge server with original upstream JARs and the full unmodified pack. The loaded native guard and live content, registered commands, normal readiness, authenticated shutdown and all-dimension saving are checked. No player connects; this is not mission gameplay or visual evidence."
    }
    process = None
    failure = None
    try:
        with log_path.open("wb") as log:
            process = subprocess.Popen(["bash", "run.sh", "nogui"], cwd=instance, stdout=log,
                                       stderr=subprocess.STDOUT, start_new_session=True)
            deadline = started + args.timeout
            while time.monotonic() < deadline:
                text = log_path.read_text(errors="replace")
                require(not FATAL.search(text), "Installed server reported a fatal/linkage/loader error; see retained log")
                if re.search(r"Done \([0-9.]+s\)!", text) and PROVIDER_STATUS in text:
                    receipt["ready"] = receipt["native_provider_guard"] = receipt["home_contact_guard"] = True
                    receipt["startup_seconds"] = round(time.monotonic() - started, 2)
                    break
                require(process.poll() is None, "Installed server exited before readiness and the native guard diagnostic")
                time.sleep(.5)
            require(receipt["ready"], "Installed server timed out before full-provider native guard readiness")
            commands = {command: query(password, port, command) for command in ("help jetsetcraft", "help tacz")}
            require("/jetsetcraft liberty" in commands["help jetsetcraft"] and "/tacz" in commands["help tacz"],
                    "Expected JetSetCraft Liberty and real TaCZ commands were not registered")
            receipt["rcon_command_checks"] = commands
            with stop_path.open("w") as stop_log, contextlib.redirect_stdout(stop_log):
                stop_server(password, port)
            receipt["rcon_stopped"] = True
            receipt["exit_code"] = process.wait(timeout=60)
            final = log_path.read_text(errors="replace")
            receipt["all_dimensions_saved"] = "All dimensions are saved" in final
            receipt["saved_world"] = (instance / properties.get("level-name", "world") / "level.dat").is_file()
            receipt["fatal_or_linkage_errors"] = sorted(set(FATAL.findall(final)))
            require(receipt["exit_code"] == 0 and receipt["all_dimensions_saved"] and receipt["saved_world"]
                    and not receipt["fatal_or_linkage_errors"], "Installed server did not finish a healthy normal save and exit")
            receipt["status"] = "PASS"
    except (OSError, ValueError, subprocess.SubprocessError, EOFError) as error:
        failure = str(error)
        receipt["failure"] = failure
    finally:
        if process is not None and process.poll() is None:
            try:
                with contextlib.redirect_stdout(io.StringIO()):
                    stop_server(password, port)
                process.wait(timeout=15)
            except (OSError, ValueError, EOFError, subprocess.SubprocessError):
                os.killpg(process.pid, signal.SIGTERM)
                try:
                    process.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    os.killpg(process.pid, signal.SIGKILL)
                    process.wait(timeout=10)
        output.mkdir(parents=True, exist_ok=True)
        evidence = []
        for original, name in ((log_path, "installed-server.log"), (stop_path, "installed-rcon-stop.log"),
                               (instance / "logs/debug.log", "installed-loader.log")):
            if not original.is_file():
                continue
            raw = original.read_bytes()
            sanitized = raw.decode(errors="replace").replace(password, "<RCON_SECRET>")
            for path, label in ((str(instance), "<ISOLATED_INSTANCE>"), (str(ROOT), "<CHECKOUT>"),
                                (str(Path.home() / ".gradle"), "<GRADLE_CACHE>")):
                sanitized = sanitized.replace(path, label)
            target = output / name
            target.write_text(sanitized)
            evidence.append({"file": name, "bytes": target.stat().st_size, "sha256": sha256(target),
                             "original_bytes": len(raw), "original_sha256": hashlib.sha256(raw).hexdigest(),
                             "transform": "Only absolute instance/checkout/cache paths and any private RCON secret are replaced; diagnostics are retained."})
        receipt["log_evidence"] = evidence
        receipt["elapsed_seconds"] = round(time.monotonic() - started, 2)
        receipt["verification_date_utc"] = time.strftime("%Y-%m-%d", time.gmtime())
        (output / "installed-smoke.json").write_text(json.dumps(receipt, indent=2) + "\n")
    if failure:
        raise SystemExit("Installed Liberty smoke failed: " + failure)
    print("JETSETCRAFT_LIBERTY_INSTALLED_SMOKE_PASS exact_release_jar native_guard actual_providers commands authenticated_stop all_dimensions_saved exit_zero")
    print("Evidence: " + str(output / "installed-smoke.json"))


if __name__ == "__main__":
    main()
