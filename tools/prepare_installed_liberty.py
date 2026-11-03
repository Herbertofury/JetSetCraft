#!/usr/bin/env python3
"""Prepare a fresh official Forge server for smoke_installed_liberty.py without accepting new EULA terms."""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import secrets
import shutil
import signal
import socket
import subprocess
import tempfile
import time
import urllib.request
import zipfile

import prepare_liberty_helldivers as mcdivers
import prepare_liberty_pack as gunpack

ROOT = Path(__file__).resolve().parents[1]
# Exact original files from docs/verification/liberty-campaign/installed-smoke.json.
# The Forge download index independently publishes the same installer SHA-1.
INSTALLER = {
    "file": "forge-1.20.1-47.4.23-installer.jar",
    "url": "https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.23/forge-1.20.1-47.4.23-installer.jar",
    "bytes": 8_836_161,
    "sha1": "ed31ce02ac69176f34353235cb2508d5a0f1e088",
    "sha256": "75cfcb11f60cc641dc83757c1357c08c9bdba255ec529feb115c5e89a59cf75a",
    "minecraft": "1.20.1",
    "version": "1.20.1-forge-47.4.23",
}
TACZ = {
    "file": "tacz-1.20.1-1.1.8-hotfix2.jar",
    "url": "https://edge.forgecdn.net/files/9037/989/tacz-1.20.1-1.1.8-hotfix2.jar",
    "source": "https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero/files/9037989",
    "bytes": 57_472_579,
    "sha256": "2cbb89f44333d2eb4e825d86d789ff119b272ff75184390b55bafd4f0e7f10c2",
}
ANIMATION = {
    "file": "player-animation-lib-forge-1.0.2-rc1+1.20.jar",
    "url": "https://maven.kosmx.dev/dev/kosmx/player-anim/player-animation-lib-forge/1.0.2-rc1+1.20/player-animation-lib-forge-1.0.2-rc1+1.20.jar",
    "bytes": 181_438,
    "sha256": "4a624602ba90d305a4cdcfbde07c893466ddbc0a1210b559bf607bf715626cb0",
}
MCDIVERS = {"file": mcdivers.NAME, "url": mcdivers.URL, "source": mcdivers.PAGE,
            "bytes": mcdivers.BYTES, "sha256": mcdivers.SHA256}
PACK = {"file": gunpack.FILE_NAME, "url": gunpack.DOWNLOAD_URL, "source": gunpack.SOURCE_PAGE,
        "bytes": gunpack.EXPECTED_BYTES, "sha256": gunpack.EXPECTED_SHA256}


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


def verify(path: Path, pin: dict) -> None:
    require(path.is_file() and path.stat().st_size == pin["bytes"],
            "Original artifact is missing or has the wrong byte count: " + pin["file"])
    for algorithm in ("sha256", "sha1"):
        if algorithm in pin:
            with path.open("rb") as stream:
                require(hashlib.file_digest(stream, algorithm).hexdigest() == pin[algorithm],
                        "Original artifact checksum mismatch (deobfuscated/repacked JARs are not accepted): " + pin["file"])


def acquire(folder: Path, pin: dict, source: Path | None) -> Path:
    """Copy only an explicitly supplied original, or download the exact pinned official file."""
    destination = folder / pin["file"]
    temporary = None
    try:
        with tempfile.NamedTemporaryFile(dir=folder, prefix=".liberty-original-", suffix=".part", delete=False) as output:
            temporary = Path(output.name)
            if source is not None:
                verify(source, pin)
                with source.open("rb") as original:
                    shutil.copyfileobj(original, output)
            else:
                request = urllib.request.Request(pin["url"], headers={"User-Agent": "JetSetCraft-Installed-Verification/1.0"})
                with urllib.request.urlopen(request, timeout=45) as response:
                    received = 0
                    while chunk := response.read(1 << 20):
                        received += len(chunk)
                        require(received <= pin["bytes"], "Download exceeds the pinned artifact size: " + pin["file"])
                        output.write(chunk)
        verify(temporary, pin)
        os.replace(temporary, destination)
        temporary = None
    finally:
        if temporary is not None:
            temporary.unlink(missing_ok=True)
    return destination


def existing_eula(source: Path) -> bytes:
    path = source / "eula.txt"
    require(path.is_file() and path.stat().st_size <= 65_536,
            "--source-instance must already contain an accepted eula.txt; this tool does not accept terms")
    content = path.read_bytes()
    active = [line.strip() for line in content.decode("utf-8-sig").splitlines()
              if line.strip() and not line.lstrip().startswith(("#", "!"))]
    require(active == ["eula=true"],
            "Source eula.txt must already contain the single active setting eula=true; terms were not accepted")
    return content


def private_write(path: Path, content: bytes) -> None:
    # O_EXCL prevents overwriting any unexpected installer-created or existing file.
    with os.fdopen(os.open(path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600), "wb") as stream:
        stream.write(content)


def loopback_ports() -> tuple[int, int]:
    sockets = [socket.socket(socket.AF_INET, socket.SOCK_STREAM) for _ in range(2)]
    try:
        for connection in sockets:
            connection.bind(("127.0.0.1", 0))
        return tuple(connection.getsockname()[1] for connection in sockets)
    finally:
        for connection in sockets:
            connection.close()


