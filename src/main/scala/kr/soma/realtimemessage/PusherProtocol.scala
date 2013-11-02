package kr.soma.realtimemessage
import org.vertx.scala.core.Vertx
import org.vertx.scala.core.http._
import scala.collection.mutable
import org.vertx.scala.core.buffer.Buffer
import org.vertx.scala.core.json._
import java.util.UUID

/**
 * Connection class contains socket id and socket object
 * @param socketid initialized ID for the socket connection
 * @param socket connected socket object
 */
case class Connection(socketid:String, socket:ServerWebSocket)


object PusherProtocol
{
  def apply(vertx:Vertx) = new PusherProtocol(vertx)
}

/**
 * Simulate the Protocol of Pusher
 * @param vertx vertx object from verticle
 */
class PusherProtocol(val vertx:Vertx){
  // Collection of connections for this Verticle
  val connections = mutable.HashMap[String,Connection]()

  /**
   * connect the socket to use pusher protocol
   * @param socket socket to connect
   */
  def connect(socket:ServerWebSocket)={
    val socketid= UUID.randomUUID().toString
    val connection = new Connection(socketid,socket)
    connections+=socketid->connection

    socket.dataHandler(DataRecieved(connection) _)

    eventEstablished(connection)
    System.out.println("connected")

    connection
  }

  /**
   * the data receive callback for each connection
   * @param connection the owner of this data
   * @param data the data what the connection sent
   */
  def DataRecieved(connection:Connection)(data:Buffer)
  {
    val strData = data.getString(0,data.length())
    val jsonData = new JsonObject(strData)

    val event = jsonData.getString("event")
    val eventData = jsonData.getObject("data")

    event match{
      case "pusher:subscribe"=>requestSubscribe(connection,eventData) //subscribe channel
      case "pusher:unsubscribe"=>requestUnsubscribe(connection,eventData) //unsubscribe channel
      case "pusher:ping"=>sendPong(connection) //ping
      case event:String => requestSendMessage(connection,event,jsonData.getString("channel"),eventData) //publish event
    }

  }

  /**
   * try to subscribe
   * @param connection the connection that subscribe
   * @param data the information for subscribing channel. It should contains "channel"(must), "auth"(optional) and  "channel_data"(optional)
   */
  def requestSubscribe(connection:Connection,data:JsonObject)
  {
    val channelName = data getString "channel"
    val auth = data getString "auth"
    val json = new JsonObject(data getString "channel_data")


    channel.subscribe(connection,channelName,auth,json)
  }

  /**
   * try to unsubscribe
   * @param connection  the connection that unsubscribe
   * @param data the information for unsubscribing channel. It should contains "channel"
   */
  def requestUnsubscribe(connection:Connection,data:JsonObject)
  {
    val channelName = data getString "channel"
    channel.unsubscribe(connection,channelName)
  }

  /**
   * Try to send a event to channel
   * @param connection the connection that try to send
   * @param event the event name
   * @param channelName the channel to send
   * @param data the contents of event
   */
  def requestSendMessage(connection:Connection,event:String,channelName:String,data:JsonObject)
  {
    if(!channel.publishEventByClient(connection,event,channelName,data))
    {
      //Err
      connection.socket.writeTextFrame(Event.error("not allowed this event to published",301).toString)
    }
  }

  /**
   * send connection established event to connection
   * @param connection the connection to send
   */
  def eventEstablished(connection:Connection){
    connection.socket.writeTextFrame(Event.established(connection.socketid).toString)
    System.out.println(Event.established(connection.socketid).toString)
  }

  /**
   * send pong event to connection
   * @param connection the connection to send
   */
  def sendPong(connection:Connection)
  {
    connection.socket.writeTextFrame(Event("pusher:pong",new JsonObject).toString)
  }

  /**
   * disconnect from pusher protocol
   * @param socket socket to disconnect
   */
  def disconnect(connection:Connection)
  {
    channel.unsubscribeAll(connection)
    connections-=connection.socketid
  }

}