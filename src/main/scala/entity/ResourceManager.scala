package entity

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import event.WaveEnd
import report.Reporter
import simulator.Simulator

// the general resource manager
abstract class ResourceManager(private val resourceCount: Long,
                               val requestQueue: 
                               mutable.HashMap[String, mutable.Queue[ResourceRequest]]) 
  extends Reporter {
  
  //TODO: make it to be a resource specific variable
  // the duration length for finishing one workload unit (packet) for each resource unit
  val unitWorkload = Simulator.conf.getLong("simulator.unitWorkload")
  
  // decides the moment at which the request is allocated resource and the moments which the
  // requests are finished (do not need to insert to the event queue in the simulator)
  def receiveNewRequest(request: ResourceRequest)
  
  def schedule()

  def report()

  def endRequest(request: String)
}


object ResourceManager {
  
  def apply(resourceManagerName: String, resourceCount: Int): ResourceManager = 
    resourceManagerName match {
      case "DRR" => new DRRResourceManager(resourceCount, 
        new mutable.HashMap[String, mutable.Queue[ResourceRequest]])
      case _ => null
  }
}

