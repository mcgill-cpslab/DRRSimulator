package simulator

import java.io.File

import scala.collection.mutable

import com.typesafe.config.{Config, ConfigFactory}
import event._
import report.Reporter

/**
 * the main class of the simulator 
 * @param startTime when the start time is larger than 0, the events happening between 0 and 
 *                  startTime - 1 will be ignored                    
 * @param endTime the endTime of the simulation
 */
class Simulator(startTime: Long, endTime: Long, traceGenerator: SimulationTraceGenerator) {
  val eventQueue = new mutable.PriorityQueue[Event]
  var currentTime = 0L
  
  var validEndEvent = 0
  
  private def init(): Unit = {
    for (evt <- traceGenerator.generateTrace()) {
      eventQueue.enqueue(evt)  
    }
  }
  
  private def processEvent(event: Event) = event match {
    case event: FlowArrivalEvent => 
      event.router.receiveNewFlow(event.flow)
    case schedulingTick: SchedulingTick =>
      schedulingTick.router.scheduleFlow()
    case endEvent: WaveEnd =>
      if (endEvent == endEvent.resourceManager.requestQueue(endEvent.requestId).head.waveEndEvent) {
        endEvent.resourceManager.endRequest(endEvent.requestId)
        validEndEvent += 1
      }
    case _ => //nop
  }
  
  def enqueue(e: Event): Unit = eventQueue.enqueue(e)
  
  def run(): Unit = {
    while (currentTime < endTime &&  validEndEvent < 12790) {
      try {
        val event = eventQueue.dequeue()
        currentTime  = event.evtMoment
        processEvent(event)
      } catch {
        case e: Exception => 
          e.printStackTrace()
      }
    }
    //report the simulation result
    for (reporter <- Reporter.getReporters) {
      reporter.report()
    }
  }
  //init the simulator
  init()
}

object Simulator {
  
  private var simulatorInstance: Simulator = null
  var conf: Config = null
  
  def currentTime: Long = simulatorInstance.currentTime
  
  def enqueue(e: Event): Unit = {
    if (simulatorInstance != null) {
      simulatorInstance.enqueue(e)
    }
  }
  
  def main(args: Array[String]): Unit = {
    if (args.length != 1) {
      println("Usage: program configuration_path")
      sys.exit(1)
    } 
    conf = ConfigFactory.parseFile(new File(args(0)))
    val startTime = conf.getLong("simulator.simulation.startTime")
    val endTime = conf.getLong("simulator.simulation.endTime")
    
    val traceGenerator = SimulationTraceGenerator(
      conf.getString("simulator.simulation.generatorName"))
    
    simulatorInstance = new Simulator(startTime, endTime, traceGenerator)
    simulatorInstance.enqueue(new EndSimulation(endTime))
    simulatorInstance.run()
  }
}

