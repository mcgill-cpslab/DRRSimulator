package simulator

import com.typesafe.config.Config
import event.Event

class SimulationTraceGenerator {
  def generateTrace(): Seq[Event] = {
    Seq[Event]()
  }
}

object SimulationTraceGenerator {
  def apply(generatorName: String): SimulationTraceGenerator = generatorName match {
    case "video" => new VideoTraceGenerator
    case _ => new SimulationTraceGenerator
  }
}
