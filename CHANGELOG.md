# Wijzigingen

## 2.0.0

- Zelfstandige Fabric-distributie op basis van de oorspronkelijke Meteor-code voor Minecraft Java 26.2 en 26.3.
- De oorspronkelijke Meteor-interface met categorievensters, zoeken, rechtsklikinstellingen, schuifregelaars, blok-/itemlijsten, sneltoetsen, favorieten en profielen.
- Negen beschikbare modules: Flight, Elytra Fly, Fast Use, Auto Clicker, Xray, Auto Eat, Anti Hunger, No Fall en Air Place, met hun upstream-instellingen.
- Extra Xray-listmodus: afzonderlijk bewaarde whitelist en blacklist. Opacity 0–255, vloeistofkeuze en Exposed Only blijven beschikbaar.
- Eigen configuratiemap `timon-client`, F8 om alle modules uit te schakelen, Timon-naam en GitHub-verwijzingen.
- Andere module-instanties blijven intern aanwezig voor gedeelde hooks; activeren en zoeken is geblokkeerd. Clientcommando's beperkt tot module-/instellingenbeheer.
- Meteor-spelerteller en automatisch ophalen van Meteor-capes uitgeschakeld; Discord Presence wordt niet gestart.
- GPL-3.0-broncode, credits, versiehandleiding en GitHub Actions. Iedere geslaagde main-build krijgt een eigen ontwikkelrelease; `v...`-tags krijgen een gewone release.

## 1.0.0

- Eerste onafhankelijke MIT-implementatie van de negen modules, met eenvoudig menu en beperkte instellingen.
- Aparte jars en survivaltest voor 26.2 en 26.3.
- Bewaard als oudere versie; deze bevat nog niet de Meteor-interface of uitgebreide Xray-instellingen.
