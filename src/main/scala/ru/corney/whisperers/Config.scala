package ru.corney.whisperers


case class Config(
                   numWhisperers: Int = 0,
                   scheduleWhisperers: Int = 0,
                   removeWhispers: Int = 0,
                   delay: Option[Int] = None,
                   askForMetrics: Boolean = false
                 )

object Config {

  def get(args: Array[String]): Option[Config] = {
    new scopt.OptionParser[Config]("whisperer") {
      head("whisperer", "0.1")

      opt[Int]('w', "whisperer")
        .action((n, c) => c.copy(numWhisperers = n))
        .text("Start jvm with number of scheduled whisperers")


      opt[Int]('s', "schedule")
        .action((n, c) => c.copy(scheduleWhisperers = n))
        .text("DANGEROUS: Schedule whisperers on already started jvm")

      opt[Int]('r', "remove")
        .action((n, c) => c.copy(removeWhispers = n))
        .text("Remove whisperers from running jvm")

      opt[Int]('d', "delay")
        .action((d, c) => c.copy(delay = Some(d)))
        .text("Set delay between whispers, ms")

      opt[Unit]('m', "metrics")
        .action((_, c) => c.copy(askForMetrics = true))

    }.parse(args, Config())

  }
}