package api;

import utils.Request;

public class MailApi extends Api {
    public MailApi(Request request) {
        super(request);
    }

    @Override
    public String urlPath() {
        return "mail.ru";
    }
}
