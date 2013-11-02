package kr.soma.realtimemessage

import org.vertx.scala.platform._
import org.vertx.scala.core.eventbus._
import org.vertx.scala.core.http._
import org.vertx.scala.core.json._

object Pusher
{
  var port:Int =0
  var eventBus_prefix:String=null
  var ExternalCommandChannel:String=null
  var authorizationChannel:String = null
  var sharedData_prefix:String=null
  var redis_enable:Boolean=false
  var redis_address:String=null
}

class Pusher extends Verticle
{
  override def start() {

    //load configuration
   val config = container.config()
   Pusher.port = config.getInteger("port",8080)
   Pusher.eventBus_prefix = config.getString("eventbus_prefix","Pusher::")
   Pusher.sharedData_prefix = config.getString("sharedData_prefix",Pusher.eventBus_prefix)
   Pusher.ExternalCommandChannel =config.getString("commandChannel","pusher_command")
   Pusher.authorizationChannel = config.getString("authorizationChannel","pusher_auth")
   Pusher.redis_enable =config.getBoolean("redis_enable",false)
   val redis_config = config.getObject("redis_config")
   Pusher.redis_address = redis_config.getString("address")


    container.deployModule("io.vertx.mod-redis",redis_config)

    val eventbus = vertx.eventBus
    val pusherProtocol = PusherProtocol(vertx)
    channel.apply(vertx)

    //register command handler
    eventbus.registerHandler(Pusher.ExternalCommandChannel,handleExternalMessage _)

    //create websocket server
    vertx.createHttpServer().websocketHandler(handleWebSocketOpened _).listen(Pusher.port)

    /**
     * handles message from other verticle
     * @param msg message on eventbus
     */
    def handleExternalMessage(msg:Message[JsonObject]) {
      val command = msg.body.getString("command")
      val data = msg.body.getObject("data")

      command match {
        case "publish_event"=> channel.publishEventByServer(data)
        case _=>
      }

    }

    /**
     * handles websocket connection request
     * @param socket the socket to connect
     */
    def handleWebSocketOpened(socket:ServerWebSocket){
      import org.vertx.scala.core.FunctionConverters.fnToHandler
      val connection = pusherProtocol.connect(socket)
      socket.closeHandler(fnToHandler(handleWebSocketClosed(connection) _))
    }

    def handleWebSocketClosed(connection:Connection)(void:Void){
      pusherProtocol.disconnect(connection)
    }
  }
}