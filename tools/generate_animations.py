#!/usr/bin/env python3
from __future__ import annotations

import json
import math
import uuid
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/assets/jetsetcraft/player_animation'
OUT.mkdir(parents=True, exist_ok=True)

DANCE_OFFSETS = (0, 4, 8, 12, 20, 24)
DANCE_COUNTS = (4, 4, 4, 8, 4, 4)
DANCE_COUNT = sum(DANCE_COUNTS)


def part(pitch=0.0, yaw=0.0, roll=0.0):
    return {'pitch': round(pitch, 5), 'yaw': round(yaw, 5), 'roll': round(roll, 5)}


def frame(tick, easing='INOUTSINE', **parts):
    result = {'tick': tick, 'easing': easing}
    result.update(parts)
    return result


def write_clip(name: str, end_tick: int, moves: list[dict], loop: bool = False):
    payload = {
        'version': 1,
        'uuid': str(uuid.uuid5(uuid.NAMESPACE_URL, f'https://jetsetcraft.dev/animation/{name}')),
        'name': name,
        'emote': {
            'beginTick': 0,
            'endTick': end_tick,
            'stopTick': end_tick,
            'isLoop': loop,
            'returnTick': 0,
            'nsfw': False,
            'degrees': False,
            'moves': moves,
        },
    }
    (OUT / f'{name}.json').write_text(json.dumps(payload, indent=2) + '\n', encoding='utf-8')


def lower_body_pose(name: str, boost: bool, scooter: bool):
    end = 24
    lean = 0.22 if boost else 0.10
    stance = 0.46 if scooter else 0.28
    moves = [
        frame(0, body=part(lean), leftLeg=part(stance, 0.0, 0.08), rightLeg=part(-stance, 0.0, -0.08)),
        frame(12, body=part(lean + 0.04, 0.0, 0.05),
              leftLeg=part(stance - 0.14, 0.0, 0.13), rightLeg=part(-stance + 0.12, 0.0, -0.13)),
        frame(end, body=part(lean), leftLeg=part(stance, 0.0, 0.08), rightLeg=part(-stance, 0.0, -0.08)),
    ]
    write_clip(name, end, moves, loop=True)


def trick_clip(index: int, grind: bool):
    name = f'grind_trick_{index}' if grind else f'trick_{index}'
    end = 18 if grind else 22
    direction = -1.0 if index % 2 else 1.0
    spin = (0.45 + index * 0.11) * direction
    pitch = 0.18 + (index % 3) * 0.16
    moves = [
        frame(0, body=part(0.08), leftLeg=part(0.18), rightLeg=part(-0.16)),
        frame(end // 2, 'OUTQUAD', body=part(pitch, spin, 0.36 * direction),
              leftLeg=part(0.58, 0.18 * direction, 0.32),
              rightLeg=part(-0.52, -0.18 * direction, -0.32)),
        frame(end, body=part(0.08), leftLeg=part(0.18), rightLeg=part(-0.16)),
    ]
    write_clip(name, end, moves)


def full_pose(body=(0.0, 0.0, 0.0), head=(0.0, 0.0, 0.0),
              left_arm=(0.0, 0.0, 0.0), right_arm=(0.0, 0.0, 0.0),
              left_leg=(0.0, 0.0, 0.0), right_leg=(0.0, 0.0, 0.0)):
    return {
        'body': part(*body),
        'head': part(*head),
        'leftArm': part(*left_arm),
        'rightArm': part(*right_arm),
        'leftLeg': part(*left_leg),
        'rightLeg': part(*right_leg),
    }


def dance_identity(index: int) -> tuple[int, int, int]:
    for style, (offset, count) in enumerate(zip(DANCE_OFFSETS, DANCE_COUNTS)):
        if offset <= index < offset + count:
            slot = index - offset
            duration = 28 + (slot % 4) * 4 + (4 if style == 3 else 0)
            return style, slot, duration
    raise ValueError(index)


