# Player Chapters, Posse & Mob Archetypes

High reputation with a gang should eventually let the player bring that gang **home** without turning JetSetCraft into a settlement/worldgen mod.

The prestige reward is a gang-specific **Chapter Boombox**: a themed version of the Boombox bound to the gang's stable `gang_id`. Placing it at a player-selected location establishes a small player-founded chapter there.

Examples should be playful and species-aware. A Mooshroom gang can award a mushroom-covered **Shroom Box**; aquatic gangs can use bubbles/water motifs; Bone Drones can use skeletal speaker cages. These are data-driven gang models/skins over one compatible Chapter Boombox implementation rather than separate invasive systems.

## Found Your Own Chapter

At a high configurable reputation tier (default target: Member/Veteran-level trust), the player unlocks that gang's Chapter Boombox.

Placing it:

- creates a stable player-founded `site_id` linked to the gang's `gang_id`;
- makes the placed Boombox the chapter's home anchor;
- creates/invites roughly **five permanent residents by default** (configurable/server-budgeted);
- uses original vanilla/mod-owned entity types rather than JetSetCraft replacements;
- equips real JetSetCraft Street Gear so normal persistent gangification rules apply;
- never creates a worldgen structure, reserves terrain, force-loads chunks, or replaces source/player blocks.

The Chapter Boombox can be intentionally picked up and moved. Its `site_id`, roster, customizations, gang identity, names, and posse assignments survive the move. Re-placing the same chapter anchor relocates home without duplicating residents.

## Stable crew roster, temporary physical bodies

Each permanent member gets a JetSetCraft `crew_member_id`. That is the character identity across deaths and provider changes; a runtime entity UUID is only the current physical body.

A roster entry can persist:

- `crew_member_id`;
- `gang_id`;
- stable semantic `mob_archetype_id`;
- current/preferred provider `entity_type_id`;
- player-chosen display name;
- role/personality seed;
- Street Gear/cosmetic loadout;
- home `site_id`;
- home/following/participating/recovering/missing-provider state;
- posse assignment;
- bounded memorable interaction flags;
- optional signature trick/favorite move.

These residents are not disposable event actors. The player can name them, interact with them, and—after sufficient trust—invite them into their **posse**.

## Posse followers

Eligible chapter residents can become followers and travel/skate with the player, join JetSetCraft activities, pose/dance, and return home when dismissed.

Performance remains bounded:

- no follower chunk tickets;
- no global per-tick missing-member scans;
- normal loaded navigation first;
- bounded catch-up/rejoin only when a member is hopelessly separated;
- all residents can be follower-eligible while servers may configure an active-posse budget;
- residents left at home use the cheap shared Hangout Brain.

The player still has one canonical reputation relationship with the gang's `gang_id`; naming individual members does not create five global reputation meters.

## Chapter Boombox is their bed/spawn point

The placed Chapter Boombox is the permanent crew's **home/spawn anchor**, conceptually like a player's bed while remaining entirely JetSetCraft-owned.

If a chapter resident or posse member is killed or otherwise truly dies:

1. its current source entity death lifecycle is allowed to complete;
2. its `crew_member_id` roster entry becomes `recovering`;
3. after a configurable recovery delay it may return/re-form near its bound Chapter Boombox;
4. recovery happens **only when the home chunk is naturally loaded**—JetSetCraft never force-loads the base just to respawn a member;
5. the recovered member keeps its name, role, gang, posse assignment, JetSetCraft memories, Street Gear and cosmetics;
6. a fresh runtime UUID is allowed/expected after true death; `crew_member_id` is the stable character identity.

JetSetCraft minigame defeats do not have to kill the mob. A non-lethal downed/resting result can simply send the member home. Vanilla/mod combat death uses the recovery system.

If the Chapter Boombox is picked up while a member is recovering, the member remains safely pending until that same chapter anchor is placed again.

### No death-farm or duplication exploit

