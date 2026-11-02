# Liberty Grind — Helldivers loadout integration

Liberty Grind uses the actual TaCZ Helldivers weapons through an optional adapter. JetSetCraft still loads without TaCZ. Deployment requires the supported weapons and their magazines to be present in TaCZ's **live server content index**; an installed mod JAR alone is insufficient. The war map explains missing content before the player leaves their world.

## Install the matched release

The current integration target, checked on **2026-11-02**, is Minecraft **1.20.1 / Forge**:

| Component | Exact release | Official source |
| --- | --- | --- |
| Timeless and Classics Zero | `tacz-1.20.1-1.1.8-hotfix2.jar`, CurseForge project `1028108`, file `9037989`, 2026-10-02 | [TaCZ release](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero/files/9037989) |
| Helldivers weapons | **[TaCZ]Helldivers:Escalation of freedom — [eof]Helldivers:Liberty Day**, by zeta1918 with Koei's merged pack; project `1091118`, file `9341504`, 2026-10-27 | [Merged pack release](https://www.curseforge.com/minecraft/customization/tacz-helldivers-escalation-of-freedom/files/9341504) |

Install the TaCZ JAR in `mods/` and the **unextracted** `[eof]helldivers_gun_pack.zip` in the instance's **`tacz/`** directory. Put the matching releases in the server instance and every participating client. Restart, or use TaCZ's documented `/tacz reload` or `/reload` commands after changing the pack. The older `config/tacz/custom` instructions apply to old TaCZ releases, not this target. [Official installation instructions](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero).

