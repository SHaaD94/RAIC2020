import impl.*
import impl.global.State
import impl.global.State.availableSupply
import impl.global.entityStats
import impl.global.initEntityStats
import impl.util.*
import impl.util.algo.distance
import model.*

class MyStrategy {
    fun getAction(playerView: PlayerView, debugInterface: DebugInterface?): Action {
        initEntityStats(playerView)
        State.update(playerView)

        val resActions = mutableMapOf<Int, EntityAction>()

        val gatherResources: Map<Int, EntityAction> = assignWorkersResources()

        resActions.putAll(gatherResources)

        resActions.putAll(buildWorkers())

        resActions.putAll(buildRanges())

        resActions.putAll(repairBuildings())

        println("Supply $availableSupply")
        if (availableSupply < 5) {
            buildSupply(resActions)
        }

        return Action(resActions)
    }

    private fun repairBuildings(): Map<Int, EntityAction> =
        myBuildings().filter { it.health != it.maxHP() }.flatMap { b ->
            myWorkers().sortedBy { it.distance(b) }.take(2).map { it.id to repairAction(it, b) }
        }.toMap()

    private fun buildWorkers(): Map<Int, EntityAction> = myBuildings(EntityType.BUILDER_BASE).map {
        it.id to buildUnit(it, EntityType.BUILDER_UNIT)
    }.toMap()

    private fun buildRanges(): Map<Int, EntityAction> = myBuildings(EntityType.RANGED_BASE).map {
        it.id to buildUnit(it, EntityType.RANGED_UNIT)
    }.toMap()

    private fun assignWorkersResources(): Map<Int, EntityAction> =
        myWorkers().map { w -> w to resources().minByOrNull { w.distance(it) }!! }
            .map { (w, r) -> w.id to attackAction(r) }
            .toMap()

    private fun buildSupply(resActions: MutableMap<Int, EntityAction>) {
        val supplyPos: Vec2Int
        val supplySize = entityStats[EntityType.HOUSE]!!.size
        var xMax = 0
        var yMax = 0
        outer@ while (true) {
            for (x in (0..xMax)) {
                val noCollisions = entities().none {
                    intersects(
                        x, x + supplySize,
                        yMax, yMax + supplySize,
                        if (it.isBuilding()) it.position - 1 else it.position,
                        if (it.isBuilding()) it.size() + 2 else it.size()
                    )
                }
                if (noCollisions) {
                    supplyPos = Vec2Int(x, yMax)
                    break@outer
                }
            }
            for (y in (0..yMax)) {
                val noCollisions = entities().none {
                    intersects(
                        xMax, xMax + supplySize,
                        y, y + supplySize,
                        if (it.isBuilding()) it.position - 1 else it.position,
                        if (it.isBuilding()) it.size() + 2 else it.size()
                    )
                }
                if (noCollisions) {
                    supplyPos = Vec2Int(xMax, y)
                    break@outer
                }
            }
            xMax += 1
            yMax += 1
        }

        println(supplyPos)
        val position = supplyPos
        val worker = myWorkers().filter {
            !intersects(supplyPos, supplySize, it.position, it.size())
        }.minByOrNull { it.distance(position) }!!
        resActions[worker.id] = constructBuilding(worker, EntityType.HOUSE, position)
    }

    fun debugUpdate(playerView: PlayerView, debugInterface: DebugInterface) {
//        debugInterface.send(model.DebugCommand.Clear())
//        debugInterface.getState()
    }
}
