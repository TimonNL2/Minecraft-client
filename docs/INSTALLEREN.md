# Timon Client installeren

Download je mod bij [Releases](https://github.com/TimonNL2/Minecraft-client/releases). Elke release bevat een aparte jar voor Minecraft **Java 26.2** en **Java 26.3**. Kies de versie van je Minecraft-profiel. Een `-sources.jar` bevat broncode en hoort niet in je mods-map.

| Minecraft | Normale releasejar | Modloader | Extra mod | Java |
|---|---|---|---|---|
| 26.2 | `timon-client-2.0.0-mc26.2.jar` | Fabric Loader 0.19.5+ | Fabric API 0.161.0+26.2 | 25 |
| 26.3 | `timon-client-2.0.0-mc26.3.jar` | Fabric Loader 0.19.5+ | Fabric API 0.161.0+26.3 | 25 |

Bij ontwikkelbuilds staat daarnaast `dev.<buildnummer>` in de bestandsnaam. Nieuwere releases gebruiken hun eigen versienummer.

1. Installeer [Fabric](https://fabricmc.net/use/installer/) voor de gewenste Minecraft-versie.
2. Open de `mods`-map van dat profiel, op Windows normaal `%APPDATA%\.minecraft\mods`.
3. Verwijder de gewone Meteor-jar en eventuele oudere Timon-jar uit **dit profiel**. Timon 2 bevat zelf de Meteor-basis en kan niet samen met Meteor geladen worden.
4. Plaats één Timon Client-jar en [Fabric API](https://modrinth.com/mod/fabric-api) voor dezelfde Minecraft-versie in de map.
5. Start Minecraft via dat Fabric-profiel. Er is geen servermod, OP-recht of Creative-modus nodig om het clientmenu en de modules te gebruiken.

Gebruik eerst de standaard renderer. Optionele Sodium/Iris/Baritone-koppelingen zijn uit upstream overgenomen, maar alleen de combinatie zonder die extra mods wordt hier als getest aangeboden. Met Iris-shaders wordt Xray-opacity door upstream naar 0 geforceerd.

## Bediening

- **Rechter Shift:** Meteor-menu openen/sluiten.
- **Linkermuisknop op een module:** aan/uit.
- **Rechtermuisknop op een module:** instellingen openen.
- **F8:** alle modules uit. Deze toets kun je wijzigen bij Minecraft → Besturing.
- In **Bind** stel je een eigen sneltoets per module in.
- Vensters kun je verslepen; **Search** zoekt in modules en instellingen. **GUI** bevat de oorspronkelijke Meteor-thema-instellingen.
- **Profiles** bewaart groepen instellingen. De client gebruikt een eigen map `timon-client` binnen je Minecraft-profiel. De oude v1-config `config/timonclient.json` wordt niet automatisch geïmporteerd.

Moduletoestanden en instellingen worden zoals in Meteor opgeslagen. Controleer je actieve modules wanneer je een wereld of server opent. Met F8 kun je ze direct uitzetten.

## Xray

Open Xray met rechtsklikken. Kies bij **List Mode**:

- **Whitelist:** de blokken in **Whitelist** blijven normaal zichtbaar; andere blokken krijgen de ingestelde opacity.
- **Blacklist:** de blokken in **Blacklist** krijgen de ingestelde opacity; andere blokken blijven normaal zichtbaar.

Klik op een bloklijst om de oorspronkelijke Meteor-blokselector te openen. Je kunt blokken zoeken op naam of ID, toevoegen en verwijderen. Beide lijsten worden afzonderlijk bewaard, ook wanneer je van modus wisselt.

**Opacity** loopt van 0 (onzichtbaar) tot 255 (ondoorzichtig). **Fluid Opacity** kiest None, Water, Lava of Both. **Exposed Only** beperkt de normaal zichtbare selectie tot blootliggende blokken. Bij een wijziging wordt de wereld opnieuw opgebouwd; dat kan even duren.

## Survival en servergedrag

De modules sturen normale en aangepaste clientacties, zonder OP-commando's te gebruiken. De server bepaalt uiteindelijk beweging, schade, honger, bereik en itemgebruik. Flight, No Fall, Anti Hunger en Air Place kunnen daarom gecorrigeerd of geweigerd worden. Server Anti-Xray kan blokinformatie verbergen die geen client terug kan halen. Eten, elytra's, vuurwerk en te plaatsen blokken moeten echt in je inventaris zitten.

Gebruik op de server van je maat de instellingen die daar werken. Bij Air Place is bijvoorbeeld een Custom Range van **3** een bruikbaar begin binnen het normale survivalbereik. Fast Use verlaagt de clientvertraging, niet de serverduur van eten of aanvalscooldowns.
