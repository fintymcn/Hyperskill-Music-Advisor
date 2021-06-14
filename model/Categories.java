package advisor.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class Categories implements SpotifyCollection {

    private static Categories instance = new Categories();

    private Map<String, String> entries;
    private boolean updatedThisSession;
    private String[] categoryNames;

    private Categories() {
        entries = new HashMap<>();
        updatedThisSession = false;
    }

    public static Categories getInstance() {
        return instance;
    }

    @Override
    public void update(String response) {

        JsonArray categories = JsonParser.parseString(response)
                .getAsJsonObject().getAsJsonObject("categories").getAsJsonArray("items");

        for (int i = 0; i < categories.size(); i++) {
            JsonObject currentCategory = categories.get(i).getAsJsonObject();
            String name = currentCategory.get("name").getAsString().toLowerCase();
            String id = currentCategory.get("id").getAsString();
            entries.putIfAbsent(name, id);
        }

        updatedThisSession = true;
    }

    @Override
    public int totalEntries() {
        return entries.size();
    }

    @Override
    public boolean isPopulated() {
        return updatedThisSession;
    }

    public boolean categoryIdPresent(String candidate) {
        return entries.containsKey(candidate);
    }

    public String getCategoryId(String key) {
        return entries.getOrDefault(key, "error");
    }

    @Override
    public String get(int i) {
        if (categoryNames == null) {
            categoryNames = entries.keySet().toArray(new String[0]).clone();
        }
        return categoryNames[i];
    }
}
