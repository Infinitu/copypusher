package kr.soma.realtimemessage.channel

import kr.soma.realtimemessage.{Pusher, Event, Connection}
import org.vertx.scala.core.json._
import org.vertx.scala.core.json.JsonObject
import scala.collection.{immutable, mutable}

/**
 * Created with IntelliJ IDEA.
 * User: infinitu
 * Date: 2013. 10. 25.
 * Time: 오전 2:19
 * To change this template use File | Settings | File Templates.
 */

/**
 * A trait for Presence Channel
 * It can be used with BaseChannel
 */
trait PresenceChannel extends BaseChannel with PresenceData
{
  val connectionHash = mutable.HashMap[Connection,String]()


  /**
   * subscribe this channel
   * @param connection the connection that try to subscribe this channel
   * @param auth the auth information
   * @param channelData other information for subscribe
   */
  override def subscribe(connection:Connection,auth:String, channelData:JsonObject)
  {
    print(channelData)

    publishMemberAdded(channelData)

    super.subscribe(connection,auth,channelData)
    val id = channelData.getString("user_id")
    val info = channelData.getObject("user_info")
    addMember(id,info)

    connectionHash += connection->id

  }


  /**
   * unsubscribe this channel
   * @param connection the connection that try to unsubscribe
   */
  override def unsubscribe(connection: Connection)
  {
    val id = connectionHash get connection
    connectionHash-=connection
    removeMember(id.get)
    super.unsubscribe(connection)
  }

  /**
   * send member added event
   * @param data event data
   */
  def publishMemberAdded(data:JsonObject)
  {
    publishEvent(Event.memberAdded(channelName,data).toString)
  }

  /**
   * send member removed event
   * @param data event data
   */
  def publishMemberRemoved(data:JsonObject)
  {
    publishEvent(Event.memberRemoved(channelName,data).toString)
  }

  def sendPresenceData(connection: Connection, data: JsonObject)(presence:JsonObject)
  {
    data.putObject("presence",presence)
    super.sendSubscribeSuccessMessage(connection,data)
  }

  override def sendSubscribeSuccessMessage(connection: Connection, data: JsonObject)
  {
    presenceData(sendPresenceData(connection,data))
  }
}
