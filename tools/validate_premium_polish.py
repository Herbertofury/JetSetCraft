#!/usr/bin/env python3
"""High-signal offline acceptance for authored models, animation continuity, and compatibility boundaries."""
from __future__ import annotations

import json
import math
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/jetsetcraft"
JAVA = ROOT / "src/main/java/com/herberto/jetsetcraft"
ERRORS: list[str] = []

CORE_MODELS = {
    "inline_skates",
    "quad_skates",
    "street_board",
    "hoverboard",
    "bmx",
    "scooter",
    "spray_can",
}


def fail(message: str) -> None:
    ERRORS.append(message)


def finite_triplet(value: object) -> bool:
    return (
        isinstance(value, dict)
        and all(isinstance(value.get(axis), (int, float)) and math.isfinite(float(value[axis]))
                for axis in ("pitch", "yaw", "roll"))
    )


# Item model metadata: OBJ items need a normal Forge parent and a particle texture so inventory/world particles
# never fall back to the missing-texture checkerboard.
for name in sorted(CORE_MODELS):
    model_path = ASSETS / "models/item" / f"{name}.json"
    if not model_path.exists():
        fail(f"missing item model metadata: {model_path.relative_to(ROOT)}")
        continue
    model = json.loads(model_path.read_text(encoding="utf-8"))
    if model.get("parent") != "forge:item/default":
        fail(f"{model_path.relative_to(ROOT)}: OBJ item must inherit forge:item/default")
    if model.get("loader") != "forge:obj" or model.get("flip_v") is not True:
        fail(f"{model_path.relative_to(ROOT)}: invalid Forge OBJ loader contract")
    expected_obj = f"jetsetcraft:models/obj/{name}.obj"
    if model.get("model") != expected_obj:
        fail(f"{model_path.relative_to(ROOT)}: expected model {expected_obj!r}")
    particle = model.get("textures", {}).get("particle")
    if not isinstance(particle, str) or not particle.startswith("jetsetcraft:item/"):
        fail(f"{model_path.relative_to(ROOT)}: missing namespaced particle texture")
    else:
        texture = ASSETS / "textures/item" / f"{particle.rsplit('/', 1)[-1]}.png"
        if not texture.exists():
            fail(f"{model_path.relative_to(ROOT)}: particle texture does not exist: {texture.relative_to(ROOT)}")


