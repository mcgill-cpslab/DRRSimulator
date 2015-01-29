package simulator

import com.typesafe.config.Config
import event.Event

class SimulationTraceGenerator(conf: Config) {
  def generateTrace(): Seq[Event] = {
    Seq[Event]()
  }
}

object SimulationTraceGenerator {
  def apply(generatorName: String, conf: Config): SimulationTraceGenerator = generatorName match {
    case "video" => new VideoTraceGenerator(conf)
    case _ => new SimulationTraceGenerator(conf)
  }
}
