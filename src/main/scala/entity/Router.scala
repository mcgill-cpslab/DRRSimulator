package entity

import simulator.Simulator

abstract class Router {
  
  val resourceCount = Simulator.conf.getInt("simulator.resource.resourceCount")
  
  val resourceManagers: Seq[ResourceManager] = null
  
  def receiveNewFlow(flow: Flow)

  def scheduleFlow()
}
