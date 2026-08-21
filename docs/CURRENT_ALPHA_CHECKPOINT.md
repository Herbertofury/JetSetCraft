# JetSetCraft Alpha Checkpoint — a3843e0

Status: pre-compile implementation checkpoint

- Local source commit: `a3843e074047c54da5781cec2769392cd36acfce`
- Source tree: `5a7fb49d408fcfbd40014bd019ee63dc1e14ec33`
- Exact git-archive SHA-256: `bcc84f0cca2c9c471d6f67db170abec51093ff20619cb52df2e486db31009995`
- Drive checkpoint ZIP SHA-256: `d86c9bf1f504afc3a81af32ece02b1b8bb4304398f1c40f9298fbfd97d22037e`

This checkpoint contains the current Forge 1.20.1 implementation: ride states, server-authoritative momentum/boost/powerslide/tricks/combos/wall-rides, block-edge and rail grinding, Create track/Bezier geometry, vanilla-world physics composition, the useful hands-free Red Skate loadout architecture, TACZ/Epic Fight-safe movement layering, supplied graffiti integration, official branding, and gameplay/asset regression contracts.

Local validation at this checkpoint:

- `python3 tools/validate_gameplay_contract.py` — PASS
- `python3 tools/validate_assets.py` — PASS

The authoritative Forge/Java compile is still pending GitHub Actions. This checkpoint is not a verified release JAR until CI passes.
