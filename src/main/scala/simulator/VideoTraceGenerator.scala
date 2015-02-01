package simulator

import scala.collection.mutable
import scala.util.Random

import entity._
import event.{Event, FlowArrivalEvent, SchedulingTick}
import org.apache.commons.math3.distribution.PoissonDistribution
import report.Reporter

// the trace generator generate the flow arrival event based on the CC_WEB_VIDEO dataset
// flow size and poisson arrival pattern
// NOTE: map each video to a one-packet flow
class VideoTraceGenerator extends SimulationTraceGenerator {
  
  private val sizeBoundaries = new Array[Int](16)
  
  private val probablityBoundaries = new Array[Double](16)

  val mean = Simulator.conf.getDouble("simulator.poisson.poissonMean")
  val poissonDis = new PoissonDistribution(mean)
  
  val cluster = new RouterWithDRR
  
  val random = new Random(System.currentTimeMillis())
  
  private def init(): Unit = {
    
    sizeBoundaries(0) = 10
    sizeBoundaries(1) = 20
    sizeBoundaries(2) = 40
    sizeBoundaries(3) = 60
    sizeBoundaries(4) = 80
    sizeBoundaries(5) = 100
    sizeBoundaries(6) = 200
    sizeBoundaries(7) = 400
    sizeBoundaries(8) = 600
    sizeBoundaries(9) = 800
    sizeBoundaries(10) = 1000
    sizeBoundaries(11) = 2000
    sizeBoundaries(12) = 4000
    sizeBoundaries(13) = 6000
    sizeBoundaries(14) = 8000
    sizeBoundaries(15) = 10000

    probablityBoundaries(0) = 0.17501346
    probablityBoundaries(1) = 0.3087417
    probablityBoundaries(2) = 0.49273021
    probablityBoundaries(3) = 0.62735595
    probablityBoundaries(4) = 0.71261892
    probablityBoundaries(5) = 0.77364926
    probablityBoundaries(6) = 0.79339436
    probablityBoundaries(7) = 0.91725004
    probablityBoundaries(8) = 0.95045773
    probablityBoundaries(9) = 0.9639203
    probablityBoundaries(10) = 0.97289535
    probablityBoundaries(11) = 0.99084545
    probablityBoundaries(12) = 0.99362771
    probablityBoundaries(13) = 0.99542272
    probablityBoundaries(14) = 0.99811524
    probablityBoundaries(15) = 1.0

  }
   
  private def nextVideoSize(): Int = {
    val seed = random.nextDouble()
    var i = 0
    while (probablityBoundaries(i) < seed) {
      i += 1
    }
    val minimum = if (i != 0) sizeBoundaries(i - 1) else 0
    val maximum = sizeBoundaries(i)
    random.nextInt(maximum - minimum + 1) + minimum
  }
  
  override def generateTrace(): Seq[Event] = {
    val startTime = Simulator.conf.getLong("simulator.simulation.startTime")
    val endTime = Simulator.conf.getLong("simulator.simulation.endTime")
    var eventSeq = Seq[Event]()
    val unitTime = (endTime - startTime) / 12790
    //generate the flow arrival events
    for (i <- 0 until 12790) {
      val moment = unitTime * (i + 1) + startTime //evenly distribute workload
      val keyframeNum = nextVideoSize()
      val packet = new Packet(keyframeNum)
      val packetsQueue = new mutable.Queue[Packet]
      packetsQueue.enqueue(packet)
      eventSeq = eventSeq :+ new FlowArrivalEvent(new Flow(s"video-$i", packetsQueue), cluster,
        moment)
    }
    // generate the router scheduling events
    val schedulingInterval = Simulator.conf.getLong("simulator.router.schedulingInterval")
    for (i <- startTime until endTime by schedulingInterval) {
      eventSeq = eventSeq :+ new SchedulingTick(cluster, i)
    }
    eventSeq
  }
  
  init()
}
