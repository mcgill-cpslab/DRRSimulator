package entity

import scala.collection.mutable.ListBuffer

import com.typesafe.config.Config

class RouterWithDRR(conf: Config, queues: ListBuffer[FlowQueue]) extends Router(conf, queues) {
  override def receiveNewFlow(flow: Flow): Unit = {
    
    
  }

  override def scheduleFlow(): Unit = {
    
    
  }
}
