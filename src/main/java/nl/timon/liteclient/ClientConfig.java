package nl.timon.liteclient;

/** Persisted preferences. Module activation deliberately only lasts for the current session. */
public final class ClientConfig {
    public double flightSpeed = 0.8;
    public double elytraSpeed = 1.2;
    public int fastUseDelay = 0;
    public int clicksPerSecond = 8;
    public int eatAt = 16;
    public int airPlaceDelay = 4;
    public double airPlaceReach = 3.0;
    public boolean clickRight = false;
    public boolean clickHold = true;
    public boolean flightAntiKick = true;

    public void sanitize() {
        flightSpeed = finiteClamp(flightSpeed, 0.1, 3.0, 0.8);
        elytraSpeed = finiteClamp(elytraSpeed, 0.1, 4.0, 1.2);
        fastUseDelay = Math.clamp(fastUseDelay, 0, 4);
        clicksPerSecond = Math.clamp(clicksPerSecond, 1, 20);
        eatAt = Math.clamp(eatAt, 1, 19);
        airPlaceDelay = Math.clamp(airPlaceDelay, 1, 20);
        airPlaceReach = finiteClamp(airPlaceReach, 1.0, 4.5, 3.0);
    }

    private static double finiteClamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }
}
