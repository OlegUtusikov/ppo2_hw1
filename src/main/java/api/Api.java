package api;

import utils.Request;

public abstract class Api {
    private final Request request;

    protected Api(Request request) {
        this.request = request;
    }


    public String getHttpUrl(String address, int port, boolean secure) {
        return secureHttp(secure) + "://" + address + ":" + port + "/" + urlPath() + "/" + request.get();
    }

    protected abstract String urlPath();

    private String secureHttp(boolean secure) {
        return secure ? "https" : "http";
    }
}
