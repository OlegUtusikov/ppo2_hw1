package api;

import utils.Request;

public class YandexApi extends Api {
    public YandexApi(Request request) {
        super(request);
    }

    @Override
    public String urlPath() {
        return "yandex.ru";
    }
}
