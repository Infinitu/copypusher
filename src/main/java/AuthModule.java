import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.*;
import org.vertx.java.platform.Verticle;


public class AuthModule extends Verticle {
    int i = 0;
    @Override
    public void start() {
        vertx.eventBus().registerHandler("pusher_auth",new Handler<Message>() {
            @Override
            public void handle(Message event) {
                JsonObject object = (JsonObject) event.body();
                System.out.println("log received "+object.toString());
                if("Internal:subscribeAuth".equals(object.getString("event")))
                {
                    JsonObject result= new JsonObject();
                    result.putBoolean("result",object.getObject("data").getString("auth").equals("authString..."));
                    result.putObject("data", new JsonObject());
                    event.reply(result);
                    System.out.println("Auth::success to Auth");
                }
            }
        });

        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest event) {
                HttpServerResponse res = event.response();
                res.putHeader("Access-Control-Allow-Origin","*");
                res.end("{\"auth\":\"authString...\",\"channel_data\":\"{\\\"user_id\\\":\\\""+(++i)+"\\\",\\\"user_info\\\":{\\\"name\\\":\\\"Mr. Pusher\\\"}}\"}");

            }
        }).listen(6060);
    }
}
