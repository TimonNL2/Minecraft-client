# Attribution and source

Vesper Client (previously Timon Client) is a modified distribution of [Meteor Client](https://github.com/MeteorDevelopment/meteor-client), copyright Meteor Development, licensed under GNU GPL version 3. The original copyright headers and GPL license are retained. This is an independent project, not an official Meteor release.

Upstream snapshots:

- Minecraft 26.2: `5f274f542342d2525bd2c32b9ce677b4ccddc527` (master).
- Minecraft 26.3: `7a0c4032dcdac91af3968ff8c96de5fb096eba3d` (mc-update), adapted to the final release.

The shared source is in `src/main`; changed 26.3 files are in `src/mc26.3`. `removed.txt` lists files removed by that port. Version-specific source jars contain the effective source for that target. Complete buildable source, including Gradle scripts and both targets, is in this repository and each release's source archive.

Vesper modifications by TimonNL2 (2026): restrict available modules to the requested nine, add Xray blacklist selection, separate configuration directory, emergency disable key, Sodium compatibility fixes, removal of main-menu branding, packaging and release automation. Supporting upstream module classes remain internally for compatibility with Meteor's shared rendering and movement hooks, but cannot be enabled or selected.

Bundled libraries keep their original licenses in their nested jars. See `build.gradle` for exact dependencies. Comfortaa and JetBrains Mono use the SIL Open Font License; notices are in `licenses/` and included in the jar. Other upstream bundled fonts are omitted. Meteor's default Comfortaa font and original UI are retained. Minecraft itself is not included.

The earlier independent implementation remains available at tag `v1.0.0` under its original MIT license.
