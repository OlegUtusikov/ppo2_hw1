package utils;

public class Request {
    private final String data;

    public Request(String data) {
        this.data = data;
    }

    public String get() {
        return data;
    }
}
