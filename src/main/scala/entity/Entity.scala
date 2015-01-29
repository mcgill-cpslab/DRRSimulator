package entity

import scala.collection.mutable

case class Packet(size: Long)

case class Flow(packets: Array[Packet])

case class FlowQueue(queueName: String, queue: mutable.Queue[Flow])

class ResourceRequest(requester: Flow, requestDemand: Int, var startTime: Long,
                      var finishTime: Long)

