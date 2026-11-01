# JetSetCraft research catalogue

**[Open the visual browser](https://herbertofury.github.io/JetSetCraft/research.html)** · **[Read the wiki research hub](https://github.com/Herbertofury/JetSetCraft/wiki/Inspiration-and-Research)**

## What this contribution contains

The research pass checked **October 31, 2026** adds **196 records** and preserves **173 earlier records**, for **369 catalogue entries**. These counts describe entries rather than unique organizations, projects or artists; a creator can appear in different research contexts.

| Category | New entries |
|---|---:|
| Games and movement/world design | 40 |
| Specific music tracks and recordings | 100 |
| Art, culture and presentation | 24 |
| Engineering, audio, performance and accessibility | 22 |
| Choreography and character-motion papers | 10 |

The music collection includes 66 distinct artist/collaboration credits, a 16-track starting sequence, seven listening shelves, 12 cover cards and 25 new crew-specific audition candidates. The detailed wiki includes six route briefs, ten movement refinements, ten source-aware engineering opportunities and a proposed choreography evaluation workflow.

## Source files and generated browser

- **[`catalogue.json`](catalogue.json)** is the machine-readable source, including the full entry set, music starting sequence, cover gallery and official video destinations.
- **[`browser-template.html`](browser-template.html)** contains the accessible, dependency-free interface.
- **[`../research.html`](../research.html)** is the generated, self-contained browser served by the existing GitHub Pages site.
- **[`../../tools/build_research_browser.py`](../../tools/build_research_browser.py)** validates required metadata, source URLs, priorities, types and wiki targets, then produces the browser.
- **`wiki/`** remains the canonical source mirror for the published GitHub wiki. The four established research lanes carry the complete narrative research.

Regenerate and verify from the repository root:

```sh
python tools/build_research_browser.py
python tools/build_research_browser.py --check
python tools/validate_wiki.py
git diff --check
```

No package installation or third-party JavaScript is required. The browser embeds its catalogue, renders through DOM text APIs, loads images lazily and opens sources on explicit clicks. Search, category/priority filters, sorting, inclusion of historical entries and saved-only views can be combined. Query state is preserved in the page URL. The optional shortlist uses the visitor's browser storage; CSV export contains all matching records and guards spreadsheet formula-like text.

## Record contract

Every entry has a stable `id`, `title`, `category`, `source_url`, `lesson`, `application`, `checked`, and an editorial `priority` (`P1`, `P2`, `P3`, or `Legacy`). Optional metadata includes artist, tags, media and thumbnail URLs, image credit, prototype question, additional source, wiki lane and evidence note. Music records also preserve exact listening URLs, public artist/label entry points and audition/contact status.

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

## Verification scope

The research validates source identities/metadata and documented current status. The interface has functional checks for filtering, URL restoration, shortlist behavior, pagination focus, source links and CSV export; the generated file must match its source. The 10 game gallery thumbnails and 12 music cover cards were verified with actual HTTP responses and image validation. Live publishing is verified separately through the repository/wiki and Pages deployment.

No new mod gameplay, native client/audio audition, or performance improvement is claimed by this documentation contribution. The existing runtime/build gates remain necessary for subsequent gameplay implementation.
