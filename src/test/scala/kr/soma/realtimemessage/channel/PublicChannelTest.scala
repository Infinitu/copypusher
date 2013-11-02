package kr.soma.realtimemessage.channel

import org.vertx.testtools.VertxAssert._
import org.junit._
import kr.soma.realtimemessage._
import org.vertx.scala.core.json.{JsonObject, Json}
import org.vertx.scala.testtools._

/**
 * Created with IntelliJ IDEA.
 * User: infinitu
 * Date: 2013. 11. 1.
 * Time: 오후 11:46
 * To change this template use File | Settings | File Templates.
 */
class PublicChannelTest extends TestVerticle{
  val CHANNEL_NAME = "CHANNEL_NAME"
  def createConnection(callback:(String)=>Unit)=new Connection("testid",new MockServerWebSocket(callback))


  @Test  def subscribeTest()
  {
    Pusher.eventBus_prefix="test"
    channel.eventBus = vertx.eventBus
    val testChannel = channel.makeChannel(CHANNEL_NAME)
    def callback(str:String){
      val result = Event("pusher_internal:subscription_succeeded",CHANNEL_NAME,"{}")
      assertEquals(result,new JsonObject(str))
      testComplete()
    }
    val connection = createConnection(callback _)
    testChannel.subscribe(connection,"",Json.obj())
    assertTrue(testChannel.connections.contains(connection))
  }

  @Test  def unsubscribeTest()
  {
    Pusher.eventBus_prefix="test"
    channel.eventBus = vertx.eventBus
    val testChannel = channel.makeChannel(CHANNEL_NAME)
    val connection = createConnection(null)
    testChannel.connections+=connection
    testChannel.unsubscribe(connection)
    assertFalse(testChannel.connections.contains(connection))
    testComplete()
  }

  @Test  def publishEventTest{
    Pusher.eventBus_prefix="test"
    channel.eventBus = vertx.eventBus
    val testChannel = channel.makeChannel(CHANNEL_NAME)
    val eventName = "testEvent"
    val data = "testData"
    val event = Event(eventName,data)
    def callback(str:String){
      assertTrue(new JsonObject(str).equals(event))
      testComplete()
    }
    val connection = createConnection(callback _)
    testChannel.connections+=connection
    testChannel.publishEvent(event.toString)
    testChannel.publishEvent(eventName,data)
  }


  //TODO Publish with EventBus!!
}
