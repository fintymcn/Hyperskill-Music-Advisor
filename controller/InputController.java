package advisor.controller;

import advisor.model.*;
import advisor.view.NumberedPage;
import advisor.view.Page;
import advisor.view.View;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Scanner;

public class InputController {

    private Scanner scanner;
    private boolean running;
    private boolean authorised;
    private boolean noHyperSkillTest;
    private String[] args;

    final String clientID = "ed668c7235ad43c9bd4ddcb05e732ae7";
    final String clientSecret = "4b678e6dcbe047f68dfdbf34d2b50fcf";
    final String redirectURI = "http://localhost:8080";
    String accessToken = "";
    String accessPoint = "https://accounts.spotify.com";
    String apiPath = "https://api.spotify.com";
    private int itemsPerPage = 5;


    SpotifyApiCallSender apiCaller = new SpotifyApiCallSender();
    FeaturedCollection featured = FeaturedCollection.getInstance();
    NewCollection newAlbums = NewCollection.getInstance();
    PlaylistsCollection playlists = PlaylistsCollection.getInstance();
    Categories categories = Categories.getInstance();

    Page playlistNameError = new Page("Playlist name not found, please try again");
    Page exitView = new Page("Bye!");
    Page unauthorisedView = new Page("Please provide access for the program. Type auth for details.");
    Page generalError = new Page("An error occurred, please try again.");
    Page unknownCommand = new Page("Unknown command, please try again.");

    public InputController(String[] args) {
        scanner = new Scanner(System.in);
        running = false;
        noHyperSkillTest = true;
        this.args = args;
    }

    private void initialiseArguments() {
        for (int i = 0; i < args.length; i++) {
            if ("-access".equals(args[i])) {
                accessPoint = (i + 1 < args.length) ? args[i + 1] : accessPoint;
                noHyperSkillTest = false;
            } else if ("-resource".equals(args[i])) {
                apiPath = (i + 1 < args.length) ? args[i + 1] : apiPath;
                noHyperSkillTest = false;
            } else if ("-page".equals(args[i])) {
                itemsPerPage = (i + 1 < args.length) ? Integer.valueOf(args[i + 1]) : itemsPerPage;
                noHyperSkillTest = false;
            }
        }
        running = true;
    }

    public void run() {
        initialiseArguments();
        View currentView = new Page("");
        while (running) {
            String[] input = scanner.nextLine().toLowerCase().trim().split(" ");


            switch (input[0]) {
                case "new":
                    currentView = getNewAlbums();
                    break;
                case "featured":
                    currentView = getFeatured();
                    break;
                case "playlists":
                    if (input.length > 1) {
                        currentView = getPlaylists(input[1]);
                    } else {
                        currentView = playlistNameError;
                    }
                    break;
                case "categories":
                    currentView = getCategories();
                    break;
                case "exit":
                    currentView = exitView;
                    this.running = false;
                    break;
                case "prev":
                    currentView.prev();
                    break;
                case "next":
                    currentView.next();
                    break;
                case "auth":
                    if (!authorised) {
                        currentView = authoriseProgram();
                    }
                    break;
                default:
                    currentView = unknownCommand;
                    break;
            }

            currentView.printPage();

        }
    }

    private View authoriseProgram() {
        CodeReceiver receiver = null;
        try {
            receiver = new CodeReceiver(HttpServer.create(new InetSocketAddress(getPort()), 0));
        } catch (IOException e) {}

        if (receiver == null) {
            return generalError;
        }

        String authLink = String.format("%s/authorize?" +
                "client_id=%s" +
                "&response_type=code" +
                "&redirect_uri=%s%n", accessPoint, clientID, redirectURI);

        Page authPage = new Page("use this link to request the access code:\n" +
                authLink + "\nwaiting for code...");
        authPage.printPage();

        String authCode = receiver.run();

        authPage = new Page("code received\n" +
                "Making http request for access_token...");
        authPage.printPage();

        apiCaller.setCallMethod(new getAccessToken(redirectURI, clientID, clientSecret));
        String response = apiCaller.send(authCode, accessPoint);

        if ("error".equals(response)) {
            return generalError;
        }

        accessToken = response;
        authorised = true;

        return new Page("Success!");
    }

    private View getPlaylists(String category) {
        if (!authorised) {
            return unauthorisedView;
        }

        if (!categories.categoryIdPresent(category) && !categories.isPopulated()) {
            getCategories();
        }

        String categoryID = categories.getCategoryId(category);

        if ("error".equals(categoryID)) {
            return playlistNameError;
        }

        apiCaller.setCallMethod(new getPlaylistsCall(categoryID));
        String response = apiCaller.send(accessToken, apiPath);
        playlists.update(response);
        System.out.println("im right here getting yo playlistss boyyy");
        return new NumberedPage(itemsPerPage, spotifyCollectionToStringArray(playlists));
    }

    private View getNewAlbums() {
        if (!authorised) {
            return unauthorisedView;
        }

        if (!newAlbums.isPopulated()) {
            apiCaller.setCallMethod(new getNewCall());
            String response = apiCaller.send(accessToken, apiPath);
            newAlbums.update(response);
        }

        return new NumberedPage(itemsPerPage, spotifyCollectionToStringArray(newAlbums));
    }

    private View getFeatured() {
        if (!authorised) {
            return unauthorisedView;
        }

        if (!featured.isPopulated()) {
            apiCaller.setCallMethod(new getFeaturedCall());
            String response = apiCaller.send(accessToken, apiPath);
            featured.update(response);
        }

        return new NumberedPage(itemsPerPage, spotifyCollectionToStringArray(featured));
    }

    private View getCategories() {
        if (!authorised) {
            return unauthorisedView;
        }

        if (!categories.isPopulated()) {
            apiCaller.setCallMethod(new getCategoriesCall());
            String response = apiCaller.send(accessToken, apiPath);
            categories.update(response);
        }

        return new NumberedPage(itemsPerPage, spotifyCollectionToStringArray(categories));
    }

    private String[] spotifyCollectionToStringArray(SpotifyCollection collection) {
        String[] array = new String[collection.totalEntries()];
        for (int i = 0; i < collection.totalEntries(); i++) {
            array[i] = collection.get(i);
        }

        return array;
    }

    private int getPort() {
        if (noHyperSkillTest) {
            return 8080;
        }
        return 8000 + new Random().nextInt(1000);
    }
}
