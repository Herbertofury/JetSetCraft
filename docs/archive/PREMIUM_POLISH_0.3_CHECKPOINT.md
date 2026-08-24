# JetSetCraft 0.3 Premium Polish Checkpoint

Date: 2026-08-22 (America/Denver)  
Target: Minecraft 1.20.1, Forge 47.4.23, Java 17  
Implementation commit: `035f2b99d589c47d11af00a8138140e0d5dd4b4b`

## What changed

- Reauthored all core ride, boost, grind, wall-ride, manual, powerslide, and trick clips around anticipation, apex, recovery, and seam-safe loop poses while keeping ordinary ride animation lower-body-only for combat-mod composition.
- Retained all seven authored OBJ meshes and fixed ellipsoid pole topology so generated boots and curved components do not emit zero-area cap faces.
- Added proper Forge OBJ item parents and particle textures for every ride item and spray can.
- Hardened player input, serialized capability data, client mirrors, camera input, vectors, momentum, and score accumulation against unknown bits, stale state, overflow, NaN, and infinity.
- Hardened same-entity Street Gear persistence: one physical item, source-owned mob identity, data-driven anatomy rigs, safe conversion/death handling, corrupt-state repair, and no permanent mutation of another mod's equipment drop rules.
- Corrected mob skate orientation and changed body-contact, aerial, and aquatic layouts to a paired under-body carriage instead of fake humanoid feet.
- Added a sixth real Forge GameTest for vanilla-zombie Street Gear identity, physical-item persistence, NBT save/load, removal, and spider/slime anatomy routing.
- Added offline premium validators for OBJ topology/materials/bounds, animation UUIDs/timing/seams/radian safety, optional-mod classloading boundaries, Java syntax, and release contracts.

## Local verification completed

The following passed together from a clean regenerated working tree:

- 7 registered item assets and 68 animation clips
- 7 authored OBJ meshes with geometry/material/texture checks
- 68 seam-checked deterministic animations
- gameplay-contract validation
- optional-mod boundary validation
- Java syntax parse for 69 source files
- 21-page wiki validation
- Python tool compilation
- `git diff --check`

## Runtime verification boundary

This environment did not have a Gradle distribution, Forge dependency cache, network access to Gradle/Maven, or a GitHub write action. Therefore the 0.3 source checkpoint has **not** been represented as a compiled/reobfuscated release. The repository workflow now requires all of the following before a 0.3 JAR is called verified:

1. ForgeGradle build and reobfuscation.
2. Six required Forge GameTests with all six pass markers.
3. Dedicated-server ready-state smoke.
4. Fresh artifact hashes.
5. Real-client visual and modpack acceptance.

## Installable presentation/safety preview

A separate JAR, `jetsetcraft-1.20.1-0.2.1-alpha.1-premium-preview.jar`, was produced from the previously verified 0.2.0 binary. It includes only the compatible premium presentation resources and a narrowly verified `JetSetData` bytecode hardening patch. It does **not** claim to contain the uncompiled 0.3 Street Gear implementation.

Preview SHA-256: `c3dc6de19b34e06273eec3e9630e57b4c93700b086dc184d47e9f2c75531c347`  
Preview bytes: `3605594`
