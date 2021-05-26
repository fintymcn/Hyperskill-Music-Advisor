package advisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


public class Main {
    final static String clientID = "REDACTED";
    final static String clientSecret = "REDACTED";
    final static String redirectURI = "http://localhost:8080";
    static String accessPoint = "https://accounts.spotify.com";
    static String apiPath = "https://api.spotify.com";
    final static String[] authResponse = {"", ""};
    static String accessToken = "";
    static String refreshToken = "";
    static HashMap<String, String> categoryIds;
    static boolean categoriesUpdatedThisSession;
    static HttpClient client = HttpClient.newBuilder().build();


    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        categoryIds = new HashMap<>();
        loadCategoryIds();
        categoriesUpdatedThisSession = false;

        for (int i = 0; i < args.length; i++) {
            if ("-access".equals(args[i])) {
                accessPoint = (i + 1 < args.length) ? args[i + 1] : accessPoint;
            } else if ("-resource".equals(args[i])) {
                apiPath = (i + 1 < args.length) ? args[i + 1] : apiPath;
            }
        }

        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8080), 0);

        server.createContext("/",
                new HttpHandler() {
                    public void handle(HttpExchange exchange) throws IOException {
                        String success = "Got the code. Return back to your program.";
                        String failed = "Authorization code not found. Try again.";
                        authResponse[0] = exchange.getRequestURI().getQuery().split("=")[0];
                        authResponse[1] = exchange.getRequestURI().getQuery().split("=")[1];
                        if ("code".equals(authResponse[0])) {
                            exchange.sendResponseHeaders(200, success.length());
                            exchange.getResponseBody().write(success.getBytes());
                        } else {
                            exchange.sendResponseHeaders(200, failed.length());
                            exchange.getResponseBody().write(failed.getBytes());
                        }
                        exchange.getResponseBody().close();
                    }
                });


        server.start();

        String goodbye = "---GOODBYE!---";
        boolean running = true;
        boolean auth = false;

        while (running) {
            String[] input = scanner.nextLine().trim().split(" ");
            if (auth) {
                switch (input[0]) {
                    case "new":
                        getNew();
                        break;
                    case "featured":
                        getFeatured();
                        break;
                    case "categories":
                        getCategories(true);
                        break;
                    case "playlists":
                        if (input.length < 2) {
                            System.out.println("Please provide a playlist category. Try again");
                        } else {
                            getPlaylists(input[1].toLowerCase());
                        }
                        break;
                    case "exit":
                        saveCategoryIds();
                        System.out.println(goodbye);
                        server.stop(1);
                        running = false;
                        break;
                    case "auth":
                        System.out.println("Already logged in");
                        break;
                    default:
                        System.out.println("Unknown command");
                        break;
                }
            } else {
                switch (input[0]) {
                    case "new":
                    case "featured":
                    case "categories":
                    case "playlists Mood":
                        System.out.println("Please, provide access for application.");
                        break;
                    case "exit":
                        System.out.println(goodbye);
                        running = false;
                        server.stop(1);
                        break;
                    case "auth":
                        auth = authoriseSpotifyAccount();
                        if (auth) {
                            System.out.println("---SUCCESS---");
                        }
                        break;
                    default:
                        System.out.println("Unknown command");
                        break;
                }
            }
        }
    }

    public static void getPlaylists(String category) throws IOException, InterruptedException {
        if (!categoryIds.containsKey(category) && !categoriesUpdatedThisSession) {
            getCategories(false);
        }

        if (!categoryIds.containsKey(category)) {
            System.out.println("Unknown category name.");
            return;
        }

        String categoryID = categoryIds.get(category);

        HttpRequest getPlaylists = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(String.format("%s/v1/browse/categories/%s/playlists", apiPath, categoryID)))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getPlaylists, HttpResponse.BodyHandlers.ofString());

        if (!response.headers().toString().contains("status=[200]")) {
            if (response.body().contains("error")) {
                JsonObject error = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonObject("error");
                System.out.println(error.get("message").getAsString());
            } else {
                System.out.println("An error occurred, please try again");
            }
            return;
        }

        JsonArray playlists = JsonParser.parseString(response.body())
                .getAsJsonObject().getAsJsonObject("playlists").getAsJsonArray("items");

        for (int i = 0; i < playlists.size(); i++) {
            JsonObject currentPlaylist = playlists.get(i).getAsJsonObject();
            System.out.println(currentPlaylist.get("name").getAsString());
            System.out.println(currentPlaylist.getAsJsonObject("external_urls").get("spotify").getAsString());
            System.out.println();
        }

    }

    public static void getCategories(Boolean print) throws IOException, InterruptedException {
        HttpRequest getCategories = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(String.format("%s/v1/browse/categories", apiPath)))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getCategories, HttpResponse.BodyHandlers.ofString());

        if (!response.headers().toString().contains("status=[200]")) {
            System.out.println("An error occurred, please try again");
            return;
        }

        JsonArray categories = JsonParser.parseString(response.body())
                .getAsJsonObject().getAsJsonObject("categories").getAsJsonArray("items");

        for (int i = 0; i < categories.size(); i++) {
            JsonObject currentCategory = categories.get(i).getAsJsonObject();
            String name = currentCategory.get("name").getAsString().toLowerCase();
            String id = currentCategory.get("id").getAsString();
            categoryIds.putIfAbsent(name, id);
            if (print) {
                System.out.println(currentCategory.get("name").getAsString());
            }
        }

        categoriesUpdatedThisSession = true;
    }

    public static void getFeatured() throws IOException, InterruptedException {
        HttpRequest getFeatured = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(String.format("%s/v1/browse/featured-playlists", apiPath)))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getFeatured, HttpResponse.BodyHandlers.ofString());

        if (!response.headers().toString().contains("status=[200]")) {
            System.out.println("An error occurred, please try again");
            return;
        }

        JsonArray playlists = JsonParser.parseString(response.body())
                .getAsJsonObject().getAsJsonObject("playlists").getAsJsonArray("items");

        for (int i = 0; i < playlists.size(); i++) {
            JsonObject currentPlaylist = playlists.get(i).getAsJsonObject();
            System.out.println(currentPlaylist.get("name").getAsString());
            System.out.println(currentPlaylist.getAsJsonObject("external_urls").get("spotify").getAsString());
            System.out.println();
        }


    }

    public static void getNew() throws IOException, InterruptedException {
        HttpRequest getNew = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(String.format("%s/v1/browse/new-releases", apiPath)))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getNew, HttpResponse.BodyHandlers.ofString());

        if (!response.headers().toString().contains("status=[200]")) {
            System.out.println("An error occurred, please try again");
            return;
        }

        JsonArray albums = JsonParser.parseString(response.body())
                .getAsJsonObject().getAsJsonObject("albums").getAsJsonArray("items");

        for (int i = 0; i < albums.size(); i++) {
            JsonObject currentAlbum = albums.get(i).getAsJsonObject();
            System.out.println(currentAlbum.get("name").getAsString());
            List<String> artists = new ArrayList<>();
            for (JsonElement artist : currentAlbum.getAsJsonArray("artists")) {
                JsonObject artistObj = artist.getAsJsonObject();
                artists.add(artistObj.get("name").getAsString());
            }
            System.out.println(artists.toString());
            System.out.println(currentAlbum.getAsJsonObject("external_urls").get("spotify").getAsString());
            System.out.println();
        }
    }

    public static boolean authoriseSpotifyAccount() throws IOException, InterruptedException {
        System.out.println("use this link to request the access code:");
        System.out.printf("%s/authorize?" +
                "client_id=%s" +
                "&response_type=code" +
                "&redirect_uri=%s%n", accessPoint, clientID, redirectURI);
        //Thread.sleep(2000);

        System.out.println("waiting for code...");

        int checks = 0;
        
        while (checks < 30 && !"code".equals(authResponse[0])) {
            Thread.sleep(1000);
            checks++;
        }

        if ("code".equals(authResponse[0])) {

            System.out.println("code received");
            System.out.println("making http request for access_token...");

            String requestBody = String.format("grant_type=authorization_code&" +
                    "code=%s&" +
                    "redirect_uri=%s&" +
                    "client_id=%s&" +
                    "client_secret=%s", authResponse[1], redirectURI, clientID, clientSecret);

            HttpRequest getTokens = HttpRequest.newBuilder()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .uri(URI.create(String.format("%s/api/token", accessPoint)))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(getTokens, HttpResponse.BodyHandlers.ofString());
            String[] formatResponse = response.body().replaceAll("[\"{}]", "")
                    .replace(',', ':')
                    .split(":");
            accessToken = formatResponse[1];
            refreshToken = formatResponse[7];

            return true;
        } else {
            System.out.println("Something went wrong, please try again");
        }
        return false;
    }

    public static void saveCategoryIds() {
        if (categoriesUpdatedThisSession) {
            try {
                FileOutputStream fos = new FileOutputStream("categoryIds.txt");
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                oos.writeObject(categoryIds);

                oos.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error saving category info");
            }
        }
    }

    public static void loadCategoryIds() {
        File savedIds = new File("categoryIds.txt");
        //System.out.println("in load ids");
        if (savedIds.exists()) {
            try {
                FileInputStream fis = new FileInputStream(savedIds);
                ObjectInputStream ois = new ObjectInputStream(fis);

                categoryIds = (HashMap<String, String>) ois.readObject();

                ois.close();
                fis.close();
                //System.out.println(categoryIds.toString());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Error loading category info");
            }
        }
        //System.out.println("leaving load ids");
    }
}