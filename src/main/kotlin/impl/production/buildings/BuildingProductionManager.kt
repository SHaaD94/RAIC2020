package impl.production.buildings

import impl.*
import impl.global.State.availableSupply
import impl.util.intersects
import model.*
import java.util.*

data class BuildingRequest(val type: EntityType, var coordinate: Vec2Int)

//TODO probably it is not ActionProvider anymore
object BuildingProductionManager : ActionProvider {
    val buildingRequests = LinkedList<BuildingRequest>()

    override fun provideActions(): Map<Int, EntityAction> {
        monitorFinishedBuildings()
        when {
            availableSupply < 5 && numbersOfBuildingsInQueue(EntityType.HOUSE) == 0 -> {
                buildingRequests.add(
                    BuildingRequest(EntityType.HOUSE, findPosition(EntityType.HOUSE))
                )
            }
//            availableResources() > EntityType.RANGED_BASE.cost() -> {
//                constructBuilding(EntityType.RANGED_BASE)
//            }
        }
        return mapOf()
    }

    fun reservedResources() = buildingRequests.sumBy { it.type.cost() }

    //TODO perfect place for index usage
    private fun monitorFinishedBuildings() {
        val finishedRequests = buildingRequests.filter { br ->
            myBuildings(br.type).firstOrNull { it.position == br.coordinate && it.active } != null
        }
        buildingRequests.removeAll(finishedRequests)
    }

    private fun numbersOfBuildingsInQueue(type: EntityType) =
        buildingRequests.count { it.type == type }

    //TODO place for optimizations
    private fun findPosition(buildingType: EntityType): Vec2Int {
        val supplyPos: Vec2Int
        val supplySize = buildingType.size()
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
        return supplyPos
    }

}