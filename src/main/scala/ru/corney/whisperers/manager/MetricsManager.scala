package ru.corney.whisperers.manager

import akka.actor.{ActorSystem, Cancellable, PoisonPill, Props, RootActorPath}
import akka.cluster.Member
import com.typesafe.config.ConfigFactory
import ru.corney.whisperers.whisperer.Message.{AskMetrics, Metrics, Tick}
import ru.corney.whisperers.whisperer.Whisperer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by corney on 19.10.16.
  */
class MetricsManager extends AbstractManager {
  val TtlBeforeFirstMember = 1000
  val TtlBetweenMembers = 200

  var total = 0
  var count = 0

  var timer = context.system.scheduler.scheduleOnce(TtlBeforeFirstMember milliseconds, self, Tick)


  override def process(whisperer: Member) {
    timer.cancel()
    timer = context.system.scheduler.scheduleOnce(TtlBetweenMembers milliseconds, self, Tick)

    val selection = context.actorSelection(RootActorPath(whisperer.address) / "user" / Whisperer.Name)
    selection ! AskMetrics
  }

  override def handleDefault(msg: Any) {
    msg match {
      case Metrics(msgProcessed) =>
        total += msgProcessed
        count += 1
      case Tick =>
        val avg = if (count > 0) total / count else 0
        log.info("Average messages processed on %d nodes is %d".format(count, avg))

        context.stop(self)
        context.system.terminate()
        case _ =>
        //
    }
  }
}

object MetricsManager {
  val Name = "MetricsManager"

  def askForMetrics() {
    val config =
      ConfigFactory.parseString("akka.remote.netty.tcp.port=0")
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [manager]"))
        .withFallback(ConfigFactory.load())
    val system = ActorSystem("WhispererCluster", config)

    system.actorOf(Props[MetricsManager], name = Name)
  }
}
