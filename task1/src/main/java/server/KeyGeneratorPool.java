package server;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class KeyGeneratorPool {
    private final BlockingQueue<KeyPair> keyPool;

    public KeyGeneratorPool(int capacity, int numThreads) {
        this.keyPool = new ArrayBlockingQueue<>(capacity);

        for (int i = 0; i < numThreads; i++) {
            Thread worker = new Thread(this::generationLoop, "KeyGenWorker-" + i);
            worker.setDaemon(true);
            worker.start();
        }
    }

    private void generationLoop() {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(8192);
            
            while (!Thread.currentThread().isInterrupted()) {
                KeyPair kp = keyPairGen.generateKeyPair();
                keyPool.put(kp);
                System.out.println("[KeyPool] Сгенерирована пара ключей впрок. Ключей в запасе: " + keyPool.size());
            }
        } catch (Exception e) {
            System.err.println("Ошибка в генераторе ключей: " + e.getMessage());
        }
    }

    public KeyPair takeKey() throws InterruptedException {
        return keyPool.take();
    }
}
