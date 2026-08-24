#!/usr/bin/env python3
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WIKI = ROOT / 'wiki'
EXPECTED = {
    'Home.md', 'Getting-Started.md', 'Controls.md', 'Ride-Styles.md',
    'Tricks-Combos-and-Flow.md', 'Breakdance-and-Cyphers.md',
    'Grinding-and-Transfers.md', 'World-Physics.md', 'Graffiti.md',
    'Gang-Wars-Boombox-and-Mob-Atlas.md',
    'Crafting-and-Survival.md', 'Compatibility.md', 'Modpack-Maker-Guide.md',
    'Configuration.md', 'Multiplayer-and-Servers.md', 'Testing-and-Verification.md',
    'Developer-Architecture.md', 'Troubleshooting.md', 'FAQ.md', 'Roadmap.md',
    '_Sidebar.md', '_Footer.md',
}
errors: list[str] = []

missing = EXPECTED - {p.name for p in WIKI.glob('*.md')}
if missing:
    errors.append(f'missing wiki pages: {sorted(missing)}')

link_pattern = re.compile(r'\[\[(?:[^\]|]+\|)?([^\]]+)\]\]')
for path in sorted(WIKI.glob('*.md')):
    text = path.read_text(encoding='utf-8')
    if path.name not in {'_Sidebar.md', '_Footer.md'} and len(text.strip()) < 450:
        errors.append(f'{path.name}: page is too thin for the production wiki')
    if re.search(r'(?:^|\s)(?:TODO|TBD)\s*:|<PLACEHOLDER>', text, re.IGNORECASE | re.MULTILINE):
        errors.append(f'{path.name}: placeholder marker found')
    for target in link_pattern.findall(text):
        slug = target.strip().replace(' ', '-')
        if slug.lower() == 'home':
            candidate = WIKI / 'Home.md'
        else:
            candidate = WIKI / f'{slug}.md'
        if not candidate.exists():
            errors.append(f'{path.name}: broken wiki link target {target!r}')

sidebar = (WIKI / '_Sidebar.md').read_text(encoding='utf-8') if (WIKI / '_Sidebar.md').exists() else ''
for page in EXPECTED - {'_Sidebar.md', '_Footer.md'}:
    slug = page[:-3]
    if slug != 'Home' and slug not in sidebar:
        errors.append(f'_Sidebar.md: missing navigation entry for {page}')

joined = '\n'.join(p.read_text(encoding='utf-8') for p in WIKI.glob('*.md'))
for token in ('0.3.0', '28 named', '139', 'cypher', 'scooter', 'hoverboard',
              'Aether', 'Twilight Forest', 'reducedMotion', 'GameTest',
              'Boombox', 'gang_id', 'Junior', 'Mob Atlas'):
    if token.lower() not in joined.lower():
        errors.append(f'wiki coverage missing required token: {token}')

if errors:
    print('JetSetCraft wiki validation FAILED')
    for error in errors:
        print(' -', error)
    sys.exit(1)
print(f'JetSetCraft wiki validation OK: {len(list(WIKI.glob("*.md")))} pages')
