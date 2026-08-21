#!/usr/bin/env python3
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ANIM_DIR = ROOT / 'src/main/resources/assets/jetsetcraft/player_animation'
CONTROLLER = ROOT / 'src/main/java/com/herberto/jetsetcraft/client/animation/RideAnimationController.java'

# JetSetCraft locomotion owns the torso lean and legs. Weapon/combat mods must retain the weapon limbs.
ALLOWED_RIDE_TRACKS = {'body', 'leftLeg', 'rightLeg'}
FORBIDDEN_WEAPON_TRACKS = {'leftArm', 'rightArm', 'head'}
errors: list[str] = []
checked = 0

for path in sorted(ANIM_DIR.glob('*.json')):
    try:
        data = json.loads(path.read_text(encoding='utf-8'))
    except Exception as exc:
        errors.append(f'{path.name}: invalid animation JSON: {exc}')
        continue
    moves = data.get('emote', {}).get('moves', [])
    if not moves:
        errors.append(f'{path.name}: no authored moves')
        continue
    checked += 1
    for index, move in enumerate(moves):
        authored = {key for key in move if key not in {'tick', 'easing'}}
        forbidden = authored & FORBIDDEN_WEAPON_TRACKS
        if forbidden:
            errors.append(f'{path.name} move {index}: steals weapon-owned tracks {sorted(forbidden)}')
        unknown = authored - ALLOWED_RIDE_TRACKS
        if unknown:
            errors.append(f'{path.name} move {index}: unexpected ride animation tracks {sorted(unknown)}')

controller = CONTROLLER.read_text(encoding='utf-8')
if 'ride_lower_body' not in controller:
    errors.append('ride animation layer no longer identifies itself as lower-body composition')
if 'Upper-body weapon animation is intentionally not selected here' not in controller:
    errors.append('RideAnimationController lost the explicit combat-animation ownership contract')

if checked < 20:
    errors.append(f'expected at least 20 authored ride/trick clips, found {checked}')

if errors:
    print('JetSetCraft combat/animation ownership validation FAILED')
    for error in errors:
        print(' -', error)
    sys.exit(1)

print(f'JetSetCraft combat/animation ownership validation OK: {checked} clips; arms/head remain weapon-owned')
