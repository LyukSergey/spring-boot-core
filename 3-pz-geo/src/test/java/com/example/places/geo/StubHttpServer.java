package com.example.places.geo;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Мінімальний локальний HTTP-сервер для тестів NominatimClient.
 * Віддає заздалегідь заданий JSON на будь-який запит і запам'ятовує
 * останній шлях та заголовок User-Agent для перевірок.
 */
final class StubHttpServer implements AutoCloseable {

    private final HttpServer server;
    private volatile String lastPath;
    private volatile String lastUserAgent;

    private StubHttpServer(HttpServer server) {
        this.server = server;
    }

    static StubHttpServer start(String responseJson) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        StubHttpServer stub = new StubHttpServer(server);
        byte[] body = responseJson.getBytes(StandardCharsets.UTF_8);
        server.createContext("/", exchange -> {
            stub.lastPath = exchange.getRequestURI().toString();
            stub.lastUserAgent = exchange.getRequestHeaders().getFirst("User-Agent");
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        return stub;
    }

    String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    String lastPath() {
        return lastPath;
    }

    String lastUserAgent() {
        return lastUserAgent;
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
