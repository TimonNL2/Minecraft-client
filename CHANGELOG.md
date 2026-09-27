# Changelog

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
