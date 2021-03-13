package actors;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import api.Api;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import utils.Config;
import utils.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Worker extends AbstractActor {
    @Override
    public Receive createReceive() {
        return new ReceiveBuilder().match(Api.class, this::onResponse).build();
    }

    private void onResponse(Api api) {
        StringBuilder builder = new StringBuilder();
        try {
            URL url = new URL(api.getHttpUrl(Config.address, Config.port, Config.secure));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            while(reader.ready()) {
                builder.append(reader.readLine());
            }
        } catch (IOException e) {
            System.err.println("[ERROR]: try request. More:" + e.getMessage());
        }
        JsonArray array = JsonParser.parseString(builder.toString()).getAsJsonObject().get("answers").getAsJsonArray();
        Response response = new Response();
        for (int i = 0; i < Math.min(array.size(), Config.top); ++i) {
            response.addData(array.get(i).getAsString());
        }
        sender().tell(response, self());
    }
}
