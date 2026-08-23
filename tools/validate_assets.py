#!/usr/bin/env python3
from __future__ import annotations
import json, math, re, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT/'src/main/resources/assets/jetsetcraft'
errors=[]

# All JSON must parse.
for p in sorted((ROOT/'src/main/resources').rglob('*.json')):
    try: json.loads(p.read_text(encoding='utf-8'))
    except Exception as e: errors.append(f'{p.relative_to(ROOT)}: invalid JSON: {e}')

# Locomotion remains lower-body only so combat mods retain arms/hands. Explicit dances and ground stunts
# are full-body performance clips, and the client suppresses them whenever a weapon overlay is active.
for p in sorted((ASSETS/'player_animation').glob('*.json')):
    data=json.loads(p.read_text(encoding='utf-8'))
    text=json.dumps(data)
    emote=data.get('emote', {})
    moves=emote.get('moves', [])
    ticks=[move.get('tick') for move in moves]
    if len(moves) < 2 or any(not isinstance(tick, int) for tick in ticks):
        errors.append(f'{p.relative_to(ROOT)}: animation needs at least two integer-timed keyframes')
    elif ticks != sorted(set(ticks)):
        errors.append(f'{p.relative_to(ROOT)}: animation keyframe ticks must be unique and increasing')
    elif ticks[0] != emote.get('beginTick') or ticks[-1] != emote.get('endTick'):
        errors.append(f'{p.relative_to(ROOT)}: keyframes do not align with begin/end ticks')
    for move in moves:
        for bone, angles in move.items():
            if bone in {'tick', 'easing'} or not isinstance(angles, dict):
                continue
            for axis, value in angles.items():
                if not isinstance(value, (int, float)) or not math.isfinite(value):
                    errors.append(f'{p.relative_to(ROOT)}: non-finite {bone}.{axis} value')
    full_body = p.stem.startswith('dance_') or p.stem.startswith('stunt_')
    if not full_body and len(moves) < 5:
        errors.append(f'{p.relative_to(ROOT)}: gameplay motion clip has only {len(moves)} keyframes; premium floor is 5')
    if full_body:
        for required in ('body','leftArm','rightArm','leftLeg','rightLeg'):
            if required not in text:
                errors.append(f'{p.relative_to(ROOT)}: full-body performance clip missing {required}')
        for forbidden in ('leftItem','rightItem'):
            if forbidden in text:
                errors.append(f'{p.relative_to(ROOT)}: performance clip must not animate held-item bones {forbidden}')
    else:
        for forbidden in ('leftArm','rightArm','head','leftItem','rightItem'):
            if forbidden in text:
                errors.append(f'{p.relative_to(ROOT)}: forbidden upper-body key {forbidden}')

# Every registered ride item has an item model and every OBJ face index is in bounds.
items_java=(ROOT/'src/main/java/com/herberto/jetsetcraft/registry/ModItems.java').read_text()
registered=re.findall(r'ITEMS\.register\("([a-z0-9_]+)"',items_java)
for item in registered:
    model=ASSETS/'models/item'/f'{item}.json'
    if not model.exists(): errors.append(f'missing item model for {item}: {model.relative_to(ROOT)}')

for obj in sorted((ASSETS/'models/obj').glob('*.obj')):
    vertices=0
    faces=0
    for line_no,line in enumerate(obj.read_text(encoding='utf-8').splitlines(),1):
        if line.startswith('v '): vertices += 1
        elif line.startswith('f '):
            faces += 1
            for token in line.split()[1:]:
                try: idx=int(token.split('/')[0])
                except ValueError:
                    errors.append(f'{obj.relative_to(ROOT)}:{line_no}: invalid face token {token}')
                    continue
                if idx == 0 or abs(idx)>vertices:
                    errors.append(f'{obj.relative_to(ROOT)}:{line_no}: face index {idx} out of current range 1..{vertices}')
    if vertices < 1000 and obj.stem in {'bmx','inline_skates','quad_skates','street_board','hoverboard','scooter','spray_can'}:
        errors.append(f'{obj.relative_to(ROOT)}: core model only has {vertices} vertices; quality floor is 1000')
    if not faces: errors.append(f'{obj.relative_to(ROOT)}: no faces')

