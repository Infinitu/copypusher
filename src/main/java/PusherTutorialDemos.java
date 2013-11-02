import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class PusherTutorialDemos extends Verticle{

    public static final String EV_PREFIX = "Pusher::";

    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest httpServerRequest) {
                httpServerRequest.bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer event) {
                        JsonObject data = new JsonObject(event.toString());
                        vertx.eventBus().publish(EV_PREFIX+"http-pusher-com-tutorials-realtime_chat_widget",data.getObject("activity")) ;
                        httpServerRequest.response().end("abc");
                    }
                });
            }
        }).listen(7070);

    }


}
