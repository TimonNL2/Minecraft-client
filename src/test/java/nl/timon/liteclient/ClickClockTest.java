package nl.timon.liteclient;

import nl.timon.liteclient.modules.ClickClock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClickClockTest {
    @Test void everySupportedRateIsCorrectAcrossOneSecond() {
        for (int rate = 1; rate <= 20; rate++) {
            ClickClock clock = new ClickClock();
            int clicks = 0;
            for (int tick = 0; tick < 20; tick++) if (clock.tick(rate)) clicks++;
            assertEquals(rate, clicks, "Wrong CPS for " + rate);
        }
    }
    @Test void pausingCannotAccumulateBurstClicks() {
        ClickClock clock = new ClickClock();
        clock.tick(8);
        clock.tick(8);
        clock.reset();
        assertFalse(clock.tick(8));
        assertFalse(clock.tick(8));
        assertTrue(clock.tick(8));
    }
}
