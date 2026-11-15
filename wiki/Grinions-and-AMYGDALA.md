# Grinions and AMYGDALA

Owner direction recorded November 15, 2026. **Grinions** are a higher-ranked Honkistas sub-crew, eligible for rare appearances within Honkistas events. **AMYGDALA** is a separate gang with FENNAH character and music references, not a Honkistas alias or a Grinions spawn variant.

## Implementation status

The three stable identities `jetsetcraft:honkistas`, `jetsetcraft:grinions` and `jetsetcraft:amygdala` now have resource definitions compatible with the existing gang reload listener. They deliberately map no vanilla/provider mobs and are not Boombox-eligible. Neutral disposition is safe dormant metadata, not a final combat design. Existing mob identities, source classes, rewards and music selections remain unchanged. The generic original-score reference is a fallback, not a Skashi/FENNAH recording.

`HonkistasVariantPolicy` implements a bounded selection boundary for the future authoritative event director. Selection requires an admitted Honkistas event, server authority, no prior recorded decision, neither parent nor variant defeated, and ready assets. Other gangs, including AMYGDALA, pass through unchanged. Invalid probability/roll values fail closed. Probability must be explicitly supplied below 50%; no production rate is set. The 5% exercised in tests is a synthetic boundary example, **not an owner-approved balance value**.

**Not live gameplay:** main currently has no Honkistas nested-event director. This policy is not wired to a spawn path. It neither provides persistence nor guarantees once-only spawning. The future caller must persist the decision before spawning, retain it on reload, and reject duplicate/out-of-order admission. Do not attach it to ordinary Boombox starts: the established Honkistas trigger is gang event completion → rival challenge victory → possible Honkistas event. Grinions selection occurs only inside that admitted event and cannot recursively create another event.

## Selected media and provenance

| Reference | Creator | Source date | Reuse state |
|---|---|---|---|
| [Nightmare March](https://www.youtube.com/watch?v=Zows6t4BV6Q) | DatSkashi; creator credits animation/music/SFX, Warai and Grin voices; Smiley Cindy voice MisuKiyuDub | 2019-12-26 | Owner-attested permission with attribution; source files not imported |
| [Purple Grinion](https://www.newgrounds.com/audio/listen/1402398) | Skashi95 | 2025-02-18 | Public page requires contact; owner now reports permission with attribution |
| [Concertina Ballerina (Official Video)](https://www.youtube.com/watch?v=q7d_MQDPJsQ) | FENNAH; music credited Rob & Alan Fennah | 2026-08-27 | Owner-attested permission with attribution; source files not imported |
| [Bernadette – Animated Short](https://www.youtube.com/watch?v=RVrX7xZkFqI) | FENNAH | 2023-12-23 | Owner-attested permission with attribution; source files not imported |

Metadata checked November 15, 2026. Player-ready metadata does not establish actual playback. The Purple Grinion audio references an adult animation; the linked audio and any character design must be reviewed separately for suitability. No animation, audio, character model, artwork or extracted video is bundled by this change. The owner subsequently stated both Skashi and FENNAH granted character and music use permission provided they are credited. This is an owner-attested permission record, not independently reviewed license correspondence.

[Skashi soundtrack index](https://skashi95.newgrounds.com/news/post/1543005) and [creator lore](https://skashi95.newgrounds.com/news/post/1530400) are provenance references. The owner-attested grant covers the requested character and music uses with credit. Preserve Skashi/DatSkashi and FENNAH credits and recording-specific contributors above. Record the source file, hash and applicable credit when each authorized asset is supplied or obtained through a permitted download. Do not bypass platform restrictions or infer authorization for unrelated works. No source asset files are currently imported; placeholders are not official creator material.

## Remaining acceptance

- Implement the parent Honkistas nested event director, durable admission and decline/exit/recovery first.
- Persist Grinions higher-rank relationship independently of reputation aliases; preserve defeat and stripped-power rules.
- Provide original or authorized actors, readable higher-tier choreography and music handoff, without stealing native mob identity.
- Define AMYGDALA character roster, encounter and rank independently; no unrequested rare-spawn relationship.
- Native-test rewards, repeated triggers, save/reload, death/disconnect, dimension transitions, multiplayer and missing-media fallback.
- Run full build and GameTests before native acceptance. The dependency-free policy checks are not a native spawn/playback/performance test.

Run `python tools/test_honkistas_policy.py` for real Java policy execution (Java 17 target) and safe resource checks. See [music-driven events](Music‐Driven-Dances-and-Mob‐Themed-Bosses) for the preserved parent requirements.
