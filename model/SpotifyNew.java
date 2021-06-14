package advisor.model;

public class SpotifyNew {
    private String albumName;
    private String artists;
    private String URL;

    public SpotifyNew(String albumName, String artists, String URL) {
        this.albumName = albumName;
        this.artists = artists;
        this.URL = URL;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtists() {
        return artists;
    }

    public String getURL() {
        return URL;
    }

    @Override
    public String toString() {
        return String.format("%s\n%s\n%s\n", albumName, artists, URL);
    }
}
