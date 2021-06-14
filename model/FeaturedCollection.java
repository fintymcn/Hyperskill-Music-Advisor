package advisor.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class FeaturedCollection implements SpotifyCollection{

    private static FeaturedCollection instance = new FeaturedCollection();

    private List<SpotifyFeatured> entries;
    boolean populated;

    private FeaturedCollection() {
        entries = new ArrayList<>();
        populated = false;
    }

    public static FeaturedCollection getInstance() {
        return instance;
    }

    @Override
    public void update(String response) {

        JsonArray playlists = JsonParser.parseString(response)
                .getAsJsonObject().getAsJsonObject("playlists").getAsJsonArray("items");

        for (int i = 0; i < playlists.size(); i++) {
            JsonObject currentPlaylist = playlists.get(i).getAsJsonObject();
            String name = currentPlaylist.get("name").getAsString();
            String URL = currentPlaylist.getAsJsonObject("external_urls").get("spotify").getAsString();

            entries.add(new SpotifyFeatured(name, URL));
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
