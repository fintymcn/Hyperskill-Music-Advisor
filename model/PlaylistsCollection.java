package advisor.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsCollection implements SpotifyCollection {

    private static PlaylistsCollection instance = new PlaylistsCollection();

    private List<SpotifyPlaylists> entries;
    private boolean populated;

    private PlaylistsCollection() {
        this.entries = new ArrayList<>();
        this.populated = false;
    }

    public static PlaylistsCollection getInstance() {
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

            entries.add(new SpotifyPlaylists(name, URL));
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
