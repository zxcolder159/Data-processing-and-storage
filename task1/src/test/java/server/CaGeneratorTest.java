package server;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import static org.junit.jupiter.api.Assertions.*;

public class CaGeneratorTest {
    @Test
    public void testCaGenerator() throws Exception {
        File tempFile = File.createTempFile("ca_key", ".key");
        tempFile.deleteOnExit();

        CaGenerator.main(new String[]{tempFile.getAbsolutePath()});

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);
        
        byte[] keyBytes = Files.readAllBytes(tempFile.toPath());
        assertNotNull(keyBytes);
        assertTrue(keyBytes.length > 500); 
    }

    @Test
    public void testCaKeyIsValidPkcs8() throws Exception {
        File tempFile = File.createTempFile("ca_key_spec", ".key");
        tempFile.deleteOnExit();

        CaGenerator.main(new String[]{tempFile.getAbsolutePath()});

        byte[] keyBytes = Files.readAllBytes(tempFile.toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey pk = kf.generatePrivate(spec);
        assertNotNull(pk);
        assertEquals("RSA", pk.getAlgorithm());
    }

    @Test
    public void testCaGeneratorOverwritesExistingFile() throws Exception {
        File tempFile = File.createTempFile("ca_key_overwrite", ".key");
        tempFile.deleteOnExit();

        CaGenerator.main(new String[]{tempFile.getAbsolutePath()});
        long len1 = tempFile.length();

        CaGenerator.main(new String[]{tempFile.getAbsolutePath()});
        long len2 = tempFile.length();

        assertTrue(len1 > 0 && len2 > 0);
    }
}
