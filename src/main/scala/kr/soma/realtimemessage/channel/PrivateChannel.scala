package kr.soma.realtimemessage.channel

import kr.soma.realtimemessage.{Event, Connection, Pusher}
import org.vertx.scala.core.eventbus.Message
import org.vertx.scala.core.json.JsonObject
import scala.util.continuations._
import scala.collection.mutable

/**
 * Created with IntelliJ IDEA.
 * User: infinitu
 * Date: 2013. 10. 25.
 * Time: 오전 2:19
 * To change this template use File | Settings | File Templates.
 */

/**
 * A trait for private channel
 * It can be used with BasedChannel
 */
trait PrivateChannel extends BaseChannel
{
  // the auth data for this channel on whole Verticle
  val authMap = mutable.HashMap[String,JsonObject]()
  override def subscribe(connection:Connection,auth:String, data:JsonObject)
  {
    val authEvent =Event.internalAuth(connection.socketid,channelName,auth)
    val handler = authCallback(connection,auth,data) _
    eventBus.send(Pusher.authorizationChannel,authEvent,handler)
  }

  /**
   * The callback method of authorization request.
   * @param connection the connection that try to subscribe this channel
   * @param auth the auth information
   * @param data other information for subscribe
   * @param msg reply message from authorization module
   */
  def authCallback(connection:Connection,auth:String, data:JsonObject)(msg:Message[JsonObject])
  {
    val result =msg.body.getBoolean("result")
    val authData = msg.body.getObject("data")
    if(result)
    {
      authMap+=connection.socketid->authData
      super.subscribe(connection,auth,data)
      return
    }
    subscriptionFiled(connection,"authorization failed")
  }

}

