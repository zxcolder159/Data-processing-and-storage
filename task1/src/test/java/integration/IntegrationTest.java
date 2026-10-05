package integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import server.Server;
import client.Client;
import server.CaGenerator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {
    
    private int findFreePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    @Test
    public void testFullFlow() throws Exception {
        
        File caKeyFile = File.createTempFile("ca_key", ".key");
        caKeyFile.deleteOnExit();
        CaGenerator.main(new String[]{caKeyFile.getAbsolutePath()});


        int port = findFreePort();
        Thread serverThread = new Thread(() -> {
            try {

                Server.main(new String[]{String.valueOf(port), caKeyFile.getAbsolutePath(), "TestCA", "1"});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();


        Thread.sleep(2000);

        String clientName = "TestClientIntegration";
        File crtFile = new File(clientName + ".crt");
        File keyFile = new File(clientName + ".key");

        if (crtFile.exists()) crtFile.delete();
        if (keyFile.exists()) keyFile.delete();

        Client.main(new String[]{"localhost", String.valueOf(port), clientName});


        assertTrue(crtFile.exists(), "Certificate file not created");
        assertTrue(keyFile.exists(), "Key file not created");
        

        String crtContent = Files.readString(crtFile.toPath());
        assertTrue(crtContent.contains("BEGIN CERTIFICATE"), "Invalid CRT content");
        
        String keyContent = Files.readString(keyFile.toPath());
        assertTrue(keyContent.contains("BEGIN PRIVATE KEY"), "Invalid KEY content");

        crtFile.delete();
        keyFile.delete();
        serverThread.interrupt();
    }
}
