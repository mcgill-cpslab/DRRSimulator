package entity

import scala.collection.mutable.ListBuffer

import com.typesafe.config.Config

abstract class Router(conf: Config, queues: Array[FlowQueue]) {
  
  val resourceCount = conf.getInt("simulator.resource.resourceCount")
  
  val resourceManagers = Array.fill(conf.getInt("simulator.entity.router.resourceManagerNumber"))(
    new ResourceManager(resourceCount, new ListBuffer[ResourceRequest]()))
  
  def receiveNewFlow(flow: Flow)

  def scheduleFlow()
}