# Geometry integrity. This is deliberately stricter than a face-count check: non-finite coordinates, zero-area
# faces, unresolved materials, or implausible extents are all release blockers even if Forge can parse the file.
for obj_path in sorted((ASSETS / "models/obj").glob("*.obj")):
    vertices: list[tuple[float, float, float]] = []
    face_count = 0
    degenerate = 0
    used_materials: set[str] = set()
    mtllibs: list[str] = []
    for line_no, raw in enumerate(obj_path.read_text(encoding="utf-8").splitlines(), 1):
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        fields = line.split()
        if fields[0] == "v":
            if len(fields) < 4:
                fail(f"{obj_path.relative_to(ROOT)}:{line_no}: malformed vertex")
                continue
            try:
                vertex = tuple(float(value) for value in fields[1:4])
            except ValueError:
                fail(f"{obj_path.relative_to(ROOT)}:{line_no}: non-numeric vertex")
                continue
            if not all(math.isfinite(value) for value in vertex):
                fail(f"{obj_path.relative_to(ROOT)}:{line_no}: non-finite vertex")
            vertices.append(vertex)  # type: ignore[arg-type]
        elif fields[0] == "f":
            face_count += 1
            indices: list[int] = []
            for token in fields[1:]:
                try:
                    raw_index = int(token.split("/", 1)[0])
                except ValueError:
                    fail(f"{obj_path.relative_to(ROOT)}:{line_no}: invalid face token {token!r}")
                    continue
                index = raw_index - 1 if raw_index > 0 else len(vertices) + raw_index
                if index < 0 or index >= len(vertices):
                    fail(f"{obj_path.relative_to(ROOT)}:{line_no}: face index {raw_index} out of range")
                else:
                    indices.append(index)
            if len(indices) >= 3:
                a, b, c = (vertices[index] for index in indices[:3])
                ab = (b[0] - a[0], b[1] - a[1], b[2] - a[2])
                ac = (c[0] - a[0], c[1] - a[1], c[2] - a[2])
                cross = (
                    ab[1] * ac[2] - ab[2] * ac[1],
                    ab[2] * ac[0] - ab[0] * ac[2],
                    ab[0] * ac[1] - ab[1] * ac[0],
                )
                if sum(value * value for value in cross) <= 1.0e-14:
                    degenerate += 1
        elif fields[0] == "usemtl" and len(fields) >= 2:
            used_materials.add(fields[1])
        elif fields[0] == "mtllib" and len(fields) >= 2:
            mtllibs.extend(fields[1:])

    if obj_path.stem in CORE_MODELS and len(vertices) < 1000:
        fail(f"{obj_path.relative_to(ROOT)}: only {len(vertices)} vertices; premium floor is 1000")
    if face_count <= 0:
        fail(f"{obj_path.relative_to(ROOT)}: no faces")
    if face_count and degenerate / face_count > 0.01:
        fail(f"{obj_path.relative_to(ROOT)}: {degenerate}/{face_count} faces are degenerate")
    if vertices:
        spans = [max(v[axis] for v in vertices) - min(v[axis] for v in vertices) for axis in range(3)]
        if max(spans) > 5.0 or max(spans) < 0.10 or sum(span > 0.01 for span in spans) < 2:
            fail(f"{obj_path.relative_to(ROOT)}: implausible bounds {tuple(round(span, 5) for span in spans)}")

    defined_materials: set[str] = set()
    for library in mtllibs:
        mtl_path = obj_path.parent / library
        if not mtl_path.exists():
            fail(f"{obj_path.relative_to(ROOT)}: missing material library {library}")
            continue
        for raw in mtl_path.read_text(encoding="utf-8").splitlines():
            fields = raw.strip().split()
            if len(fields) >= 2 and fields[0] == "newmtl":
                defined_materials.add(fields[1])
            if len(fields) >= 2 and fields[0] == "map_Kd":
                texture_id = fields[-1]
                if ":" not in texture_id:
                    fail(f"{mtl_path.relative_to(ROOT)}: unnamespaced texture {texture_id!r}")
                else:
                    namespace, path = texture_id.split(":", 1)
                    texture = ROOT / "src/main/resources/assets" / namespace / "textures" / f"{path}.png"
                    if not texture.exists():
                        fail(f"{mtl_path.relative_to(ROOT)}: missing texture {texture.relative_to(ROOT)}")
    missing_materials = used_materials - defined_materials
    if missing_materials:
        fail(f"{obj_path.relative_to(ROOT)}: undefined materials {sorted(missing_materials)}")


# Animation continuity and safety. Every loop must be seam-closed, ticks must be monotonic, and all authored
# Euler values must remain finite and within a generous radians envelope.
animation_dir = ASSETS / "player_animation"
clips = sorted(animation_dir.glob("*.json"))
uuids: set[str] = set()
for clip_path in clips:
    payload = json.loads(clip_path.read_text(encoding="utf-8"))
    clip_uuid = payload.get("uuid")
    if not isinstance(clip_uuid, str) or clip_uuid in uuids:
        fail(f"{clip_path.relative_to(ROOT)}: missing/duplicate deterministic UUID {clip_uuid!r}")
    else:
        uuids.add(clip_uuid)
    emote = payload.get("emote", {})
    end_tick = emote.get("endTick")
    moves = emote.get("moves")
    if not isinstance(end_tick, int) or end_tick <= 0 or not isinstance(moves, list) or len(moves) < 2:
        fail(f"{clip_path.relative_to(ROOT)}: invalid endTick/move list")
        continue
    ticks = [move.get("tick") for move in moves if isinstance(move, dict)]
    if ticks != sorted(ticks) or len(set(ticks)) != len(ticks) or ticks[0] != 0 or ticks[-1] != end_tick:
        fail(f"{clip_path.relative_to(ROOT)}: keyframe ticks are not unique, ordered, and endpoint-complete: {ticks}")
    for frame_index, move in enumerate(moves):
        if not isinstance(move, dict):
            fail(f"{clip_path.relative_to(ROOT)}: frame {frame_index} is not an object")
            continue
        for part_name, rotation in move.items():
            if part_name in {"tick", "easing"}:
                continue
            if not finite_triplet(rotation):
                fail(f"{clip_path.relative_to(ROOT)}: frame {frame_index} part {part_name} is non-finite/malformed")
                continue
            if any(abs(float(rotation[axis])) > 4.0 for axis in ("pitch", "yaw", "roll")):
                fail(f"{clip_path.relative_to(ROOT)}: frame {frame_index} part {part_name} exceeds safe radians envelope")
    if emote.get("isLoop") is True and moves[0] != {**moves[-1], "tick": 0, "easing": moves[0].get("easing", moves[-1].get("easing"))}:
        # Compare actual poses independently of tick/easing; a seam may use a different interpolation token.
        def pose(move: dict) -> dict:
            return {key: value for key, value in move.items() if key not in {"tick", "easing"}}
        if pose(moves[0]) != pose(moves[-1]):
            fail(f"{clip_path.relative_to(ROOT)}: looping animation has a visible first/last pose seam")