The October 27 merged release includes Koei's original Helldivers 2 weapons. **Remove the old standalone Helldivers 2 ZIP when installing the merged pack.** The pack author's release notes identify simultaneous installation as a source of missing textures and other bugs. Do not install the October 3 release marked by its author as defective. [Creator's current description and release notes](https://www.curseforge.com/minecraft/customization/tacz-helldivers-escalation-of-freedom).

TaCZ's Forge build lists no mandatory companion mod. JetSetCraft already uses PlayerAnimator. Gunsmith Lib enables additional features in the larger merged pack, including the revised AC-8's fragmentation mode; the initial Liberator/Peacemaker loadout does not call Gunsmith Lib. Extra weapons and their optional extensions are not a promise of support by this adapter. [TaCZ requirements](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero), [pack release](https://www.curseforge.com/minecraft/customization/tacz-helldivers-escalation-of-freedom/files/9341504).

**Test-log note:** the pinned full pack includes optional Create crafting recipes under `zeta:create_ammo_recipe`. A test instance without Create reports unknown recipe types such as `create:compacting` while reloading those recipes. The initial mission loans and native reload tests use the loaded gun/ammo indices and do not depend on those crafting recipes. Keep these upstream diagnostics visible and distinguish them from the required GameTest results; Create is not a prerequisite for the supported loan loadout.

## Verified weapon identities and reload behavior

These IDs were read from the official release ZIP, not inferred from display names. The ZIP contains `data/helldiver2/index/guns/` and `data/zeta/index/ammo/` entries and corresponding gun data.

| Loan | TaCZ resource ID | Magazine resource ID | Loaded capacity | Reserve item stack limit |
| --- | --- | --- | ---: | ---: |
| AR-23 Liberator | `helldiver2:ar23_liberator` | `zeta:ar_ammo` | 45 rounds | 20 |
| P-2 Peacemaker | `helldiver2:p2_peacemaker` | `zeta:p113_ammo` | 15 rounds | 8 |

Both definitions use `closed_bolt` and `reload.type: fuel`. In TaCZ's fuel reload implementation, **one reserve ammo item fills a magazine**. The adapter issues six reserve magazine items for each gun, rather than treating each item as one bullet. Each starting gun has one chambered round plus capacity minus one in its magazine. Resupply tops carried loan magazines up to six and leaves chamber/magazine state alone, so players still use TaCZ's reload controls. [TaCZ reload implementation](https://github.com/MCModderAnchor/TACZ/blob/1.20.1/src/main/java/com/tacz/guns/item/ModernKineticGunItem.java).

The original standalone pack uses the `helldiver2` namespace too, so detecting a namespace is not enough. The adapter checks both exact guns, their expected merged-pack ammo IDs, loaded ammo indices, bounded magazine capacity, closed-bolt operation and fuel reload semantics. Missing or incompatible data blocks deployment with an actionable explanation.

## Integration contract

`compat/LibertyTaczCompat.java` exposes:

- `available()` and `status()` for admission and the war-map message.
- `provision(ServerPlayer)` for a fully staged loadout in inventory slots **0, 1, 9 and 10**. Slot 2 is reserved for the mission spray can. Provisioning refuses occupied target slots.
- `resupply(ServerPlayer)` to top up owned reserve magazines, without manufacturing another pair of guns or overwriting other inventory items.
- `markLoan`, `isLoan(stack)` and `isLoan(stack, owner)` for the manager's inventory and dropped-item cleanup.

Loan NBT uses `jetsetcraft_liberty_loan` and `jetsetcraft_liberty_loan_owner`. The mission manager owns active-session validation, resupply cooldowns, inventory escrow, prevention of item export, and cleanup on extraction, failure, disconnect and recovery. An owner marker does not replace session ownership or a saved return snapshot.

The adapter reflects only public TaCZ methods: `TimelessAPI.getCommonGunIndex/getCommonAmmoIndex`, `CommonGunIndex.getGunData`, gun-data accessors, `GunItemBuilder`, `AmmoItemBuilder`, and `IAmmo.getAmmoId`. Reflection metadata is cached; content availability is rechecked against the live indices so pack reloads cannot leave a stale successful admission result. There are no TaCZ imports, registry replacements, gameplay Mixins or mandatory TaCZ dependencies. Failed reflection disables only this loadout. [Public API](https://github.com/MCModderAnchor/TACZ/blob/1.20.1/src/main/java/com/tacz/guns/api/TimelessAPI.java), [gun builder](https://github.com/MCModderAnchor/TACZ/blob/1.20.1/src/main/java/com/tacz/guns/api/item/builder/GunItemBuilder.java), [ammo builder](https://github.com/MCModderAnchor/TACZ/blob/1.20.1/src/main/java/com/tacz/guns/api/item/builder/AmmoItemBuilder.java).

## Provenance and distribution

The merged pack's published license is **CC BY-NC-SA 4.0**. Koei's original standalone project publishes **CC BY 4.0**. TaCZ publishes its code under **GPLv3** and its assets under **CC BY-NC-ND 4.0**. Preserve the applicable credits, notices, source links and conditions when distributing those works. These are separate upstream works, not original JetSetCraft assets. [Merged-pack license](https://www.curseforge.com/minecraft/customization/tacz-helldivers-escalation-of-freedom/license), [original-pack license](https://www.curseforge.com/minecraft/customization/tacz-helldivers2-gunpack-by-koei/license), [TaCZ licensing](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero).

JetSetCraft's integration supplies code and references; it does **not** commit, republish, alter or extract the upstream gun models, textures, sounds, pack ZIP or mod JAR. Obtain the upstream files through their official project pages. Helldivers is the gameplay inspiration and source-pack theme; this integration does not claim endorsement by the game's creators or pack authors.

## Reproduction and evidence

The public release archives were downloaded for inspection outside the repository. Their exact hashes are:

| Artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| `tacz-1.20.1-1.1.8-hotfix2.jar` | 57,472,579 | `2cbb89f44333d2eb4e825d86d789ff119b272ff75184390b55bafd4f0e7f10c2` |
| `[eof]helldivers_gun_pack.zip` | 72,883,841 | `fe4e01693cf3957d971e556a1d2fcd9ae0c98cb973cf47aa6d52e157c187378a` |

The shipped JAR's class signatures confirm the reflected API methods. The pack index and data files confirm the exact IDs, reload type, capacities and stack limits above. Static inspection is evidence of those facts, **not** a claim that a complete multiplayer or visual playtest has passed.

For an opt-in provider test lane, the official Curse Maven coordinate is `curse.maven:timeless-and-classics-zero-1028108:9037989`, loaded through ForgeGradle `fg.deobf` as a runtime-only test dependency. Place the pinned pack ZIP in that test instance's `tacz/` directory. Keep the standalone/no-TaCZ lane and the TaCZ-present/no-pack lane: each must retain a working JetSetCraft world and reject Liberty deployment cleanly. The full-provider lane should additionally verify real weapon creation, reserve consumption during reload, resupply caps, projectile attribution, recovery and cleanup. [Published Maven coordinate](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero/files/9037989).

Run the prepared optional lane with:

```sh
python tools/prepare_liberty_pack.py
./gradlew runGameTestServer -PlibertyTacz=true
```

The preparation script checks the published release against the pinned size and SHA-256, writes the intact ZIP only into the ignored `run/tacz/` directory, refuses duplicate Helldivers definitions, and does not silently overwrite a mismatched archive. `--source /path/to/already-downloaded.zip` reuses a local official download with the same verification. `--verify-only` performs no transfer.

The default build remains the **TaCZ-absent** lane. For a separate **TaCZ-present/pack-absent** fixture, keep the Helldivers ZIP outside that instance's `tacz/` directory and run `./gradlew runGameTestServer -PlibertyTacz=true -PlibertyPackExpected=false`. A full-provider run expects its pack and fails if it is missing; it cannot silently pass by skipping native tests. Native tests emit explicit `JETSETCRAFT_GAMETEST_SKIP` markers only for intentionally absent optional content. The admission test still checks that missing content leaves the player's inventory unchanged.

The separate [Liberty provider workflow](../.github/workflows/liberty-provider-compat.yml) runs **full-provider** and **missing-pack** jobs in fresh workspaces on source changes or manual dispatch. Both use the exact TaCZ pin and require the complete declared GameTest suite to pass. The full-provider job also requires native loadout, reload, combat, lifecycle and extraction markers with no Liberty skips; the missing-pack job requires explicit absence diagnostics and skips.

The full-provider job then starts a normal dedicated server, requests a normal stop through the existing authenticated loopback RCON tool, and requires successful launcher exit plus **All dimensions are saved**. Its saved world seeds the disposable **LibertyGrindAudit** client world. The Xvfb client gate requires the native audit PASS marker, no FAIL marker, and all **fifteen named PNG captures** covering the activity handoff, default/minimum UI, actual pod/weapon/mission and ordinary early return. A separate artifact contains the PNGs and their dimensions, byte lengths, SHA-256 hashes, capture markers and source commit. Objective completion and ship extraction are covered by the native server tests; this client audit does not fabricate their completion. The missing-pack job runs only the shared GameTests and intentional admission checks.

Logs, crash reports and JetSetCraft's own build are retained as artifacts. The upstream pack ZIP and TaCZ JAR are not uploaded. The existing standalone and other-provider workflows remain independent. A configured CI gate is not itself a claim that the hosted run has passed.

`LibertyTaczGameTests` covers exact public gun/ammo identities, chamber and reserve state, loan ownership, atomic rejection of an occupied loadout slot, moved magazine stacks, bounded resupply, full-inventory rejection, and actual TaCZ `startReload/tickReload` consumption. Its reload fixture advances only the fake player's provider timer; the upstream implementation consumes the magazine and chambers the rounds. This does not replace a human client playtest of animations, aiming, skating feel or multiplayer latency.

`LibertyGunCombatGameTests` calls the real `IGunOperator.shoot` method in two production mission sessions. The fixture waits for native pod and director ticks, then lets the positioned targets' actual entity ticks settle their movement history before shooting. It captures actual `tacz:bullet` projectiles, controls their trajectories and invokes the provider's collision tick. Assertions cover real ammo consumption, shooter/direct-entity attribution, one owned objective defeat, rejection of a foreign mission's bullet, and removal of an in-flight bullet on return. Its target health and draw delay are controlled fixture conditions, not damage-balance or animation evidence.
