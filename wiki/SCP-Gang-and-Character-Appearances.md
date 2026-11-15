# SCP gang and character appearances

The owner requested an **SCP** gang for MalO/SCP-1471-A, SCP-682, SCP-049, SCP-173 and SCP-939, with female and femboy interpretations of each. These are noncanonical design variants. Pomni is requested for Honkistas, separately from SCP and AMYGDALA.

## Implemented candidate scope

The existing Crew Charter **mission roster** now offers these five SCP identities with three profile choices each: base, female interpretation and femboy interpretation. Pomni is another named profile. This reuses existing server-authorized recruitment, costs, capacity, missions, persistence and display. Selecting any variant removes all alternate offers for that same character from that player's crew; its canonical identity survives renaming/reload. Different players may recruit their own profile. It is not a world-global NPC claim.

A dormant `jetsetcraft:scp` gang definition maps no Minecraft/provider entities. It does not transform native mobs. **No visible character, rare event, imported model/image/music or live animation is provided.** The roster is implemented as a candidate; appearance work remains planned. The parent Honkistas event director is still required for rare guests.

## Sources and reuse boundaries

- [SCP-1471](https://scp-wiki.wikidot.com/scp-1471): LurkD.
- [SCP-682](https://scp-wiki.wikidot.com/scp-682): Dr Gears and Epic Phail Spy.
- [SCP-049](https://scp-wiki.wikidot.com/scp-049): Gabriel Jade; rewritten by djkaktus and Gabriel Jade.
- [SCP-173](https://scp-wiki.wikidot.com/scp-173): Moto42. The former Izumi Kato sculpture image is not covered by the general SCP license; no reproduction is included.
- [SCP-939](https://scp-wiki.wikidot.com/scp-939): Adam Smascher and EchoFourDelta.
- [Pomni / The Amazing Digital Circus](https://www.glitchprod.com/digital-circus): Gooseworx, produced by GLITCH. Asset reuse rights remain unverified.

SCP article-derived adaptations must retain attribution and [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/) obligations. This does not establish permission for fan artwork, model files, recordings or the former SCP-173 sculpture likeness. No such asset is included. User-attested FENNAH/Skashi permissions do not extend to these unrelated creators.

The SCP-173 design request specifies **ordinary human feet**, with heel, sole, ankle and toes, instead of stump-like feet. Supplied references were inspected privately; explicit details are excluded from the game brief and images are not published. Source images are not an authorization to copy them.

[Structured appearance/provenance inventory](../docs/character-appearances.json) maps confirmed Memories IDs and records Roku, God of Nothing and Shion as user-named, unverified model references. Neither model site challenge was bypassed. No title-derived franchise, license, topology, rig quality or game compatibility is inferred. Favorites do not trigger automatic asset copying.

## Tests and outstanding acceptance

`python tools/test_named_recruits.py` executes the real Java catalog checks for seventeen profiles, stable IDs, canonical variant identity, affiliation and unknown inputs. A new Forge GameTest exercises real SCP variant recruitment, persisted gang and canonical identity, and removal of alternate offers. Native test results must be reported separately from static checks and local catalog tests.

Still required: full final-commit CI, ledger UI, save/reload in ordinary play, authored/authorized character assets, model import safety review, animation/follower/event logic, playable encounter and accessibility/performance tests. A green mission-roster test is not evidence of rendered characters or playable guest events.
