# Install Vesper Client

Download from [GitHub Releases](https://github.com/TimonNL2/Minecraft-client/releases). Choose the normal jar matching your **Minecraft Java** version. Files ending in `-sources.jar` contain source code and must not go in your mods folder.

| Minecraft | Normal release jar | Loader | Additional mod | Java |
|---|---|---|---|---|
| 26.2 | `vesper-client-2.0.1-mc26.2.jar` | Fabric Loader 0.19.5+ | Fabric API 0.161.0+26.2 | 25 |
| 26.3 | `vesper-client-2.0.1-mc26.3.jar` | Fabric Loader 0.19.5+ | Fabric API 0.161.0+26.3 | 25 |

Development builds include `dev.<build number>` in their filenames. Later releases use their own version number.

1. Install [Fabric](https://fabricmc.net/use/installer/) for your Minecraft version.
2. Open that profile's `mods` folder. With the standard Windows launcher this is usually `%APPDATA%\.minecraft\mods`. In **Prism Launcher**, right-click your instance, select **Edit → Mods**, and open its mods folder.
3. Disable or remove the regular Meteor jar and older **Timon Client/Vesper Client** jars from this profile. Vesper includes Meteor's core and cannot load alongside a separate Meteor installation.
4. Add one Vesper jar and [Fabric API](https://modrinth.com/mod/fabric-api) for the same Minecraft version.
5. Launch the Fabric profile. No server mod, OP permission or Creative mode is needed to use the client.

## Updating from Timon Client 2.0.0

For 26.3, replace `timon-client-2.0.0-mc26.3.jar` with `vesper-client-2.0.1-mc26.3.jar`. Use the matching 26.2 jar for that version. Do not leave both enabled. Settings remain in the existing `timon-client` directory inside your Minecraft profile; no migration is required.

Version 2.0.1 fixes the Sodium 0.9.3-alpha.1 fluid-renderer crash when joining a world. You can enable that Sodium version again with the repaired Vesper jar. Other mods may introduce separate compatibility issues.

## Controls

- **Right Shift:** open or close the module menu.
- **Left-click a module:** enable or disable it.
- **Right-click a module:** open its settings.
- **F8:** disable every module. Change this binding in Minecraft's Controls screen.
- **Bind:** choose a keybind for an individual module.
- Drag category windows to move them. **Search** finds modules and settings; **GUI** contains the original Meteor theme options.
- **Profiles** stores groups of settings. The old v1 configuration, `config/timonclient.json`, is not imported automatically.

Module states and settings are saved as in Meteor. Check active modules when entering a world or server; F8 disables them immediately.

Minecraft's title screen shows no Vesper/Meteor credit overlay, client buttons or promotional splash text. Extra account/proxy controls are removed from the multiplayer list. The module menu remains accessible through Right Shift.

## Xray

Right-click Xray and choose **List Mode**:

- **Whitelist:** selected blocks remain visible; other blocks use the configured opacity.
- **Blacklist:** selected blocks use the configured opacity; other blocks remain visible.

Click a block list to open Meteor's block selector. Search by name or ID, then add or remove blocks. Both lists are saved separately when switching modes.

**Opacity** ranges from 0 (invisible) to 255 (opaque). **Fluid Opacity** selects None, Water, Lava or Both. **Exposed Only** limits the normally visible selection to exposed blocks. Changing settings rebuilds world geometry and may briefly take time.

Vanilla/Indigo and the pinned Sodium versions are covered by client tests. Iris/shaders, Vulkan and every possible mod combination are not covered; upstream forces Xray opacity to 0 when Iris shaders are active.

## Survival and server behavior

The modules use client actions without OP commands. The server ultimately controls movement, damage, hunger, reach and item use. Flight, No Fall, Anti Hunger and Air Place may be corrected or rejected. Server Anti-Xray may withhold ore information. Food, elytras, fireworks and blocks must actually be in your inventory.

Use settings accepted by your server. Air Place's Custom Range of **3** is a reasonable starting point within normal survival reach. Fast Use reduces client delay, not server-controlled eating duration or attack cooldowns.
