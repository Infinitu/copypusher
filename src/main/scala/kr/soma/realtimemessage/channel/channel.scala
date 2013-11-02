package kr.soma.realtimemessage

import scala.collection.mutable
import org.vertx.scala.core.json._
import org.vertx.scala.core.Vertx
import org.vertx.scala.core.eventbus._
import org.vertx.scala.core.shareddata.SharedData

package object channel
{
  val channelMap = new mutable.HashMap[String,BaseChannel]
  var eventBus:EventBus=null
  var sharedData:SharedData=null

  /**
   * initalize channel
   */
  def apply(vertx:Vertx)
  {
    eventBus = vertx.eventBus
    sharedData = vertx.sharedData
  }

  /**
   * subscribe channel
   * @param connection the connection that try to subscribe
   * @param channelName name of channel
   * @param auth authorization information
   * @param data the other data
   */
  def subscribe(connection:Connection,channelName:String, auth:String, data:JsonObject)
  {
    if(!channelMap.contains(channelName))
    {
      channelMap+=channelName->makeChannel(channelName)
    }
    val channel = (channelMap get channelName).get

    channel.subscribe(connection,auth,data)
  }

  /**
   * unsubscribe channel
   * @param connection the connection that try to unsubscribe
   * @param channelName name of channel
   */
  def unsubscribe(connection:Connection,channelName:String)
  {
    if(!channelMap.contains(channelName)) return
    val channel = (channelMap get channelName).get
    channel.unsubscribe(connection)
  }

  /**
   * unsubscribe channel
   * @param connection the connection that try to unsubscribe
   * @param channelName name of channel
   */
  def unsubscribeAll(connection:Connection)
  {
    for(channel<-channelMap)
      channel._2.unsubscribe(connection)
  }

  /**
   * Publish event to client directly. It is called by Pusher external command
   * @param event event object
   */
  def publishEventByServer(event:JsonObject)
  {
    val channel = channelMap get event.getString("event")
    if(channel.isEmpty) return
    channel.get.handleEvent(event.toString)
  }

  val clientTriggerPattern="client-.*"

  /**
   * publish event from client
   * @param connection the connection that sent
   * @param event event name
   * @param channelName channel name
   * @param data event data
   * @return true is success to send. false is not allow to send
   */
  def publishEventByClient(connection:Connection,event:String,channelName:String,data:JsonObject):Boolean=
  {
    val channel = (channelMap get channelName).get

    if(!(channel.connections contains connection)) return false
    if(!event.matches(clientTriggerPattern)) return false
    //if(!isAllowTrigger) return false

    channel.publishEvent(event,data)
  }

  val privatePattern = "(private-.*)".r
  val presencePattern = "(presence-.*)".r

  /**
   * make new channel
   * @param channelName name of new channel
   * @return new channel
   */
  def makeChannel(channelName:String) = channelName match{
    case privatePattern(c) =>{
      //private channel
      System.out.println("private channel Create")
      new BaseChannel(channelName) with PrivateChannel
    }
    case presencePattern(c) if Pusher.redis_enable => {
      //presence channel with redis
      System.out.println("presence channel Create with redis!!")
      new BaseChannel(channelName)  with PresenceChannel with PrivateChannel with RedisPresenceData
    }

    case presencePattern(c) => {
      //presence channel with shared data
      System.out.println("presence channel Create with SharedData")
      new BaseChannel(channelName)  with PresenceChannel with PrivateChannel with SharedPresenceData
    }
    case _ =>{
      //public channel
      System.out.println("public channel Create")
      new BaseChannel(channelName)
    }
  }

  /**
   * send subscription failed message
   * @param connection the connection to send
   * @param msg the detail error message
   */
  def subscriptionFiled(connection:Connection,msg:String)
  {
    connection.socket.writeTextFrame(Event.error(msg,301).toString)
  }

  /**
   * send subscription success event
   * @param connection the connection to send
   * @param channelName name of channel
   * @param data other information
   */
  def subscriptionSuccess(connection:Connection,channelName:String,data:JsonObject)
  {
    connection.socket.writeTextFrame(Event.subscribeSuccess(channelName,data).toString)
  }

}

