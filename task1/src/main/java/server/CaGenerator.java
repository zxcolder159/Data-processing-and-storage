package server;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
public class CaGenerator {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Использование: ./gradlew runCa --args=\"<файл_выходного_ключа>\"");

            System.exit(1);
        }
        String outputFile = args[0];
        System.out.println("Генерация 4096-битной RSA пары ключей CA (это может занять некоторое время)...");
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(4096); 
        KeyPair keyPair = keyPairGen.generateKeyPair();
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(privateKeyBytes);
        }
        System.out.println("Успешно сгенерирован закрытый ключ CA и сохранен в: " + outputFile);
    }
}
