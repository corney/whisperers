package ru.corney.whisperers

import akka.actor.{ActorSystem, Props}
import ch.qos.logback.classic.LoggerContext
import com.typesafe.config.ConfigFactory
import org.slf4j.{LoggerFactory, MDC}

/**
  * Created by user on 10/14/16.
  */
object App {
  def main(args: Array[String]): Unit = {



    if (args.isEmpty) {
      startup(Seq("2551", "2552", "0"))
    } else
      startup(args)
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>

      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
        withFallback(ConfigFactory.load())

      val system = ActorSystem("WhispererCluster", config)

      system.actorOf(Props[Whisperer], name = Whisperer.Name)
    }
  }

}