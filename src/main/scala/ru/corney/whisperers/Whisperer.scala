package ru.corney.whisperers

import akka.actor.{Actor, ActorLogging, ActorPath, ActorSelection, RootActorPath}
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp}
import akka.cluster.{Cluster, Member}
import ru.corney.whisperers.Message.{Tick, Whisper}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by user on 10/14/16.
  */
class Whisperer extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  override def preStart() =
    cluster.subscribe(self, classOf[MemberUp], classOf[MemberRemoved])

  override def postStop() = cluster.unsubscribe(self)

  var cancellable =
    context.system.scheduler.schedule(1000 milliseconds,
      3000 milliseconds,
      self,
      Tick)

  var members = Map.empty[ActorPath, ActorSelection]

  var whispers = 0l

  def receive = {
    case MemberUp(member) =>
      register(member)
    case MemberRemoved(member, status) =>
      log.info("Member left: " + member)
    case Whisper =>
      whispers += 1
      log.info("Whisper: " + whispers)
    case Tick =>
      tick()
    case msg =>
      log.info("Unknown message: " + msg)
  }

  def register(member: Member) {
    val path = RootActorPath(member.address) / "user" / Whisperer.Name
    if (! members.contains(path)) {

      val selection = context.actorSelection(path)
      val selfSelection = context.actorSelection(self.path)
      if (selection != selfSelection) {
        log.info("Member up: " + path)
        members += path -> selection
      }

    }
  }

  def unregister(member: Member) {
    val path = RootActorPath(member.address) / "user" / Whisperer.Name
    members -= path
  }

  def tick() {
    for {
      selection <- members.values
    } {
      selection ! Whisper
    }
  }
}

object Whisperer {
  val Name = "whisperer"
}