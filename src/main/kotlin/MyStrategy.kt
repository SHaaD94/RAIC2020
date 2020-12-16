import debug.*
import impl.global.*
import impl.micro.TurretsActionProvider
import impl.micro.army.ArmyMovementManager
import impl.micro.army.ArmyPF
import impl.micro.scouts.ScoutsMovementManager
import impl.micro.workers.WorkersManager
import impl.micro.workers.WorkersPF
import impl.myWorkers
import impl.production.buildings.BuildingProductionManager
import impl.production.units.UnitProductionManager
import impl.util.algo.CellIndex
import model.Action
import model.Color
import model.EntityAction
import model.PlayerView

class MyStrategy {
    var timeTotal = 0L
    val actionProviders = listOf(
        TurretsActionProvider,
        BuildingProductionManager,
        ScoutsMovementManager,
        WorkersManager,
        UnitProductionManager,
        ArmyMovementManager
    )

    fun getAction(playerView: PlayerView, debugInterface: DebugInterface?): Action {
        val start = System.currentTimeMillis()
        globalDebugInterface = debugInterface
        initEntityStats(playerView)
        Vision.update(playerView)
        State.update(playerView)
        CellIndex.update(playerView)
        OnceSeenEntities.update(playerView)
        WorkersPF.update(playerView)
//        ClusterManager.update(playerView)
        ArmyPF.clearCachesAndUpdate()

        val resActions = mutableMapOf<Int, EntityAction>()

        actionProviders.forEach {
            it.provideActions().forEach { (u, action) ->
                if (resActions.containsKey(u)) {
                    debugInterface?.let { println("FUCK! DUPLICATED MOVES FOR ONE UNIT") }
                } else {
                    resActions[u] = action
                }
            }
        }

        timeTotal += System.currentTimeMillis() - start
        debugInterface?.let { println("Spent $timeTotal") }

        return Action(resActions)
    }

    fun debugUpdate(playerView: PlayerView, debugInterface: DebugInterface) {
//        if (currentTick() > 5) drawClusters()
//        drawWorkersPf(debugInterface)
    }

}
