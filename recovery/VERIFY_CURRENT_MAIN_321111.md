# JetSetCraft current-main recovery verification

Base product commit: `32111151889acbd6fab610ac3ae55d07e245cdda`.

This branch exists only to verify the exact current product source after the final Forge `LazyOptional` diagnostics repair. It adds a CI-only exposed-edge probe and build-workflow assertion so the real dedicated Forge server proves:

- internal full-block seams are rejected;
- a real exposed ledge is discovered;
- blocked rider clearance is rejected;
- Forge reaches the dedicated-server ready state with no guarded JetSetCraft lifecycle/fatal errors.

The acceptance instrumentation is not intended for merge. Close this PR without merge after the exact head passes; canonical product source remains `main`.
