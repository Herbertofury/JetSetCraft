# JetSetCraft 0.3.0 Release Evidence

## Candidate identity

- Artifact: `jetsetcraft-1.20.1-0.3.0.jar`
- Size: 5,098,964 bytes
- SHA-256: `9639199cab05ef60fc0207fc107c556c49744ee7a275ff639259ea875618d671`
- Runtime: Eclipse Temurin 17.0.20.1, Minecraft 1.20.1, Forge 47.4.23
- Client animation dependency: PlayerAnimator 1.0.2-rc1+1.20

## Verification completed on 2026-08-24

1. Deterministic model, animation, graffiti, and brand generation completed.
2. Asset, gameplay-contract, Java syntax, premium-polish, and 25-page wiki validators passed.
3. `gradlew.bat clean build --no-daemon` completed from a clean build directory.
4. All nine real Forge GameTests passed. The explicit markers cover hoverboard, neutral stop/directional restart/one-block parkour vault/swimming, scooter, dance Flow, combat sovereignty, catalogs/input hardening, same-entity Street Gear, all sixteen craftable paint balloons plus bounded 1,000-ray splashes, and graffiti lifecycle/custom persistence.
5. The opt-in real Forge client visual audit captured the leg-baked inline skates, compact HUD, painted wall, persistent Free Paint/cyan selector state, and custom 16 × 10 painter, emitted `JETSETCRAFT_VISUAL_AUDIT_PASS`, and exited cleanly.
6. The exact SHA-256-matched candidate JAR was copied into an isolated Forge 47.4.23 server with no optional mods. It reached `Done (2.282s)!`, exposed Minecraft 1.20.1 protocol 763, and produced no JetSetCraft/FML fatal match.
7. Minecraft Mod Vault 0.13.0 TestGrid queried the running server through the Java status protocol and received the `JetSetCraft 0.3.0 Verification` description with zero players.
8. The server accepted a console `stop`, saved all three dimensions, and exited cleanly.

## Visual evidence

- `art/screenshots/v0.3.0-ride-hud.png`
- `art/screenshots/v0.3.0-graffiti-selector.png`
- `art/screenshots/v0.3.0-custom-graffiti.png`

GitHub Actions repeats deterministic generation, validators, clean build, nine GameTests, standalone dedicated-server startup, and checksum sealing on the public `main` commit. The release is published only after that workflow is green.
