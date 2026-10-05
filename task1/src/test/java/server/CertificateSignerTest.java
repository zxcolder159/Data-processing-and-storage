package server;

import org.junit.jupiter.api.Test;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import static org.junit.jupiter.api.Assertions.*;

public class CertificateSignerTest {
    @Test
    public void testSignCertificate() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair caKeyPair = kpg.generateKeyPair();
        KeyPair clientKeyPair = kpg.generateKeyPair();

        CertificateSigner signer = new CertificateSigner(caKeyPair.getPrivate(), "Test CA");
        byte[] certBytes = signer.signCertificate("Test Client", clientKeyPair);

        assertNotNull(certBytes);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
        
        assertEquals("CN=Test CA", cert.getIssuerX500Principal().getName());
        assertEquals("CN=Test Client", cert.getSubjectX500Principal().getName());
        cert.verify(caKeyPair.getPublic());
    }
}
