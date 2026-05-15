package com.example.ai_engineering_enablement_portal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.ai_engineering_enablement_portal.ai.LocalAiModelService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class LocalAiModelServiceTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void generateCallsOllamaCompatibleEndpoint() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/generate", exchange -> {
            byte[] response = """
                    {"response":"assessment: output generated from local model"}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(response);
            }
        });
        server.start();

        LocalAiModelService service = new LocalAiModelService(
                RestClient.builder(),
                "http://127.0.0.1:" + server.getAddress().getPort(),
                "test-model",
                1);

        String output = service.generate("Review this code");

        assertEquals("assessment: output generated from local model", output);
    }
}
