# Timon Client

[![Build](https://github.com/TimonNL2/Minecraft-client/actions/workflows/build.yml/badge.svg)](https://github.com/TimonNL2/Minecraft-client/actions/workflows/build.yml)
[![Downloads](https://img.shields.io/github/v/release/TimonNL2/Minecraft-client)](https://github.com/TimonNL2/Minecraft-client/releases)

Een zelfstandige Fabric-client voor **Minecraft Java 26.2 en 26.3**, met de oorspronkelijke Meteor-interface en alleen deze negen beschikbare modules:

**Flight · Elytra Fly · Fast Use · Auto Clicker · Xray · Auto Eat · Anti Hunger · No Fall · Air Place**

**[Download](https://github.com/TimonNL2/Minecraft-client/releases/latest)** · **[Installeren en gebruiken](docs/INSTALLEREN.md)** · **[Alle builds](https://github.com/TimonNL2/Minecraft-client/releases)** · **[Wijzigingen](CHANGELOG.md)**

Timon Client 2 is een aangepaste [Meteor Client](https://github.com/MeteorDevelopment/meteor-client)-distributie onder GPL-3.0. Hij bevat Meteor zelf; je installeert dus geen afzonderlijke Meteor-jar. Zie [credits en herkomst](NOTICE.md).

![Het menu met de negen modules](docs/images/menu.png)

## Download kiezen

| Je Minecraft-versie | Kies | Vereist |
|---|---|---|
| 26.2 | jar met `mc26.2` in de naam | Java 25, Fabric Loader 0.19.5+, Fabric API 0.161.0+26.2 |
| 26.3 | jar met `mc26.3` in de naam | Java 25, Fabric Loader 0.19.5+, Fabric API 0.161.0+26.3 |

Download de **normale jar**, niet de `-sources.jar`. Zet alleen de passende versie in je mods-map. Verwijder Meteor en oudere Timon-jars uit hetzelfde profiel. Uitgebreide stappen staan in [INSTALLEREN.md](docs/INSTALLEREN.md).

**Rechter Shift** opent het menu. Links klikken schakelt een module om, rechts klikken opent de instellingen. **F8** zet alles uit. Vensters, kleuren, lettertype, schaal, zoeken, favorieten, profielen en de sneltoetseditor komen uit Meteor.

## Modules en opties

| Module | Beschikbare instellingen |
|---|---|
| Flight | Abilities/Velocity, snelheid, verticale snelheid, No Sneak, anti-kick None/Normal/Packet, interval en duur. |
| Elytra Fly | Vanilla/Packet/Pitch40/Bounce, horizontale/verticale snelheid, automatisch opstijgen, versnelling, auto-hover, water-/chunk-/botsingscontroles, pitch/yaw, elytra wisselen, vuurwerk aanvullen en autopilot. |
| Fast Use | All/Some, itemlijst, blokkenfilter en cooldown in ticks. |
| Auto Clicker | Links en rechts afzonderlijk Disabled/Hold/Press, afzonderlijke vertragingen en klikken in schermen. |
| Xray | **Whitelist of blacklist**, aparte bewaarde bloklijsten, opacity 0–255, vloeistofkeuze en Exposed Only. |
| Auto Eat | Voedselblacklist, volledige inventaris doorzoeken, voedselprioriteit, gezondheid-/hongerdrempels en combinatiemodi. De oorspronkelijke pauzeopties zijn behouden. |
| Anti Hunger | Sprintpakket- en on-ground-opties afzonderlijk instelbaar. |
| No Fall | Packet/Place/AirPlace, keuze plaatsingsitem, plaatsingsmoment, centreren, Anti Bounce en pauzeren met een mace. |
| Air Place | Normaal/aangepast bereik, plaatsingsvoorbeeld, Shape Mode en kleuren voor vlakken en lijnen. |

De modulecode en de bijbehorende instellingen komen uit de vastgelegde Meteor-versies; de extra Xray-listmodus is voor Timon toegevoegd. Dit is geen belofte van identiek gedrag aan iedere toekomstige Meteor-versie. Opties voor optionele externe mods doen alleen iets als die mods aanwezig zijn.

Bij **Xray → List Mode → Whitelist** blijven geselecteerde blokken zichtbaar. Bij **Blacklist** worden juist de geselecteerde blokken transparant. De schuif **Opacity** bepaalt hoeveel je nog ziet van de overige/uitgesloten blokken. De blokselector ondersteunt zoeken op naam en blok-ID.

![Xray-instellingen](docs/images/xray-settings.png)

## Survival

De mod gebruikt geen OP-commando's en vraagt geen serverpermissies aan. Je hebt wel echte items nodig voor eten, elytra-vlucht en blokplaatsing. De server kan beweging, schade, honger en plaatsing controleren; vooral Flight, No Fall, Anti Hunger en Air Place blijven serverafhankelijk. Server Anti-Xray kan ertsen verbergen die de client niet ontvangt. Fast Use verkort geen door de server bepaalde eetduur of cooldowns.

De automatische speltest gebruikt een tijdelijke vanilla-survivalwereld. Hij controleert de interface, modulelijst, instellingen bewaren, Xray-rendering en daadwerkelijke beweging/plaatsing/eten/valschade. Extra renderers en alle combinaties van instellingen zijn niet volledig getest. Zie [testresultaten](verification/RESULTATEN.md).

## Iedere build op GitHub

- Een push naar **main** bouwt beide Minecraft-versies en draait de clienttests. Bij succes verschijnt een eigen **ontwikkelrelease** `build-<nummer>` met beide jars, passende sources-jars, volledige broncode, uitleg en SHA-256-controlesommen.
- Een tag **`v2.0.0`** (of een volgend versienummer) publiceert een gewone release. De tag moet overeenkomen met `mod_version` in `gradle.properties`.
- Pull requests worden gebouwd en getest; hun resultaten staan bij **Actions → Artifacts**. Mislukte builds worden niet als downloadrelease gepubliceerd.
- Jars en testresultaten zijn ook Actions-artifacts (respectievelijk 90 en 30 dagen). Gepubliceerde Releases blijven beschikbaar totdat je ze zelf verwijdert. Een lokale build wordt pas gepubliceerd wanneer de bijbehorende broncode naar GitHub wordt gepusht.

Je kunt de workflow ook starten via **Actions → Build and publish client → Run workflow**. Er is geen persoonlijke token als repository-secret nodig; publiceren gebruikt GitHub's tijdelijke `GITHUB_TOKEN`.

## Zelf bouwen

Installeer **JDK 25** en stel `JAVA_HOME` daarop in. Gradle 9.6.0 wordt met een vastgelegde checksum door de wrapper opgehaald.

```powershell
# Windows: beide versies
.\build-all.ps1
# Inclusief echte Minecraft-clienttests
.\build-all.ps1 -GameTests
# Eén versie
.\gradlew.bat -Pminecraft_version=26.3 collectRelease
```

```sh
# Linux/macOS: één versie
./gradlew -Pminecraft_version=26.2 collectRelease
# Linux zonder beeldscherm, met Xvfb en Mesa geïnstalleerd
xvfb-run -a ./gradlew -Pminecraft_version=26.3 runClientGameTest
```

Jars verschijnen in `dist/`. Tests gebruiken `build/run/clientGameTest`, geen bestaande Minecraft-wereld of launcherprofiel.

## Projectstructuur

- `src/main`: gemeenschappelijke Meteor-bron met Timon-aanpassingen.
- `src/mc26.3`: alleen afwijkende bestanden voor 26.3; deze vervangen hun 26.2-equivalent tijdens bouwen.
- `src/gametest`: integratietest en afgeschermde testhulpmiddelen; deze komen **niet** in de mod-jar.
- `gradle/`, `gradlew`, `gradlew.bat`: de Gradle-wrapper.
- `.github/workflows/build.yml`: bouwen, testen en releases publiceren.
- `docs/`, `CHANGELOG.md`, `NOTICE.md`, `LICENSE`, `licenses/`: uitleg, wijzigingen en licenties.

Intern blijven ondersteunende Meteor-klassen aanwezig om gedeelde rendering- en bewegingshooks intact te houden. Alleen de negen genoemde modules zijn zichtbaar, worden opgeslagen en kunnen geactiveerd worden. Configuratie staat apart in de map `timon-client` van je Minecraft-profiel. De eerdere eigen implementatie blijft op tag **v1.0.0** beschikbaar.
