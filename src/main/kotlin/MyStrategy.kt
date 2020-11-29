import impl.global.State
import impl.global.initEntityStats
import impl.micro.army.ArmyMovementManager
import impl.micro.workers.WorkersManager
import impl.production.buildings.BuildingProductionManager
import impl.production.units.UnitProductionManager
import model.Action
import model.EntityAction
import model.PlayerView

class MyStrategy {

    val actionProviders = listOf(
        BuildingProductionManager,
        WorkersManager,
        UnitProductionManager,
        ArmyMovementManager
    )

    fun getAction(playerView: PlayerView, debugInterface: DebugInterface?): Action {
        initEntityStats(playerView)
        State.update(playerView)

        val resActions = mutableMapOf<Int, EntityAction>()

        actionProviders.forEach { resActions.putAll(it.provideActions()) }

        return Action(resActions)
    }

    fun debugUpdate(playerView: PlayerView, debugInterface: DebugInterface) {
//        debugInterface.send(model.DebugCommand.Clear())
//        debugInterface.getState()
    }
}
