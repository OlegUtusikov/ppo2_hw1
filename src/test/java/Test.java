import actors.Collector;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.xebialabs.restito.builder.stub.StubHttp;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.semantics.ConditionWithApplicables;
import com.xebialabs.restito.server.StubServer;
import junit.framework.TestCase;
import org.glassfish.grizzly.http.server.Response;
import utils.Config;
import utils.Request;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

public class Test extends TestCase {
    private Set<String> collect(String solt) throws InterruptedException {
        ActorSystem system = ActorSystem.create("collect");
        Set<String> res = new TreeSet<>();
        system.actorOf(Props.create(Collector.class, res), "collector").tell(new Request(solt), ActorRef.noSender());
        Thread.sleep(Config.timeout);
        return res;
    }

    private final Condition googleCheck = new ConditionWithApplicables(obj -> obj.getUri().startsWith("/google"));
    private final Condition yandexCheck = new ConditionWithApplicables(obj -> obj.getUri().startsWith("/yandex"));
    private final Condition mailCheck = new ConditionWithApplicables(obj -> obj.getUri().startsWith("/mail"));

    Response createResponse(Response response, String msg) {
        try {
            response.getWriter().write(msg);
        } catch (IOException  e) {
            e.printStackTrace();
        }
        return response;
    }

    @org.junit.Test
    public void testSmallResponse() throws InterruptedException {
        StubServer server = new StubServer(Config.port).run();
        StubHttp.whenHttp(server).match(googleCheck).then(response -> createResponse(response, "{ answers: [\"1g\", \"2g\", \"3g\"] }"));
        StubHttp.whenHttp(server).match(mailCheck).then(response -> createResponse(response, "{ answers: [\"1m\", \"2m\", \"3m\", \"4m\"] }"));
        StubHttp.whenHttp(server).match(yandexCheck).then(response -> createResponse(response, "{ answers: [\"1y\", \"2y\", \"3y\", \"4y\", \"5y\"] }"));
        Set<String> res = collect("test_small");
        Set<String> expected = Set.of("1g", "2g", "3g", "1y", "2y", "3y", "4y", "5y", "1m", "2m", "3m", "4m");
        assertEquals(expected, res);
        server.stop();
    }

    @org.junit.Test
    public void testLargeResponse() throws InterruptedException {
        StubServer server = new StubServer(Config.port).run();
        StubHttp.whenHttp(server).match(googleCheck).then(response -> createResponse(response, "{ answers: [\"1g\", \"2g\", \"3g\", \"4g\", \"5g\", \"6g\", \"7g\"] }"));
        StubHttp.whenHttp(server).match(mailCheck).then(response -> createResponse(response, "{ answers: [\"1m\", \"2m\", \"3m\", \"4m\", \"5m\"] }"));
        StubHttp.whenHttp(server).match(yandexCheck).then(response -> createResponse(response, "{ answers: [\"1y\", \"2y\", \"3y\", \"4y\", \"5y\", \"6y\"] }"));
        Set<String> res = collect("test_large");
        Set<String> expected = Set.of("1g", "2g", "3g", "4g", "5g", "1y", "2y", "3y", "4y", "5y", "1m", "2m", "3m", "4m", "5m");
        assertEquals(expected, res);
        server.stop();
    }

    @org.junit.Test
    public void testTimeout() throws InterruptedException {
        StubServer server = new StubServer(Config.port).run();

        StubHttp.whenHttp(server).match(googleCheck).then(response -> createResponse(response, "{ answers: [] }"));
        StubHttp.whenHttp(server).match(mailCheck).then(response -> createResponse(response, "{ answers: [] }"));
        StubHttp.whenHttp(server).match(yandexCheck).then(response -> createResponse(response, "{ answers: [] }"));
        Set<String> res = collect("test_timeout");
        Thread.sleep(Config.timeout + 100);
        Set<String> expected = Set.of(Config.timeout_flag);
        assertEquals(expected, res);
        server.stop();
    }
}
