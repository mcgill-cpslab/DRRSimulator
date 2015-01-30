package entity

import scala.util.Random

import com.typesafe.config.Config
import simulator.Simulator

class RouterWithDRR extends Router {
  
  override val resourceManagers = Seq.fill(Simulator.conf.getInt(
    "simulator.router.resourceManagerNumber"))(
    ResourceManager(resourceManagerName = "DRR",
      resourceCount = Simulator.conf.getInt("simulator.resource.resourceCount")))
  
  // TODO: make it more general to handle multi-packets flow
  override def receiveNewFlow(flow: Flow): Unit = {
    // build the resource request queue and assign that to the ResourceManager
    val resourceReq = new ResourceRequest(flow, flow.packets.head.size, allocatedResources = 0,
      submitTime = Simulator.currentTime, startTime = -1L, finishTime = -1L, 
      lastAllocationTime = -1L, waveEndEvent = null, 
      remainingWorkload = flow.packets.head.size * resourceManagers(0).unitWorkload)
    resourceManagers(Random.nextInt(resourceManagers.size)).receiveNewRequest(resourceReq)
  }

  override def scheduleFlow(): Unit = {
    // call the resourceManager to allocate the resources
    for (resourceManager <- resourceManagers) {
      resourceManager.schedule()
    }
  }
}
