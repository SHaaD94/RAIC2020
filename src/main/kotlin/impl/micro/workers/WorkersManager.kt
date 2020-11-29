package impl.micro.workers

import impl.ActionProvider
import impl.myBuildings
import impl.myWorkers
import impl.resources
import impl.util.algo.distance
import impl.util.attackAction
import impl.util.repairAction
import model.EntityAction

object WorkersManager : ActionProvider {
    override fun provideActions(): Map<Int, EntityAction> {
        return assignWorkersResources() + repairBuildings()
    }

    private fun repairBuildings(): Map<Int, EntityAction> =
        myBuildings().filter { it.health != it.maxHP() }.flatMap { b ->
            myWorkers().sortedBy { it.distance(b) }.take(2).map { it.id to repairAction(it, b) }
        }.toMap()

    private fun assignWorkersResources(): Map<Int, EntityAction> =
        myWorkers().map { w -> w to resources().minByOrNull { w.distance(it) }!! }
            .map { (w, r) -> w.id to attackAction(r) }
            .toMap()
}