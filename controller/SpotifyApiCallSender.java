package advisor.controller;

public class SpotifyApiCallSender {

    private SpotifyApiCall callMethod;

    public void setCallMethod(SpotifyApiCall callMethod) {
        this.callMethod = callMethod;
    }

    public String send(String accessToken, String apiPath) {
        if (callMethod == null) {
            return "error";
        }

        return callMethod.callAPI(accessToken, apiPath);
    }
}
