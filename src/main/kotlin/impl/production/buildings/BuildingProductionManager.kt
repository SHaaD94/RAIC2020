package impl.production.buildings

import impl.ActionProvider
import impl.entities
import impl.global.State.availableSupply
import impl.myWorkers
import impl.util.algo.distance
import impl.util.constructBuilding
import impl.util.intersects
import model.EntityAction
import model.EntityType
import model.Vec2Int
import model.size

object BuildingProductionManager : ActionProvider {

    override fun provideActions(): Map<Int, EntityAction> {
        return if (availableSupply < 5) {
            buildSupply()
        } else mapOf()
    }

    //TODO this class should use production QUEUE

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

        val worker = myWorkers().filter {
            !intersects(supplyPos, supplySize, it.position, it.size())
        }.minByOrNull { it.distance(supplyPos) }!!

        return mapOf(worker.id to constructBuilding(worker, EntityType.HOUSE, supplyPos))
    }

}