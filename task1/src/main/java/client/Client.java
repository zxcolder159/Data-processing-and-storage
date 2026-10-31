package client;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
public class Client {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Использование: ./gradlew runClient --args=\"<хост> <порт> <имя> [--delay <секунды>] [--abort]\"");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String name = args[2];
        int delaySeconds = 0;
        boolean abort = false;
        for (int i = 3; i < args.length; i++) {
            if (args[i].equals("--delay") && i + 1 < args.length) {
                delaySeconds = Integer.parseInt(args[i + 1]);
                i++;
            } else if (args[i].equals("--abort")) {
                abort = true;
            }
        }
        try (Socket socket = new Socket(host, port)) {
            System.out.println("Подключено к серверу. Отправка имени: " + name);
            OutputStream out = socket.getOutputStream();
            out.write(name.getBytes(StandardCharsets.US_ASCII));
            out.write(0); 
            out.flush();
            if (delaySeconds > 0) {
                System.out.println("Имитация медленного клиента, задержка на " + delaySeconds + " секунд...");
                Thread.sleep(delaySeconds * 1000L);
            }
            if (abort) {
                System.out.println("Имитация аварийного завершения (прерывание).");
                return;
            }
            System.out.println("Ожидание ответа...");
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            int certLen = dis.readInt();
            byte[] certBytes = new byte[certLen];
            dis.readFully(certBytes);
            int keyLen = dis.readInt();
            byte[] keyBytes = new byte[keyLen];
            dis.readFully(keyBytes);
            System.out.println("Сгенерированные ключи получены. Сохранение в файлы...");
            String certPem = "-----BEGIN CERTIFICATE-----\n" +
                    Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(certBytes) +
                    "\n-----END CERTIFICATE-----\n";
            Files.writeString(Paths.get(name + ".crt"), certPem);
            String keyPem = "-----BEGIN PRIVATE KEY-----\n" +
                    Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(keyBytes) +
                    "\n-----END PRIVATE KEY-----\n";
            Files.writeString(Paths.get(name + ".key"), keyPem);
            System.out.println("Успешно сохранены " + name + ".crt и " + name + ".key");
        }
    }
}
