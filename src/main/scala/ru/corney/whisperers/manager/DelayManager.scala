package ru.corney.whisperers.manager

import akka.actor.{ActorSystem, Props, RootActorPath}
import akka.cluster.Member
import com.typesafe.config.ConfigFactory
import ru.corney.whisperers.whisperer.Message.{SetDelay, Tick}
import ru.corney.whisperers.whisperer.Whisperer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by corney on 19.10.16.
  */
class DelayManager(delay: Int) extends AbstractManager {

  val TtlBeforeFirstMember = 1000
  val TtlBetweenMembers = 200

  var timer = context.system.scheduler.scheduleOnce(TtlBeforeFirstMember milliseconds, self, Tick)

  var count = 0

  override def process(whisperer: Member) {
    count += 1
    timer.cancel()
    timer = context.system.scheduler.scheduleOnce(TtlBetweenMembers milliseconds, self, Tick)
    val selection = context.actorSelection(RootActorPath(whisperer.address) / "user" / Whisperer.Name)
    selection ! SetDelay(delay)
  }

  override def handleDefault(msg: Any) {
    msg match {
      case Tick =>
        timer.cancel()
        log.info("SetDelay(%d) sent to %d whisperers".format(delay, count))
        context.stop(self)
        context.system.terminate()
      case _ =>
      //
    }
  }
}

object DelayManager {
  val Name = "DelayManager"

  def setDelay(delay: Int) {
    val config =
      ConfigFactory.parseString("akka.remote.netty.tcp.port=0")
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [manager]"))
        .withFallback(ConfigFactory.load())
    val system = ActorSystem("WhispererCluster", config)

    system.actorOf(Props(new DelayManager(delay)), name = Name)
  }
}


