package ru.corney.whisperers.manager

import akka.actor.{ActorSystem, Props, RootActorPath}
import akka.cluster.Member
import com.typesafe.config.ConfigFactory
import ru.corney.whisperers.whisperer.Message.{ScheduleWhisperers, Tick}
import ru.corney.whisperers.whisperer.Whisperer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by corney on 19.10.16.
  */
class ScheduleWhisperersManager(n: Int) extends AbstractManager {

  val Ttl = 2000

  var timer = context.system.scheduler.scheduleOnce(Ttl milliseconds, self, Tick)

  var sent = false

  override def process(whisperer: Member) {
    if (! sent) {
      sent = true
      timer.cancel()
      val selection = context.actorSelection(RootActorPath(whisperer.address) / "user" / Whisperer.Name)
      selection ! ScheduleWhisperers(n)

      log.info("Scheduling %d whisperers".format(n))
      self ! Tick
    }
  }

  override def handleDefault(msg: Any) {
    msg match {
      case Tick =>
        timer.cancel()
        context.stop(self)
        context.system.terminate()
      case _ =>
      //
    }
  }
}

object ScheduleWhisperersManager {
  val Name = "ScheduleWhisperersManager"

  def schedule(n: Int) {
    val config =
      ConfigFactory.parseString("akka.remote.netty.tcp.port=0")
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [manager]"))
        .withFallback(ConfigFactory.load())
    val system = ActorSystem("WhispererCluster", config)

    system.actorOf(Props(new ScheduleWhisperersManager(n)), name = Name)
  }
}