def toprock_frames(slot: int, end: int) -> list[dict]:
    direction = -1.0 if slot % 2 else 1.0
    cross = 0.26 + slot * 0.055
    return [
        frame(0, **full_pose(body=(0.06, 0.0, 0.0), left_arm=(-0.28, 0.0, -0.22),
                             right_arm=(-0.28, 0.0, 0.22), left_leg=(0.10, 0.0, 0.08), right_leg=(-0.08, 0.0, -0.08))),
        frame(end // 4, 'OUTQUAD', **full_pose(body=(0.10, 0.18 * direction, 0.16 * direction),
                             head=(-0.06, -0.15 * direction, -0.08 * direction),
                             left_arm=(-0.70, 0.12, -0.55 * direction), right_arm=(-0.18, -0.10, 0.62 * direction),
                             left_leg=(cross, 0.16 * direction, 0.24 * direction), right_leg=(-0.20, -0.08, -0.12))),
        frame(end // 2, **full_pose(body=(0.04, -0.12 * direction, -0.10 * direction),
                             left_arm=(-0.12, -0.08, 0.48 * direction), right_arm=(-0.66, 0.12, -0.50 * direction),
                             left_leg=(-0.16, 0.0, -0.12), right_leg=(cross + 0.05, -0.16 * direction, -0.24 * direction))),
        frame(end * 3 // 4, 'OUTQUAD', **full_pose(body=(0.12, 0.22 * direction, 0.18 * direction),
                             head=(-0.05, -0.12 * direction, -0.10 * direction),
                             left_arm=(-0.74, 0.10, -0.60 * direction), right_arm=(-0.20, -0.12, 0.58 * direction),
                             left_leg=(cross + 0.10, 0.18 * direction, 0.30 * direction), right_leg=(-0.24, 0.0, -0.14))),
        frame(end, **full_pose(body=(0.06, 0.0, 0.0), left_arm=(-0.28, 0.0, -0.22),
                             right_arm=(-0.28, 0.0, 0.22), left_leg=(0.10, 0.0, 0.08), right_leg=(-0.08, 0.0, -0.08))),
    ]


def popping_frames(slot: int, end: int) -> list[dict]:
    direction = -1.0 if slot % 2 else 1.0
    angular = 0.38 + slot * 0.12
    return [
        frame(0, 'OUTQUAD', **full_pose(body=(0.02, 0.0, 0.0), left_arm=(-0.35, 0.0, -0.22),
                                       right_arm=(-0.35, 0.0, 0.22), left_leg=(0.08,), right_leg=(-0.08,))),
        frame(end // 5, 'OUTQUAD', **full_pose(body=(0.08, angular * direction, 0.10 * direction),
                                       head=(-0.10, -angular * 0.55 * direction, -0.08 * direction),
                                       left_arm=(-1.05, angular * 0.35, -1.05 * direction),
                                       right_arm=(-0.12, -angular * 0.25, 0.22 * direction),
                                       left_leg=(0.22, 0.0, 0.14), right_leg=(-0.08, 0.0, -0.08))),
        frame(end * 2 // 5, 'OUTQUAD', **full_pose(body=(-0.02, -angular * 0.55 * direction, -0.08 * direction),
                                       head=(0.08, angular * 0.30 * direction, 0.06 * direction),
                                       left_arm=(-0.22, -0.48 * direction, 0.18 * direction),
                                       right_arm=(-1.18, 0.48 * direction, -1.00 * direction),
                                       left_leg=(-0.05, 0.0, -0.10), right_leg=(0.20, 0.0, 0.13))),
        frame(end * 3 // 5, 'OUTQUAD', **full_pose(body=(0.13, angular * 0.35 * direction, 0.18 * direction),
                                       left_arm=(-0.78, 0.62 * direction, -0.72 * direction),
                                       right_arm=(-0.78, -0.62 * direction, 0.72 * direction),
                                       left_leg=(0.18, 0.12 * direction, 0.18), right_leg=(-0.18, -0.12 * direction, -0.18))),
        frame(end * 4 // 5, 'OUTQUAD', **full_pose(body=(-0.04, -angular * direction, -0.10 * direction),
                                       head=(0.05, angular * 0.45 * direction, 0.05 * direction),
                                       left_arm=(-1.22, -0.25, 0.88 * direction), right_arm=(-0.15, 0.20, -0.32 * direction),
                                       left_leg=(-0.06, 0.0, -0.08), right_leg=(0.24, 0.0, 0.15))),
        frame(end, 'OUTQUAD', **full_pose(body=(0.02, 0.0, 0.0), left_arm=(-0.35, 0.0, -0.22),
                                       right_arm=(-0.35, 0.0, 0.22), left_leg=(0.08,), right_leg=(-0.08,))),
    ]


