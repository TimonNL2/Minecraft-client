# Timon Client

Zelfstandige Fabric-clientmod met alleen Flight, Elytra Fly, Fast Use, Autoclicker, Xray, Auto Eat, Anti Hunger, No Fall en Air Place. Eigen implementatie, zonder Meteor-afhankelijkheid of overgenomen Meteor-code. Minecraft **Java Edition 26.2 en 26.3**, met een apart jar-bestand per versie.

## Installeren

1. Maak in je launcher een Fabric-profiel voor **26.2** of **26.3** met **Fabric Loader 0.19.5 of nieuwer**. Beide Minecraft-versies gebruiken Java 25. [Fabric installeren](https://fabricmc.net/use/installer/).
2. Zet uit `dist` **alleen de Timon Client-jar voor jouw Minecraft-versie** in de `mods`-map van dat profiel.
3. Voeg **Fabric API 0.161.0 voor dezelfde Minecraft-versie** toe. Officiële downloads: [26.2](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.161.0+26.2/fabric-api-0.161.0+26.2.jar) / [26.3](https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.161.0+26.3/fabric-api-0.161.0+26.3.jar).
4. Start dit profiel. Gebruik voor deze eerste versie een profiel zonder Meteor, Sodium en Iris. Xray gebruikt de standaard Minecraft-renderer en kan niet worden ingeschakeld als Sodium/Iris geladen is.

De mod wordt alleen op jouw client geïnstalleerd. Je hebt geen OP-rechten nodig. Hij vraagt geen servercommando's aan en wijzigt geen serverinstellingen.

## Bediening

- **Rechter Shift**: opent het menu in de wereld. **Escape** of **Klaar** sluit het.
- **F8**: zet alle modules direct uit.
- Klik bij **Modules** op een knop om die module aan of uit te zetten. Houd de muis op een knop voor uitleg.
- Bij **Instellingen** klik je om door waarden te bladeren. Op een klein scherm gebruik je Vorige/Volgende.
- Je kunt de twee sneltoetsen wijzigen bij Minecraft → Opties → Besturing → Toetsen → Timon Client.

Instellingen worden opgeslagen in `config/timonclient.json`. Modules starten uit en worden uitgeschakeld bij uitloggen, overlijden of een nieuwe spelerinstantie. Automatische klik- en eetacties stoppen in menu's en wanneer Minecraft niet actief is. Flight blijft in een menu zweven; bewegingstoetsen worden daar niet gebruikt. Flight en Elytra Fly zijn wederzijds exclusief.

## Wat iedere module doet

| Module | Gebruik | Grenzen |
|---|---|---|
| Flight | WASD, springen omhoog, sluipen omlaag. Instelbare snelheid en kleine periodieke daling. | Servercorrecties, bewegingscontroles en de standaard vliegende-spelerkick kunnen blijven optreden. |
| Elytra Fly | Trek een bruikbare elytra aan, begin normaal te zweven en bestuur met WASD/jump/sneak. | Verbruikt normale elytra-duurzaamheid; snelheid en vlucht zijn serverafhankelijk. |
| Fast Use | Houd gebruiken ingedrukt; kies 0–4 ticks vertraging. | Verkort de clientpauze tussen gebruikspogingen, niet de duur van eten, boogladen of servercooldowns. |
| Autoclicker | 1–20 CPS, aanvallen of gebruiken. Standaard moet je de bijbehorende knop vasthouden. | Aanvalscooldown en mining-snelheid blijven gelden. Pauzeert tijdens gebruik van een item en Auto Eat. |
| Xray | Verbergt gewone blokken en vloeistof; toont ertsen, ancient debris en raw iron/copper/gold blocks op volle helderheid. | Alleen de standaard renderer. Kan door de server verborgen/vervangen ertsen niet terughalen. Block entities en entities blijven zichtbaar. |
| Auto Eat | Eten uit hotbar of offhand bij de ingestelde voedselwaarde; herstelt de vorige hotbarselectie. | Heeft echt voedsel nodig. Slaat golden apples, enchanted golden apples, chorus fruit en een aantal schadelijke/onvoorspelbare etenswaren over. |
| Anti Hunger | Vermindert bepaalde bewegingsuitputting via sprint- en bewegingsberichten. | Voorkomt geen honger door alle oorzaken. Werkt niet bij elke serverimplementatie. |
| No Fall | Past de grondstatus in uitgaande bewegingsberichten aan tijdens een val. | Serverafhankelijk; geen gegarandeerde valbescherming. Slaat elytra, vloeistof, voertuigen en mace in de hoofdhand over. |
| Air Place | Houd een blok vast en kijk in de lucht; gebruik de gebruikknop. Bereik en interval instelbaar. | Normaal bereik en echte blokvoorraad; server kan de plaatsing weigeren. |

Survival wordt ondersteund; de automatisering wijzigt geen Creative- of Spectator-spelgedrag. De server blijft beslissen over beweging, honger, schade, itemgebruik en plaatsing. Geen clientmod kan voor elke server beloven dat alle negen functies zonder OP werken. Als de standaard flightkick op de server van je maat actief is, kan de beheerder `allow-flight=true` instellen; dat geeft spelers geen OP of Creative-vliegrechten. [Paper server properties](https://docs.papermc.io/paper/reference/server-properties/#allow-flight). [Uitleg over server Anti-Xray](https://docs.papermc.io/paper/anti-xray/).

## Zelf bouwen

Vereist JDK 25. De meegeleverde Gradle-wrapper downloadt Gradle 9.6.0. Dit project gebruikt Fabric Loom 1.17.21, Loader 0.19.5 en Fabric API 0.161.0.

```powershell
.\build-all.ps1
# Of één versie, met JAVA_HOME ingesteld op een JDK 25:
.\gradlew.bat -Pminecraft_version=26.3 collectRelease
```

De kant-en-klare mod-jars verschijnen in `dist`. Broncode-jars en testrapporten staan apart onder `build/26.2` en `build/26.3`. Installeer geen `-sources.jar`.

```powershell
# Tests in echte Minecraft-clients met tijdelijke testwerelden:
.\build-all.ps1 -GameTests
```

De testmod onder `src/gametest` wordt niet in de uitgeleverde jar opgenomen. Bouwcache, ontwikkelruntime en referentiebestanden zijn lokale ontwikkelbestanden en horen niet in je mods-map. De broncode staat onder MIT; Minecraft en Fabric behouden hun eigen licenties.

## Controle

De builds en tests zijn uitgevoerd op Java 25 met Fabric API 0.161.0. De speltest start een echte Minecraft-client en een tijdelijke survivalwereld en controleert het laden van de mixins, menu en instellingen, modulewissels, Auto Eat en het terugzetten van de hotbar, Flight omhoog/zweven, een door de server bevestigde Air Place, Xray met een verborgen diamond ore, No Fall na een val van 18 blokken en Elytra Fly met een echte elytra. Xray is ook op screenshots visueel gecontroleerd.

De vier overige JUnit-tests controleren configuratieherstel, ongeldige waarden, alle kliktempo's en stoppen/hervatten zonder extra klikstoot. Autoclicker, Fast Use en Anti Hunger hebben geen afzonderlijke volledige gedragstest tegen een externe multiplayer-server. De server van je maat, anticheat en andere mods zijn niet getest. Het volledige testverslag staat in `verification/RESULTATEN.md`.
