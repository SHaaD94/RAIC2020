import impl.*
import impl.global.State
import impl.global.State.availableSupply
import impl.global.initEntityStats
import impl.production.UnitProductionGenerator
import impl.util.*
import impl.util.algo.distance
import model.*

class MyStrategy {

    var currentOrder: EntityType? = null

    fun getAction(playerView: PlayerView, debugInterface: DebugInterface?): Action {
        initEntityStats(playerView)
        State.update(playerView)

        val resActions = mutableMapOf<Int, EntityAction>()

        val gatherResources: Map<Int, EntityAction> = assignWorkersResources()

        resActions.putAll(gatherResources)

        resActions.putAll(repairBuildings())

        fun produceUnit(builder: EntityType, unit: EntityType) =
            myBuildings(builder).map { it.id to buildUnit(it, unit) }.toMap()

        fun produce(e: EntityType) = when (e) {
//            EntityType.HOUSE -> buildSupply()
            EntityType.MELEE_UNIT -> produceUnit(EntityType.MELEE_BASE, EntityType.MELEE_UNIT)
            EntityType.RANGED_UNIT -> produceUnit(EntityType.RANGED_BASE, EntityType.RANGED_UNIT)
            EntityType.BUILDER_UNIT -> produceUnit(EntityType.BUILDER_BASE, EntityType.BUILDER_UNIT)
            else -> mapOf()
        }

        if (currentOrder == null) {
            currentOrder = UnitProductionGenerator.nextUnitToProduce.next()
        }
        if (availableResources() >= currentOrder!!.cost()) {
            resActions.putAll(produce(currentOrder!!))
            currentOrder = null
        }

        println("Supply $availableSupply")
        if (availableSupply < 5) {
            resActions.putAll(buildSupply())
        }

        resActions.putAll(
            myArmy().map { u ->
                val closesEnemy = enemies().map { it to it.distance(u.position) }.filter { it.second < 30.0 }
                    .minByOrNull { it.second }?.first
                u.id to if (closesEnemy == null) moveAction(Vec2Int(20, 20)) else attackAction(closesEnemy)
            }
        )

        return Action(resActions)
    }

    private fun repairBuildings(): Map<Int, EntityAction> =
        myBuildings().filter { it.health != it.maxHP() }.flatMap { b ->
            myWorkers().sortedBy { it.distance(b) }.take(2).map { it.id to repairAction(it, b) }
        }.toMap()

    private fun assignWorkersResources(): Map<Int, EntityAction> =
        myWorkers().map { w -> w to resources().minByOrNull { w.distance(it) }!! }
            .map { (w, r) -> w.id to attackAction(r) }
            .toMap()

    private fun buildSupply(): Map<Int, EntityAction> {
        val supplyPos: Vec2Int
        val supplySize = EntityType.HOUSE.size()
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

        println("Supply position $supplyPos")
        val worker = myWorkers().filter {
            !intersects(supplyPos, supplySize, it.position, it.size())
        }.minByOrNull { it.distance(supplyPos) }!!

        return mapOf(worker.id to constructBuilding(worker, EntityType.HOUSE, supplyPos))
    }

    fun debugUpdate(playerView: PlayerView, debugInterface: DebugInterface) {
//        debugInterface.send(model.DebugCommand.Clear())
//        debugInterface.getState()
    }
}
