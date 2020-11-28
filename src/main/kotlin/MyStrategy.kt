import impl.*
import impl.global.State
import impl.global.State.availableSupply
import impl.global.entityStats
import impl.global.initEntityStats
import impl.util.algo.distance
import impl.util.buildUnit
import impl.util.attackAction
import impl.util.constructBuilding
import impl.util.intersects
import model.*
import kotlin.math.roundToInt

class MyStrategy {
    fun getAction(playerView: PlayerView, debugInterface: DebugInterface?): Action {
        initEntityStats(playerView)
        State.update(playerView)
        val resActions = mutableMapOf<Int, EntityAction>()
        if (currentTick() % 5 == 0) {
            val gatherResources: Map<Int, EntityAction> =
                myWorkers().map { w -> w to resources().minByOrNull { w.distance(it) }!! }
                    .map { (w, r) -> w.id to attackAction(r) }
                    .toMap()


            resActions.putAll(gatherResources)
        }

        val buildWorkers = myBuildings(EntityType.BUILDER_BASE).map {
            it.id to buildUnit(it, EntityType.BUILDER_UNIT)
        }.toMap()
        resActions.putAll(buildWorkers)


        println("Supply $availableSupply")
        if (availableSupply < 5) {
            var supplyPos = Vec2Int()
            val supplySize = entityStats[EntityType.HOUSE]!!.size
            var xMax = 0
            var yMax = 0
            outer@ while (true) {
                for (x in (0..xMax)) {
                    val noCollisions = entities().none {
                        intersects(
                            x, x + supplySize,
                            yMax, yMax + supplySize,
                            it.position, it.size() + 1
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
                            it.position, it.size()
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
            val position = supplyPos/* + (supplySize / 2.0).roundToInt()*/
            val worker = myWorkers().filter {
                !intersects(supplyPos, supplySize, it.position, it.size())
            }.minByOrNull { it.distance(position) }!!
            resActions[worker.id] = constructBuilding(worker, EntityType.HOUSE, position)
            println(position)
        }

        return Action(resActions)
    }

    fun debugUpdate(playerView: PlayerView, debugInterface: DebugInterface) {
        debugInterface.send(model.DebugCommand.Clear())
        debugInterface.getState()
    }
}
