# JetSetCraft Wiki receipt verification

When a wiki publication receipt is carried across a newer main tip, its page list must still describe the exact published source commit, not the current working tree.

Run `python tools/wiki_receipt.py .agents-memory/WIKI_PUBLICATION.json` from a checkout with the recorded source commit available. This is a read-only comparison of the receipt's page names and count to that commit. It does not authenticate, push or verify the live wiki bytes.

Closeout map: [recorded receipt](../.agents-memory/WIKI_PUBLICATION.json) → [verifier](../tools/wiki_receipt.py) → pinned Git wiki subtree → [read-only Actions check](../.github/workflows/verify-wiki-receipt.yml). A [disposable Git regression](../tools/wiki_receipt_test.py) advances main by one addition and one removal and verifies that the older publication still names the original pages. This check does not alter the existing publisher; a mismatch needs a source-to-wiki reconciliation, not a blind republish. See [Athena's closeout draft](https://github.com/Herbertofury/Athena/pull/459) for how to record source observations separately from live proof.
