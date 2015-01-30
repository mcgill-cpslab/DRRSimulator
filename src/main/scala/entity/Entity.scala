package entity

import scala.collection.mutable

import event.WaveEnd

case class Packet(size: Int)

case class Flow(id: String, packets: mutable.Queue[Packet])

//requestDemand => cores
//allocatedResources => cores
//remainingWorkload => time * cores
class ResourceRequest(val requester: Flow, var requestDemand: Int, var allocatedResources: Int,
                      var submitTime: Long, var startTime: Long, var finishTime: Long,
                      var lastAllocationTime: Long, var waveEndEvent: WaveEnd = null,
                      var remainingWorkload: Long)

