package kr.soma.realtimemessage

import org.vertx.scala.core.eventbus.{Message, MessageData, EventBus}

/**
 * Created with IntelliJ IDEA.
 * User: infinitu
 * Date: 2013. 11. 1.
 * Time: 오후 11:37
 * To change this template use File | Settings | File Templates.
 */
class MockEventBus(val publishCallback:(String,String)=>Unit ) extends EventBus(null){

  override def send[T](address: String, value: T, handler: (Message[T]) => Unit)
                      (implicit evidence$3: (T) => MessageData): EventBus ={
    publishCallback(address,value.toString)
    this
  }

  override def publish[T](address: String, value: T)
                         (implicit evidence$1: (T) => MessageData): EventBus = {

    publishCallback(address,value.toString)
    this
  }
}
