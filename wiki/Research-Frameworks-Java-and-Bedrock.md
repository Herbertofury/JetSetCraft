# 🧰 Engineering, Frameworks & Performance

**[← Research hub](Inspiration-and-Research)** · **[Open the visual research browser](https://herbertofury.github.io/JetSetCraft/research.html)** · [Latest expansion](#november-1-expansion) · [First expansion](#october-31-expansion) · [Earlier catalogue](#earlier-catalogue)

**38 curated engineering references across two passes**, followed by the preserved earlier catalogue. The **November 1 expansion adds 16 references** for creator tools, replay workflows, input ownership, assets, audio, accessibility and performance. Source observations and proposed acceptance checks are tied to the current Forge baseline and existing task IDs. Earlier engineering opportunities remain intact below.

## November 1 expansion

**Checked 1 November 2026 · 16 additional primary references · Nine concrete prototype proposals.**

This section turns the new references into work that fits JetSetCraft's current repository. The baseline remains Forge 1.20.1 and Java 17, followed by the existing version order. New tools are candidates to learn from or evaluate; a product's documentation does not establish that its runtime belongs in this mod.

### What the source review actually found

The following are observations from the code and current execution contract, not performance measurements or claims that a prototype has shipped:

| Observed foundation | Concrete implication |
|---|---|
| [GraffitiSelectorScreen](https://github.com/herbertofury/JetSetCraft/blob/bfd1732/src/main/java/com/herberto/jetsetcraft/client/screen/GraffitiSelectorScreen.java) draws a four-by-three page of artwork and its palette manually, handles selection through mouse clicks, and creates native widgets for footer controls. | T065/T066 have a specific target: give artwork and palette controls explicit keyboard/controller focus and narration. The existing twelve-card page is pagination, not a limit on the collection. |
| [GraffitiEditorScreen](https://github.com/herbertofury/JetSetCraft/blob/bfd1732/src/main/java/com/herberto/jetsetcraft/client/screen/GraffitiEditorScreen.java) decodes held-item pixels in init(), provides Clear/Cancel/Done and paints through mouse input. | Screen reinitialization appears capable of replacing an unsaved draft. This is a source-level risk to test on resize or GUI-scale changes, not a runtime-confirmed bug. A separate draft model and gesture-level undo are useful candidates. |
| [JetSetConfig](https://github.com/herbertofury/JetSetCraft/blob/bfd1732/src/main/java/com/herberto/jetsetcraft/config/JetSetConfig.java) already exposes dynamicCamera and reducedMotion. | Preserve and extend those preferences coherently; do not present a camera-disable option as missing. |
| [The execution contract](https://github.com/herbertofury/JetSetCraft/blob/bfd1732/docs/JETSETCRAFT_CLAUDE_FINISH_EXECUTION.md) already specifies full music identity, preferences, ducking, duplicate-playback handoffs, pack hashing and stale-result protection. | The audio and asynchronous proposals give those existing tasks an implementation experiment, rather than introducing a smaller substitute catalogue. |
| T354 refers to replaying progression events in a deterministic simulator. | A cinematic replay or race ghost is a new proposal. It should not be described as an already specified T354 feature. |

The same boundaries apply throughout: preserve the 16×10 four-bit custom canvas, server authority, source-mob identity and AI ownership, vanilla fluids, combat-arm priority, neutral-input stopping and the full content catalogue. Improve work scheduling through demand, visibility, caching and lifecycle management; do not impose an arbitrary population or content cap.

### Nine prototypes worth reviewing

#### 1. A public-space grammar for a complete hangout

**Purpose.** Make a hangout support a flowing run, practice, a conversation, watching and an event without making those activities obstruct each other.

**Build.** Draft one site with three connected skating areas, clearly visible approaches, recovery points and social edges. Use the Skatepark Development Guide to examine line conflicts, Superkilen for differentiated functions, and Southbank or Underpass Park for respecting the found structure. Give each area a recognizable landmark and a reason to return.

**Acceptance.** Trace each route with all six ride styles and representative participating mob envelopes. Check headroom, landings, approach visibility, safe exit and spectator placement. An unsuitable route should produce an actionable location problem, not terrain destruction or an incompatible teleport. Maintain unrelated visitors and the source world's existing construction.

**Existing scope.** T132 terrain preservation; T154 route/checkpoint validation and ride fairness; T253 safe activity space; T255 canonical opponent movement and scoring.

#### 2. A graffiti blackbook with a durable draft model

**Purpose.** Make collecting and making graffiti feel like an ongoing practice, while preserving the established server-validated format.

**Build.** Keep custom pixels in a draft object whose lifetime is separate from screen layout. Add undo at the level of one painting gesture or a clear operation. Give catalogue works stable identity, complete titles, source provenance, favorites and relationships to the gang's sticker/tag/emblem/mural ladder. Use Getty and Cornell as models for how works acquire context; use the Whitney identity as a model for accommodating varied proportions.

**Acceptance.** Test resize, GUI-scale change, filtering, paging, returning from the painter and intentional Cancel. An unsaved draft should follow the explicitly designed policy, not disappear because a layout method ran. Undo should restore the prior gesture. Done must still encode only the bounded canvas payload and pass the existing server validation. A hand or item change must not silently apply a draft to an unrelated item.

**Existing scope.** T063 bounded wire format; T065/T066 selector and navigation; T278 provenance and gallery organization; T370 every curated gang's art ladder. The resize issue remains a source-derived hypothesis until reproduced.

#### 3. A gallery and painter that work through focus

**Purpose.** Let the same art collection be explored and used with mouse, keyboard, narration and a suitable controller adapter.

**Build.** Give gallery cards and colors explicit focusable controls, complete accessible names and stable directional navigation. Preserve the selected work when the collection is filtered or paged where possible; otherwise move focus to a clear, predictable result. In the painter, show a visible cell cursor and a separate palette selection so a player can move, paint and inspect without a mouse. A detailed view should reveal the complete title even when a compact card needs truncation.

**Acceptance.** Starting from a fresh screen, select a work, inspect its source, favorite it, change page, choose a color, paint, undo and return using digital input only. Confirm that focus survives expected UI changes and Escape has consistent meaning. Test narration with long names and a no-results search.

**Existing scope.** T065/T066 and T234. XAG 112 provides the navigation reference; XAG 107 covers alternatives beyond basic rebinding.

#### 4. Explicit input context and preference behavior

**Purpose.** Add useful accessible control choices without changing who owns movement.

**Build.** Write a small input-ownership table for normal movement, riding, item use, a screen, swimming and focus loss. Context changes should release obsolete actions. Add suitable hold/toggle or digital alternatives only where the action can retain a clear stop condition. Keep existing camera and reduced-motion preferences integrated with every resulting presentation effect.

**Acceptance.** With each supported input preference, hold an action and then open a GUI, enter water, use a weapon or lose focus. The appropriate higher-priority system must take over immediately. Return to neutral and confirm that the mod does not create continued movement or delayed combat-arm ownership.

**Existing scope.** T039 vanilla swimming; T058/T059 ownership and cancellation; T265 accessible challenges. Steam Input's layering is a conceptual reference, not a dependency requirement.

#### 5. A small race ghost before a cinematic editor

**Purpose.** Let a player study a line, share a performance or watch a challenge without changing the original result.

**Build.** Define an event record with authoritative ticks, dimension, ride identity, resource/version identity, useful state samples and checkpoint/trick/result markers. Draw a noncolliding playback actor. Keep camera keyframes and playback time separate, borrowing the useful distinction from ReplayMod. Decide explicitly whether later work needs scene recording, a cinematic editor or only ghosts; Flashback offers another original project to study.

**Acceptance.** Replay the same run with several cameras and playback speeds. The original finish order and markers must agree, and playback must not trigger checkpoints, deliver rewards or interfere with source mobs. Missing resource versions should produce an explicit degraded view or incompatibility result, not silently simulate a different run.

**Existing scope.** T154 supplies race-validation requirements. This is a new proposal; T354's progression-event simulator is a separate tool. Modern ReplayMod and Flashback are Fabric projects, not verified Forge 1.20.1 integrations.

#### 6. A contact-aware animation import preview

**Purpose.** Improve transitions and contact without confusing deliberate skating movement with erroneous foot sliding.

**Build.** Annotate clips with planted, rolling, sliding and airborne contacts, plus rig compatibility, available channels, phase and ownership. Validate timing and interpolation before export. Review short offline bridge clips between approved poses; use a simple blend or explicit selector as the baseline before considering a learned method.

**Acceptance.** Inspect a planted dance step, skate push, grind, intentional slide, landing and a cancel into item use. The importer should preserve intentional wheel motion and hold poses, reject incompatible rigs with an understandable reason, and never bake combat-arm ownership into a lower-body-only action. A presentation comparison must use the same server trajectory on both sides.

**Existing scope.** T057–T059, T277 and T369 contact hints. The paper section supplies UnderPressure, motion in-betweening, phase control and skeleton-aware retargeting as research references, not runtime promises.

#### 7. A music director with explicit timing and an accessible mix

**Purpose.** Make personal radio, event music, Boomboxes and previews behave as one coherent listening system.

**Build.** Attach stable track identity and authored tempo/meter/section metadata where available. Gameplay requests a section or musical role; the client schedules an eligible transition and a clear priority/handoff. Use phrase boundaries where musically appropriate and retain full tracks as a first-class option. Expose meaningful volume groups and readable equivalents for critical events.

**Acceptance.** Request the same track from personal radio, a nearby Boombox and a preview, then enter and leave an event. There should be one intentional playback owner or a deliberate transition, not accidental doubling. Test resume, late joining, an irregular meter, a missing asset, music disabled, mono output and narration priority. Measure drift or resource behavior in the implementation; this research reports none.

**Existing scope.** T281/T286–T289 and T291–T293. Preserve the canonical Mocca Sins ordered track identity and the approved music-driven dance direction. FMOD documentation informs authoring and resource concepts; it does not establish that FMOD should replace Minecraft's audio backend.

#### 8. A typed, terrain-aware route authoring format

**Purpose.** Give creators a repeatable way to make events that can be validated before they involve a player or a mob.

**Build.** Define typed checkpoints, line segments, approaches, recovery anchors, spectator zones, support requirements and participant envelopes. A Tiled-based sketch is one possible front end; an in-world authoring tool is another. Separate high-level route guidance, candidate generation, physical validation and reversible placement. Use local procedural compatibility only to propose candidates.

**Acceptance.** Validate a normal route plus deliberate failures: insufficient clearance, a fluid landing, an inaccessible recovery, an unloaded area and a locally compatible dead end. Explain the invalid edge or missing condition. A deterministic seed should reproduce a candidate, but no seed should excuse unsafe placement or an impossible opponent.

**Existing scope.** T132, T154, T253 and T255. Red Blob explains the graph distinction; the street-modeling and model-synthesis papers support authoring ideas. None proves Minecraft traversability by itself.

#### 9. Versioned asynchronous asset snapshots with real measurement

**Purpose.** Keep a large complete collection responsive without stale loads corrupting the current screen or world.

**Build.** Perform suitable file I/O, metadata work and decoding in an explicitly owned asynchronous pipeline. Publish immutable results only when their generation still matches the current world, screen and asset-pack identity. Release obsolete resources through the appropriate owning thread. Preserve stable IDs and unlock receipts when assets are temporarily absent.

**Acceptance.** Make an old request finish after a newer resource reload, close the screen during a load, switch worlds and inject a corrupt asset. Old results must not overwrite the current generation; cancellation alone is insufficient proof. Measure representative startup, memory, decode work and frame-time behavior before and after one targeted change. Use RenderDoc only if capture succeeds in the real graphics setup, and use CPU profiling for CPU questions.

**Existing scope.** T195, T281, T291–T293 and T373. For the research browser, retain the full catalogue, reserved image dimensions, an eager hero, offscreen lazy loading and useful broken-image fallbacks. Native controls remain the preferred baseline until a more complex search widget earns its complexity.

### Source catalogue

The references below are deliberately specific. They provide the control behavior, file semantics, authoring model or measurement tool needed for the proposals above.

### Replay & spectator tools

#### ReplayMod — camera and replay-time keyframes

[Primary source](https://www.replaymod.com/docs/)

**Evidence:** Original documentation; modern Minecraft versions use Fabric.

**What to study.** ReplayMod documents distinct camera-position and replay-time keyframes, event markers, spectator targeting, path previews and interpolation. Its installation documentation specifies Fabric for Minecraft 1.14 and later.

**JetSetCraft application.** Borrow the separation between an authoritative recorded event timeline and a cinematic camera timeline. This is a design reference for a possible Forge 1.20.1 replay or ghost system, not a compatible ReplayMod installation recommendation.

**Prototype check.** Record a short race, then replay it with a static camera, a following camera and a slowed camera sequence. Recorded checkpoints, finish order and rewards must remain unchanged, and the replay must never issue new gameplay actions.

**Priority:** P1 · **Atlas ID:** r2-eng-replaymod

#### Moulberry / Flashback — original replay project

[Primary source](https://github.com/Moulberry/Flashback)

**Evidence:** Original project repository; Fabric-based replay mod.

**What to study.** Flashback's own repository describes recording and playing back Minecraft sessions to make cinematic footage. It is a Fabric project with its own license, so its architecture and interface are references rather than an assumed Forge dependency.

**JetSetCraft application.** Compare scene recording, editing and export workflows before choosing what JetSetCraft needs: a lightweight race ghost, a spectator mode or a full cinematic recorder. These are different products with different storage and compatibility requirements.

**Prototype check.** Write a replay-format sketch containing dimension, event tick, ride identity, relevant resource versions and event markers. Demonstrate a noncolliding ghost that cannot trigger checkpoints, challenge participation or reward delivery.

**Priority:** P2 · **Atlas ID:** r2-eng-flashback


### Accessible controls & navigation

#### Steam Input — Action Set Layers

[Primary source](https://partner.steamgames.com/doc/features/steam_controller/action_set_layers)

**Evidence:** Original input-system documentation.

**What to study.** Steam Input action-set layers let a game apply and remove contextual bindings on top of a base action set, with defined ordering when layers overlap.

**JetSetCraft application.** Use explicit input context and precedence for ordinary movement, riding, menus and item use. The concept fits existing Forge key mappings and a possible controller adapter; it does not require making Steam Input a runtime dependency.

**Prototype check.** Document which context owns each action while riding, opening a GUI, swimming, using a weapon and losing focus. Entering a higher-priority context must release any lower-priority latch and must not create movement without player input.

**Priority:** P1 · **Atlas ID:** r2-eng-steam-input-layers

#### Xbox Accessibility Guideline 107 — Input

[Primary source](https://learn.microsoft.com/en-us/xbox/accessibility/xbox-accessibility-guidelines/107)

**Evidence:** Original accessibility guideline.

**What to study.** XAG 107 considers barriers beyond rebinding, including held inputs, repeated presses, simultaneous inputs and situations that lack digital alternatives to analog controls.

**JetSetCraft application.** Offer suitable hold/toggle and digital-navigation choices for manual, boost and painting workflows. Preserve the user's control over starting and stopping; accessibility options should not introduce involuntary movement or change canonical scoring.

**Prototype check.** Complete the same short ride and paint tasks using ordinary keys, remapped keys and an optional controller adapter. Test focus loss, GUI entry, swimming and item use with each preference; all must release or yield safely.

**Priority:** P1 · **Atlas ID:** r2-eng-xag107

#### Xbox Accessibility Guideline 105 — Audio accessibility

[Primary source](https://learn.microsoft.com/en-us/xbox/accessibility/xbox-accessibility-guidelines/105)

**Evidence:** Original accessibility guideline.

**What to study.** XAG 105 covers independently adjustable audio categories, mono output and the relationship between narration and competing audio. It treats audio customization as part of accessing the game.

**JetSetCraft application.** Separate music, announcer, crowd, critical movement cues and interface feedback where the sound architecture permits. Couple mixing preferences to existing radio and ducking plans, with visible equivalents for information needed to play.

**Prototype check.** Run a challenge with music off, narration prioritized, mono output and loud crowd ambience. The player should still understand invitations, route events and results; an announcer should not unintentionally mute an essential movement cue.

**Priority:** P1 · **Atlas ID:** r2-eng-xag105

#### Xbox Accessibility Guideline 112 — UI navigation

[Primary source](https://learn.microsoft.com/en-us/xbox/accessibility/xbox-accessibility-guidelines/112)

**Evidence:** Original accessibility guideline.

**What to study.** XAG 112 describes predictable focus movement and consistent navigation across keyboard and controller interfaces, including digital access to interactive elements.

**JetSetCraft application.** The current graffiti selector draws its gallery and palette manually and handles their selection through mouse clicks. Give those controls explicit focus, narration and directional navigation while retaining the existing native footer widgets and inventory-like feel.

**Prototype check.** Select, favorite and inspect artwork, change page, enter the painter, choose a color and return using only keys or controller directions. Announce complete titles and the focused action; preserve focus predictably after filtering or paging.

**Priority:** P1 · **Atlas ID:** r2-eng-xag112


### Reactive audio & resource handling

#### FMOD Studio 2.04 — authoring events and timeline logic

[Primary source](https://www.fmod.com/docs/2.04/studio/authoring-events.html)

**Evidence:** Original audio-authoring documentation.

**What to study.** FMOD's event-authoring documentation describes transition regions, conditions, destination selection and musical quantization, including transitions aligned to beats and bars.

**JetSetCraft application.** Define a music director that receives gameplay events and schedules eligible musical changes at an authored phrase boundary. Keep track identity, meter and tempo metadata explicit so a transition does not assume every song is uniform 4/4.

**Prototype check.** Author transitions among a free-roam section, a challenge section and a result sting. Check an ordinary track, a tempo change and an unusual meter; preserve the approved full-track catalogue and existing canonical music identities.

**Priority:** P1 · **Atlas ID:** r2-eng-fmod-timeline

#### FMOD Core 2.04 — resource management and virtual voices

[Primary source](https://www.fmod.com/docs/2.04/api/managing-resources-in-the-core-api.html)

**Evidence:** Original runtime-resource documentation.

**What to study.** FMOD describes managing sound resources and virtual voices, including maintaining logical playback while deciding which voices are actually audible and need active mixing resources.

**JetSetCraft application.** Investigate whether JetSetCraft's actual sound backend can preserve a distant Boombox's logical music position while suspending unnecessary decoding or mixing work. This suggests demand-based processing without removing tracks, visitors or sound identities.

**Prototype check.** Walk in and out of a Boombox's audible range and return during the same track. Resume at the intended position, release resources when the world closes and profile the real backend before making any memory or frame-time claim.

**Priority:** P2 · **Atlas ID:** r2-eng-fmod-virtual-voices


### Creator pipelines & authoring

#### Khronos glTF — animation channels, samplers and interpolation

[Primary source](https://github.khronos.org/glTF-Tutorials/gltfTutorial/gltfTutorial_007_Animations.html)

**Evidence:** Original standards-group tutorial.

**What to study.** The Khronos tutorial explains typed animation channels and their samplers, including STEP, LINEAR and CUBICSPLINE interpolation for animated transforms.

**JetSetCraft application.** Use a structured offline import manifest to check channel ownership, finite key times, duration, looping and intentional held poses before baking assets into JetSetCraft's approved runtime format. This does not imply replacing PlayerAnimator.

**Prototype check.** Validate a looping push clip, a held attitude pose and a short landing. Flag unexpected arm channels, nonfinite times and loop discontinuities while preserving deliberate stepped animation and the original resource identity.

**Priority:** P1 · **Atlas ID:** r2-eng-gltf-animation

#### Aseprite — command-line asset processing

[Primary source](https://www.aseprite.org/docs/cli/)

**Evidence:** Original editor documentation.

**What to study.** Aseprite's command-line interface supports batch workflows for palettes, layers, animation tags, sprite sheets and exported metadata.

**JetSetCraft application.** Build a reproducible maintainer workflow for authorized graffiti, icons and animated stickers with stable IDs, source attribution and hashes. Retain original art separately; the in-game custom painter continues to use its bounded 16×10 four-bit format.

**Prototype check.** Process three differently sized artworks and an animated icon twice from the same inputs. Require deterministic manifest identity, correct transparency, preserved attribution and an explicit palette conversion preview where conversion is intended.

**Priority:** P2 · **Atlas ID:** r2-eng-aseprite-cli

#### Tiled — objects and custom properties

[Primary source](https://doc.mapeditor.org/en/stable/manual/objects/) · [Supporting source](https://doc.mapeditor.org/en/stable/manual/custom-properties/)

**Evidence:** Original editor documentation.

**What to study.** Tiled supports points, shapes, polylines and objects with custom properties and references. These features can express authored structures separately from a rendered tile layer.

**JetSetCraft application.** Explore a typed route-authoring format with checkpoints, approach directions, recovery anchors, spectator zones and contact requirements. Tiled is a possible 2D authoring front end; a separate height/terrain adapter would be needed for Minecraft's 3D world.

**Prototype check.** Export one route graph and validate every node type, required property and reference. Reject unsafe or physically impossible edges before spawning an event, with a message identifying the offending approach or clearance.

**Priority:** P1 · **Atlas ID:** r2-eng-tiled-objects


### Route validation & authoring

#### Red Blob Games — Introduction to A*

[Primary source](https://www.redblobgames.com/pathfinding/a-star/introduction.html)

**Evidence:** Original interactive algorithm explanation.

**What to study.** Red Blob explains graph search through nodes, edges and costs. A pathfinder's answer depends on the graph it is given; graph connectivity alone does not establish that a physical character can traverse an edge.

**JetSetCraft application.** Build route validation from actual block shapes, approach speeds, contact requirements and participant envelopes before ranking interesting paths. Keep this separate from replacing a source mob's own navigator or granting it impossible checkpoint authority.

**Prototype check.** Include an apparently connected edge with insufficient headroom, a landing in fluid and a disconnected recovery point. A validator should explain each failure instead of teleporting the participant or rewriting the terrain.

**Priority:** P1 · **Atlas ID:** r2-eng-redblob-astar


### Async assets & browser usability

#### Java 17 — CompletableFuture

[Primary source](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/CompletableFuture.html)

**Evidence:** Original Java 17 API documentation.

**What to study.** CompletableFuture composes asynchronous work and explicit executors. Its documentation distinguishes execution policies and notes that cancellation is a completion mechanism rather than a guarantee that underlying work is interrupted.

**JetSetCraft application.** Load metadata and decode suitable assets off the render thread, then publish immutable results only if their world, pack and screen generation are still current. Keep texture creation and Minecraft state mutation on the appropriate owning thread.

**Prototype check.** Delay an old load, switch asset packs or worlds, and let the old work finish last. The result must be discarded without replacing newer state; corrupt or missing content should retain stable IDs and display a useful fallback.

**Priority:** P1 · **Atlas ID:** r2-eng-java-completablefuture

#### W3C WAI-ARIA — Combobox pattern

[Primary source](https://www.w3.org/WAI/ARIA/apg/patterns/combobox/)

**Evidence:** Original accessibility design pattern.

**What to study.** The WAI-ARIA Authoring Practices describe combobox variants, focus relationships and keyboard interactions, including editable fields with suggestion popups.

**JetSetCraft application.** If the research browser later needs an artist or title suggestion field, implement its complete interaction contract rather than adding a visually attractive but keyboard-incomplete popup. The current native select controls can remain native; Minecraft screens borrow behavior, not HTML roles.

**Prototype check.** Test typing, arrow navigation, Escape, Enter, no matches and a response arriving after the field changes. A stale suggestion must not overwrite the new query or move focus unexpectedly.

**Priority:** P2 · **Atlas ID:** r2-eng-wai-combobox

#### web.dev — browser-level image lazy loading

[Primary source](https://web.dev/articles/browser-level-image-lazy-loading)

**Evidence:** Original browser-platform guidance.

**What to study.** The guide explains native lazy loading for offscreen images, reserving image dimensions to limit layout shifts and avoiding lazy loading for immediately important imagery.

**JetSetCraft application.** Keep the research browser's full catalogue discoverable while deferring offscreen artwork. Reserve appropriate image space, leave the hero eager and make a failed image degrade to a usable source link rather than a broken card.

**Prototype check.** Test an image-heavy search, a narrow viewport, a failed remote image and keyboard navigation while thumbnails arrive. Measure layout movement and requests before claiming a performance improvement; pagination or rendering windows must not become content caps.

**Priority:** P2 · **Atlas ID:** r2-eng-lazy-images


### Graphics diagnostics & measurement

#### RenderDoc — graphics frame capture

[Primary source](https://github.com/baldurk/renderdoc) · [Supporting source](https://github.com/baldurk/renderdoc/blob/v1.x/docs/window/capture_attach.rst)

**Evidence:** Original graphics debugger repository and documentation.

**What to study.** RenderDoc is a graphics frame-capture and inspection tool with OpenGL support. Its capture documentation covers launching or attaching to an application for investigation.

**JetSetCraft application.** Evaluate it in a controlled mod-development session to inspect graffiti layers, translucent effects, gear rendering and draw calls. Pair a graphics capture with CPU-side profiling when needed; neither tool alone explains every frame-time problem.

**Prototype check.** Capture one reproducible scene if the actual Minecraft/driver setup supports it, identify a concrete rendering issue, make one targeted change and compare the same scene. Do not report tool compatibility or performance gains before that experiment succeeds.

**Priority:** P2 · **Atlas ID:** r2-eng-renderdoc

---

## October 31 expansion

### Forge-aware engineering opportunities from the research

**Scope:** proposed improvements tied to the inspected JetSetCraft source and existing acceptance goals. No gameplay implementation, performance result or native test pass is claimed by this research pass.

JetSetCraft’s current production target is **Forge 1.20.1 / Java 17**, with PlayerAnimator in the existing client presentation path. Continue the approved order **1.20.1 → 1.21.1 → later explicit targets** and the shared-core/thin-adapter architecture in [Developer Architecture](https://github.com/Herbertofury/JetSetCraft/wiki/Developer-Architecture). Modern Fabric/NeoForge examples are not drop-in code for the present target, and a framework migration is not a prerequisite for the improvements below.

#### Primary implementation and research references

##### Forge baseline and UI

| Reference | What it can improve | Small review / prototype | Source and media |
|---|---|---|---|
| **Forge 1.20.1 — Screens**<br>ENG01 · High | The existing Screen/widget system supports focus, narration, rendering and teardown. Reuse it for gallery, radio and HUD-layout work. | Keyboard-only and narrator passes for selector, editor and future Radio; verify Escape, focus order, resize and restored selection. | [Primary source](https://docs.minecraftforge.net/en/1.20.1/gui/screens/) |
| **Forge 1.20.1 — Key Mappings**<br>ENG02 · High | Use ordinary rebindable key mappings and context-aware input rather than hard-coded key combinations. | Rebind every JetSetCraft action, open chat/inventory during held input, and confirm no queued action fires after returning to play. | [Primary source](https://docs.minecraftforge.net/en/1.20.1/misc/keymappings/) |
| **Forge 1.20.1 — SimpleImpl networking**<br>ENG03 · High | Thread ownership, sender validation and loaded-chunk checks remain essential when adding challenge, route and audio messages. | Exercise stale, duplicate, wrong-dimension and out-of-order event messages; confirm no score authority or chunk loading comes from an untrusted client payload. | [Primary source](https://docs.minecraftforge.net/en/1.20.1/networking/simpleimpl/) |
| **Forge 1.20.1 — Synchronizing Entities**<br>ENG04 · High | Forge distinguishes spawn data from changing entity data and warns against adding data parameters to other mods’ entities. | Keep source mobs intact and use JetSetCraft-owned data/transport for dance and gear state. Test a tracking client arriving halfway through an event. | [Primary source](https://docs.minecraftforge.net/en/1.20.1/networking/entities/) |
| **Forge 1.20.1 — Logical and physical sides**<br>ENG05 · High | Single-player contains a logical server, while dedicated servers lack client classes. Audio, renderers and animation stay behind client boundaries. | Verify future music/CPM integration on a real dedicated server with the optional client presentation dependency absent. | [Primary source](https://docs.minecraftforge.net/en/1.20.1/concepts/sides/) |
| **Forge 1.20.1 — GameTests**<br>ENG06 · High | Structured in-world fixtures are useful for deterministic rail, event-lifecycle and state-restoration checks. | Add focused native fixtures only when implementing a change; pair logic checks with real-client visual/audio evidence for presentation behavior. | [Primary source](https://docs.minecraftforge.net/en/1.20.1/misc/gametest/) |

##### Audio and choreography engineering

| Reference | What it can improve | Small review / prototype | Source and media |
|---|---|---|---|
| **Forge 1.20.1 — Sounds**<br>ENG07 · High | The documentation explains sound events, categories, subtitles, side-specific playback and streaming long files. | Stream long tracks, keep important SFX/subtitles legible, and prove a local/shared handoff does not play the same track twice. | [Primary source](https://docs.minecraftforge.net/en/1.20.1/gameeffects/sounds/) |
| **Forge 1.20.1 — Sound definition generation**<br>ENG08 · High | Generated sound definitions provide a reproducible place to set subtitles, streaming, attenuation and file references. | Generate catalog/sound mappings from one manifest; reject broken IDs and verify every authorized track remains addressable after packaging. | [Primary source](https://docs.minecraftforge.net/en/1.20.1/datagen/client/sounds/) |
| **CRI Middleware — Interactive music with AISAC and selectors**<br>ENG09 · High | A primary middleware tutorial demonstrates intensity changes and beat-synchronized transitions. Study the scheduling pattern; it is not a Forge dependency recommendation. | Prototype immediate hit/landing accents alongside bar-aligned layer changes; never delay the underlying gameplay action. | [Primary source](https://blog.criware.com/index.php/2024/07/08/adx-beginners-guide-12-interactive-music-with-aisac-and-selectors/) · [Video](https://blog.criware.com/wp-content/uploads/2024/03/Video_01_Music_AISAC.mp4) |
| **Ableton Link — tempo, phase and quantum concepts**<br>ENG10 · High | Tempo, beat phase and bar/loop alignment are separate concepts. This helps specify shared choreography timelines precisely. | Represent an event epoch, tempo map, phrase position and sequence ID. Test late joins and tempo/section changes without resetting the whole song. | [Primary source](https://ableton.github.io/link/) |
| **FFmpeg — loudnorm and audio filter documentation**<br>ENG11 · High | The official filter documentation supports reproducible loudness measurement and preparation of authorized source masters. | Measure masters offline, store metadata and compare transitions at equal perceived level. Preserve masters and avoid repeated lossy transcoding; choose mix targets through listening tests. | [Primary source](https://ffmpeg.org/ffmpeg-filters.html#loudnorm) |

##### Performance and simulation

| Reference | What it can improve | Small review / prototype | Source and media |
|---|---|---|---|
| **spark — profiler documentation**<br>ENG12 · High | A Minecraft-focused profiling workflow helps separate tick cost, allocation pressure and apparent lag. | Capture matched idle, dense graffiti, active cypher, rail transfer and event-load scenarios before and after an optimization. | [Primary source](https://spark.lucko.me/docs) |
| **JDK 17 — Flight Recorder**<br>ENG13 · High | Java Flight Recorder provides runtime evidence in the project’s existing Java 17 toolchain. | Compare allocation, GC, I/O and thread activity around startup, resource reload, canvas rendering and long music sessions; retain exact build and workload metadata. | [Primary source](https://docs.oracle.com/en/java/javase/17/jfapi/index.html) |
| **async-profiler — profiling modes**<br>ENG14 · Explore | CPU, allocation and wall-clock modes can distinguish computation from waits and memory churn. | Use a supported host/runtime combination to test whether suspected render-path decoding or HUD formatting is material before changing it. | [Primary source](https://github.com/async-profiler/async-profiler/blob/master/docs/ProfilingModes.md) |
| **Glenn Fiedler — Fix Your Timestep**<br>ENG15 · High | Separate fixed simulation steps from presentation, and account for the cost of catch-up work. | Test the same ride/secondary-motion sequence at different render rates and under stalls. Preserve server physics and do not tie movement to visual frame rate. | [Primary source](https://gafferongames.com/post/fix_your_timestep/) |
| **Gabriel Gambetta — prediction and reconciliation**<br>ENG16 · High | The author explains input sequence history and reconciliation with authoritative state. This is a conceptual reference for responsive presentation. | Measure local input-to-visible response and correction distance under latency/jitter. Any prediction extension must retain server scoring, neutral-stop and fluid ownership. | [Primary source](https://www.gabrielgambetta.com/client-side-prediction-server-reconciliation.html) |
| **Yuksel, Schaefer and Keyser — Catmull-Rom parameterization**<br>ENG17 · Explore | The paper proves a within-segment cusp/self-intersection property for centripetal cubic Catmull-Rom curves. It does not prove global route safety or constant speed. | For new event-authored curves only, compare curvature, arc-length travel and contact normals; preserve native vanilla rail and Create Bezier geometry. | [Primary source](https://www.cemyuksel.com/research/catmullrom_param/) · [Paper](https://cemyuksel.com/research/catmullrom_param/catmullrom.pdf) |
| **Macklin, Müller and Chentanez — XPBD**<br>ENG18 · High | The authors address timestep/iteration dependence in compliant constraints. This is useful research for approved skating-responsive hair and loose costume parts. | Compare a small authored hair chain with explicit anchors, damping and collision envelopes. Preserve flat CPM details, expression channels and the existing native-preview evidence. | [Primary source](https://matthias-research.github.io/pages/publications/XPBD.pdf) · [Author demos and videos](https://matthias-research.github.io/pages/tenMinutePhysics/index.html) |
| **Macklin et al. — Small Steps in Physics Simulation**<br>ENG19 · Explore | The paper compares substeps with solver iterations; its results motivate experiments, not a universal Minecraft speedup claim. | Test acceleration, reversal, braking, landing, teleport/reset and return from hidden state. Measure stability and cost without removing characters or costume detail. | [Primary source](https://matthias-research.github.io/pages/publications/smallsteps.pdf) · [Author interactive demo](https://matthias-research.github.io/pages/challenges/pendulum.html) |

##### Accessibility and readability

| Reference | What it can improve | Small review / prototype | Source and media |
|---|---|---|---|
| **Xbox Accessibility Guideline 117 — motion settings**<br>ENG20 · High | Camera movement, scrolling information and unprompted motion need deliberate user controls. | Audit camera/FOV, Boombox ornaments, HUD pulses and future radio backgrounds under reduced motion. Preserve mechanics and all important information. | [Primary source](https://learn.microsoft.com/en-us/xbox/accessibility/xbox-accessibility-guidelines/117) |
| **Xbox Accessibility Guideline 103 — multiple cue channels**<br>ENG21 · High | Important information should be perceivable through more than one sensory channel. | Test dance invitations, hit confirms, route warnings and music milestones with sound muted and with color discrimination removed. | [Primary source](https://learn.microsoft.com/en-us/gaming/accessibility/xbox-accessibility-guidelines/103) |
| **Xbox Accessibility Guideline 118 — photosensitivity**<br>ENG22 · High | Use the guideline’s assessment methods when designing reactive stage lighting and high-contrast visual effects. | Review worst-case combined light/particle/announcer sequences, provide reduced-flash controls, and verify event readability without strobing. | [Primary source](https://learn.microsoft.com/en-us/xbox/accessibility/xbox-accessibility-guidelines/118) |

#### Concrete source-aware opportunities

These are observations of the checked source, followed by proposed changes and evidence to collect. Existing task IDs refer to [the finish execution contract](https://github.com/Herbertofury/JetSetCraft/blob/main/docs/JETSETCRAFT_CLAUDE_FINISH_EXECUTION.md). They preserve that backlog rather than creating a competing plan.

| Opportunity and priority | Evidence in the inspected source | Proposed implementation and acceptance |
|---|---|---|
| **Cache custom-canvas decoding — early measurement** | [`GraffitiRenderer.renderCustom`](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/java/com/herberto/jetsetcraft/client/render/GraffitiRenderer.java) calls `CustomGraffiti.decode(encoded)` during rendering and emits each nontransparent cell’s quad. | Cache decoded immutable pixels or reusable geometry by the validated encoded pattern; update only on changed content and release entity-owned references on unload. Compare pixel-perfect output, all six faces, repaint, reload and dense-scene allocation/frame time. Preserve all pixels, palette entries, sizing and the 16 × 10 wire format. **T063, T197.** |
| **One HUD layout owner — high impact** | [`ClientEvents.hud`](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/java/com/herberto/jetsetcraft/client/ClientEvents.java) places the current display at fixed offsets from the hotbar and trims the mode label to 15 characters. | Implement the already approved measured-bounds modules, Auto-Avoid and per-user Layout Editor. Measure text in pixels, retain understandable labels and use an intentional compact variant. Test GUI scales, long localized strings, armor/air/mount rows, subtitles, chat and real optional overlays. **T013–T027, T221–T236.** |
| **Change-driven HUD formatting — measure first** | The same HUD path builds labels and formats the multiplier while rendering. | Rebuild display strings on relevant snapshot/config/language changes, retaining frame interpolation only where visible. Profile before/after; do not claim that a small allocation automatically causes player-visible lag. **T197.** |
| **Close the reduced-motion audit — high value** | [`ClientEvents`](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/java/com/herberto/jetsetcraft/client/ClientEvents.java) already checks `reducedMotion` for camera/FOV; [`BoomboxRenderer`](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/java/com/herberto/jetsetcraft/client/render/BoomboxRenderer.java) visibly computes spin and active-event bob without that guard in the inspected file. | Decide and document reduced-motion behavior for each nonessential ornament, HUD pulse, stage effect and future music browser. Retain a static state indicator and all gameplay information. Review the whole scene instead of assuming one camera toggle covers it. **T022, T265, T349–T350.** |
| **Animation continuity with ownership — high value** | [`RideAnimationController.apply`](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/java/com/herberto/jetsetcraft/client/animation/RideAnimationController.java) avoids reapplying the same clip and starts a new `KeyframeAnimationPlayer` when the resource changes. The controller already separates ride and action layers. | Inspect the pinned library’s actual blend/modifier APIs before adding short, state-aware transitions or phase continuity. Never blend stale motion across neutral-stop, fluid, weapon or cancellation boundaries. Compare full-speed and slowed recordings, item/weapon combinations and rapid state changes. **T057–T059, T277.** |
| **Presentation interpolation — evaluate carefully** | [`ClientRideState.accept`](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/java/com/herberto/jetsetcraft/client/state/ClientRideState.java) installs sanitized snapshots; camera/HUD consume those snapshots. | If recordings show visible stepping, interpolate cosmetic values from consecutive timestamped states. Snap critical state changes immediately and clear history on dimension/identity changes. This is separate from any gameplay prediction work. Test multiple frame rates and packet jitter; do not delay hit, landing, score or input. **T197, existing multiplayer goals.** |
| **One music/event clock — flagship work** | The source exposes original gang sound events in [`ModSounds`](https://github.com/Herbertofury/JetSetCraft/blob/main/src/main/java/com/herberto/jetsetcraft/registry/ModSounds.java); the live wiki separately requires full Radio, reactive fight/trick music and shared choreography. | Build a shared event/timeline contract consumed by audio, significant choreography, announcer and lights. Keep immediate feedback separate from beat/bar/phrase transitions; deduplicate confirmed event IDs. Verify late join, wrong/stale sequence, handoff, restart, resource reload and muted play. **T281–T296; live music/choreography requirements.** |
| **Offline media metadata and streaming — high value** | Long-track support is accepted backlog; Forge 1.20.1 already provides streamed sound definitions. | Keep complete catalog coverage while generating duration, source/artist credits, hashes and optional beat/phrase/loudness metadata ahead of playback. Stream long media, reuse decoded/analysis results where appropriate and avoid per-tick full-track analysis. Demonstrate startup, memory, resource-reload and long-session results. **T270–T272, T281, T287–T295.** |
| **Skating-driven secondary motion — approved quality work** | The live [Honkistas quality page](https://github.com/Herbertofury/JetSetCraft/wiki/Honkistas%E2%80%90NPC%E2%80%90Quality%E2%80%90and%E2%80%90Skating%E2%80%90Physics) records a native CPM reference preview and an unintegrated chain-solver prototype. Neither establishes shipped skating physics. | Integrate only after explicit part/pose ownership. Compare an authored damped-chain/XPBD approach using actual acceleration, turns and landings. Preserve the model’s planes, expressions, detail and source credit. Profile distance/visibility work with smooth state restoration; never meet cost targets by shrinking the roster or deleting costume detail. **Existing issue #38 / live acceptance checklist.** |
| **Authored event geometry with truthful contact — later slice** | Current architecture already follows vanilla rail shapes and Create’s native Bezier geometry. Live terrain-aware events require real collision and recovery. | Evaluate smooth curves only for newly authored event geometry; use spatial queries, precomputed arc-length data and revision-aware caches when justified. Native rails remain native. Verify swept movement, curvature, junctions, landing/clearance and event cleanup. A pleasant curve drawing does not prove a safe playable route. **Existing terrain-aware event backlog.** |

#### A measured experiment sequence

1. **Record one exact baseline build.** Capture the same dense graffiti wall, HUD states, mixed crowd, rail sequence and long-track scenario. Record hardware, Java, loader, optional mods, GUI scale, view distance and asset-pack identities.
2. **Change one concrete cost or quality problem.** Start with decoded canvas reuse, readable HUD layout, a reduced-motion gap or an animation transition shown to be poor. Avoid simultaneous architectural rewrites that make cause and effect unclear.
3. **Prove behavior equivalence where expected.** Image comparisons and state traces should establish that an optimization preserves content, movement, scoring, cleanup and compatibility. Cosmetic redesigns need player comparison rather than pretending bit-identical output is the goal.
4. **Collect the evidence that matches the claim.** Frame/tick percentiles, allocation, I/O, packets, startup time, input-to-visible response and human usability tests answer different questions. A successful compile cannot establish smoothness or musical impact.
5. **Promote only proven improvements.** Keep successful research connected to the existing task/issue and exact tested revision. Preserve failures and open questions so the next iteration does not repeat them.

Use workload-aware scheduling, caching, sharing, coalescing and locality to support the full approved content and participation ambition. Do not turn a profiling budget into an arbitrary roster, art, track, dance or population cap. Necessary existing hostile-input bounds and safe event lifecycle rules still apply.

#### Audio implementation notes worth preserving

Personal Radio remains client-owned. A nearby Boombox or event may supply shared track identity and time, but should use a deliberate arbitration rule to prevent duplicated music. Store queue/favorites/volume/resume preferences locally; restore the previous owner cleanly when an event finishes. Do not reset the song at every trick or start one full track per reacting NPC.

For reactive music, provide two paths: an immediate response to a confirmed gameplay event and a scheduled structural change at a suitable musical boundary. A single-track fallback must still sound coherent if stems or detailed timing metadata are unavailable. Music-generated choreography, crew joins and light cues should share meaningful phrase state without granting audio code authority over movement or score. Tempo changes, pauses, cancellation and late joins need explicit recovery rules.

The full approved track/content set remains the target. This research proposes no downloads, no artist outreach and no change to existing permissions. The new music candidate register and the already authorized production catalog are separate records.


---

### Earlier catalogue

The **32 earlier entries** below remain intact for continuity. Their inherited scores and version notes are historical research metadata, not fresh benchmark results or a claim that every external release was rechecked in this pass. The [earlier 173-row sheet](https://docs.google.com/spreadsheets/d/1liWhRQL7rmPy8LLgSRVlkrLANqUIJE4UbFX3--KMJjg/edit?gid=1570163195#gid=1570163195) remains available.

#### Code & Frameworks · 9

Reusable architectural lessons for geometry, animation, data, integration, and moving worlds.

| Project | Environment | What to study | JetSetCraft opportunity | Links | Rights / contact |
|---|---|---|---|---|---|
| **Sable**<br><sub>Mod / source architecture reference · score 9.00</sub> | Java<br>Fabric / NeoForge<br>1.21.1 (2026) | Moving interactive sub-levels containing blocks, block entities and entities; physics, buoyancy, Create integration and repeated portal/explosion/velocity hardening. | High-value torture test for JetSetCraft riding/grinding on moving worlds. Study coordinate transforms and physics edge cases, but keep JetSetCraft additive and avoid Sable-style intrusive global ownership. | [Project](https://modrinth.com/mod/sable) · [Source](https://github.com/ryanhcode/sable) | Ask author / verify terms before shipping |
| **Create**<br><sub>Mod / source reference · score 8.80</sub> | Java<br>Forge / NeoForge / Fabric ports<br>Modern | Track geometry, moving contraptions, kinetic systems and rich block-state interactions. | JetSetCraft already supports Create track optionally; keep studying geometry continuity and moving-world interactions for advanced trick lines. | [Project](https://www.curseforge.com/minecraft/mc-mods/create) · [Source](https://github.com/Creators-of-Create/Create) | Ask author / verify terms |
| **Splinecart**<br><sub>Mod / source reference · score 8.70</sub> | Java<br>Fabric<br>1.21-1.21.3 | Smooth spline rails, oriented ties, uphill chain drive, redstone magnetic acceleration/braking, camera rotation options, network sticking fixes and VBO performance improvements. | Top geometric-path reference for smooth curves, deterministic acceleration zones and rail camera comfort. Valuable for JetSetCraft custom grind rails and spline-like creator blocks. | [Project](https://modrinth.com/mod/splinecart) · [Source](https://github.com/FoundationGames/Splinecart) | Ask author / verify terms before shipping |
| **Better Combat**<br><sub>Compatibility / architecture reference · score 7.55</sub> | Java<br>Fabric / Forge / NeoForge<br>Modern | Combat animation/action framework with widespread compatibility ecosystem. | Reference for animation priority, held-item ownership and preserving combat while ride locomotion controls lower-body movement. | [Project](https://modrinth.com/mod/better-combat) | Ask author / verify terms |
| **Epic Fight**<br><sub>Compatibility / architecture reference · score 7.55</sub> | Java<br>Forge / NeoForge<br>Modern | Complex combat animation/state machine and compatibility ecosystem. | Important coexistence reference for body-part ownership, action state and cancellation when JetSetCraft riding remains active. | [Project](https://www.curseforge.com/minecraft/mc-mods/epic-fight-mod) | Ask author / verify terms |
| **peterwolf Splinecart**<br><sub>Mod / fork reference · score 7.40</sub> | Java<br>Fabric<br>26.1-26.2 (2026) | Current splinecart fork with automatic supports, survival recipes and commands for spline lines, paths, connecting and support modes. | Fresh creator-tool comparison for making smooth path systems authorable in survival and via debugging/admin commands. | [Project](https://modrinth.com/mod/pw_splinecart) | Ask author / verify terms before shipping |
| **BO's Easy Model Entities**<br><sub>GitHub / framework reference · score 7.10</sub> | Java<br>Forge / NeoForge ecosystem<br>Multi-version | Reusable model-entity abstractions and model-driven content architecture. | Potential reference for clean model entity scaffolding and asset-driven entity presentation without entangling JetSetCraft core. | [Project](https://github.com/MarkusBordihn/BOs-Easy-Model-Entities) · [Source](https://github.com/MarkusBordihn/BOs-Easy-Model-Entities) | Ask author / verify terms |
| **TACZ**<br><sub>Compatibility / architecture reference · score 6.85</sub> | Java<br>Forge / NeoForge<br>Modern | Highly animated firearms, first-person/third-person item handling and attachment systems. | JetSetCraft already targets TACZ; use as a stress case for hands-free ride equipment and upper-body animation preservation. | [Project](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero) | Ask author / verify terms |
| **Sable Tool**<br><sub>Debug / source utility · score 6.75</sub> | Java<br>NeoForge<br>1.21.1-26.1.x | Visualizes sub-level bounds and centers of mass, tracks forces and exposes clone/schematic/teleport/management tooling. | Good model for JetSetCraft debug overlays: make grind paths, moving-frame transforms, velocity sources and bad seams inspectable instead of debugging motion blind. | [Project](https://modrinth.com/mod/sable-tool) | Ask author / verify terms before shipping |

#### Bedrock & Marketplace · 23

Cross-edition mechanic references; Marketplace packages remain purchase-locked research unless separately licensed.

| Project | Environment | What to study | JetSetCraft opportunity | Links | Rights / contact |
|---|---|---|---|---|---|
| **LB Street Skate**<br><sub>Bedrock / Marketplace reference · score 7.85</sub> | Bedrock<br>Marketplace / Lifeboat<br>Any / varies | 38 tricks, grinding, bailing, challenges, board customization and a large trick/park vocabulary. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://chunk.gg/@lifeboat/lb-street-skate) | Ask author / verify terms |
| **RedSkate Rebellion [BE]**<br><sub>Bedrock / Marketplace reference · score 7.25</sub> | Bedrock<br>CurseForge Bedrock<br>Any / varies | Roller skates, rollerblades, ice skates, skate parks, spray painting/posters, futuristic city, progression. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://www.curseforge.com/minecraft-bedrock/addons/redskate-rebellion-be) | Ask author / verify terms |
| **Bikes, Scooters & Skateboards**<br><sub>Bedrock / Marketplace reference · score 7.15</sub> | Bedrock<br>Marketplace / Lifeboat<br>Any / varies | Skateboards, roller skates, ice skates, longboards, hoverboards, bikes, scooters, tricycles, half-pipes and 180s. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://chunk.gg/en/@lifeboat/bikes-scooters-and-skateboards) | Ask author / verify terms |
| **BMX - Project Moonboot**<br><sub>Bedrock / Marketplace reference · score 7.05</sub> | Bedrock<br>Marketplace / Project Moonboot<br>Any / varies | BMX complex, paintable bikes, racing and progression/upgrades. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://chunk.gg/@project-moonboot/bmx) | Ask author / verify terms |
| **Skateboards - Kreatik Studios**<br><sub>Bedrock / Marketplace reference · score 7.05</sub> | Bedrock<br>Marketplace / Kreatik Studios<br>Any / varies | Nine animated tricks, board customization, halfpipes, rails, ramps, stairs and indoor/outdoor parks. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://chunk.gg/@kreatik-studios/skateboards) | Ask author / verify terms |
| **CT Cubiks Skater**<br><sub>Bedrock / Marketplace reference · score 6.85</sub> | Bedrock<br>MCPEDL<br>Any / varies | Command-driven ollies, rotations, trick XP/unlocks and skatepark construction. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://mcpedl.com/ct-cubiks-skater/) | Ask author / verify terms |
| **Mine BMX**<br><sub>Bedrock / Marketplace reference · score 6.85</sub> | Bedrock<br>CurseForge Bedrock<br>Any / varies | Inventory-item-driven BMX movement instead of a conventional mount; alternative control architecture. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://www.curseforge.com/minecraft-bedrock/addons/mine-bmx-bikes-for-minecraft) | Ask author / verify terms |
| **Mountain Bikes - Lifeboat**<br><sub>Bedrock / Marketplace reference · score 6.75</sub> | Bedrock<br>Marketplace / Lifeboat<br>Any / varies | Multiple bike types, tracks, jumps/gaps and wheelies. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://chunk.gg/en/@lifeboat/mountain-bikes) | Ask author / verify terms |
| **Sports Day: Summer City**<br><sub>Bedrock / Marketplace reference · score 6.75</sub> | Bedrock<br>Marketplace / Blockception<br>Any / varies | Skateboard, roller skates, bicycle, scooter, surfboard, tricks and custom player animation. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://chunk.gg/@blockception/sports-day-summer-city) | Ask author / verify terms |
| **Hoverboards - BLOCKLAB Studios**<br><sub>Bedrock / Marketplace reference · score 6.55</sub> | Bedrock<br>Marketplace / BLOCKLAB Studios<br>Any / varies | Hoverboards/skateboards, trick play and obstacle courses. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://chunk.gg/@blocklab-studios/hoverboards) | Ask author / verify terms |
| **BicycleCraft**<br><sub>Bedrock / Marketplace reference · score 6.25</sub> | Bedrock<br>MCPEDL<br>Any / varies | Craftable BMX, cruiser and mountain bikes with different stats. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://mcpedl.com/bicyclecraft-survival-friendly-bicycles/) | Ask author / verify terms |
| **MINE SURFER - Pickaxe Studios**<br><sub>Bedrock / Marketplace reference · score 6.25</sub> | Bedrock<br>Marketplace / Pickaxe Studios<br>Any / varies | Board-riding obstacle movement and unlockable boards. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://www.bedrockexplorer.com/marketplace-offer/pickaxe-studios/mine-surfer) | Ask author / verify terms |
| **Solarpunk Vehicles**<br><sub>Bedrock / Marketplace reference · score 6.25</sub> | Bedrock<br>CurseForge Bedrock<br>Any / varies | Scooter, tuk-tuk, quad and van with animations intended to suggest weight and movement physics. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://www.curseforge.com/minecraft-bedrock/addons/solarpunk-vehicles) | Ask author / verify terms |
| **Vehicles Pack - Dodo Studios**<br><sub>Bedrock / Marketplace reference · score 6.25</sub> | Bedrock<br>Marketplace / Dodo Studios<br>Any / varies | Skateboards, bikes, quads, cars, motorcycles and aircraft in one vehicle ecosystem. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://chunk.gg/en/@dodo-studios/vehicles-pack) | Ask author / verify terms |
| **Hockeyland - Blockworks**<br><sub>Bedrock / Marketplace reference · score 6.15</sub> | Bedrock<br>Marketplace / Blockworks<br>Any / varies | Ice skating, skating progression and sport-specific movement. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://www.bedrockexplorer.com/marketplace-offer/blockworks/hockeyland) | Ask author / verify terms |
| **This Is High School - Lifeboat**<br><sub>Bedrock / Marketplace reference · score 6.05</sub> | Bedrock<br>Marketplace / Lifeboat<br>Any / varies | Skateboarding through ordinary built environments rather than only dedicated parks. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://chunk.gg/@lifeboat/this-is-high-school) | Ask author / verify terms |
| **BikeCraft**<br><sub>Bedrock / Marketplace reference · score 5.65</sub> | Bedrock<br>MCPEDL<br>Any / varies | Motorcycles, motocross, scooter and quad-bike vehicle variety. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://mcpedl.com/bikecraft-add-on/) | Ask author / verify terms |
| **Segway And Bike Addon**<br><sub>Bedrock / Marketplace reference · score 5.65</sub> | Bedrock<br>MCPEDL<br>Any / varies | Rideable bicycle/Segway with custom riding animation. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://mcpedl.com/segway-and-bike-addon/) | Ask author / verify terms |
| **Advanced Roleplay - Lifeboat**<br><sub>Bedrock / Marketplace reference · score 5.45</sub> | Bedrock<br>Marketplace / Lifeboat<br>Any / varies | Skating as one activity inside a larger city/activity sandbox. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://www.bedrockexplorer.com/@lifeboat/advanced-roleplay) | Ask author / verify terms |
| **Bicycles by Four**<br><sub>Bedrock / Marketplace reference · score 5.45</sub> | Bedrock<br>CurseForge Bedrock<br>Any / varies | Craft, dye and pick up a bicycle; no experiments and achievement-friendly positioning. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://www.curseforge.com/minecraft-bedrock/addons/bicycles-by-four) | Ask author / verify terms |
| **School City - Chillcraft**<br><sub>Bedrock / Marketplace reference · score 5.45</sub> | Bedrock<br>Marketplace / Chillcraft<br>Any / varies | Skateboard, scooter and bicycle traversal inside a city environment. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://chunk.gg/en/@chillcraft/school-city) | Ask author / verify terms |
| **Suburbia - Project Moonboot**<br><sub>Bedrock / Marketplace reference · score 5.45</sub> | Bedrock<br>Marketplace / Project Moonboot<br>Any / varies | Bike traversal embedded in a broader roleplay/city environment. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://chunk.gg/en/@project-moonboot/suburbia) | Ask author / verify terms |
| **Tynker Skate Add-ons**<br><sub>Bedrock / Marketplace reference · score 4.55</sub> | Bedrock<br>Community add-ons<br>Any / varies | Small experimental rideable skateboard implementations; useful as breadth reference. | Cross-edition reference for controls, trick vocabulary, presentation, progression and world design. Mine the interaction idea, not the platform limitation. | [Project](https://www.tynker.com/minecraft/skate/addons/) | Ask author / verify terms |

---

**32 entries preserved from the earlier catalogue.** Inclusion records research value, not bundled code/assets or automatic redistribution permission. Return to **[Inspiration & Research](Inspiration-and-Research)**.
