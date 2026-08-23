# Testing and Verification

JetSetCraft treats a passing Java compile as intermediate evidence, not final proof. The repository CI regenerates assets, validates contracts, builds the reobfuscated JAR, runs real Forge GameTests, and starts a real dedicated server.

## Local source checks

```text
python -m pip install -r tools/requirements.txt
python tools/generate_models.py
python tools/generate_animations.py
python tools/generate_brand.py
python tools/validate_assets.py
python tools/validate_gameplay_contract.py
python tools/validate_premium_polish.py
python tools/validate_java_syntax.py
python tools/validate_wiki.py
gradle --no-daemon build
```

The model generator is offline-safe: when the pinned optional graffiti archive cannot be fetched, it removes any partial download and produces the original JetSetCraft fallback catalog.

## Forge GameTests

The GameTest server verifies:

1. Hoverboard registry identity, persistent loadout NBT, and the production ground movement solver.
2. Scooter registry identity, persistent loadout NBT, and the universal `JetSetMovement` path.
3. Breakdance starting with no ride gear, server-owned move selection, completed-phrase scoring, and Flow.
4. Immediate dance cancellation when a combat swing takes presentation/input authority.
5. Stable one-to-one identity for all 28 dance moves, complete 24-trick catalog coverage, and hostile input/NBT sanitization.
6. Same-entity Street Gear identity, one physical item, real entity NBT save/load, clean removal, and spider/slime anatomy routing.

The player tests use uniquely identified Forge fake players and a real `ServerLevel` rather than mocked movement classes. Unique profiles prevent parallel GameTests from moving or mutating the same automation player.

## Dedicated-server smoke

CI launches Forge with `eula=true`, waits for the real ready line, and fails on lifecycle/mod-loading/fatal JetSetCraft errors. The produced JAR and logs are uploaded as workflow artifacts.

## Runtime acceptance world

Run:

- `/jetsetcraft build_vanilla_lab`
- `/jetsetcraft status`
- `/jetsetcraft set_momentum <speed>` as an operator-only diagnostic

Then exercise every ride style, dance family, surface lane, rail type, wall, hazard, transfer, combat overlay, dimension transition, death/respawn, reconnect, and multiplayer tracking path.

## Wiki verification

`tools/validate_wiki.py` ensures the complete page set exists, sidebar destinations resolve, internal wiki links are valid, required technical terms are present, and no unfinished-marker language ships. A separate workflow synchronizes the validated `wiki/` directory to the GitHub wiki after main-branch updates.

## Premium offline gates

The premium gate additionally rejects malformed or degenerate OBJ geometry, unresolved materials/textures, non-finite or visibly unclosed animation loops, hard optional-mod class imports, invasive mixin/ASM paths, stale client-cache regressions, and Java syntax failures before ForgeGradle begins. These checks are strong preflight evidence, but they never replace the real Forge build, GameTests, dedicated-server smoke, or client acceptance.
