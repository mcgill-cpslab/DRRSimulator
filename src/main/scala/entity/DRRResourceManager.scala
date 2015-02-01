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
  
  private var taskPool = new ListBuffer[ResourceRequest]

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
      }
    }
    activeList = activeList.filterNot(allocatedQueues.contains)
  }
  
  private def allowToAllocate(activeQueueID: String, demand: Int): Boolean = {
    if (Simulator.conf.getBoolean("simulator.drr.enable")) {
      demand <= deficitsRecords(activeQueueID)
    } else {
      true
    }
  }

  //TODO: should be more flexible to support multiple resource allocation policy instead of 
  // simply comparing available resources and demand
  private def allocateResource(request: Option[ResourceRequest]): Unit = {
    // scheduling WaveEnd
    // if last wave, update finish time
    request.foreach(req => taskPool += req)
    var poolPointer = 0
    while (!taskPool.isEmpty) {
      val demandingRequest = taskPool(poolPointer)
      if (availableResources > 0) {
        val allocateAmount = math.min(
          demandingRequest.requestDemand - demandingRequest.allocatedResources, 1)
        calculateRemainingWorkload(demandingRequest)
        addResources(demandingRequest, allocateAmount)
        //reschedule waveend
        if (demandingRequest.allocatedResources != 0 && allocateAmount != 0) {
          rescheduleWaveEnd(demandingRequest)
        }
        if (demandingRequest.startTime == -1) {
          //first time to get resources
          demandingRequest.startTime = Simulator.currentTime
        }
        if (demandingRequest.allocatedResources == demandingRequest.requestDemand ||
          demandingRequest.allocatedResources == resourceCount) {
          taskPool.remove(poolPointer)
        }
        if (poolPointer >= taskPool.size - 1) {
          poolPointer = 0
        } else {
          poolPointer += 1
        }
      } else {
        return
      }
    }
  }
  
  private def addResources(request: ResourceRequest, amount: Int): Unit = {
    request.allocatedResources += amount
    availableResources -= amount
  }
  
  override def endRequest(request: String): Unit = {
    requestQueue(request).head.finishTime = Simulator.currentTime
    availableResources += requestQueue(request).head.allocatedResources
    requestQueue(request).head.allocatedResources = 0
    allocateResource(None)
    /*if (!waitingQueue.isEmpty && waitingQueue.head == requestQueue(request).head) {
      waitingQueue.dequeue()
    }*/
  }
  
  private def calculateRemainingWorkload(request: ResourceRequest): Unit = {
    if (request.waveEndEvent != null) {
      val currentSimulationTime = Simulator.currentTime
      request.remainingWorkload -= (currentSimulationTime - request.lastAllocationTime) *
        request.allocatedResources
    }
  }
  
  private def rescheduleWaveEnd(request: ResourceRequest): Unit = {
    if (request.remainingWorkload == 0) {
      return
    }
    request.waveEndEvent = WaveEnd(this, request.requester.id,
      request.remainingWorkload / request.allocatedResources + Simulator.currentTime)
    request.lastAllocationTime = Simulator.currentTime
    Simulator.enqueue(request.waveEndEvent)
  }
  
  override def report(): Unit = {
    var finishedVideoCount = 0
    for ((queueId, requests) <- requestQueue) {
      val request = requests.dequeue()
      if (request.finishTime != -1) {
        println(s"${request.requester.id} ${request.requestDemand} ${request.submitTime} " +
          s"${request.startTime} ${request.finishTime}")
        finishedVideoCount += 1
      }
    }
    println(s"finishedCount = $finishedVideoCount")
  }
}
