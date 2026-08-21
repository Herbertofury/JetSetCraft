#!/usr/bin/env python3
from __future__ import annotations

from generate_models import Mesh, OBJ_DIR, TEX_DIR, texture_gradient


def make_hoverboard() -> None:
    # Dedicated dense hoverboard: no wheels, no skateboard trucks. The silhouette is a slightly longer magnetic
    # deck with four levitation pods, visible field coils, a protected energy core, side emitters and foot pads.
    m = Mesh('hoverboard')
    m.deck(length=1.30, width=.39, thickness=.060, mat='shell', segments=64)

    # Armored underbody spine and inset side rails.
    m.box((0.0, 0.040, 0.0), (.20, .055, .96), 'metal')
    m.box((-.175, 0.055, 0.0), (.035, .045, .94), 'glow')
    m.box(( .175, 0.055, 0.0), (.035, .045, .94), 'glow')

    # Four sculpted antigravity pods. Dense ellipsoids make the board read as purpose-built equipment rather than
    # a skateboard with its wheels deleted, while still staying compact enough to fit a Minecraft player cleanly.
    for x in (-.155, .155):
        for z in (-.355, .355):
            m.ellipsoid((x, -0.015, z), (.115, .070, .205), 'metal', lon=34, lat=18)
            m.ellipsoid((x, -0.050, z), (.078, .030, .142), 'glow', lon=30, lat=14)
            m.torus((x, -0.085, z), .070, .014, 'glow', major_seg=36, minor_seg=10, plane='xz')

    # Central power cell and concentric stabilizer field rings.
    m.cylinder((0.0, -0.040, 0.0), .088, .060, 'y', 'core', segments=36)
    m.cylinder((0.0, -0.046, 0.0), .050, .072, 'y', 'glow', segments=32)
    m.torus((0.0, -0.082, 0.0), .115, .018, 'metal', major_seg=44, minor_seg=12, plane='xz')
    m.torus((0.0, -0.086, 0.0), .083, .012, 'glow', major_seg=40, minor_seg=10, plane='xz')

    # Mechanical braces tying the levitation pods into the spine.
    for z in (-.355, .355):
        m.tube_between((-.145, .000, z), (-.040, .025, z * .62), .018, 'metal', segments=18)
        m.tube_between(( .145, .000, z), ( .040, .025, z * .62), .018, 'metal', segments=18)

    # Raised non-slip foot pads with luminous perimeter guides.
    for z in (-.245, .245):
        m.box((0.0, .145, z), (.285, .022, .225), 'pad')
        m.box((0.0, .158, z), (.235, .010, .175), 'accent')

    # Front/rear field emitters and tapered-looking edge fins built from high-detail tubes + plates.
    for z in (-.585, .585):
        m.cylinder((0.0, .085, z), .050, .30, 'x', 'glow', segments=28)
        m.cylinder((0.0, .085, z), .023, .33, 'x', 'metal', segments=20)
    for x in (-.205, .205):
        m.box((x, .075, 0.0), (.025, .055, .62), 'accent')
        m.tube_between((x, .080, -.48), (x * .88, .120, -.60), .014, 'glow', segments=16)
        m.tube_between((x, .080,  .48), (x * .88, .120,  .60), .014, 'glow', segments=16)

    # Small sensor/vent details keep the top silhouette readable at third-person distance.
    for z in (-.47, -.12, .12, .47):
        m.box((-.105, .128, z), (.032, .018, .070), 'metal')
        m.box(( .105, .128, z), (.032, .018, .070), 'metal')

    m.write({
        'shell': 'hover_shell',
        'metal': 'metal',
        'glow': 'hover_glow',
        'core': 'hover_core',
        'pad': 'hover_pad',
        'accent': 'accent',
    })


def make_hover_textures() -> None:
    # Textures are generated at useful source resolution rather than flat single-color placeholders.
    texture_gradient(TEX_DIR / 'hover_shell.png', (256, 256),
                     [(8, 28, 30), (22, 92, 58), (198, 255, 22), (246, 255, 92)], 1)
    texture_gradient(TEX_DIR / 'hover_glow.png', (256, 256),
                     [(4, 38, 18), (38, 255, 92), (224, 255, 50), (255, 250, 170)], 1)
    texture_gradient(TEX_DIR / 'hover_core.png', (256, 256),
                     [(8, 24, 42), (18, 158, 196), (120, 255, 214), (250, 255, 210)], 1)
    texture_gradient(TEX_DIR / 'hover_pad.png', (256, 256),
                     [(10, 12, 14), (32, 38, 34), (66, 76, 52), (18, 22, 18)], 1)


if __name__ == '__main__':
    make_hover_textures()
    make_hoverboard()
    obj = OBJ_DIR / 'hoverboard.obj'
    vertices = sum(1 for line in obj.open(encoding='utf-8') if line.startswith('v '))
    faces = sum(1 for line in obj.open(encoding='utf-8') if line.startswith('f '))
    print('hoverboard', 'vertices', vertices, 'faces', faces, 'bytes', obj.stat().st_size)
    if vertices < 6000:
        raise SystemExit(f'hoverboard quality floor failed: expected >=6000 vertices, got {vertices}')