if len(clips) != 68:
    fail(f"expected exactly 68 authored player animation clips, found {len(clips)}")


# Compatibility boundary: production Java can depend on Minecraft/Forge/player-animation, never on an optional
# gameplay mod class. Data tags may reference optional registry IDs with required=false; Java classloading may not.
allowed_import_prefixes = (
    "com.google.gson.",
    "com.herberto.jetsetcraft.",
    "com.mojang.",
    "dev.kosmx.playerAnim.",
    "java.",
    "javax.",
    "net.minecraft.",
    "net.minecraftforge.",
    "org.jetbrains.",
    "org.joml.",
    "org.lwjgl.",
    "org.slf4j.",
)
optional_compat_prefixes = ("com.simibubi.create.", "com.tacz.guns.")
for java_path in sorted(JAVA.rglob("*.java")):
    text = java_path.read_text(encoding="utf-8")
    in_isolated_compat = "compat" in java_path.relative_to(JAVA).parts
    for imported in re.findall(r"^import\s+([^;]+);", text, re.MULTILINE):
        if imported.startswith(optional_compat_prefixes):
            if not in_isolated_compat:
                fail(f"{java_path.relative_to(ROOT)}: optional mod class escaped isolated compat package: {imported}")
        elif not imported.startswith(allowed_import_prefixes):
            fail(f"{java_path.relative_to(ROOT)}: hard optional/unknown class import {imported}")
    for forbidden in (
        "org.spongepowered.asm.mixin",
        "net.bytebuddy",
        "org.objectweb.asm",
        "Class.forName(\"com.aether",
        "Class.forName(\"twilightforest",
    ):
        if forbidden in text:
            fail(f"{java_path.relative_to(ROOT)}: forbidden invasive/optional classloading token {forbidden}")

source_contracts = {
    JAVA / "client/ClientEvents.java": (
        "ClientRideState.remove(entityId)",
        "ClientMobGearState.remove(entityId)",
        "RideAnimationController.remove(entityId)",
        "mc.screen == null",
    ),
    JAVA / "data/JetSetData.java": (
        "finiteVector(value)",
        "Float.isFinite(value)",
        "Double.isFinite(value)",
        "physical item is the source of truth",
        "setRideGear(other.rideGear)",
        "other.style == physicalStyle",
    ),
    JAVA / "event/MobStreetGearEvents.java": (
        "remainder.shrink(1)",
        "conversion never deletes gear",
    ),
    JAVA / "client/render/MobRideGearLayer.java": (
        "No fake humanoid feet",
        "made half the rig face backward",
    ),
}
for path, needles in source_contracts.items():
    text = path.read_text(encoding="utf-8")
    for needle in needles:
        if needle not in text:
            fail(f"{path.relative_to(ROOT)}: missing premium contract {needle!r}")

all_java = "\n".join(path.read_text(encoding="utf-8") for path in sorted(JAVA.rglob("*.java")))
compat_manager = (JAVA / "compat/CompatManager.java").read_text(encoding="utf-8")
create_bridge = (JAVA / "movement/CreateRailBridge.java").read_text(encoding="utf-8")
if 'ModList.get().isLoaded("tacz")' not in compat_manager or 'TACZ && TaczCompat.isGun(stack)' not in compat_manager:
    fail("TacZ compatibility is not guarded behind Forge ModList and Java short-circuiting")
if 'ModList.get().isLoaded("create")' not in create_bridge or 'Class.forName("com.herberto.jetsetcraft.compat.create.CreateRailProvider")' not in create_bridge:
    fail("Create compatibility is not isolated behind ModList plus reflective provider loading")
if "setDropChance(event.getSlot(), 0.0F)" in all_java:
    fail("Street Gear must not permanently overwrite another mod's future equipment drop chance")
if all_java.count("double extra = JetSetConfig.CLIENT.maxExtraFov.get() * speedEffect;") != 1:
    fail("dynamic-FOV calculation must have exactly one local declaration")

if ERRORS:
    print("JetSetCraft premium-polish validation FAILED")
    for error in ERRORS:
        print(" -", error)
    sys.exit(1)

print(
    "JetSetCraft premium-polish validation OK: "
    f"{len(CORE_MODELS)} authored meshes, {len(clips)} seam-checked animations, compatibility boundary intact"
)
