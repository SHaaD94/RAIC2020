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

class MyStrategy {

    val actionProviders = listOf(
        TurretsActionProvider,
        BuildingProductionManager,
        WorkersManager,
        UnitProductionManager,
        ArmyMovementManager
    )

    fun getAction(playerView: PlayerView, debugInterface: DebugInterface?): Action {
        initEntityStats(playerView)
        State.update(playerView)
        CellIndex.update(playerView)
        WorkersPF.update(playerView)

        val resActions = mutableMapOf<Int, EntityAction>()

        actionProviders.forEach { resActions.putAll(it.provideActions()) }

        require(resActions.map {
            sequenceOf(
                it.value.moveAction,
                it.value.buildAction,
                it.value.attackAction,
                it.value.repairAction,
            ).filter { it != null }.count()
        }.all { it <= 1 })
        return Action(resActions)
    }

    fun debugUpdate(playerView: PlayerView, debugInterface: DebugInterface) {
//        drawWorkersPf(debugInterface)
    }

    private fun drawWorkersPf(debugInterface: DebugInterface) {
        val gradient = ColorGradient(Color(0F, 255F, 0F, 0.3F), Color(255F, 0F, 0F, 0.3F))
        val min = WorkersPF.field.flatMap { it.toList() }.minOrNull()!!

        WorkersPF.field.forEachIndexed { x, arr ->
            arr.forEachIndexed { y, _ ->
                if (WorkersPF.field[x][y] == 0) return@forEachIndexed
                debugInterface.drawSquare(x, y, 1, gradient.getColor(WorkersPF.field[x][y] * 1.0 / min).copy(a = 0.3F))
            }
        }
    }
}
