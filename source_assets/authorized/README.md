# Authorized source inputs

This directory holds owner-supplied binary inputs that may be used only within the scope recorded in
[`docs/ASSET_PROVENANCE.md`](../../docs/ASSET_PROVENANCE.md). The binaries are preserved in the canonical Desktop and
Google Drive project copies but are intentionally excluded from Git.

- `jsr/JSRGraffiti.zip` — exact owner-supplied graffiti archive; SHA-256
  `8541009fcfb3ec77f22e7aeafb2bcfceebd64decddf168171df24182438c70d9`.
- `red-skate/redskaterebellion-0.0.2.jar` — owner-supplied implementation reference; SHA-256
  `06ea09db5abbe82e9a6be3d7b8fa87946bf4a444a7cc8ca50890dfab6bd37d01`.

Do not publish, relicense, or copy these source binaries into a release artifact unless the provenance ledger explicitly
allows that exact use. Generated JetSetCraft runtime derivatives have their own entries in the ledger.
