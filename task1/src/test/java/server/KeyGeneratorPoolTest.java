package server;

import org.junit.jupiter.api.Test;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
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

    @Test
    public void testKeyBitLengthIs8192() throws InterruptedException {
        KeyGeneratorPool pool = new KeyGeneratorPool(1, 1);
        KeyPair kp = pool.takeKey();
        RSAPrivateKey rsaPriv = (RSAPrivateKey) kp.getPrivate();
        assertEquals(8192, rsaPriv.getModulus().bitLength());
    }

    @Test
    public void testMultipleKeysAreUnique() throws InterruptedException {
        KeyGeneratorPool pool = new KeyGeneratorPool(2, 2);
        KeyPair kp1 = pool.takeKey();
        KeyPair kp2 = pool.takeKey();
        assertNotEquals(kp1.getPublic(), kp2.getPublic());
    }
}
