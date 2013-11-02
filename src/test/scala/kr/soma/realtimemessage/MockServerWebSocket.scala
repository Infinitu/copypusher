package kr.soma.realtimemessage

import org.vertx.scala.core.http._


/**
 * Created with IntelliJ IDEA.
 * User: infinitu
 * Date: 2013. 11. 1.
 * Time: 오후 11:30
 * To change this template use File | Settings | File Templates.
 */
class MockServerWebSocket(writeCallback:(String)=>Unit) extends ServerWebSocket(null){
  override def writeTextFrame(str: String): this.type =
  {
    writeCallback(str)
    this
  }
}