def install_forge(instance: Path, java: str, timeout: int) -> None:
    log_path = instance / "forge-install.log"
    with log_path.open("wb") as log:
        process = subprocess.Popen([java, "-jar", INSTALLER["file"], "--installServer"], cwd=instance,
                                   stdin=subprocess.DEVNULL, stdout=log, stderr=subprocess.STDOUT,
                                   start_new_session=True)
        try:
            require(process.wait(timeout=timeout) == 0,
                    "Official Forge installation failed; inspect the disposable instance's forge-install.log")
        finally:
            if process.poll() is None:
                os.killpg(process.pid, signal.SIGTERM)
                try:
                    process.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    os.killpg(process.pid, signal.SIGKILL)
                    process.wait(timeout=10)
    require((instance / "run.sh").is_file()
            and (instance / "libraries/net/minecraftforge/forge/1.20.1-47.4.23/unix_args.txt").is_file(),
            "Official Forge installation did not produce the pinned server launcher")


def receipt(pin: dict) -> dict:
    return {"file": pin["file"], "bytes": pin["bytes"], "sha256": pin["sha256"],
            "source": pin.get("source", pin["url"])}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--instance", type=Path, required=True, help="New disposable directory; existing directories are refused")
    parser.add_argument("--source-instance", type=Path, required=True,
                        help="Existing authorized instance whose eula.txt is copied unchanged; no world or settings are copied")
    for name in ("installer", "tacz", "animation", "mcdivers", "pack"):
        parser.add_argument("--" + name + "-source", type=Path,
                            help="Explicit cached original file to verify and reuse instead of downloading")
    parser.add_argument("--installer-timeout", type=int, default=600)
    args = parser.parse_args()
    require(os.name == "posix", "This installed-server smoke uses the official Unix run.sh launcher")
    require(60 <= args.installer_timeout <= 1800, "Installer timeout must be between 60 and 1800 seconds")
    require(not args.instance.exists() and not args.instance.is_symlink(), "Refusing to reuse or overwrite an existing instance")
    instance, source = args.instance.resolve(), args.source_instance.resolve()
    require(not ROOT.is_relative_to(instance) and instance != ROOT / "run"
            and not instance.is_relative_to(source) and not source.is_relative_to(instance),
            "Use a separate disposable directory outside the source instance; do not replace the checkout")
    eula = existing_eula(source)
    java = shutil.which("java")
    require(java is not None, "Java 17 must be on PATH before preparing the installed server")
    version = subprocess.run([java, "-version"], stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                             text=True, timeout=15, check=True)
    require(re.search(r'version\s+"17(?:\.|\")', version.stdout) is not None,
            "The official Minecraft 1.20.1 installed smoke requires Java 17")

    instance.mkdir(parents=True, mode=0o700)
    (instance / "mods").mkdir()
    (instance / "tacz").mkdir()
    private_write(instance / "eula.txt", eula)
    acquire(instance, INSTALLER, args.installer_source)
    for pin, cached in ((TACZ, args.tacz_source), (ANIMATION, args.animation_source), (MCDIVERS, args.mcdivers_source)):
        acquire(instance / "mods", pin, cached)
    acquire(instance / "tacz", PACK, args.pack_source)
    mcdivers.verify(instance / "mods" / MCDIVERS["file"])
    gunpack.verify(instance / "tacz" / PACK["file"])
    print("Installing pinned official Forge 1.20.1-47.4.23; installer diagnostics remain in the disposable instance.", flush=True)
    install_forge(instance, java, args.installer_timeout)
    # Set memory only in this new installation; never copy development/Gradle launch arguments.
    (instance / "user_jvm_args.txt").write_text("-Xms512M\n-Xmx2G\n")
    server_port, rcon_port = loopback_ports()
    password = secrets.token_hex(32)
    settings = (f"server-ip=127.0.0.1\nserver-port={server_port}\nenable-rcon=true\nrcon.port={rcon_port}\n"
                f"rcon.password={password}\nbroadcast-rcon-to-ops=false\nenable-query=false\nonline-mode=true\n"
                "level-name=LibertyInstalledSmoke\nlevel-seed=0\nmax-players=1\nview-distance=6\n"
                "simulation-distance=4\nspawn-protection=0\nmotd=JetSetCraft installed package smoke\n")
    private_write(instance / "server.properties", settings.encode())
    prepared = {
        "schema": 1, "kind": "installed-liberty-preparation", "forge_install_complete": True,
        "installer": INSTALLER, "dependencies": [receipt(pin) for pin in (TACZ, ANIMATION, MCDIVERS)],
        "pack": receipt(PACK), "sourceSets": False, "world_copied": False,
        "eula_copied_from_existing_instance": True, "eula_sha256": hashlib.sha256(eula).hexdigest(),
        "prepared_date_utc": time.strftime("%Y-%m-%d", time.gmtime()),
    }
    private_write(instance / "prepared.json", (json.dumps(prepared, indent=2) + "\n").encode())
    print("JETSETCRAFT_LIBERTY_INSTALLED_PREPARED original_providers full_pack official_forge inherited_eula private_loopback")
    print("Prepared receipt: " + str(instance / "prepared.json"))


if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, subprocess.SubprocessError, zipfile.BadZipFile) as error:
        raise SystemExit("Installed Liberty preparation failed: " + str(error)) from error