Home recovery must not duplicate equipment, inventories, or turn permanent residents into infinite source-mob farms.

- Bound JetSetCraft Street Gear/cosmetics belong to the roster and return with the member rather than duplicating as drops.
- Any death/loot adjustments apply only to JetSetCraft-owned chapter residents through narrow Forge events/accounting; never modify global loot tables or unrelated mobs.
- Repeatable resurrection must have sensible loot/cooldown rules so repeatedly killing one chapter member cannot farm vanilla/modded drops forever.

## Super-intelligent Mob Archetype Resolver

JetSetCraft must recognize **creature concepts**, not depend on one provider mod forever.

This matters for mobs such as **Mooblooms**. A Moobloom might come from one backport today, another mod tomorrow, a replacement fork, a future implementation, or multiple installed providers at once.

Use three separate identities:

- `mob_archetype_id` — semantic creature identity, e.g. `jetsetcraft:moobloom`;
- `entity_type_id` — the installed mod's current physical provider;
- `gang_id` — the gang the archetype maps to.

Resolution should be confidence-based and cached after registry load:

1. explicit JetSetCraft/Forge archetype tags and server overrides;
2. curated version-gated provider aliases/adapters;
3. normalized registry path/translation-key/name synonyms (`moobloom`, `moo_bloom`, etc.);
4. safe family/trait evidence from public server-side metadata/tags/capabilities;
5. modpack/user aliases for differently named equivalents;
6. unresolved/dormant if confidence is insufficient—never guess and corrupt a save.

Never identify an archetype by one Java class, one namespace, a mod filename, renderer internals, or texture scanning. Optional providers remain optional and must never be hard-classloaded when absent.

### Multiple providers

If two mods both provide a Moobloom:

- explicit server priority wins first;
- otherwise use curated confidence/compatibility scoring;
- expose the chosen provider in the Mob Atlas/debug UI;
- never randomly swap an already-living member's entity type.

### Provider changed or removed

Persist both `mob_archetype_id` and the last provider.

If the provider disappears:

- the JetSetCraft roster/hangout identity survives because it is keyed to the archetype;
- a missing member enters `missing-provider`/dormant state instead of crashing or being deleted;
- on a later safe spawn/recovery, another installed provider satisfying the same archetype may be selected;
- JetSetCraft preserves its own identity data (name, `crew_member_id`, gang, role, gear, memories) but never pretends it can recreate third-party provider-specific NBT from an absent mod.

This architecture should generalize beyond Mooblooms to old-vote mobs, backports, variants, butterflies, sharks, fireflies, copper golems, rascal-style mobs, and any future creature concept implemented by competing mods.

## Acceptance gates

1. High reputation unlocks the correct gang-specific Chapter Boombox via stable `gang_id`.
2. Placing it at the player's chosen base creates one bounded chapter with about five permanent roster members and no worldgen/chunk-ticket side effects.
3. Residents can be named and recruited/dismissed as posse followers.
4. Moving the Chapter Boombox preserves chapter/roster identity without duplication.
5. Killing a permanent member sends the roster entry into recovery; it returns at the home Boombox after the delay when the chunk is naturally loaded.
6. Recovery preserves JetSetCraft identity but does not duplicate Street Gear/inventory or create an infinite mob-loot farm.
7. `jetsetcraft:moobloom` resolves correctly from provider A, survives provider removal at the save-data level, and can rebind on later recovery/spawn to provider B.
8. Two Moobloom providers resolve deterministically and never randomly swap a living member.
9. No safe provider means dormant/recoverable state plus a useful diagnostic, not a crash or deletion.

See also [[Gang Hangouts, Soft Territory & Reputation|Hangouts-Territory-and-Reputation]], [[Gang Wars, Boombox & Mob Atlas|Gang-Wars-Boombox-and-Mob-Atlas]], [[Standalone Compatibility Covenant|Standalone-Compatibility-Covenant]], and [[Compatibility]].
