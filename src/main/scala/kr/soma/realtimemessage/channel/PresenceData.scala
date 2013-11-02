package kr.soma.realtimemessage.channel

import org.vertx.scala.core.json._

/**
 * Created with IntelliJ IDEA.
 * User: infinitu
 * Date: 2013. 10. 31.
 * Time: 오후 8:38
 * To change this template use File | Settings | File Templates.
 */
trait PresenceData {
  val channelName:String

  def addMember(id:String,userInfo:JsonObject):Unit

  def removeMember(id:String):Unit

  def presenceData(callback:(JsonObject=>Unit)):Unit
}