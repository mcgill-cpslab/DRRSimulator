package event

import entity.{ResourceManager, Flow, Router}

class Event(val evtMoment: Long) extends Ordered[Event] {
  override def compare(that: Event): Int = {
    //(evtMoment - that.evtMoment).toInt
    if (evtMoment > that.evtMoment) {
      -1
    } else {
      if (evtMoment == that.evtMoment) {
        0    
      } else {
        1
      }
    }
  }
}

case class FlowArrivalEvent(flow: Flow, router: Router, moment: Long) extends Event(moment)

/**
 * the tick trigger the scheduling of the router
 */
case class SchedulingTick(router: Router, moment: Long) extends Event(moment)

case class EndSimulation(moment: Long) extends Event(moment)

case class WaveEnd(resourceManager: ResourceManager, requestId: String, moment: Long)
  extends Event(moment)