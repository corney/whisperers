package ru.corney.whisperers.whisperer

import akka.actor.{ActorContext, ActorPath, ActorRef, ActorSelection, RootActorPath}
import akka.cluster.Member
import ru.corney.whisperers.whisperer.Message.Whisper

/**
  * Created by corney on 14.10.16.
  */
trait Subscriber {

  var members = Map.empty[ActorPath, ActorSelection]

  def register(member: Member)(implicit context: ActorContext, self: ActorRef) {
    val path = RootActorPath(member.address) / "user" / Whisperer.Name
    if (!members.contains(path)) {

      val selection = context.actorSelection(path)
      val selfSelection = context.actorSelection(self.path)
      if (selection != selfSelection) {
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
