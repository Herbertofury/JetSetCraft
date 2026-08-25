# JetSetCraft 0.3.0 Reconciliation Record

This record explains how the public 0.3.0 source was selected and why the repository's many historical recovery branches were not blindly merged at release time.

## Canonical source

- Desktop source: `C:\Users\Owner\Desktop\JetSetCraft`
- Public repository: `https://github.com/Herbertofury/JetSetCraft`
- Release line: `release/final-reconciliation` promoted to `main`
- Minecraft / loader: Minecraft 1.20.1, Forge 47.4.23, Java 17

The release lineage starts from the most complete data-driven gang/head compatibility branch and semantically carries forward the valuable work recovered across the repository: the original alpha payload, exposed-edge collision solver, vanilla-world physics, acceptance lab, persistent loadout, hoverboard, dedicated hoverboard mesh, Style Flow, combat animation ownership, Create 6.0.8 geometry bridge, input trust/watchdog/cadence work, Street Gear, Boombox lifecycle, and data-driven gang/head mappings.

## Git history audit

Every remote branch and unique commit subject was enumerated after a fresh `git fetch --prune --tags`. Divergent branches were compared by feature/content, not timestamps. Their valuable production behavior is present in the canonical tree and guarded by current validators or runtime tests.

Recovery scaffolds were intentionally not merged as release content when they contained only split archive chunks, source-export payloads, one-shot promotion workflows, duplicate CI trigger commits, Drive transport probes, or superseded test bootstraps. Keeping those commits on their original remote branches preserves history without polluting the production tree or reviving stale source snapshots.

At the audit point, the public repository had no release tags or GitHub Releases, so `v0.3.0` is a new tag rather than an overwrite. The previously published `0.2.0-alpha.1` identity remains documented in the changelog.

## Desktop and Drive-only source assets

The complete canonical project retains owner-supplied and research-only inputs under `source_assets/`. The public repository and production JAR intentionally exclude full-resolution brand masters, approved source archives, paid Marketplace references, and research-only model archives. Generated/public derivatives and runtime assets are reproducible and covered by `docs/ASSET_PROVENANCE.md`.

## Release safeguards

- deterministic generators and clean-tree gates;
- 93-source Java syntax parsing;
- 8 registered-item and 72 animation-clip asset validation;
- seven dense authored OBJ mesh checks plus animation seam checks;
- packet, input, persistence, gameplay, compatibility, and wiki contract validation;
- nine real Forge GameTests, including neutral stop, vanilla swimming authority, and bounded surface paint;
- unattended real-client ride/HUD/graffiti screenshots;
- exact release JAR launch in a clean standalone Forge server;
- Minecraft Mod Vault TestGrid Java status probe against that live server;
- SHA-256 publication and GitHub Actions verification before release tagging.

This document is the durable explanation of the final branch choice. Historical branches remain available for forensic reference; `main` is the only production source of truth after 0.3.0 is published.
