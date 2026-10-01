# Changelog

## 2.0.7 — Whitelist highlights for Minecraft 26.3

- Added optional colored boxes through walls around Xray-whitelisted blocks, including spawners. Choose Lines, Sides or Both, and any color and opacity.
- Configurable range and block limit keep dense whitelists manageable. Highlights follow whitelist edits, block updates and Xray's Exposed Only setting, and turn off in Blacklist mode.
- Scans loaded chunks incrementally and skips block palettes without matching blocks. Minecraft 26.2 remains unchanged.

## 2.0.6 — Xray search input for Minecraft 26.3

- Fixed text input in the Xray block selector by registering text-box focus with Minecraft 26.3's SDL text input manager.
- The same fix restores typing in other Vesper text fields on 26.3. Earlier releases and Minecraft 26.2 remain unchanged.

## 2.0.5 — Flight placement compatibility for Minecraft 26.3

- Fixed the "Invalid move player packet received" disconnect when Flight's Packet mode is used with EasyPlaceFix or Tweakeroo placement rotations.
- Flight now preserves rotation-only and status-only packets. It adjusts existing position packets only, retaining its normal scheduled position updates and settings.
- Added a client regression test covering placement rotations, status packets and the server's one-position-update-per-tick rule while Flight stays enabled.
- The Flight compatibility fix is restricted to Minecraft 26.3.

## 2.0.3 — Mace Spoof for Minecraft 26.3

- Added a standalone Mace Spoof module under Combat, adapted from Meteor's mace smash implementation. It only acts on attacks with a mace against living targets.
- Configurable simulated fall height from 1.501 to 100 blocks (default 10), with clearance checks and No Fall compatibility. Effectiveness depends on the server; higher settings do not guarantee one-hit damage.
- New builds and releases target Minecraft 26.3 only. All previous releases remain available, including Minecraft 26.2 in v2.0.1.


## 2.0.2 — Storage ESP and Auto Fish for Minecraft 26.3

- Enabled Storage ESP and Auto Fish only on Minecraft 26.3, bringing that version to eleven available modules.
- Minecraft 26.2 retains its original nine modules and behavior.
- Client smoke tests check the version-specific module count and availability, and toggle every available module.


## 2.0.1 — Vesper Client

- Renamed the client and download files to **Vesper Client**. Existing 2.0.0 settings continue to load from their original directory.
- Fixed the crash when joining worlds with Sodium 0.9.3-alpha.1 on Minecraft 26.3. Fluid hooks use the supported world/position overload and no longer capture a fragile local variable.
- Added Sodium client tests alongside vanilla/Indigo, including water and lava with every Xray fluid-opacity mode.
- Removed the client credit overlay, promotional splash text and client links from the main menu, plus extra account/proxy controls in the multiplayer list. The title screen uses Minecraft's normal window title.
- Repository documentation and release descriptions are now in English.

## 2.0.0 — Timon Client

- Standalone Fabric distribution based on Meteor's original source for Minecraft Java 26.2 and 26.3.
- Original Meteor interface: category windows, search, settings, sliders, block/item lists, keybinds, favorites and profiles.
- Nine available modules: Flight, Elytra Fly, Fast Use, Auto Clicker, Xray, Auto Eat, Anti Hunger, No Fall and Air Place, retaining upstream settings.
- Added Xray whitelist/blacklist mode with separately saved lists. Preserved opacity 0–255, fluid selection and Exposed Only.
- Separate `timon-client` configuration directory and F8 emergency disable key.
- Retained supporting internal classes for shared hooks while blocking activation and search of other modules. Limited commands to module/configuration management.
- Disabled Meteor's online-player counter, automatic cape downloads and automatic Discord Presence activation.
- GPL-3.0 source, attribution, installation guide and GitHub build/release automation.
- Known issue: incompatible with Sodium 0.9.3-alpha.1; fixed in 2.0.1.

## 1.0.0

- Initial independent MIT implementation of the nine modules, with a simpler custom menu and fewer settings.
- Separate jars and survival tests for 26.2 and 26.3.
- Preserved as a legacy release; it does not include the original Meteor interface or expanded Xray settings.
