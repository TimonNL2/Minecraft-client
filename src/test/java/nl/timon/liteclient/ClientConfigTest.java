package nl.timon.liteclient;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientConfigTest {
    @Test void incompleteConfigPreservesUsableDefaults() {
        ClientConfig c = new Gson().fromJson("{\"eatAt\":12}", ClientConfig.class);
        c.sanitize();
        assertEquals(12, c.eatAt);
        assertEquals(0.8, c.flightSpeed);
        assertEquals(8, c.clicksPerSecond);
        assertTrue(c.clickHold);
    }
    @Test void malformedValuesCannotProduceInvalidMovementOrZeroTickIntervals() {
        ClientConfig c = new ClientConfig();
        c.flightSpeed = Double.NaN;
        c.elytraSpeed = Double.POSITIVE_INFINITY;
        c.airPlaceReach = -200;
        c.airPlaceDelay = 0;
        c.clicksPerSecond = Integer.MAX_VALUE;
        c.eatAt = 999;
        c.sanitize();
        assertEquals(0.8, c.flightSpeed);
        assertEquals(1.2, c.elytraSpeed);
        assertEquals(1, c.airPlaceReach);
        assertEquals(1, c.airPlaceDelay);
        assertEquals(20, c.clicksPerSecond);
        assertEquals(19, c.eatAt);
    }
}
