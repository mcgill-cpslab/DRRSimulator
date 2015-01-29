package entity

import scala.collection.mutable

import report.Reporter

// the general resource manager
class ResourceManager(private val resourceCount: Long, 
                      requestQueue: mutable.ListBuffer[ResourceRequest]) extends Reporter {

  // decides the moment at which the request is allocated resource and the moments which the 
  // requests are finished (do not need to insert to the event queue in the simulator)
  def receiveNewRequest(request: ResourceRequest): Unit ={

  }

  override def report(): Unit = {
    
  }
}
