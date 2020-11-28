import impl.global.State
import impl.global.entityStats
import impl.global.initEntityStats
import impl.myBuildings
import impl.myWorkers
import impl.resources
import impl.util.algo.distance
import impl.util.buildUnit
import impl.util.moveAndAttackAction
import model.*

class MyStrategy {
    fun getAction(playerView: PlayerView, debugInterface: DebugInterface?): Action {
        initEntityStats(playerView)
        State.update(playerView)
        val resActions = mutableMapOf<Int, EntityAction>()
        val gatherResources: Map<Int, EntityAction> = myWorkers().map { w -> w to resources().minByOrNull { w.distance(it) }!! }
                .map { (w, r) -> w.id to moveAndAttackAction(r) }
                .toMap()

        resActions.putAll(gatherResources)
        val buildWorkers = myBuildings(EntityType.BUILDER_BASE).map {
            it.id to buildUnit(it, EntityType.BUILDER_UNIT)
        }.toMap()

        resActions.putAll(buildWorkers)


        return Action(resActions)
    }

    fun debugUpdate(playerView: PlayerView, debugInterface: DebugInterface) {
        debugInterface.send(model.DebugCommand.Clear())
        debugInterface.getState()
    }
}
