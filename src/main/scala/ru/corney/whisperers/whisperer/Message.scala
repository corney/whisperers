package ru.corney.whisperers.whisperer

/**
  * Created by user on 10/14/16.
  */
object Message {
  case object Whisper
  case object Tick
  case object Purge
  case object AskMetrics
  case class Metrics(msgProcessed: Int)
  case class SetDelay(delay: Int)
  case class ScheduleWhisperers(n: Int)
  case object RemoveWhisperer

}
