# Timon Client 2 — verificatie

Lokale controles: 26–27 september 2026. JDK 25.0.4.1, Gradle 9.6.0, Fabric Loom 1.17.21, Loader 0.19.5 en Fabric API 0.161.0 voor iedere Minecraft-versie. Platform: Windows, Nvidia/OpenGL, standaard Minecraft-renderer met Fabric Indigo.

| Controle | 26.2 | 26.3 |
|---|---|---|
| Compilatie, jar en toegangswijzigingen valideren | Geslaagd | Geslaagd |
| Echte client opstarten met mixins | Geslaagd | Geslaagd |
| Oorspronkelijk Meteor-menu, module-instellingen en blokselector renderen | Geslaagd | Geslaagd |
| Alleen negen modules in menu, zoeken en commando's; overige modules kunnen niet aan | Geslaagd | Geslaagd |
| Alle negen modules activeren en deactiveren | Geslaagd | Geslaagd |
| Cheats uit en speler expliciet geen operator | Geslaagd | Geslaagd |
| F8 via echte Minecraft-invoer schakelt alle modules uit | Geslaagd | Geslaagd |
| Auto Eat: voedsel neemt toe, geselecteerde hotbarslot en gebruiktoets hersteld | Geslaagd | Geslaagd |
| Flight: opstijgen in Survival, vliegrechten hersteld bij uitschakelen | Geslaagd | Geslaagd |
| Air Place: echte blokplaatsing door geïntegreerde server bevestigd | Geslaagd | Geslaagd |
| Xray: ingesloten diamond ore zichtbaar; steen onzichtbaar of gedeeltelijk transparant | Screenshot + codecontrole | Screenshot + codecontrole |
| Xray: blacklist, whitelist en opacity worden toegepast en via NBT bewaard/herladen | Geslaagd | Geslaagd |
| No Fall: 18 blokken vallen, servergezondheid blijft 20 | Geslaagd | Geslaagd |
| Elytra Fly: echte elytra, starten met glijden en horizontale besturing | Geslaagd | Geslaagd |
| Wereld normaal afsluiten, opslaan en terug naar titelscherm | Geslaagd | Geslaagd |

De laatste testversie zet cheats uit, verwijdert de testspeler uit de operatorlijst, controleert expliciet dat de speler geen OP is, en bedient F8 via de echte Minecraft-invoer. De buildlog bevat bij slagen de melding `All Timon module assertions passed without OP`.

De test draait in een apart profiel en een tijdelijke geïntegreerde vanilla-survivalwereld. Alleen de testfixture gebruikt serverconsole-opdrachten om steen/erts te plaatsen; de mod voert geen OP-commando's uit. Alle assertions moeten slagen voordat de build door kan naar publiceren.

Bewijs staat in `verification/v2/26.2/` en `verification/v2/26.3/` (screenshots). Lokale volledige logs worden niet ingecheckt. GitHub Actions bewaart de buildlogs en uploadt testbewijs bij elke run. Oudere screenshots in `verification/26.2/` en `verification/26.3/` horen bij v1, niet bij deze interface.

## Grenzen

Dit is geen test tegen externe anticheat, Paper, server Anti-Xray, Vulkan of alle combinaties van extra mods en instellingen. Fast Use en Anti Hunger zijn gecompileerd, geladen, geactiveerd en in de broncode gecontroleerd, zonder afzonderlijke meting van gebruikssnelheid of voedseluitputting. Auto Clicker is geladen en op activeren/deactiveren gecontroleerd; hier is geen geautomatiseerde gevechtstest met iedere klikmodus. Elytra's Packet/Pitch40/Bounce, alle No Fall-plaatsingsmodi en optionele Sodium/Iris/Baritone-integraties zijn niet afzonderlijk doorlopen.

De oorspronkelijke modulecode en instellingen zijn behouden; toekomstige upstream-versies kunnen afwijken. Op een server kan een ogenschijnlijk werkende clientactie later nog gecorrigeerd worden.

## Testfixture voor afsluiten

`src/gametest/.../IntegratedServerShutdownMixin.java` voorkomt een deadlock tussen Fabric's testphaser en Minecraft's blokkerende LAN-speleropruiming bij afsluiten. Alleen in de privé-testwereld wordt die opruiming ingepland in plaats van erop te blokkeren. De server stopt en slaat de wereld verder normaal op. Deze hulpcode wordt uitsluitend in de testmod geladen en komt niet in de distributiejar.

Verwachte ontwikkelwaarschuwingen over Windows-performancecounters, een ongeldig oud testprofieloptieveld en de niet aangemelde testaccount veroorzaakten geen testfouten.
