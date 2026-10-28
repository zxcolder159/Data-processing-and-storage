package spider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class SpiderRunner {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<String> run(String host, int port) throws Exception {
        String baseUrl = "http://" + host + ":" + port;

        ConcurrentHashMap<String, Boolean> visited = new ConcurrentHashMap<>();
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        Phaser phaser = new Phaser(1);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        crawl("/", baseUrl, httpClient, phaser, executor, visited, results);

        phaser.arriveAndAwaitAdvance();
        executor.shutdown();

        Collections.sort(results);
        return results;
    }

    static void crawl(String path, String baseUrl, HttpClient httpClient,
                      Phaser phaser, ExecutorService executor,
                      ConcurrentHashMap<String, Boolean> visited, List<String> results) {
        if (visited.putIfAbsent(path, Boolean.TRUE) != null) return;

        phaser.register();
        executor.submit(() -> {
            try {
                String json = fetch(path, baseUrl, httpClient);
                if (json == null) return;

                JsonNode root = mapper.readTree(json);
                String message = root.path("message").asText();
                results.add(message);

                JsonNode successors = root.path("successors");
                if (successors.isArray()) {
                    for (JsonNode s : successors) {
                        String successor = s.asText();
                        String nextPath = successor.startsWith("/") ? successor : "/" + successor;
                        crawl(nextPath, baseUrl, httpClient, phaser, executor, visited, results);
                    }
                }
            } catch (Exception e) {
                System.err.println("Ошибка при обходе " + path + ": " + e.getMessage());
            } finally {
                phaser.arriveAndDeregister();
            }
        });
    }

    private static String fetch(String path, String baseUrl, HttpClient httpClient) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
            System.err.println("HTTP " + response.statusCode() + " для пути: " + path);
            return null;
        } catch (Exception e) {
            System.err.println("Запрос не выполнен для " + path + ": " + e.getMessage());
            return null;
        }
    }
}
