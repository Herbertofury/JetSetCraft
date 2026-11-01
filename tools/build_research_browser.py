#!/usr/bin/env python3
"""Build the dependency-free JetSetCraft research browser from its source catalogue."""
from __future__ import annotations

import argparse
from collections import Counter
from datetime import date
import html
import json
import re
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
    signatures: set[tuple[str, str, str]] = set()
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
        for key in ('artist', 'status', 'prototype', 'evidence', 'image_credit', 'wiki', 'batch', 'shelf', 'version_notes'):
            if row.get(key) is not None and not isinstance(row[key], str):
                raise ValueError(f'{row["id"]}: invalid {key}')
        for key in ('source_url', 'additional_url', 'media_url', 'video_url', 'thumbnail_url', 'contact_url'):
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
        if row.get('thumbnail_url') and not row.get('image_credit'):
            raise ValueError(f'{row["id"]}: image credit is required for a thumbnail')
        if not row.get('legacy'):
            signature = tuple(' '.join(str(row.get(k, '')).casefold().split()) for k in ('category', 'artist', 'title'))
            if signature in signatures:
                raise ValueError(f'{row["id"]}: repeated curated title/artist in the same category')
            signatures.add(signature)
    # A later research pass must not silently relabel earlier records as newly checked.
    passes = catalogue.get('passes', [])
    pass_ids: set[str] = set()
    if not isinstance(passes, list) or not passes:
        raise ValueError('catalogue must declare at least one research pass')
    for research_pass in passes:
        if not isinstance(research_pass, dict):
            raise ValueError('research pass must be an object')
        pass_id = research_pass.get('id', '')
        try:
            date.fromisoformat(pass_id)
        except (ValueError, TypeError):
            raise ValueError(f'invalid research pass date: {pass_id}')
        if pass_id in pass_ids:
            raise ValueError(f'duplicate research pass: {pass_id}')
        pass_ids.add(pass_id)
        if not isinstance(research_pass.get('label'), str) or not research_pass['label'].strip():
            raise ValueError(f'{pass_id}: research pass label is required')
        actual_count = sum(row.get('batch') == pass_id and not row.get('legacy') for row in entries)
        if research_pass.get('entry_count') != actual_count:
            raise ValueError(f'{pass_id}: declared pass count does not match {actual_count} entries')
    for row in entries:
        if row.get('legacy'):
            if row.get('batch') != 'legacy':
                raise ValueError(f'{row["id"]}: historical records must retain the legacy batch')
        elif row.get('batch') not in pass_ids:
            raise ValueError(f'{row["id"]}: unknown research pass')
    if catalogue.get('latest_batch') not in pass_ids:
        raise ValueError('latest_batch must identify a declared research pass')
    lookup = {row['id']: row for row in entries}
    path_ids: set[str] = set()
    for path in catalogue.get('listening_paths', []):
        if not isinstance(path, dict) or not isinstance(path.get('id'), str) or not path['id']:
            raise ValueError('listening path requires an id')
        if path['id'] in path_ids:
            raise ValueError(f'duplicate listening path: {path["id"]}')
        path_ids.add(path['id'])
        for key in ('title', 'description'):
            if not isinstance(path.get(key), str) or not path[key].strip():
                raise ValueError(f'{path["id"]}: missing {key}')
        track_ids = path.get('entry_ids')
        if not isinstance(track_ids, list) or not track_ids or any(not isinstance(item, str) for item in track_ids):
            raise ValueError(f'{path["id"]}: entry_ids must be a nonempty string array')
        if len(track_ids) != len(set(track_ids)):
            raise ValueError(f'{path["id"]}: duplicate track in listening path')
        if any(track_id not in lookup or lookup[track_id]['category'] != 'Music' for track_id in track_ids):
            raise ValueError(f'{path["id"]}: listening path points to a missing or non-music record')
    if catalogue.get('counts') != dict(Counter(row['category'] for row in entries)):
        raise ValueError('catalogue category counts do not match the entries')
    # Escape HTML-significant characters even inside a non-executable JSON script element.
    payload = json.dumps(catalogue, ensure_ascii=False, separators=(',', ':'))
    payload = payload.replace('&', '\\u0026').replace('<', '\\u003c').replace('>', '\\u003e')
    fresh = sum(not row.get('legacy', False) for row in entries)
    legacy = len(entries) - fresh
    latest = sum(row.get('batch') == catalogue['latest_batch'] for row in entries)
    if catalogue.get('new_entries') != fresh or catalogue.get('legacy_entries') != legacy:
        raise ValueError('curated/historical totals do not match the entries')
    template = TEMPLATE.read_text(encoding='utf-8')
    if template.count('__CATALOGUE_JSON__') != 1:
        raise ValueError('browser template must contain exactly one catalogue placeholder')
    # Insert data last, so a literal placeholder inside a source title remains intact.
    template = template.replace('__LATEST_COUNT__', str(latest)).replace('__FRESH_COUNT__', str(fresh)).replace('__LEGACY_COUNT__', str(legacy)).replace('__TOTAL_COUNT__', str(len(entries))).replace('__CHECKED_DATE__', html.escape(catalogue['checked']))
    if re.search(r'__[A-Z_]+__', template.replace('__CATALOGUE_JSON__', '')):
        raise ValueError('browser template contains an unresolved placeholder')
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
