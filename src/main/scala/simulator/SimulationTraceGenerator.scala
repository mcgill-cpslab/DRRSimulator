package simulator

import event.Event

class SimulationTraceGenerator {
  def initTicketTrace(): Seq[Event] = {
    null
  }
}

object SimulationTraceGenerator {
  def apply(generatorName: String): SimulationTraceGenerator = generatorName match {
    case _ => new SimulationTraceGenerator
  }
}
