package nl.timon.liteclient.modules;

/** Fixed 20 Hz accumulator: even spacing without rounding 8 CPS down to 6. */
public final class ClickClock {
    private int credit;
    public void reset() { credit = 0; }
    public boolean tick(int clicksPerSecond) {
        credit += Math.clamp(clicksPerSecond, 1, 20);
        if (credit < 20) return false;
        credit -= 20;
        return true;
    }
}
