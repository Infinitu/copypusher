package kr.soma.realtimemessage.channel

import kr.soma.realtimemessage.{Event, Connection, Pusher}
import org.vertx.scala.core.eventbus.Message
import org.vertx.scala.core.json.JsonObject
import scala.collection.mutable


/**
 * The base channel for pusher.
 * It can be used this channel for public channel, defined on Pusher.
 * It can be Used with PrivateChannel trait or PresenceChannel trait
 * @param channelName the name of new Channel
 */
class BaseChannel(val channelName:String)
{
  // the connections that subscribe this channel
  val connections = mutable.Buffer[Connection]()
  eventBus.registerHandler(Pusher.eventBus_prefix+channelName,handleEvent _)
  System.out.println("regist Handler on "+Pusher.eventBus_prefix+channelName)

  /**
   * subscribe this channel
   * @param connection the connection that try to subscribe this channel
   * @param auth the auth information
   * @param data other information for subscribe
   */
  def subscribe(connection:Connection,auth:String, data:JsonObject)
  {
    System.out.println("subsub!!")
    sendSubscribeSuccessMessage(connection,new JsonObject)
    connections+=connection
  }

  /**
   * unsubscribe this channel
   * @param connection the connection that try to unsubscribe
   */
  def unsubscribe(connection:Connection)
  {
    System.out.println("unsub!")
    connections-=connection
  }

  /**
   * publish event from event bus to all of connections that subscribe this channel
   * @param msg the eventbus message that contains content of this event
   */
  def handleEvent(msg:Message[String])
  {
    handleEvent(msg.body)
  }

  /**
   * publish event to all of connections that subscribe this channel
   * @param event the content of this event
   */
  def handleEvent(event:String)
  {
    System.out.println("Event Handled event = "+event.toString)
    for(i <- connections)
    {
      i.socket.writeTextFrame(event.toString)
    }
  }

  /**
   * publish event to event bus
   * @param event the name of event
   * @param data content of this event
   * @return true is success to publish. false is not allowed to publish
   */
  def publishEvent[T](event:String,data:T):Boolean=
  {
    publishEvent(Event(event,channelName,data).toString)
  }

  /**
   * publish event to event bus
   * @param data the wrapped event data
   * @return true is success to publish. false is not allowed to publish
   */
  def publishEvent(data:String):Boolean=
  {
    eventBus.publish(Pusher.eventBus_prefix+channelName,data)
    System.out.println("pubpub!")
    true
  }

  /**
   * send a subscribe success event
   * @param connection the connection to send
   * @param data the other data to send with
   */
  def sendSubscribeSuccessMessage(connection:Connection,data:JsonObject)
  {
    subscriptionSuccess(connection,channelName,data)
  }

  def isAllowTrigger = false

}


