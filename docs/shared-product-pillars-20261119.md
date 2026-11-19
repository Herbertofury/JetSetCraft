# Local requirements and shared-principle adoption

Recorded 2026-11-19. Accepted requirements and open implementation backlog, not delivered parity. Preserve existing source, project-specific instructions, active work and acceptance holds.

Canonical [Athena product pillars and adoption register](https://github.com/Herbertofury/Athena/blob/main/docs/PRODUCT-PILLARS.md).

## Scope boundary

Athena's central pillars are universal engineering principles, not a feature checklist imposed on every repository. The requirements below belong to this project/domain only. Other projects consume them only where explicitly relevant; a link is not an integration mandate. Keep project-specific goals and acceptance here, with central navigation linking to them.

## Relevant local scope

Preserve Adventure Mode, Rival Rush, existing crews and each mode's mechanics. Coliseum allows the user to jump in at any time for fun, with substantially harder sequential rounds against multiple gangs. Deep replayable progression/endgame and meaningful cross-mode rewards need anti-duplication, interruption/rejoin, death/abandonment and original-world continuity tests. Creatively reuse licensed/original assets, animation, music references and saved resources without sacrificing requested quality or depth; soundtrack links are not permission to bundle audio. Preserve each crew's identity and existing art direction; black-default UI does not recolor the game world. Prefer event-driven bounded session/resource work; required game ticks and animation are not redundant polling. Portable tool-owned data must respect Minecraft's selected instance/world paths and server authority.

## Acceptance and maintenance backlog
- [ ] Reconcile current authoritative source and active work before implementing; existing private or local work may exceed public main.
- [ ] Map applicable pillars to existing owners and tests. Preserve one authoritative state/transaction path; avoid duplicated schedulers, schemas and writers.
- [ ] Verify relocation/restart, lost events, cancellation, stale results, privacy, accessibility and complete results at representative workload scale where relevant.
- [ ] Record implementation revision, fixture/build evidence, actual native/game acceptance and deployed state separately. A saved requirement is not implemented functionality.
- [ ] Carry future owner corrections into this local backlog and the central register without overwriting more specific requirements.

No source execution, installation, schedule/notification activation, source deletion or owner-data migration is authorized or performed by this documentation update.
