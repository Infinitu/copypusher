import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Created with IntelliJ IDEA.
 * User: infinitu
 * Date: 2013. 10. 24.
 * Time: 5:04PM
 * To change this template use File | Settings | File Templates.
 */
public class testPage extends Verticle {

    @Override
    public void start() {
        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {

                HttpServerResponse res = httpServerRequest.response();
                res.headers().add("Access-Control-Allow-Origin","*");
                res.sendFile("/Users/infinitu/Documents/copypusher.html");
            }
        }).listen(80);

    }
}
