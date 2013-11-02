import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Created with IntelliJ IDEA.
 * User: infinitu
 * Date: 2013. 10. 24.
 * Time: 5:04PM
 * To change this template use File | Settings | File Templates.
 */
public class simpleHttpCommander extends Verticle {

    public static final String EV_PREFIX = "";

    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {
                MultiMap map = httpServerRequest.headers();
                String c = map.get("channel");
                JsonObject d = new JsonObject(map.get("data"));

                vertx.eventBus().publish(EV_PREFIX+c,d);

                System.out.println(EV_PREFIX+c);
                System.out.println(d.toString());

                httpServerRequest.response().end("dz~");
            }
        }).listen(9090);

        vertx.eventBus().registerHandler("Pusher::MY_CHANNEL",new Handler<Message>() {
            @Override
            public void handle(Message message) {
                System.out.println("data : "+message.body().toString());
            }
        });
    }
}
