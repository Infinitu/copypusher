package kr.soma.realtimemessage.channel

import kr.soma.realtimemessage.Pusher
import org.vertx.scala.core.json.{JsonObject,JsonArray,Json}
import scala.collection.JavaConversions


trait SharedPresenceData extends PresenceData{

  lazy val presenceMap = JavaConversions.mapAsScalaConcurrentMap(
    sharedData.getMap[String,String](Pusher.sharedData_prefix+channelName))

  override def addMember(id:String,userInfo:JsonObject)
  {
    presenceMap += id->userInfo.toString
  }

  override def removeMember(id:String)
  {
    presenceMap -= id
  }

  override def presenceData(callback:(JsonObject=>Unit))
  {
    val ids:JsonArray = new JsonArray
    val hash:JsonObject = new JsonObject
    for(i<-presenceMap){
      ids.add(i._1)
      hash.putObject(i._1,new JsonObject(i._2))
    }
    val jsonData = Json.obj("ids"->ids,"hash"->hash,"count"->ids.size)
    callback(jsonData)
  }
}
