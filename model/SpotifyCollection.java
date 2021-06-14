package advisor.model;

public interface SpotifyCollection {

    void update(String response);

    int totalEntries();

    boolean isPopulated();

    String get(int i);

}