def house_frames(slot: int, end: int) -> list[dict]:
    direction = -1.0 if slot % 2 else 1.0
    jack = 0.22 + slot * 0.04
    frames = []
    for tick in (0, end // 4, end // 2, end * 3 // 4, end):
        phase = tick / end * math.tau
        bounce = abs(math.sin(phase))
        sweep = math.sin(phase) * direction
        frames.append(frame(tick, 'INOUTSINE', **full_pose(
            body=(0.08 + bounce * jack, sweep * 0.18, sweep * 0.12),
            head=(-0.04 - bounce * 0.05, -sweep * 0.10, -sweep * 0.06),
            left_arm=(-0.34 + sweep * 0.28, 0.0, -0.32 - sweep * 0.28),
            right_arm=(-0.34 - sweep * 0.28, 0.0, 0.32 - sweep * 0.28),
            left_leg=(0.16 + sweep * (0.48 + slot * 0.04), 0.10 * sweep, 0.18 + bounce * 0.12),
            right_leg=(0.16 - sweep * (0.48 + slot * 0.04), -0.10 * sweep, -0.18 - bounce * 0.12),
        )))
    return frames


def breaking_frames(slot: int, end: int) -> list[dict]:
    direction = -1.0 if slot % 2 else 1.0
    power = 0.65 + slot * 0.075
    if slot == 0:  # Six-step
        return [
            frame(0, **full_pose(body=(0.38,), left_arm=(-0.90, 0.0, -0.48), right_arm=(-0.90, 0.0, 0.48),
                                 left_leg=(0.30, 0.0, 0.30), right_leg=(-0.28, 0.0, -0.30))),
            frame(end // 3, 'OUTQUAD', **full_pose(body=(0.72, 0.30, 0.34), head=(-0.25, -0.18, -0.12),
                                 left_arm=(-1.35, 0.25, -0.82), right_arm=(-0.55, -0.15, 0.65),
                                 left_leg=(0.92, 0.25, 0.62), right_leg=(-0.30, -0.15, -0.44))),
            frame(end * 2 // 3, 'OUTQUAD', **full_pose(body=(0.72, -0.30, -0.34), head=(-0.25, 0.18, 0.12),
                                 left_arm=(-0.55, 0.15, -0.65), right_arm=(-1.35, -0.25, 0.82),
                                 left_leg=(-0.30, 0.15, 0.44), right_leg=(0.92, -0.25, -0.62))),
            frame(end, **full_pose(body=(0.38,), left_arm=(-0.90, 0.0, -0.48), right_arm=(-0.90, 0.0, 0.48),
                                 left_leg=(0.30, 0.0, 0.30), right_leg=(-0.28, 0.0, -0.30))),
        ]
    if slot in (1, 5, 6):  # Backspin / Halo / Headspin
        tilt = 1.15 if slot == 6 else 0.92
        return [
            frame(0, **full_pose(body=(0.42,), left_arm=(-0.62, 0.0, -0.65), right_arm=(-0.62, 0.0, 0.65),
                                 left_leg=(0.35, 0.0, 0.42), right_leg=(-0.35, 0.0, -0.42))),
            frame(end // 4, 'OUTQUAD', **full_pose(body=(tilt, 0.55 * direction, 0.95 * direction),
                                 head=(-0.58, -0.30 * direction, -0.48 * direction),
                                 left_arm=(-1.22, 0.40, -1.05 * direction), right_arm=(-1.22, -0.40, 1.05 * direction),
                                 left_leg=(power, 0.30, 0.90), right_leg=(-power, -0.30, -0.90))),
            frame(end // 2, **full_pose(body=(tilt, 1.10 * direction, -0.95 * direction),
                                 head=(-0.58, -0.55 * direction, 0.48 * direction),
                                 left_arm=(-1.05, -0.40, 1.00 * direction), right_arm=(-1.05, 0.40, -1.00 * direction),
                                 left_leg=(-power, -0.30, 0.90), right_leg=(power, 0.30, -0.90))),
            frame(end * 3 // 4, 'OUTQUAD', **full_pose(body=(tilt, 1.65 * direction, 0.95 * direction),
                                 head=(-0.58, -0.75 * direction, -0.48 * direction),
                                 left_arm=(-1.25, 0.35, -0.95 * direction), right_arm=(-1.25, -0.35, 0.95 * direction),
                                 left_leg=(power, 0.25, -0.90), right_leg=(-power, -0.25, 0.90))),
            frame(end, **full_pose(body=(0.42,), left_arm=(-0.62, 0.0, -0.65), right_arm=(-0.62, 0.0, 0.65),
                                 left_leg=(0.35, 0.0, 0.42), right_leg=(-0.35, 0.0, -0.42))),
        ]
    # Windmill, flare, swipe and airflare use alternating wide power arcs.
    return [
        frame(0, **full_pose(body=(0.30,), left_arm=(-0.70, 0.0, -0.55), right_arm=(-0.70, 0.0, 0.55),
                             left_leg=(0.20, 0.0, 0.20), right_leg=(-0.20, 0.0, -0.20))),
        frame(end // 4, 'OUTQUAD', **full_pose(body=(0.78, 0.42 * direction, 0.72 * direction),
                             head=(-0.34, -0.22 * direction, -0.25 * direction),
                             left_arm=(-1.36, 0.35, -0.88), right_arm=(-0.45, -0.25, 0.82),
                             left_leg=(power + 0.25, 0.32, 1.05), right_leg=(-power, -0.22, -0.72))),
        frame(end // 2, **full_pose(body=(0.92, 0.95 * direction, -0.74 * direction),
                             head=(-0.42, -0.42 * direction, 0.30 * direction),
                             left_arm=(-0.50, -0.25, 0.80), right_arm=(-1.38, 0.32, -0.90),
                             left_leg=(-power, -0.25, 0.78), right_leg=(power + 0.25, 0.32, -1.05))),
        frame(end * 3 // 4, 'OUTQUAD', **full_pose(body=(0.76, 1.42 * direction, 0.70 * direction),
                             head=(-0.34, -0.62 * direction, -0.25 * direction),
                             left_arm=(-1.34, 0.30, -0.86), right_arm=(-0.48, -0.22, 0.78),
                             left_leg=(power + 0.20, 0.28, -1.02), right_leg=(-power, -0.20, 0.72))),
        frame(end, **full_pose(body=(0.30,), left_arm=(-0.70, 0.0, -0.55), right_arm=(-0.70, 0.0, 0.55),
                             left_leg=(0.20, 0.0, 0.20), right_leg=(-0.20, 0.0, -0.20))),
    ]


