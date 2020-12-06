import debug.clear
import debug.drawArmyPF
import debug.globalDebugInterface
import impl.currentTick
import impl.global.ClusterManager
import impl.global.State
import impl.global.initEntityStats
import impl.micro.TurretsActionProvider
import impl.micro.army.ArmyMovementManager
import impl.micro.army.ArmyPF
import impl.micro.workers.WorkersManager
import impl.micro.workers.WorkersPF
import impl.production.buildings.BuildingProductionManager
import impl.production.units.UnitProductionManager
import impl.util.algo.CellIndex
import model.Action
import model.EntityAction
import model.PlayerView

class MyStrategy {

    val actionProviders = listOf(
        TurretsActionProvider,
        BuildingProductionManager,
        WorkersManager,
        UnitProductionManager,
        ArmyMovementManager
    )

    fun getAction(playerView: PlayerView, debugInterface: DebugInterface?): Action {
        globalDebugInterface = debugInterface
//        debugInterface?.clear()
        initEntityStats(playerView)
        State.update(playerView)
        CellIndex.update(playerView)
        WorkersPF.update(playerView)
        ClusterManager.update(playerView)
        ArmyPF.clearCaches()

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

        return Action(resActions)
    }

    fun debugUpdate(playerView: PlayerView, debugInterface: DebugInterface) {
//        if (currentTick() > 5) drawClusters()
//        drawWorkersPf(debugInterface)
    }

}
