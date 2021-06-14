package advisor.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class NewCollection implements SpotifyCollection {

    private static NewCollection instance = new NewCollection();

    private List<SpotifyNew> entries;
    boolean populated;

    private NewCollection() {
        entries = new ArrayList<>();
        populated = false;
    }

    public static NewCollection getInstance() {
        return instance;
    }

    @Override
    public void update(String response) {
        JsonArray albums = JsonParser.parseString(response)
                .getAsJsonObject().getAsJsonObject("albums").getAsJsonArray("items");

        for (int i = 0; i < albums.size(); i++) {
            JsonObject currentAlbum = albums.get(i).getAsJsonObject();
            String name = currentAlbum.get("name").getAsString();
            List<String> artists = new ArrayList<>();
            for (JsonElement artist : currentAlbum.getAsJsonArray("artists")) {
                JsonObject artistObj = artist.getAsJsonObject();
                artists.add(artistObj.get("name").getAsString());
            }
            String artist = artists.toString();
            String URL = currentAlbum.getAsJsonObject("external_urls").get("spotify").getAsString();

            entries.add(new SpotifyNew(name, artist, URL));
        }

        populated = true;
    }

    @Override
    public int totalEntries() {
        return entries.size();
    }

    @Override
    public boolean isPopulated() {
        return populated;
    }

    @Override
    public String get(int i) {
        return this.entries.get(i).toString();
    }


}
