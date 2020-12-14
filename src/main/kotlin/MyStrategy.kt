import debug.*
import impl.currentTick
import impl.enemies
import impl.global.ClusterManager
import impl.global.State
import impl.global.Vision
import impl.global.initEntityStats
import impl.micro.TurretsActionProvider
import impl.micro.army.ArmyMovementManager
import impl.micro.army.ArmyPF
import impl.micro.workers.WorkersManager
import impl.micro.workers.WorkersPF
import impl.myArmy
import impl.production.buildings.BuildingProductionManager
import impl.production.units.UnitProductionManager
import impl.util.algo.CellIndex
import impl.util.algo.distance
import impl.util.algo.pathFinding.findRoute
import model.Action
import model.Color
import model.EntityAction
import model.PlayerView

class MyStrategy {
    var timeTotal = 0L
    val actionProviders = listOf(
        TurretsActionProvider,
        BuildingProductionManager,
        WorkersManager,
        UnitProductionManager,
        ArmyMovementManager
    )

    fun getAction(playerView: PlayerView, debugInterface: DebugInterface?): Action {
        val start = System.currentTimeMillis()
        globalDebugInterface = debugInterface
//        debugInterface?.clear()
        initEntityStats(playerView)
        Vision.update(playerView)
        State.update(playerView)
        CellIndex.update(playerView)
        WorkersPF.update(playerView)
        ClusterManager.update(playerView)
        ArmyPF.clearCachesAndUpdate()

        val resActions = mutableMapOf<Int, EntityAction>()

        actionProviders.forEach {
            it.provideActions().forEach { (u, action) ->
                if (resActions.containsKey(u)) {
                    println("FUCK! DUPLICATED MOVES FOR ONE UNIT")
                } else {
                    resActions[u] = action
                }
            }
        }

//        debugInterface?.drawClusters()
//        if (currentTick() > 200) debugInterface?.drawArmyPF()

        timeTotal += System.currentTimeMillis() - start
        debugInterface?.let { println("Spent $timeTotal") }

        return Action(resActions)
    }

    fun debugUpdate(playerView: PlayerView, debugInterface: DebugInterface) {
//        if (currentTick() > 5) drawClusters()
//        drawWorkersPf(debugInterface)
    }

}
