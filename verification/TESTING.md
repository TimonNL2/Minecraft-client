# Vesper Client 2.0.1 — verification

Local verification: **27 September 2026**, Windows 11, Nvidia/OpenGL, JDK 25.0.4.1, Gradle 9.6.0, Fabric Loom 1.17.21, Loader 0.19.5 and Fabric API 0.161.0 for each target.

| Minecraft | Renderer | Compile/package | Real client survival test |
|---|---|---|---|
| 26.2 | Vanilla + Fabric Indigo | Passed | Passed |
| 26.2 | Sodium 0.9.1 | Passed | Passed |
| 26.3 | Vanilla + Fabric Indigo | Passed | Passed |
| 26.3 | Sodium 0.9.3-alpha.1 | Passed | Passed |

The test explicitly checks that the requested renderer is loaded. All four runs reached `All Vesper module assertions passed without OP` and completed successfully. Screenshots are in [vesper/](vesper/); raw local logs are not committed. GitHub Actions retains logs and uploads test evidence for each run.

## What is checked

- A real Minecraft client starts and applies the mixins.
- The original module menu, Xray settings and block selector render correctly.
- Minecraft's title screen has no client credit overlay or promotional splash text.
- Exactly nine modules are available through the menu, search and commands; hidden supporting modules cannot be enabled.
- Every available module can activate and deactivate.
- Cheats are disabled, the test player is removed from the operator list, and the server confirms they are not an operator.
- Auto Eat increases food, finishes eating, and restores the previous hotbar selection and use key.
- Flight lifts a Survival player and restores normal flight abilities on disable.
- Air Place places a real block confirmed by the integrated server.
- Xray renders enclosed ore while stone is invisible or partly transparent; whitelist/blacklist settings survive saving and loading.
- Enclosed water and lava render with each fluid-opacity mode (None, Water, Lava, Both), including the Sodium path responsible for the reported join crash.
- No Fall survives an 18-block fall with server health remaining at 20.
- Elytra Fly uses a real elytra, starts gliding and responds to horizontal movement input.
- F8 is pressed through Minecraft's input system and disables every active module.
- The test world closes and saves normally, returning to the title screen.

## Sodium regression

The reported 2.0.0 crash came from `SodiumDefaultFluidRendererMixin`: it targeted a four-argument `isFluidSideExposed` overload removed in Sodium 0.9.3-alpha.1. A local-variable capture also produced invalid transformed bytecode. The repaired mixin targets the world/position overload shared by 0.9.1, 0.9.2 and 0.9.3-alpha.1, and reads the neighboring block directly.

The supplied Sodium 0.9.3-alpha.1 jar was inspected locally and the matching published version is pinned in the build. Sodium 0.9.2's method signatures were inspected, but it is not claimed as a separately completed runtime test.

## Test boundaries

Tests use a separate profile and temporary integrated vanilla Survival world. Only the test fixture uses server-console commands to create the stone/ore/fluid scene; the mod does not use OP commands. The user's launcher profile, server and existing worlds are not modified by these tests.

This does not test external anticheat, Paper, server Anti-Xray, Iris shaders, Vulkan, or every combination of additional mods and settings. Fast Use and Anti Hunger are loaded, activated and source-reviewed without separately measuring use timing or exhaustion. Auto Clicker is loaded and toggled, without a complete combat test for every click mode. Elytra Packet/Pitch40/Bounce, every No Fall placement mode and optional Baritone integrations are not individually exercised.

## Test shutdown helper

`src/gametest/.../IntegratedServerShutdownMixin.java` avoids a deadlock between Fabric's test phaser and Minecraft's blocking LAN-player cleanup. In the unpublished temporary test world, cleanup is queued rather than waited on synchronously. Server shutdown and world saving otherwise follow the normal path. This helper is only loaded in the test mod and is excluded from distributable jars.

Development warnings about Windows performance counters, an old test-profile option and unauthenticated test accounts did not cause test failures.

Earlier screenshots in `verification/26.2/` and `verification/26.3/` belong to the independent v1 implementation. `verification/v2/` contains the initial 2.0.0 vanilla-renderer checks, before the Sodium regression fix and Vesper rename.
