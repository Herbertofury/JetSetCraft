# JetSetCraft research catalogue

**[Open the visual browser](https://herbertofury.github.io/JetSetCraft/research.html)** · **[Read the wiki research hub](https://github.com/Herbertofury/JetSetCraft/wiki/Inspiration-and-Research)**

## What the collection contains

The **November 1, 2026 expansion adds 136 records** to the first expansion's 196 and preserves the **173-entry earlier atlas**. The browser now contains **505 entries**, including **332 curated records** across two dated research passes. Counts describe research entries rather than unique organizations, projects or artists; a creator can appear in different contexts.

| Category | Added October 31 | Added November 1 | Curated total |
|---|---:|---:|---:|
| Games and movement/world design | 40 | 36 | 76 |
| Specific music tracks and recordings | 100 | 60 | 160 |
| Art, culture and presentation | 24 | 16 | 40 |
| Engineering, audio, performance and accessibility | 22 | 16 | 38 |
| Animation, choreography and procedural-design papers | 10 | 8 | 18 |
| **Total curated** | **196** | **136** | **332** |

The second pass explores creator-built routes, replays, social play, public-space design, authored street culture, accessible creative tools and contact-aware animation. It adds eight design briefs and a new 12-track listening route alongside the original 16-track route. The new music selection broadens turntablism, Japanese jazz and hip-hop, electro, garage, jungle and soundtrack composition. Its 16-cover wiki gallery is drawn from 41 verified cover images; ten additional official video destinations accompany the music. All earlier recommendations and detailed studies remain available.

Start with **[the latest additions](https://herbertofury.github.io/JetSetCraft/research.html?batch=2026-11-01)** or follow **[the second listening route](https://herbertofury.github.io/JetSetCraft/research.html?path=city-after-hours)**. The four wiki lanes explain the source observations, JetSetCraft applications and prototype questions in full.

## Source files and generated browser

- **[`catalogue.json`](catalogue.json)** is the machine-readable source, including the full entry set, research-pass history, ordered listening paths, cover galleries and official video destinations.
- **[`browser-template.html`](browser-template.html)** contains the accessible, dependency-free interface.
- **[`../research.html`](../research.html)** is the generated, self-contained browser served by the existing GitHub Pages site.
- **[`../../tools/build_research_browser.py`](../../tools/build_research_browser.py)** validates metadata, HTTPS source URLs, image credits, duplicate curated titles, counts, dated passes, listening-path references and wiki targets, then produces the browser.
- **[`../../tools/test_research_browser.js`](../../tools/test_research_browser.js)** exercises the actual shipped inline JavaScript with a small DOM fixture. It checks filtering, ordered paths, URL restoration, shortlist behavior, pagination focus and CSV export. Visual layout and media loading are checked separately in a real browser.
- **`wiki/`** remains the canonical source mirror for the published GitHub wiki. The four established research lanes carry the complete narrative research.

Regenerate and verify from the repository root:

```sh
python tools/build_research_browser.py
python tools/build_research_browser.py --check
node tools/test_research_browser.js
python tools/validate_wiki.py
git diff --check
```

Use Python 3.10+ and Node.js 20+ for these maintenance commands; no package installation is required for the browser generator or its logic checks. Both checks run in the existing verification workflow. The delivered browser needs no third-party JavaScript: it embeds its catalogue, renders through DOM text APIs, loads images lazily and opens sources on explicit clicks.

Search, category, priority, research pass, source-media format, sorting, historical entries and saved-only views can be combined. Listening paths preserve their editorial sequence under **Study / listening order**. The latest-additions button opens the newest declared pass. Query state is preserved in the URL, including `batch`, `media` and `path`. The optional shortlist uses browser storage and remains usable for the session if storage is unavailable. CSV export contains every matching record, includes provenance and video columns, and guards spreadsheet formula-like text.

The **direct video** filter recognizes a declared `video_url` or an actual video destination in `media_url`. A general store page, YouTube channel or video catalogue is not labeled as a direct video. Source pages may still contain useful embedded trailers even when they do not qualify for this filter.

## Record contract

Schema version 2 gives every entry a stable `id`, `title`, `category`, `source_url`, `lesson`, `application`, `checked`, a `batch`, and an editorial `priority` (`P1`, `P2`, `P3`, or `Legacy`). Optional metadata includes artist, tags, shelf, media/video/thumbnail URLs, prototype question, additional source, wiki lane and evidence note. A thumbnail requires an image credit. Music records also preserve exact listening URLs, public artist/label entry points and audition/contact status.

`passes` declares each dated batch and its exact count; `latest_batch` selects the newest collection. `listening_paths` contains stable IDs and ordered lists of Music entry IDs. Existing `music_priority_ids`, cover galleries and video destinations are retained for compatibility. Earlier entries keep their original checked dates and the `legacy` batch. Adding a new pass never rebrands an earlier reference as newly verified.

Use **P1** for the best starting studies, **P2** for strong follow-ups and **P3** for exploratory work. These are fit judgments, not benchmark scores. The earlier atlas retains a separate `legacy` flag and checked-status text; it is never silently promoted to a new verification. Use exact image/video URLs observed from a source, not invented media IDs.

A source-backed observation describes the referenced work. `application` and `prototype` describe a proposed JetSetCraft use or refinement of an already approved goal. Do not turn a trailer, research paper or successful documentation check into an implemented-feature or performance claim.

## Canonical wiki lanes

1. [Games, Movement & Rides](https://github.com/Herbertofury/JetSetCraft/wiki/Research-Street-Movement-and-Rides)
2. [Music, Graffiti & Audio](https://github.com/Herbertofury/JetSetCraft/wiki/Research-Graffiti-Music-and-Audio)
3. [Art, Animation & Creators](https://github.com/Herbertofury/JetSetCraft/wiki/Research-Animation-Models-and-Creators)
4. [Engineering & Performance](https://github.com/Herbertofury/JetSetCraft/wiki/Research-Frameworks-Java-and-Bedrock)

Add new evidence to the existing lane and source record. Preserve task IDs, approved crew identities, selected songs and current gameplay contracts. The new music list is intentionally broad and is not filtered to royalty-free candidates; it supports the owner's later listening and outreach. No audio files are bundled by this contribution. Linked cover art and game images remain hosted by their source publishers/artists.

## Reconciled live-wiki history

The inspected repository baseline was `91939d9e6d53d46614d994853513bd295839d553`. The live wiki was at `4d604d55fc9ca87fe3516aae384055090d709c0e` and contained newer content absent from the repository source mirror. Before this research expansion, **19 missing pages and seven newer page versions** were reconciled into `wiki/`, preserving all **51** live pages. This matters because the existing publishing workflow synchronizes the repository tree with deletion enabled.

Before a future wiki publication, compare the current live-wiki head and content with the repository mirror and preserve intervening edits. Do not run the source-to-wiki sync over a stale source tree. The existing workflow records the resulting publication receipt after a successful sync.

The second pass started from repository commit `bfd17322019d9e1eb9e458c493c8a07f3e5d696b` and live-wiki commit `308b9dc26dfbb5bc21e5bad8bff0def446dbafce`. First-pass provenance remains recorded above and in the catalogue; per-pass provenance records the later baseline separately.

## Verification scope

The research validates source identities/metadata and documented current status. The generated browser must match its source. Logic checks cover filters, direct-video classification, ordered listening paths, preserved earlier entries, URL restoration, shortlist persistence/fallback, pagination focus, empty states and full-result CSV export. Media audits use actual image responses/decoding and official video metadata; they do not establish that a full track or video was auditioned. Live publication is verified separately through the repository, all 51 wiki Markdown files and GitHub Pages.

No new mod gameplay, native client/audio audition, or performance improvement is claimed by this documentation contribution. The existing runtime/build gates remain necessary for subsequent gameplay implementation.
