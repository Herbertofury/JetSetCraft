# Liberty Grind — drop, tag, extract

**Helldivers-inspired expeditions on skates, connected to the rest of JetSetCraft.** Choose a district on the war map, land in a pod, clear rival crews, paint their territory, recover research and leave aboard your extraction ship. The trip returns you to your original Minecraft world and belongings.

Liberty Grind is implemented in the current development source. Stable `v0.3.0` does not include it. Historical Rival Rush test counts are separate from this mode's verification.

## Get ready

Install the supported **TaCZ 1.1.8-hotfix2** and **Helldivers:Liberty Day** gun pack on the server and participating clients. The October 27, 2026 merged pack includes Koei's original Helldivers weapons, so remove the old standalone Helldivers ZIP when using it. Put the intact ZIP in the instance's `tacz/` folder.

The [exact installation and compatibility guide](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/LIBERTY_GRIND_PACK.md) links the official files, hashes and requirements. JetSetCraft still starts without this optional content; the war map explains what is missing before deployment.

Use **Easy, Normal or Hard**, with graffiti enabled. Stand out of vehicles, finish any other activity and put away the item on your cursor before deploying.

## Choose your operation

Open the ordinary activity menu at a tuned Boombox or with `/jetsetcraft activity`, choose **Liberty Grind**, and select **Open war map**. You can also open it directly with:

```text
/jetsetcraft liberty
```

Review a district and press **Deploy in pod**.

| District | Rival crew | Approximate width | Mission budget |
| --- | --- | ---: | ---: |
| Neon Docks | Dead Beat zombies | 320 blocks | 25 minutes |
| Overpass Uprising | Bone Drones skeletons | 448 blocks | 30 minutes |
| Blackout Borough | Hex Appeal witches | 576 blocks | 35 minutes |

The pod descends into your own mission district. You receive a temporary copy of your equipped ride, or inline skates if you have none, plus armor, a spray can, food, a field radio and the real **Liberator / Peacemaker** loadout. Aim, shoot and reload with TaCZ's controls. JetSetCraft's movement controls continue to govern your skating.

## Free the district

Each operation has three objectives. Follow the objective HUD and world markers:

1. **Paint the town free.** Clear the rival defenders, then spray three distinct marked mural panels. Paint the highlighted **south-facing** wall faces. Repeating the same panel does not count twice.
2. **Pirate radio.** Clear the station, tag its panel and keep skating within twenty-four blocks for ten seconds of movement. Standing still does not broadcast progress.
3. **Grindline courier.** Clear the checkpoint, tag the dispatch panel, then skate through three lit courier beacons in order.

Use the issued spray can to paint the actual walls. Shooting a wall does not complete a graffiti objective. Defeats must belong to your mission and be credited to you.

Explore between objectives for glowing **super-credit capsules**, **common pigment samples**, **rare resonant samples** and the **super chromatic sample**. There are nine cache positions across the district. Credits are earned by playing; they are not purchased with real money.

Expect patrols between objective fights: the director attempts a new rival every twelve seconds while room remains in the encounter budget. Extraction increases that pressure to every three seconds. Keep moving and clear space around the landing zone.

## Resupply and extraction

Select the **Liberty field radio** in **hotbar slot five** and right-click to open the mission panel. It is a compass named Liberty field radio; `/jetsetcraft liberty` also opens the panel. Use it for **Resupply**, calling extraction and **Return early**.

**Resupply** tops your reserve magazines up to six for each gun, restores eight health points and refills boost. Reload normally afterward. You start with three charges, or four with Supply Skates research. Each completed objective restores one charge, up to a maximum of four, and uses have a thirty-second cooldown.

Once all three objectives are complete:

1. Reach the extraction pad and call the ship from within twelve blocks.
2. Hold within forty-eight blocks of the pad for thirty seconds while rivals pressure the landing zone.
3. Let the ship land, approach within eight blocks and right-click your ship, or select **Board ship** in the mission panel.
4. Ride the departure back to your saved world and location.

Leaving the landing zone cancels the call; you can call again. A ship that waits too long departs empty, but completed objectives remain complete. Keep an eye on the overall mission time.

**Samples need successful extraction.** A controlled early return, defeat or disconnect retains earned super credits and completed-objective crew progress while leaving field samples behind. **Return early** in the menu, or `/jetsetcraft liberty return`, restores the saved normal-world state. Loan weapons and armor stay with the mission.

## Turn samples into new techniques

Open the **Research** tab after returning. Purchases consume the displayed super-credit and sample costs once; learned passive projects apply automatically.

| Research | What it changes |
| --- | --- |
| **Pressure Saver** | Every fourth successful graffiti placement uses no spray-can durability, including ordinary-world painting. |
| **Magnetic Pigment** | Unlocks Magnetic Mixtape as a selectable Rival Rush starting preparation, giving +1.5-block pickup reach. |
| **Blast Stencil** | Unlocks Bigger Chorus as a selectable Rival Rush preparation, giving +12% paint/artifact area. |
| **Prism Ink** | Unlocks Finishing Touch as a selectable Rival Rush preparation, adding +25% base damage on critical hits. |
| **Signal Lacquer** | Makes the first Liberty mural objective require two distinct marked panels instead of three. |
| **Supply Skates** | Adds one starting resupply charge to later Liberty expeditions. |

For Magnetic Pigment, Blast Stencil or Prism Ink, choose **Select for Rival Rush** after researching it. You still have **one starting preparation**, shared with your Style Library/home choice. Selecting a crew style replaces that research selection. Your temporary Rush draft, artifact slots and evolution rules remain the same.

Liberty objectives also feed the existing **Crew Supplies, crew XP, member training and gang reputation**. A full three-objective extraction awards up to **20 Supplies, 125 crew XP, 55 training XP and 14 reputation**, subject to the account's existing caps and server settings. Crew XP continues unlocking Style Library lessons; rewards are retained even before you found a house. Open **Crew Charter → Progress** to see the accepted receipt.

## HUD and current scope

The mission menu's **HUD** button lets you move, scale or hide the Liberty display. Its layout is a local preference. Check the mission menu when you need the current objective, extraction or resupply status.

Current operations are **individual deployments**: several players can run separate districts concurrently, but a shared squad mission is not implemented yet. The war map currently selects three replayable operations. Shared galactic liberation, larger stratagem libraries, more weapons and dimension-mod theaters are planned expansions. The [implementation notes](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/LIBERTY_GRIND.md) explain the precise scope and test boundaries.

Continue with [Rival Rush](Rival-Rush), [Crew Houses and Progress](Player-Chapters-Posse-and-Mob-Archetypes), or [Graffiti](Graffiti).
