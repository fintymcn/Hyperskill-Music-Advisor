package advisor.model;

public class SpotifyFeatured {

    private String name;
    private String URL;

    public SpotifyFeatured(String name, String URL) {
        this.name = name;
        this.URL = URL;
    }

    public String getName() {
        return name;
    }

    public String getURL() {
        return URL;
    }

    @Override
    public String toString() {
        return String.format("%s\n%s\n", name, URL);
    }
}
