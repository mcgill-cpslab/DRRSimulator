package entity

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import event.WaveEnd
import simulator.Simulator

//currently, we allocate a new queue for each request
class DRRResourceManager(resourceCount: Int,
                         requestQueue: mutable.HashMap[String, mutable.Queue[ResourceRequest]])
  extends ResourceManager(resourceCount, requestQueue) {
  
  val deficitsRecords = new mutable.HashMap[String, Long]
  var activeList = List[String]()
  val quantum = Simulator.conf.getInt("simulator.drr.quantum")
  
  private val waitingQueue = new mutable.Queue[ResourceRequest]
  
  private var availableResources = resourceCount
  
  // decides the moment at which the request is allocated resource and the moments which the request
  // is finished
  override def receiveNewRequest(request: ResourceRequest): Unit = {
    deficitsRecords += request.requester.id -> 0L
    requestQueue.getOrElseUpdate(request.requester.id,
      new mutable.Queue[ResourceRequest]) += request
    activeList = activeList :+ request.requester.id
  }

  // actually this function is deciding when the request can enter into the waiting queue
  override def schedule(): Unit = {
    val allocatedQueues = new mutable.HashSet[String]
    for (activeQueueID <- activeList) {
      val requester = requestQueue(activeQueueID).head
      val demand = requester.requestDemand
      deficitsRecords(activeQueueID) += quantum
      if (allowToAllocate(activeQueueID, demand)) {
        allocatedQueues += activeQueueID
        deficitsRecords(activeQueueID) -= demand
        allocateResource(Some(requester))
        requester.submitTime = Simulator.currentTime
      }
    }
    activeList = activeList.filterNot(allocatedQueues.contains)
  }
  
  private def allowToAllocate(activeQueueID: String, demand: Int): Boolean = {
    demand <= deficitsRecords(activeQueueID)
  }

  //TODO: should be more flexible to support multiple resource allocation policy instead of 
  // simply comparing available resources and demand
  private def allocateResource(request: Option[ResourceRequest]): Unit = {
    // scheduling WaveEnd
    // if last wave, update finish time
    request.foreach(req => waitingQueue.enqueue(req))
    while (!waitingQueue.isEmpty) {
      val demandingRequest = waitingQueue.head
      if (availableResources > 0) {
        val allocateAmount = math.min(
          demandingRequest.requestDemand - demandingRequest.allocatedResources,
          availableResources)
        demandingRequest.allocatedResources += allocateAmount
        availableResources -= allocateAmount
        //reschedule waveend
        rescheduleWaveEnd(demandingRequest)
        if (demandingRequest.allocatedResources == demandingRequest.requestDemand) {
          waitingQueue.dequeue()
        }
      } else {
        return
      }
    }
  }
  
  override def endRequest(request: String): Unit = {
    requestQueue(request).head.finishTime = Simulator.currentTime
    availableResources += requestQueue(request).head.allocatedResources
    allocateResource(None)
  }
  
  private def rescheduleWaveEnd(request: ResourceRequest): Unit = {
    if (request.waveEndEvent != null) {
      val currentSimulationTime = Simulator.currentTime
      request.remainingWorkload -= (currentSimulationTime - request.lastAllocationTime) *
        request.allocatedResources
    }
    request.waveEndEvent = WaveEnd(this, request.requester.id,
      request.remainingWorkload / request.allocatedResources + Simulator.currentTime)
    request.lastAllocationTime = Simulator.currentTime
    Simulator.enqueue(request.waveEndEvent)
  }
  
  

  override def report(): Unit = {
    
  }
}