def hiphop_frames(slot: int, end: int) -> list[dict]:
    direction = -1.0 if slot % 2 else 1.0
    stride = 0.55 + slot * 0.06
    return [
        frame(0, **full_pose(body=(0.08,), left_arm=(-0.42, 0.0, -0.28), right_arm=(-0.42, 0.0, 0.28),
                             left_leg=(0.12,), right_leg=(-0.12,))),
        frame(end // 4, 'OUTQUAD', **full_pose(body=(0.20, 0.14 * direction, 0.16 * direction),
                             head=(-0.05, -0.12 * direction, -0.05 * direction),
                             left_arm=(-0.18, 0.0, 0.52 * direction), right_arm=(-0.82, 0.0, -0.50 * direction),
                             left_leg=(-0.20, 0.0, -0.20), right_leg=(stride, 0.0, 0.32))),
        frame(end // 2, **full_pose(body=(-0.04, -0.18 * direction, -0.12 * direction),
                             left_arm=(-0.88, 0.0, -0.48 * direction), right_arm=(-0.18, 0.0, 0.50 * direction),
                             left_leg=(stride, 0.0, 0.30), right_leg=(-0.22, 0.0, -0.22))),
        frame(end * 3 // 4, 'OUTQUAD', **full_pose(body=(0.22, 0.22 * direction, 0.20 * direction),
                             head=(-0.08, -0.14 * direction, -0.08 * direction),
                             left_arm=(-0.24, 0.0, 0.58 * direction), right_arm=(-0.90, 0.0, -0.55 * direction),
                             left_leg=(-0.24, 0.0, -0.24), right_leg=(stride + 0.12, 0.0, 0.38))),
        frame(end, **full_pose(body=(0.08,), left_arm=(-0.42, 0.0, -0.28), right_arm=(-0.42, 0.0, 0.28),
                             left_leg=(0.12,), right_leg=(-0.12,))),
    ]


