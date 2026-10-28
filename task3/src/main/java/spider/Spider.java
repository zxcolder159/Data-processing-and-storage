package spider;

import java.util.List;

public class Spider {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Использование: ./gradlew runSpider --args=\"<host> <port>\"");
            System.exit(1);
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        List<String> results = SpiderRunner.run(host, port);
        for (String msg : results) {
            System.out.println(msg);
        }
    }
}
