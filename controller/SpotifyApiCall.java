package advisor.controller;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

interface SpotifyApiCall {

    HttpClient client = HttpClient.newBuilder().build();

    String callAPI(String accessToken, String apiPath);
}

class getAccessToken implements SpotifyApiCall {

    private final String redirectURI;
    private String authCode;
    private String accessPoint;
    private final String clientID;
    private final String clientSecret;

    public getAccessToken(String redirectURI, String clientID, String clientSecret) {
        this.redirectURI = redirectURI;
        this.clientID = clientID;
        this.clientSecret = clientSecret;
    }

    @Override
    public String callAPI(String accessToken, String apiPath) {
        authCode = accessToken;
        accessPoint = apiPath;

        String requestBody = String.format("grant_type=authorization_code&" +
                "code=%s&" +
                "redirect_uri=%s&" +
                "client_id=%s&" +
                "client_secret=%s", authCode, redirectURI, clientID, clientSecret);

        HttpRequest getTokens = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(String.format("%s/api/token", accessPoint)))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = null;

        while (response == null) {
            try {
                response = client.send(getTokens, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (response == null || response.body().contains("error")) {
            return "error";
        }

        return response.body().replaceAll("[\"{}]", "")
                .replace(',', ':')
                .split(":")[1];
    }
}

class getNewCall implements SpotifyApiCall {

    @Override
    public String callAPI(String accessToken, String apiPath) {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(String.format("%s/v1/browse/new-releases", apiPath)))
                .GET()
                .build();

        HttpResponse<String> response = null;
        while (response == null) {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (response != null) {
            return response.body();
        } else {
            return "error";
        }
    }
}

class getFeaturedCall implements SpotifyApiCall {

    @Override
    public String callAPI(String accessToken, String apiPath) {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(String.format("%s/v1/browse/featured-playlists", apiPath)))
                .GET()
                .build();

        HttpResponse<String> response = null;
        while (response == null) {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (response != null) {
            return response.body();
        } else {
            return "error";
        }
    }
}

class getCategoriesCall implements SpotifyApiCall {

    @Override
    public String callAPI(String accessToken, String apiPath) {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(String.format("%s/v1/browse/categories", apiPath)))
                .GET()
                .build();

        HttpResponse<String> response = null;
        while (response == null) {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (response != null) {
            return response.body();
        } else {
            return "error";
        }
    }
}

class getPlaylistsCall implements SpotifyApiCall {

    private final String categoryID;

    public getPlaylistsCall(String categoryID) {
        this.categoryID = categoryID;
    }

    @Override
    public String callAPI(String accessToken, String apiPath) {
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(String.format("%s/v1/browse/categories/%s/playlists", apiPath, categoryID)))
                .GET()
                .build();

        HttpResponse<String> response = null;
        while (response == null) {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (response != null) {
            return response.body();
        } else {
            return "error";
        }
    }
}