# No stale pre-rename production identifiers.
for p in list((ROOT/'src/main/java/com/herberto/jetsetcraft').rglob('*.java')) + list((ASSETS/'lang').glob('*.json')):
    text=p.read_text(encoding='utf-8')
    if 'streetrush' in text.lower() or 'StreetRush' in text:
        errors.append(f'{p.relative_to(ROOT)}: stale StreetRush identifier')

required_clips={
    'inline_ride','inline_boost','quad_ride','quad_boost','board_ride','board_boost',
    'bmx_ride','bmx_boost','hover_ride','hover_boost','scooter_ride','scooter_boost',
    'grind','wallride','manual','powerslide',
    *{f'trick_{i}' for i in range(8)},
    *{f'grind_trick_{i}' for i in range(8)},
    *{f'dance_{i}' for i in range(28)},
    *{f'stunt_{i}' for i in range(8)},
}
actual={p.stem for p in (ASSETS/'player_animation').glob('*.json')}
for missing in sorted(required_clips-actual): errors.append(f'missing player animation clip {missing}')



# Graffiti catalog is generated from preserved source art and must remain complete/aspect-aware.
catalog_path = ROOT/'src/main/resources/assets/jetsetcraft/graffiti/catalog.json'
if not catalog_path.exists():
    errors.append('missing generated graffiti catalog')
else:
    try:
        catalog = json.loads(catalog_path.read_text(encoding='utf-8'))
        entries = catalog.get('entries', [])
        user_archive = ROOT/'source_assets/authorized/jsr/JSRGraffiti.zip'
        if not user_archive.exists():
            user_archive = ROOT/'build/asset-cache/JSRGraffiti.zip'
        if user_archive.exists():
            import zipfile, hashlib
            digest = hashlib.sha256(user_archive.read_bytes()).hexdigest()
            if digest != '8541009fcfb3ec77f22e7aeafb2bcfceebd64decddf168171df24182438c70d9':
                errors.append(f'graffiti source archive SHA mismatch: {digest}')
            with zipfile.ZipFile(user_archive) as zf:
                user_source_count = sum(1 for n in zf.namelist() if n.lower().endswith('.png') and not n.endswith('/'))
        else:
            user_source_count = 0
        expected_min = user_source_count + 4
        if len(entries) < expected_min:
            errors.append(f'graffiti catalog incomplete: expected at least {expected_min}, got {len(entries)}')
        seen = set()
        for e in entries:
            ident = e.get('id')
            if not ident or ident in seen:
                errors.append(f'graffiti catalog duplicate/blank id: {ident!r}')
            seen.add(ident)
            if int(e.get('width', 0)) <= 0 or int(e.get('height', 0)) <= 0:
                errors.append(f'graffiti catalog invalid dimensions: {ident!r}')
            tex = str(e.get('texture', ''))
            prefix = 'jetsetcraft:textures/'
            if tex.startswith(prefix):
                rel = tex[len(prefix):]
                tex_path = ROOT/'src/main/resources/assets/jetsetcraft/textures'/rel
                if not tex_path.exists(): errors.append(f'graffiti texture missing for {ident}: {rel}')
    except Exception as exc:
        errors.append(f'graffiti catalog parse failure: {exc}')

# Official project branding is exact-source only. If the owner-supplied source is mounted, every required derivative must exist.
brand_source = ROOT/'source_assets/brand/jetsetcraft_official_art.png'
if brand_source.exists():
    from PIL import Image
    brand_outputs = {
        ROOT/'src/main/resources/jetsetcraft.png': (128, 128),
        ROOT/'art/generated/jetsetcraft-icon-512.png': (512, 512),
        ROOT/'art/generated/jetsetcraft-banner-1200x630.png': (1200, 630),
    }
    for path, expected in brand_outputs.items():
        if not path.exists():
            errors.append(f'missing generated brand derivative {path.relative_to(ROOT)}')
            continue
        with Image.open(path) as img:
            if img.size != expected:
                errors.append(f'{path.relative_to(ROOT)}: expected {expected}, got {img.size}')

if errors:
    print('JetSetCraft asset validation FAILED')
    for e in errors: print(' -',e)
    sys.exit(1)
print(f'JetSetCraft asset validation OK: {len(registered)} registered items, {len(actual)} animation clips')
