import debug.globalDebugInterface
import impl.currentTick
import impl.global.ClusterManager
import impl.global.State
import impl.global.initEntityStats
import impl.micro.TurretsActionProvider
import impl.micro.army.ArmyMovementManager
import impl.micro.workers.WorkersManager
import impl.micro.workers.WorkersPF
import impl.production.buildings.BuildingProductionManager
import impl.production.units.UnitProductionManager
import impl.util.algo.CellIndex
import model.*
import kotlin.random.Random

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
        initEntityStats(playerView)
        State.update(playerView)
        CellIndex.update(playerView)
        WorkersPF.update(playerView)
        ClusterManager.update(playerView)

        val resActions = mutableMapOf<Int, EntityAction>()

        actionProviders.forEach { resActions.putAll(it.provideActions()) }

        return Action(resActions)
    }

    fun debugUpdate(playerView: PlayerView, debugInterface: DebugInterface) {
//        if (currentTick() > 5) drawClusters()
//        drawWorkersPf(debugInterface)
    }

}
