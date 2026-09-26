package nl.timon.liteclient;

/** The complete, deliberately small module list shown by the client menu. */
public enum ModuleId {
    FLIGHT("Flight", "Vliegen met WASD, springen omhoog en sluipen omlaag. De server kan beweging terugzetten of je kicken; zonder servertoestemming is vliegen niet gegarandeerd."),
    ELYTRA_FLY("Elytra Fly", "Bestuur je vlucht met WASD, springen en sluipen terwijl je met een elytra zweeft. Je hebt een bruikbare elytra nodig. De server kan snelheid en beweging begrenzen."),
    FAST_USE("Fast Use", "Verkort de clientvertraging tussen herhaald gebruiken van items. Houd gebruiken ingedrukt. Dit versnelt geen eten, boogladen of cooldowns die de server bepaalt."),
    AUTO_CLICKER("Autoclicker", "Herhaalt aanvallen of gebruiken met de ingestelde snelheid. Standaard alleen terwijl je de bijbehorende muisknop vasthoudt. Aanvalscooldowns blijven gelden."),
    XRAY("Xray", "Verbergt gewone blokken zodat ertsen zichtbaar worden. Kan alleen blokdata tonen die de server verstuurt; server-side anti-xray kan ertsen verbergen."),
    AUTO_EAT("Auto Eat", "Kiest eten uit je hotbar wanneer je voedselbalk laag wordt en zet daarna je vorige slot terug. Je moet geschikt eten bij je hebben."),
    ANTI_HUNGER("Anti Hunger", "Probeert uitputting door beweging te verminderen met aangepaste bewegingsberichten. De server bepaalt je voedselbalk; dit voorkomt niet alle honger."),
    NO_FALL("No Fall", "Probeert valschade te voorkomen met aangepaste bewegingsberichten. Moderne servers en anticheat kunnen dit weigeren. Beschouw dit nooit als gegarandeerde bescherming."),
    AIR_PLACE("Air Place", "Probeert met een blok in je hand en gebruiken ingedrukt een blok in de lucht te plaatsen. De server controleert bereik en plaatsing en kan de poging weigeren.");

    private final String displayName;
    private final String description;

    ModuleId(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }
}
