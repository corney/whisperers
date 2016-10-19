package ru.corney.whisperers.manager

import akka.actor.{Actor, ActorLogging}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent.{ClusterDomainEvent, CurrentClusterState, MemberUp}

/**
  * Created by corney on 19.10.16.
  */
abstract class AbstractManager extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  override def preStart() =
    cluster.subscribe(self, classOf[MemberUp])

  override def postStop() = cluster.unsubscribe(self)


  def receive = {
    case MemberUp(member) =>
      if (member.roles.contains("whisperer")) {
        process(member)
      }
    case msg =>
      handleDefault(msg)
  }
  def process(whisperer: Member)

  def handleDefault(msg: Any): Unit
}
