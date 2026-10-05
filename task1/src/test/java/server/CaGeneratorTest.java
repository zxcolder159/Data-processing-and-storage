package server;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Files;
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
}
