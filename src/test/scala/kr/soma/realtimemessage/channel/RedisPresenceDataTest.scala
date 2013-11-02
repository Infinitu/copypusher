package kr.soma.realtimemessage.channel

import org.vertx.testtools.VertxAssert._
import org.junit._
import kr.soma.realtimemessage._
import org.vertx.scala.core.json.{JsonObject, Json}
import org.vertx.scala.core.eventbus.Message
import org.vertx.scala.testtools._

/**
 * Created with IntelliJ IDEA.
 * User: infinitu
 * Date: 2013. 11. 2.
 * Time: 오후 4:40
 * To change this template use File | Settings | File Templates.
 */
class RedisPresenceDataTest extends TestVerticle{

  val REDIS_ADDRESS = "REDIS_ADDRESS"
  val REDIS_HOST = "localhost"
  val REDIS_PORT = "6379"
  def createConnection(callback:(String)=>Unit)=new Connection("testid",new MockServerWebSocket(callback))

  @Test def redisModuleTest()
  {
    val redis_config = Json.obj("address"->REDIS_ADDRESS,"host"->REDIS_HOST,"port"->REDIS_PORT)
    container.deployModule("io.vertx~mod-redis~1.1.2",redis_config,1,
    {result=>
      println(result.succeeded())
      println(result.result())
      vertx.eventBus.send(REDIS_ADDRESS,Json.obj("command"->"set","args"->Json.arr("redisTest","hello,redis!!")),
      {msg:Message[JsonObject]=>
        print(msg.body())
        vertx.eventBus.send(REDIS_ADDRESS,Json.obj("command"->"get","args"->Json.arr("redisTest")),
        {msg:Message[JsonObject]=>
          print(msg.body())
          assertEquals(msg.body(),"hello,redis!!")
          testComplete()
        })
      })
      //assertTrue(result.succeeded())
      //if(!result.succeeded()) testComplete()
    })
  }
}
