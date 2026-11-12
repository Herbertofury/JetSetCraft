#!/usr/bin/env python3
"""Build one optional music resource pack from a project-supplied recording. No network access."""
from __future__ import annotations

import argparse
import hashlib
import json
import math
import re
import shutil
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOGUE = ROOT / "src/main/resources/assets/jetsetcraft/jetsetcraft_music"
RESOURCE = re.compile(r"([a-z0-9_.-]+):([a-z0-9_./-]+)")


def resource(value: str) -> tuple[str, str]:
    match = RESOURCE.fullmatch(value)
    if not match or match[1] in {".", ".."} or any(part in {"", ".", ".."} for part in match[2].split("/")):
        raise ValueError(f"Invalid local resource identifier: {value!r}")
    return match[1], match[2]


def digest(path: Path) -> str:
    hasher = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def credit_label(value: object) -> str:
    if not isinstance(value, str):
        raise ValueError("Credit label must be a string")
    value = "".join(c for c in value if ord(c) >= 32 and ord(c) != 127 and c != "§").strip()
    if not 1 <= len(value.encode("utf-16-le")) // 2 <= 160:
        raise ValueError("Credit label must contain 1 to 160 display characters")
    return value


def build(track_id: str, recording: Path, destination: Path, catalogue: Path = CATALOGUE) -> dict:
    if not re.fullmatch(r"[a-z0-9_]+", track_id):
        raise ValueError("Track ID must contain lowercase letters, digits and underscores only")
    metadata = json.loads((catalogue / f"{track_id}.json").read_text(encoding="utf-8"))
    event_namespace, event_path = resource(metadata["sound_event"])
    audio_namespace, audio_path = resource(metadata["audio_resource"])
    if not audio_path.startswith("sounds/") or not audio_path.endswith(".ogg"):
        raise ValueError("audio_resource must name a local sounds/*.ogg resource")
    for key in ("title", "artist"):
        metadata[key] = credit_label(metadata.get(key))
    if "album" in metadata:
        album = metadata["album"]
        if not isinstance(album, str):
            raise ValueError("Album credit must be a string")
        album = "".join(c for c in album if ord(c) >= 32 and ord(c) != 127 and c != "§").strip()
        metadata["album"] = credit_label(album) if album else ""
    featured = metadata.get("featured_artists", [])
    if not isinstance(featured, list) or len(featured) > 16:
        raise ValueError("featured_artists must be a list of at most 16 credit labels")
    metadata["featured_artists"] = [credit_label(artist) for artist in featured]
    if not metadata.get("contexts") or not set(metadata["contexts"]).issubset({"home", "rush"}):
        raise ValueError("contexts must contain home and/or rush")
    volume = float(metadata.get("volume", 0.65))
    if not math.isfinite(volume) or not 0 < volume <= 1:
        raise ValueError("volume must be finite and in (0, 1]")
    recording = recording.expanduser().resolve(strict=True)
    destination = destination.expanduser().absolute()
    if not recording.is_file():
        raise ValueError("Input recording must be a regular file")
    if destination.exists() or destination.is_symlink():
        raise ValueError("Output already exists; choose a new pack folder so existing files remain intact")
    for command in ("ffmpeg", "ffprobe"):
        if not shutil.which(command):
            raise ValueError(f"{command} is required on PATH")
    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix=".jetsetcraft-music-", dir=destination.parent) as temporary:
        staged = Path(temporary) / "pack"
        output = staged / "assets" / audio_namespace / audio_path
        output.parent.mkdir(parents=True)
        subprocess.run(["ffmpeg", "-v", "error", "-nostdin", "-n", "-i", str(recording),
                        "-map", "0:a:0", "-vn", "-c:a", "libvorbis", "-q:a", "5", "-ar", "44100",
                        "-ac", "2", str(output)], check=True)
        probe = json.loads(subprocess.check_output(["ffprobe", "-v", "error", "-show_streams", "-show_format",
                                                    "-of", "json", str(output)], text=True))
        streams = [stream for stream in probe["streams"] if stream.get("codec_type") == "audio"]
        duration = float(probe["format"]["duration"])
        if len(streams) != 1 or streams[0]["codec_name"] != "vorbis" or not math.isfinite(duration) or duration < 1:
            raise ValueError("Encoded recording failed the Ogg/Vorbis audio validation")
        metadata["recording_status"] = "owner_audio_imported"
        write_json(staged / "pack.mcmeta", {"pack": {"pack_format": 15,
                   "description": f"JetSetCraft soundtrack: {metadata['title']} — {metadata['artist']}"}})
        write_json(staged / "assets" / event_namespace / "sounds.json", {event_path: {
            "sounds": [{"name": f"{audio_namespace}:{audio_path[7:-4]}", "stream": True}]}})
        write_json(staged / "assets/jetsetcraft/jetsetcraft_music" / f"{track_id}.json", metadata)
        receipt = {"track_id": track_id, "title": metadata["title"], "artist": metadata["artist"],
                   "album": metadata.get("album", ""), "featured_artists": metadata["featured_artists"],
                   "source_url": metadata.get("source_url", ""), "source_filename": recording.name,
                   "source_sha256": digest(recording), "audio_resource": metadata["audio_resource"],
                   "encoded_sha256": digest(output), "duration_seconds": duration,
                   "codec": streams[0]["codec_name"], "sample_rate": int(streams[0]["sample_rate"]),
                   "channels": streams[0]["channels"], "input_origin": "project-supplied local recording"}
        write_json(staged / "jetsetcraft-music-receipt.json", receipt)
        (staged / "CREDITS.txt").write_text(
            f"Title: {metadata['title']}\nArtist: {metadata['artist']}\n"
            f"Featured artists: {', '.join(metadata['featured_artists']) or 'Not specified'}\n"
            f"Release: {metadata.get('album') or 'Not specified'}\n"
            f"Reference: {metadata.get('source_url', '')}\n"
            "Music Library: https://github.com/Herbertofury/JetSetCraft/wiki/Music-Library\n"
            "Audio supplied locally by the JetSetCraft project. Full source and conversion hashes are in the receipt.\n",
            encoding="utf-8")
        staged.rename(destination)
        return receipt


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--track", required=True, help="Metadata filename without .json")
    parser.add_argument("--input", required=True, type=Path, help="Project-supplied WAV, FLAC, OGG or other audio")
    parser.add_argument("--output-pack", required=True, type=Path, help="New resource-pack folder to create")
    parser.add_argument("--catalogue", type=Path, default=CATALOGUE, help="Directory containing track metadata")
    args = parser.parse_args()
    try:
        receipt = build(args.track, args.input, args.output_pack, args.catalogue)
    except (OSError, ValueError, KeyError, TypeError, subprocess.CalledProcessError) as error:
        parser.exit(1, f"Music import failed: {error}\n")
    print(json.dumps({"output_pack": str(args.output_pack), **receipt}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
