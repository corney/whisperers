package ru.corney.whisperers

import akka.actor.{Actor, ActorLogging, ActorPath, ActorSelection, RootActorPath}
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp}
import akka.cluster.{Cluster, Member}
import ru.corney.whisperers.Message.{Purge, Tick, Whisper}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by user on 10/14/16.
  */
class Whisperer extends Actor with ActorLogging with Subscriber {

  val cluster = Cluster(context.system)

  override def preStart() =
    cluster.subscribe(self, classOf[MemberUp], classOf[MemberRemoved])

  override def postStop() = cluster.unsubscribe(self)


    context.system.scheduler.schedule(1000 milliseconds,
      50 milliseconds,
      self,
      Purge)

  var cancellable =
    context.system.scheduler.schedule(0 milliseconds,
      10 milliseconds,
      self,
      Tick)



  var whispers = Seq.empty[Long]

  def receive = {
    case MemberUp(member) =>
      register(member)
    case MemberRemoved(member, status) =>
      log.info("Member left: " + member)
    case Whisper =>
      whisp()
    case Tick =>
      tick()
    case Purge =>
      purge()
    case msg =>
      log.info("Unknown message: " + msg)
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
}