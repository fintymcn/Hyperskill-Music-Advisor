package advisor.model;

public class SpotifyPlaylists {

    String name;
    String URL;

    public SpotifyPlaylists(String name, String URL) {
        this.name = name;
        this.URL = URL;
    }

    public String getName() {
        return name;
    }

    public String getURL() {
        return URL;
    }

    public String toString() {
        return String.format("%s\n%s\n", name, URL);
    }
}
