package api;

import utils.Request;

public class GoogleApi extends Api {
    public GoogleApi(Request request) {
        super(request);
    }

    @Override
    public String urlPath() {
        return "google.com";
    }
}
