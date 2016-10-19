package ru.corney.whisperers.manager

import akka.actor.{ActorSystem, Props, RootActorPath}
import akka.cluster.Member
import com.typesafe.config.ConfigFactory
import ru.corney.whisperers.whisperer.Message.{RemoveWhisperer, ScheduleWhisperers, Tick}
import ru.corney.whisperers.whisperer.Whisperer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by corney on 19.10.16.
  */
class RemoveWhisperersManager(n: Int) extends AbstractManager {

  val TtlBeforeFirstMember = 2000
  val TtlBetweenMembers = 200

  var count = n

  var timer = context.system.scheduler.scheduleOnce(TtlBeforeFirstMember milliseconds, self, Tick)

  override def process(whisperer: Member) {
    timer.cancel()
    val selection = context.actorSelection(RootActorPath(whisperer.address) / "user" / Whisperer.Name)
    selection ! RemoveWhisperer

    count -= 1

    if (count > 0) {
      timer = context.system.scheduler.scheduleOnce(TtlBetweenMembers milliseconds, self, Tick)
    } else {
      log.info("%d whisperers removed".format(n))
      context.stop(self)
      context.system.terminate()
    }

  }

  override def handleDefault(msg: Any) {
    msg match {
      case Tick =>
        timer.cancel()
        log.error("Timeout expired, exiting. %d whisperers removed".format(n - count))
        context.stop(self)
        context.system.terminate()
      case _ =>
      //
    }
  }
}

object RemoveWhisperersManager {
  val Name = "RemoveWhisperersManager"

  def remove(n: Int) {
    val config =
      ConfigFactory.parseString("akka.remote.netty.tcp.port=0")
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [manager]"))
        .withFallback(ConfigFactory.load())
    val system = ActorSystem("WhispererCluster", config)

    system.actorOf(Props(new RemoveWhisperersManager(n)), name = Name)
  }
}
