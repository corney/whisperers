package ru.corney.whisperers.whisperer

import java.io.IOException
import java.net.{InetAddress, ServerSocket}

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp}
import com.typesafe.config.ConfigFactory
import ru.corney.whisperers.App
import ru.corney.whisperers.whisperer.Message._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._


/**
  * Created by user on 10/14/16.
  */
class Whisperer extends Actor with ActorLogging with Subscriber {

  val cluster = Cluster(context.system)

  var delay = 100

  override def preStart() =
    cluster.subscribe(self, classOf[MemberUp], classOf[MemberRemoved])

  override def postStop() = cluster.unsubscribe(self)


    context.system.scheduler.schedule(1000 milliseconds,
      50 milliseconds,
      self,
      Purge)

  var cancellable =
    scheduleTicker(delay)


  private def scheduleTicker(delay: Int): Cancellable = {
    context.system.scheduler.schedule(0 milliseconds,
      delay milliseconds,
      self,
      Tick)
  }

  var whispers = Seq.empty[Long]

  def receive = {
    case MemberUp(member) =>
      register(member)
    case MemberRemoved(member, status) =>
      log.info("Member left: " + member)
    case Whisper =>
      whisp()
    case AskMetrics =>
      sender() ! Metrics(whispers.size)
    case SetDelay(newDelay) if delay != newDelay =>
      reschedule(newDelay)
    case RemoveWhisperer =>
      context.stop(self)
      context.system.terminate()
    case ScheduleWhisperers(n) =>
      Future {
        0 until n foreach (
          _ => Whisperer.launchWhisperer(0)
          )
      }
    case Tick =>
      tick()
    case Purge =>
      purge()
    case msg =>
      log.debug("Ignoring message: " + msg)
  }


  def reschedule(newDelay: Int): Unit = {
    delay = newDelay
    cancellable.cancel()
    cancellable = scheduleTicker(newDelay)
  }

  def whisp() {
    val timestamp = System.currentTimeMillis()
    whispers :+= timestamp
    log.debug("Whisper: " + whispers.size)

  }

  def purge() {
    val ttl = System.currentTimeMillis() - 1000 // секунда в миллисекундах
    whispers = whispers.dropWhile(timestamp => timestamp <= ttl)
  }
}

object Whisperer {
  val Name = "whisperer"

  def isPortAvailable(port: Int): Boolean = {
    try {
      val ss = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"))
      try {
        ss.setReuseAddress(true)
      } finally {
        ss.close()
      }
      true
    } catch {
      case _: IOException =>
        false
    }
  }

  def launchWhisperers(n: Int) {
    if (n > 0) {
      val firstPort =
        if (isPortAvailable(2551))
          2551
        else if (isPortAvailable(2552))
          2552
        else 0

      launchWhisperer(firstPort)

      1 until n foreach( _ => launchWhisperer(0))
    }
  }

  def launchWhisperer(port: Int) {

    val config =
      ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [whisperer]"))
        .withFallback(ConfigFactory.load())
    val system = ActorSystem("WhispererCluster", config)

    system.actorOf(Props[Whisperer], name = Whisperer.Name)
  }

}