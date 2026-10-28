package spider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpiderTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private HttpServer server;
    private int port;

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();

        server.createContext("/", exchange -> {
            ObjectNode body = mapper.createObjectNode();
            body.put("message", "root");
            ArrayNode successors = body.putArray("successors");
            successors.add("/a");
            successors.add("/b");
            byte[] bytes = mapper.writeValueAsBytes(body);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        });

        server.createContext("/a", exchange -> {
            ObjectNode body = mapper.createObjectNode();
            body.put("message", "alpha");
            body.putArray("successors");
            byte[] bytes = mapper.writeValueAsBytes(body);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        });

        server.createContext("/b", exchange -> {
            ObjectNode body = mapper.createObjectNode();
            body.put("message", "beta");
            body.putArray("successors");
            byte[] bytes = mapper.writeValueAsBytes(body);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        });

        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void collectsAllMessagesInOrder() throws Exception {
        List<String> result = SpiderRunner.run("localhost", port);
        assertEquals(List.of("alpha", "beta", "root"), result);
    }

    @Test
    void doesNotVisitSamePath() throws Exception {
        List<String> result = SpiderRunner.run("localhost", port);
        assertEquals(3, result.size());
    }
}