def locking_frames(slot: int, end: int) -> list[dict]:
    direction = -1.0 if slot % 2 else 1.0
    point = 0.75 + slot * 0.10
    return [
        frame(0, 'OUTQUAD', **full_pose(body=(0.02,), left_arm=(-0.25, 0.0, -0.30), right_arm=(-0.25, 0.0, 0.30),
                                       left_leg=(0.08,), right_leg=(-0.08,))),
        frame(end // 5, 'OUTQUAD', **full_pose(body=(0.12, 0.20 * direction, 0.18 * direction),
                                       head=(-0.10, -0.22 * direction, -0.08 * direction),
                                       left_arm=(-1.28, 0.15, -point * direction), right_arm=(-0.10, -0.10, 0.42 * direction),
                                       left_leg=(0.20, 0.0, 0.18), right_leg=(-0.14, 0.0, -0.12))),
        frame(end * 2 // 5, 'OUTQUAD', **full_pose(body=(-0.05, -0.18 * direction, -0.15 * direction),
                                       left_arm=(-0.08, 0.12, 0.40 * direction), right_arm=(-1.30, -0.15, -point * direction),
                                       left_leg=(-0.12, 0.0, -0.12), right_leg=(0.22, 0.0, 0.20))),
        frame(end * 3 // 5, 'OUTQUAD', **full_pose(body=(0.18, 0.0, 0.0), head=(-0.12,),
                                       left_arm=(-0.95, 0.0, -1.12), right_arm=(-0.95, 0.0, 1.12),
                                       left_leg=(0.30, 0.0, 0.22), right_leg=(-0.30, 0.0, -0.22))),
        frame(end * 4 // 5, 'OUTQUAD', **full_pose(body=(0.08, 0.28 * direction, 0.20 * direction),
                                       head=(-0.08, -0.24 * direction, -0.08 * direction),
                                       left_arm=(-1.20, 0.20, -0.78 * direction), right_arm=(-0.20, -0.10, 0.58 * direction),
                                       left_leg=(0.28, 0.0, 0.24), right_leg=(-0.18, 0.0, -0.16))),
        frame(end, 'OUTQUAD', **full_pose(body=(0.02,), left_arm=(-0.25, 0.0, -0.30), right_arm=(-0.25, 0.0, 0.30),
                                       left_leg=(0.08,), right_leg=(-0.08,))),
    ]


def dance_clip(index: int):
    style, slot, end = dance_identity(index)
    if style == 0:
        moves = toprock_frames(slot, end)
    elif style == 1:
        moves = popping_frames(slot, end)
    elif style == 2:
        moves = house_frames(slot, end)
    elif style == 3:
        moves = breaking_frames(slot, end)
    elif style == 4:
        moves = hiphop_frames(slot, end)
    else:
        moves = locking_frames(slot, end)
    write_clip(f'dance_{index}', end, moves, loop=True)


def stunt_clip(index: int):
    name = f'stunt_{index}'
    end = 34
    direction = -1.0 if index % 2 else 1.0
    power = 0.72 + (index % 4) * 0.16
    moves = [
        frame(0, **full_pose(body=(0.08,), left_arm=(-0.10,), right_arm=(-0.10,),
                             left_leg=(0.10,), right_leg=(-0.10,))),
        frame(8, 'OUTQUAD', **full_pose(body=(0.46 + index * 0.035, 0.25 * direction, 0.55 * direction),
                             head=(-0.28, -0.18 * direction, -0.25 * direction),
                             left_arm=(-1.15, 0.35 * direction, -0.62), right_arm=(-1.15, -0.35 * direction, 0.62),
                             left_leg=(power, 0.15, 0.42), right_leg=(-power, -0.15, -0.42))),
        frame(17, 'INOUTSINE', **full_pose(body=(0.82 + (index % 3) * 0.12, 0.78 * direction, -0.62 * direction),
                             head=(-0.40, -0.30 * direction, 0.25 * direction),
                             left_arm=(-0.82, -0.30, 0.96), right_arm=(-1.25, 0.35, -0.92),
                             left_leg=(-power, 0.28, 0.82), right_leg=(power, -0.28, -0.82))),
        frame(26, 'OUTQUAD', **full_pose(body=(0.34, 1.25 * direction, 0.44 * direction),
                             head=(-0.16, -0.42 * direction, -0.18 * direction),
                             left_arm=(-1.18, 0.28, -0.78), right_arm=(-0.58, -0.25, 0.80),
                             left_leg=(power * 0.75, 0.22, -0.66), right_leg=(-power * 0.75, -0.22, 0.66))),
        frame(end, **full_pose(body=(0.08,), left_arm=(-0.10,), right_arm=(-0.10,),
                             left_leg=(0.10,), right_leg=(-0.10,))),
    ]
    write_clip(name, end, moves)


def main():
    lower_body_pose('hover_ride', False, False)
    lower_body_pose('hover_boost', True, False)
    lower_body_pose('scooter_ride', False, True)
    lower_body_pose('scooter_boost', True, True)
    for index in range(4, 8):
        trick_clip(index, False)
        trick_clip(index, True)
    for index in range(DANCE_COUNT):
        dance_clip(index)
    for index in range(8):
        stunt_clip(index)
    print('generated JetSetCraft animation clips:', 4 + 8 + DANCE_COUNT + 8)


if __name__ == '__main__':
    main()
