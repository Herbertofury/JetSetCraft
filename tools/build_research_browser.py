#!/usr/bin/env python3
"""Build the dependency-free JetSetCraft research browser from its source catalogue."""
from __future__ import annotations

import argparse
import html
import json
from pathlib import Path
from urllib.parse import urlparse

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'docs' / 'research' / 'catalogue.json'
TEMPLATE = ROOT / 'docs' / 'research' / 'browser-template.html'
OUTPUT = ROOT / 'docs' / 'research.html'


def build() -> str:
    catalogue = json.loads(SOURCE.read_text(encoding='utf-8'))
    if not isinstance(catalogue, dict) or not isinstance(catalogue.get('entries'), list):
        raise ValueError('catalogue must contain an entries array')
    if not isinstance(catalogue.get('checked'), str) or not catalogue['checked'].strip():
        raise ValueError('catalogue must contain a checked date')
    entries = catalogue['entries']
    ids: set[str] = set()
    for row in entries:
        if not isinstance(row, dict):
            raise ValueError('each catalogue entry must be an object')
        for key in ('id', 'title', 'category', 'source_url', 'lesson', 'application', 'checked'):
            if not isinstance(row.get(key), str) or not row[key].strip():
                raise ValueError(f'{row.get("id", "unknown")}: missing {key}')
        if row['id'] in ids:
            raise ValueError(f'duplicate id: {row["id"]}')
        ids.add(row['id'])
        if row.get('priority') not in ('P1', 'P2', 'P3', 'Legacy'):
            raise ValueError(f'{row["id"]}: invalid priority')
        if not isinstance(row.get('legacy', False), bool):
            raise ValueError(f'{row["id"]}: legacy must be a boolean')
        tags = row.get('tags', [])
        if not isinstance(tags, list) or any(not isinstance(tag, str) for tag in tags):
            raise ValueError(f'{row["id"]}: tags must be an array of strings')
        for key in ('artist', 'status', 'prototype', 'evidence', 'image_credit', 'wiki'):
            if row.get(key) is not None and not isinstance(row[key], str):
                raise ValueError(f'{row["id"]}: invalid {key}')
        for key in ('source_url', 'additional_url', 'media_url', 'thumbnail_url', 'contact_url'):
            value = row.get(key, '')
            if value is None or value == '':
                continue
            if not isinstance(value, str) or any(char.isspace() or ord(char) < 32 for char in value):
                raise ValueError(f'{row["id"]}: invalid {key}')
            try:
                parsed = urlparse(value)
                valid = parsed.scheme == 'https' and parsed.hostname and parsed.username is None and parsed.password is None
                parsed.port  # Validate malformed or out-of-range port declarations too.
            except ValueError:
                valid = False
            if not valid:
                raise ValueError(f'{row["id"]}: invalid {key}')
        wiki = row.get('wiki', '')
        if wiki and ('/' in wiki or '\\' in wiki or not (ROOT / 'wiki' / (wiki + '.md')).is_file()):
            raise ValueError(f'{row["id"]}: missing wiki page {wiki}')
    # Escape HTML-significant characters even inside a non-executable JSON script element.
    payload = json.dumps(catalogue, ensure_ascii=False, separators=(',', ':'))
    payload = payload.replace('&', '\\u0026').replace('<', '\\u003c').replace('>', '\\u003e')
    fresh = sum(not row.get('legacy', False) for row in entries)
    legacy = len(entries) - fresh
    template = TEMPLATE.read_text(encoding='utf-8')
    if template.count('__CATALOGUE_JSON__') != 1:
        raise ValueError('browser template must contain exactly one catalogue placeholder')
    # Insert data last, so a literal placeholder inside a source title remains intact.
    template = template.replace('__FRESH_COUNT__', str(fresh)).replace('__LEGACY_COUNT__', str(legacy)).replace('__TOTAL_COUNT__', str(len(entries))).replace('__CHECKED_DATE__', html.escape(catalogue['checked']))
    return template.replace('__CATALOGUE_JSON__', payload)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true', help='Fail if the committed browser differs from the source catalogue.')
    args = parser.parse_args()
    rendered = build()
    if args.check:
        if not OUTPUT.is_file() or OUTPUT.read_text(encoding='utf-8') != rendered:
            raise SystemExit('Research browser is stale; run python tools/build_research_browser.py')
        print('Research browser source, records, wiki targets and generated output verified.')
    else:
        OUTPUT.write_text(rendered, encoding='utf-8')
        print(f'Built {OUTPUT.relative_to(ROOT)}')


if __name__ == '__main__':
    main()
