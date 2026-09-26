# Testresultaten — 26 september 2026

| Controle | Minecraft 26.2 | Minecraft 26.3 |
|---|---|---|
| Gradle build / jar | Geslaagd | Geslaagd |
| JUnit | 4 tests, 0 fouten | 4 tests, 0 fouten |
| Echte client start, alle mixins laden | Geslaagd | Geslaagd |
| Menu en instellingen, screenshots | Geslaagd | Geslaagd |
| Negen modules aan/uit, exclusieve vliegmodi | Geslaagd | Geslaagd |
| Auto Eat verhoogt voedselbalk en herstelt hotbar | Geslaagd | Geslaagd |
| Flight stijgt en zweeft | Geslaagd | Geslaagd |
| Air Place wordt door testserver geplaatst | Geslaagd | Geslaagd |
| Xray verbergt steen en toont ingesloten diamond ore | Visueel bevestigd | Visueel bevestigd |
| No Fall: 18 blokken vallen met 20 gezondheid na landing | Geslaagd | Geslaagd |
| Elytra Fly: echt glijden met elytra, daarna gestuurde beweging | Geslaagd | Geslaagd |
| Modules uit na disconnect; geen vastgehouden eet-input | Geslaagd | Geslaagd |
| Jar heeft juiste Minecraft-versie en bevat geen testmod | Geslaagd | Geslaagd |

Toolchain: Java 25.0.4.1, Gradle 9.6.0, Fabric Loom 1.17.21, Loader 0.19.5, Fabric API 0.161.0 per Minecraft-versie. Testplatform: Windows, standaard Minecraft-renderer met Fabric Indigo, OpenGL.

De speltests gebruiken aparte, tijdelijke singleplayerwerelden met een geïntegreerde vanilla-server en Survival als spelmodus. Testcode gebruikt servercommando's alleen om de testwereld op te bouwen; de uitgeleverde clientmod gebruikt geen servercommando's of OP-checks. De testmod staat buiten de release-jar.

Dit bevestigt geen werking tegen Paper, externe servervalidatie, anticheat, server Anti-Xray of andere mods. Autoclicker heeft een geteste 1–20 CPS-timer, maar geen volledige aanvalstest met een mob op een externe server. Fast Use en Anti Hunger zijn geladen en gecontroleerd in de broncode, zonder afzonderlijke end-to-end meting van itemtempo of uitputting. Vulkan, Sodium en Iris vallen buiten deze verificatie; Xray wordt bij Sodium/Iris geblokkeerd.

De ontwikkelclient meldt waarschuwingen over Windows-performancecounters en het niet ingelogde testaccount; deze veroorzaakten geen testfouten. De testclient gebruikt geen gebruikersaccount of bestaande Minecraft-werelden.

Bewijs: `26.2-game-test-console.log`, `26.3-game-test-console.log`, `26.3-build-console.log`, de per-versie `client-test.log` en screenshots in `26.2/` en `26.3/`. JUnit XML staat onder `build/<versie>/test-results/test/`. De definitieve SHA-256-hashes staan in `dist/SHA256SUMS.txt`.
