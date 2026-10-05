package server;

import org.junit.jupiter.api.Test;
import java.security.KeyPair;
import static org.junit.jupiter.api.Assertions.*;

public class KeyGeneratorPoolTest {
    @Test
    public void testKeyGeneration() throws InterruptedException {
        KeyGeneratorPool pool = new KeyGeneratorPool(1, 1);
        KeyPair kp = pool.takeKey();
        assertNotNull(kp);
        assertNotNull(kp.getPrivate());
        assertNotNull(kp.getPublic());
        assertEquals("RSA", kp.getPrivate().getAlgorithm());
    }
}
