package kr.soma.realtimemessage.channel

import org.vertx.scala.core.json.{JsonObject, JsonArray,Json}
import org.vertx.scala.core.eventbus.Message
import kr.soma.realtimemessage.Pusher
import scala.collection.JavaConversions

trait RedisPresenceData extends PresenceData{

  lazy val hashName = Pusher.sharedData_prefix+channelName;
  def redisSetDataHandler(msg:Message[JsonObject])
  {
    if(msg.body.getString("status").equals("error"))
    {
      printf("Redis Error :: "+msg.body.getString("message"))
    }
  }

  def redisGetDataHandler(callback:(JsonObject=>Unit))(msg:Message[JsonObject])
  {
    if(msg.body.getString("status").equals("error"))
    {
      printf("Redis Error :: "+msg.body.getString("message"))
      return
    }
    val value = JavaConversions.mapAsScalaMap(msg.body.getObject("value").toMap)
    val ids:JsonArray = new JsonArray
    val hash:JsonObject = new JsonObject
    for(i<-value){
      ids.add(i._1)
      hash.putObject(i._1,new JsonObject(i._2.toString))
    }
    val jsonData = Json.obj("ids"->ids,"hash"->hash,"count"->ids.size)
    callback(jsonData)
  }

  override def addMember(id:String,userInfo:JsonObject)
  {
    val data = Json.obj("command"->"hset","args"->Json.arr(hashName,id,userInfo.toString))
    eventBus.send(Pusher.redis_address,data,redisSetDataHandler _)
  }

  override def removeMember(id:String)
  {
    val data = Json.obj("command"->"hdel","args"->Json.arr(hashName,id))
    eventBus.send(Pusher.redis_address,data,redisSetDataHandler _)
  }

  override def presenceData(callback:(JsonObject=>Unit))
  {
    val data = Json.obj("command"->"hgetall","args"->Json.arr(hashName))
    eventBus.send(Pusher.redis_address,data,redisSetDataHandler _)
  }
}
