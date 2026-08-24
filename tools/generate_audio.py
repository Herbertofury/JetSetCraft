#!/usr/bin/env python3
"""Generate original deterministic gang entrance stingers for JetSetCraft.

The synthesis uses only Python's standard library. FFmpeg is used solely as the Vorbis encoder; no samples, stems,
models, or third-party audio are downloaded or read. The committed OGG files are release assets, while the generated
manifest lets validation reject silence, truncation, or accidental replacement.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import math
import random
import shutil
import struct
import subprocess
import tempfile
import wave
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOUNDS_JSON = ROOT / "src/main/resources/assets/jetsetcraft/sounds.json"
OUTPUT_ROOT = ROOT / "src/main/resources/assets/jetsetcraft/sounds"
MANIFEST = ROOT / "tools/audio_manifest.json"
SAMPLE_RATE = 22_050
DURATION_SECONDS = 3.2
FRAME_COUNT = round(SAMPLE_RATE * DURATION_SECONDS)


def gang_sound_paths() -> list[str]:
    payload = json.loads(SOUNDS_JSON.read_text(encoding="utf-8"))
    paths: set[str] = set()
    for event in payload.values():
        for sound in event.get("sounds", []):
            name = sound.get("name", "") if isinstance(sound, dict) else str(sound)
            prefix = "jetsetcraft:"
            if name.startswith(prefix + "music/gangs/"):
                paths.add(name[len(prefix) :])
    return sorted(paths)


def envelope(time: float, start: float, decay: float) -> float:
    age = time - start
    return 0.0 if age < 0.0 else math.exp(-age / decay)


def synthesize(name: str) -> tuple[list[int], float, float]:
    digest = hashlib.sha256(("JetSetCraft original audio v1\0" + name).encode("utf-8")).digest()
    seed = int.from_bytes(digest[:8], "big")
    rng = random.Random(seed)
    bpm = 132 + seed % 31
    beat = 60.0 / bpm
    root_hz = 48.0 * (2.0 ** ((seed % 12) / 12.0))
    scale = (0, 3, 5, 7, 10)
    bass_pattern = [scale[(digest[i] + i) % len(scale)] for i in range(8)]
    lead_pattern = [scale[(digest[i + 8] + i * 2) % len(scale)] + 12 for i in range(8)]
    samples: list[int] = []
    energy = 0.0
    peak = 0.0

    for index in range(FRAME_COUNT):
        time = index / SAMPLE_RATE
        beat_index = int(time / beat)
        beat_phase = time - beat_index * beat
        eighth_index = int(time / (beat * 0.5))
        eighth_phase = time - eighth_index * beat * 0.5

        kick_env = envelope(beat_phase, 0.0, 0.085)
        kick_frequency = 48.0 + 72.0 * math.exp(-beat_phase / 0.035)
        kick = math.sin(2.0 * math.pi * kick_frequency * beat_phase) * kick_env * 0.62

        snare = 0.0
        if beat_index % 4 in (1, 3) and beat_phase < 0.18:
            snare_env = math.exp(-beat_phase / 0.070)
            snare = (rng.random() * 2.0 - 1.0) * snare_env * 0.22
            snare += math.sin(2.0 * math.pi * 185.0 * beat_phase) * snare_env * 0.08

        hat = 0.0
        if eighth_phase < 0.055:
            hat_env = math.exp(-eighth_phase / 0.020)
            alternating = 0.72 if eighth_index % 2 else 1.0
            hat = (rng.random() * 2.0 - 1.0) * hat_env * alternating * 0.075

        bass_step = bass_pattern[beat_index % len(bass_pattern)]
        bass_frequency = root_hz * (2.0 ** (bass_step / 12.0))
        bass_phase = 2.0 * math.pi * bass_frequency * time
        bass_gate = 0.72 + 0.28 * math.exp(-beat_phase / 0.11)
        bass = (math.sin(bass_phase) + 0.30 * math.sin(bass_phase * 2.0)) * bass_gate * 0.20

        lead_step = lead_pattern[eighth_index % len(lead_pattern)]
        lead_frequency = root_hz * (2.0 ** (lead_step / 12.0))
        lead_gate = math.exp(-eighth_phase / (0.11 + (seed % 5) * 0.012))
        lead = math.sin(2.0 * math.pi * lead_frequency * time + digest[20] / 255.0 * math.pi)
        lead += 0.24 * math.sin(2.0 * math.pi * lead_frequency * 2.0 * time)
        lead *= lead_gate * (0.075 if beat_index < 4 else 0.13)

        accent = 0.0
        if beat_index == 7:
            accent = math.sin(2.0 * math.pi * root_hz * 4.0 * time) * envelope(beat_phase, 0.0, 0.22) * 0.13

        value = math.tanh((kick + snare + hat + bass + lead + accent) * 1.12)
        fade = min(1.0, time / 0.025, max(0.0, (DURATION_SECONDS - time) / 0.070))
        value *= fade * 0.88
        peak = max(peak, abs(value))
        energy += value * value
        samples.append(max(-32768, min(32767, round(value * 32767.0))))

    return samples, peak, math.sqrt(energy / FRAME_COUNT)


def write_wav(path: Path, samples: list[int]) -> None:
    with wave.open(str(path), "wb") as stream:
        stream.setnchannels(1)
        stream.setsampwidth(2)
        stream.setframerate(SAMPLE_RATE)
        stream.writeframes(b"".join(struct.pack("<h", sample) for sample in samples))


def encode_vorbis(ffmpeg: str, wav_path: Path, ogg_path: Path) -> None:
    ogg_path.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(
        [
            ffmpeg,
            "-hide_banner",
            "-loglevel",
            "error",
            "-y",
            "-fflags",
            "+bitexact",
            "-i",
            str(wav_path),
            "-map_metadata",
            "-1",
            "-c:a",
            "libvorbis",
            "-q:a",
            "4",
            "-flags:a",
            "+bitexact",
            str(ogg_path),
        ],
        check=True,
    )


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--ffmpeg", help="Path to FFmpeg; defaults to the executable on PATH")
    args = parser.parse_args()
    ffmpeg = args.ffmpeg or shutil.which("ffmpeg")
    if not ffmpeg:
        raise SystemExit("FFmpeg is required to author JetSetCraft Vorbis assets")

    entries = []
    with tempfile.TemporaryDirectory(prefix="jetsetcraft-audio-") as temporary:
        wav_path = Path(temporary) / "stinger.wav"
        for sound_path in gang_sound_paths():
            samples, peak, rms = synthesize(sound_path)
            write_wav(wav_path, samples)
            destination = OUTPUT_ROOT / f"{sound_path}.ogg"
            encode_vorbis(ffmpeg, wav_path, destination)
            encoded = destination.read_bytes()
            entries.append(
                {
                    "path": destination.relative_to(ROOT).as_posix(),
                    "bytes": len(encoded),
                    "sha256": hashlib.sha256(encoded).hexdigest(),
                    "duration_seconds": DURATION_SECONDS,
                    "pcm_peak": round(peak, 6),
                    "pcm_rms": round(rms, 6),
                }
            )

    payload = {
        "schema": 1,
        "generator": "tools/generate_audio.py",
        "license": "Original JetSetCraft procedural audio; project code/content license applies",
        "sample_rate": SAMPLE_RATE,
        "channels": 1,
        "asset_count": len(entries),
        "entries": entries,
    }
    MANIFEST.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
    print(f"generated {len(entries)} original JetSetCraft gang stingers")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
