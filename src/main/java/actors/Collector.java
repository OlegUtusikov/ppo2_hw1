package actors;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import api.GoogleApi;
import api.MailApi;
import api.YandexApi;
import scala.concurrent.duration.Duration;
import utils.Config;
import utils.Request;
import utils.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Collector extends AbstractActor {
    private final Set<String> responses;
    private final Map<ActorRef, Integer> counters;
    private final Map<String, ActorRef> workers;

    public Collector(Set<String> responses) {
        workers = new HashMap<>();
        counters = new HashMap<>();
        this.responses = responses;
    }

    @Override
    public Receive createReceive() {
        return new ReceiveBuilder().match(Request.class, this::onRequest)
                                   .match(Response.class, this::onResponse)
                                   .match(ReceiveTimeout.class, this::onTimeout).build();
    }

    private void onRequest(Request request) {
        workers.put("mail", context().actorOf(Props.create(Worker.class)));
        workers.get("mail").tell(new MailApi(request), self());
        counters.put(workers.get("mail"), 0);
        workers.put("google", context().actorOf(Props.create(Worker.class)));
        workers.get("google").tell(new GoogleApi(request), self());
        counters.put(workers.get("google"), 0);
        workers.put("yandex", context().actorOf(Props.create(Worker.class)));
        workers.get("yandex").tell(new YandexApi(request), self());
        counters.put(workers.get("yandex"), 0);
        context().setReceiveTimeout(Duration.create(Config.timeout, TimeUnit.MILLISECONDS));
    }

    private boolean check() {
        boolean res = true;
        for (Integer i : counters.values()) {
            res &= i == Config.top;
        }
        return res;
    }

    private void onResponse(Response response) {
        while(!response.isEmpty()) {
            responses.add(response.first());
            response.removeFirst();
            counters.put(sender(), counters.get(sender()) + 1);
            if (check()) {
                System.out.println("Get all responses from workers");
                System.out.println("Responses:" + responses.toString());
                self().tell(PoisonPill.getInstance(), self());
                break;
            }
        }
    }

    private void onTimeout(ReceiveTimeout timeout) {
        System.out.println("Time out");
        System.out.println("Responses: " + responses.toString());
        responses.add(Config.timeout_flag);
        self().tell(PoisonPill.getInstance(), self());
    }
}
