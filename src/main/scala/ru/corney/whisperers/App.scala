package ru.corney.whisperers

import ru.corney.whisperers.manager.{DelayManager, MetricsManager, RemoveWhisperersManager, ScheduleWhisperersManager}
import ru.corney.whisperers.whisperer.Whisperer

/**
  * Created by user on 10/14/16.
  */
object App {
  def main(args: Array[String]): Unit = {

    Config.get(args) match {
      case Some(config) =>
        if (config.numWhisperers > 0)
          Whisperer.launchWhisperers(config.numWhisperers)
        else {
          if (config.askForMetrics)
            MetricsManager.askForMetrics()

          if (config.scheduleWhisperers > 0)
            ScheduleWhisperersManager.schedule(config.scheduleWhisperers)
          else if (config.removeWhispers > 0)
            RemoveWhisperersManager.remove(config.removeWhispers)

          config.delay match {
            case Some(delay) =>
              DelayManager.setDelay(delay)
            case None =>
            //
          }
        }
      case None =>
      // Do nothing
    }
  }
}