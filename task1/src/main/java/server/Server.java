package server;

import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.concurrent.*;

public class Server {
    private static KeyGeneratorPool keyPool;
    private static CertificateSigner certSigner;
    private static ExecutorService processingPool;
    
    private static final ConcurrentHashMap<String, CompletableFuture<KeyAndCert>> cache = new ConcurrentHashMap<>();
    private static final BlockingQueue<GenerationRequest> generationQueue = new LinkedBlockingQueue<>();
    private static final BlockingQueue<TransmissionRequest> transmissionQueue = new LinkedBlockingQueue<>();

    record KeyAndCert(byte[] privateKey, byte[] certificate) {}
    record GenerationRequest(String name, Socket clientSocket) {}
    record TransmissionRequest(Socket clientSocket, KeyAndCert result) {}

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Использование: ./gradlew runServer --args=\"<порт> <путь_к_ключу_ca> <имя_издателя> <кол_во_потоков>\"");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String caKeyPath = args[1];
        String issuerName = args[2];
        int numThreads = Integer.parseInt(args[3]);

        byte[] keyBytes = Files.readAllBytes(Paths.get(caKeyPath));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey caPrivateKey = kf.generatePrivate(spec);
        System.out.println("Ключ CA загружен. Издатель: " + issuerName);

        keyPool = new KeyGeneratorPool(20, numThreads); 
        certSigner = new CertificateSigner(caPrivateKey, issuerName);

        processingPool = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            processingPool.submit(Server::requestProcessingLoop);
        }

        for (int i = 0; i < 100; i++) {
            Thread.ofVirtual().start(Server::transmissionLoop);
        }

        System.out.println("Запуск сервера на порту " + port + "...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept();
                Thread.ofVirtual().start(() -> handleClientRead(client));
            }
        }
    }

    private static void handleClientRead(Socket client) {
        try {
            InputStream in = client.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = in.read()) != -1) {
                if (b == 0) break;
                baos.write(b);
            }
            String name = baos.toString(StandardCharsets.US_ASCII);
            if (!name.isEmpty()) {
                System.out.println("Получен запрос для имени: " + name);
                generationQueue.put(new GenerationRequest(name, client));
            } else {
                client.close();
            }
        } catch (Exception e) {
            System.err.println("Ошибка при чтении от клиента: " + e.getMessage());
            try { client.close(); } catch (Exception ignored) {}
        }
    }

    private static void requestProcessingLoop() {
        while (true) {
            try {
                GenerationRequest req = generationQueue.take();
                String name = req.name();
                
                CompletableFuture<KeyAndCert> f = new CompletableFuture<>();
                CompletableFuture<KeyAndCert> existing = cache.putIfAbsent(name, f);
                
                if (existing == null) {
                    System.out.println("Создание сертификата для " + name);
                    KeyPair kp = keyPool.takeKey();
                    byte[] cert = certSigner.signCertificate(name, kp);
                    
                    KeyAndCert result = new KeyAndCert(kp.getPrivate().getEncoded(), cert);
                    f.complete(result);
                    transmissionQueue.put(new TransmissionRequest(req.clientSocket(), result));
                } else {
                    System.out.println("Ожидание текущей генерации для " + name + " (асинхронно)");
                    existing.thenAccept(res -> {
                        try {
                            transmissionQueue.put(new TransmissionRequest(req.clientSocket(), res));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void transmissionLoop() {
        while (true) {
            try {
                TransmissionRequest req = transmissionQueue.take();
                try (Socket client = req.clientSocket()) {
                    DataOutputStream out = new DataOutputStream(client.getOutputStream());
                    byte[] cert = req.result().certificate();
                    byte[] key = req.result().privateKey();
                    out.writeInt(cert.length);
                    out.write(cert);
                    out.writeInt(key.length);
                    out.write(key);
                    out.flush();
                    System.out.println("Ответ отправлен клиенту.");
                } catch (Exception e) {
                    System.err.println("Ошибка при передаче клиенту: " + e.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
