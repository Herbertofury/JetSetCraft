# Lucy Lacemaker

Owner-selected FENNAH character for AMYGDALA, recruitable into the player's crew and intended as a rare Honkistas-event guest. Her canonical affiliation remains AMYGDALA even when visiting a Honkistas event or joining a player crew. Correct spelling is **Lacemaker**, not Lawmaker. Credit: **FENNAH**; character/music use permission is owner-attested with attribution, not independently reviewed correspondence.

## Named roster implementation candidate

The existing Crew Charter recruitment action now lists Lucy by name, using stable character ID `jetsetcraft:lucy_lacemaker`. It reuses normal supplies, capacity, owner authorization, revision checks, role selection and mission assignment. The member stores AMYGDALA affiliation separately from the player's owning crew. Existing NBT member serialization retains her stable identity across reload and renaming. Each crew can recruit at most one Lucy roster profile; this is not a world-global unique NPC.

This is an **abstract mission-roster profile**, matching existing recruited members. It does not spawn a visible follower, import a model, play music or implement Honkistas encounters. Recruitment is available in the ordinary ledger without an invented encounter prerequisite. A later world-character/encounter unlock design must be explicit rather than silently changing the existing roster promise.

The owner wants favorite characters from Memories to appear in JetSetCraft. Proposed cross-reference `core-lucy-lacemaker` is provenance only; no claim that the Memories record is already published. Do not import media or infer rights automatically from favorite status. Future appearances need independent source identity, rights and implementation evidence; MalO/SCP-1471-A needs its own rights review and is not covered by FENNAH or Skashi permission.

## Verification and remaining work

A native GameTest exercises real recruitment, canonical name, retained AMYGDALA affiliation, duplicate-cost protection and save/load. It has been added but is **not yet executed locally**. Static parsing/gameplay validation is not a native test pass. Full Forge compilation, GameTest, ledger UI, reload under actual play and mission completion with Lucy remain acceptance gates.

Rare Honkistas guest events, a visible character, animation, recruitment dialogue and authored music require separate implementation and permitted source assets. The supplied upper-body image has been visually inspected; it does not establish a full body turnaround or rig. No image is included in this change.
