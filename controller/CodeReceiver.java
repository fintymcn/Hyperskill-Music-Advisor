package advisor.controller;

import com.sun.net.httpserver.HttpServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CodeReceiver {

    private final HttpServer httpServer;
    private String code;

    private final String CODE_TOKEN = "code=";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CodeReceiver(HttpServer httpServer) {
        httpServer.createContext("/",
                exchange -> {
                    String query = exchange.getRequestURI().getQuery();
                    String response;

                    if (query != null && query.startsWith(CODE_TOKEN)) {
                        code = query.substring(CODE_TOKEN.length());
                        response = "Got the code. Return back to your program.";
                    } else {
                        response = "Authorisation code not found. Try again.";
                    }

                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();

                    if (code != null) {
                        executor.submit(() -> httpServer.stop(0));
                    }
                }
        );
        this.httpServer = httpServer;
    }

    public String run() {
        httpServer.setExecutor(executor);
        httpServer.start();
        while (code == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        executor.shutdown();
        return code;
    }
}
