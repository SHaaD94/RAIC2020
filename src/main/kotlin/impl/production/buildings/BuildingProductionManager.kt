package impl.production.buildings

import impl.*
import impl.global.State.availableSupply
import impl.global.State.totalSupply
import impl.util.intersects
import model.*
import java.util.*

data class BuildingRequest(val type: EntityType, var coordinate: Vec2Int)

//TODO probably it is not ActionProvider anymore
object BuildingProductionManager : ActionProvider {
    val buildingRequests = LinkedList<BuildingRequest>()

    override fun provideActions(): Map<Int, EntityAction> {
        monitorBuildingsRequests()
        when {
            myBuildings(EntityType.BUILDER_BASE).count() == 0 ->
                requestBuilding(EntityType.BUILDER_BASE)
            myBuildings(EntityType.RANGED_BASE).count() == 0 && myWorkers().count() > 8 ->
                requestBuilding(EntityType.RANGED_BASE)
            myBuildings(EntityType.MELEE_BASE).count() == 0 && myWorkers().count() > 8 ->
                requestBuilding(EntityType.MELEE_BASE)
            totalSupply >= 100 && myBuildings(EntityType.RANGED_BASE).count() < 2 &&
                    numbersOfBuildingsInQueue(EntityType.RANGED_BASE) == 0 -> {
                requestBuilding(EntityType.RANGED_BASE)
            }
            totalSupply >= 100 && myBuildings(EntityType.MELEE_BASE).count() < 2 &&
                    numbersOfBuildingsInQueue(EntityType.MELEE_BASE) == 0 -> {
                requestBuilding(EntityType.MELEE_BASE)
            }
            totalSupply >= 100 && availableSupply <= 20 && numbersOfBuildingsInQueue(EntityType.HOUSE) < 5 -> {
                requestBuilding(EntityType.HOUSE)
            }
            totalSupply >= 50 && availableSupply <= 10 && numbersOfBuildingsInQueue(EntityType.HOUSE) < 2 -> {
                requestBuilding(EntityType.HOUSE)
            }
            totalSupply < 100 && availableSupply <= 5 && numbersOfBuildingsInQueue(EntityType.HOUSE) == 0 -> {
                requestBuilding(EntityType.HOUSE)
            }
        }
        return mapOf()
    }

    fun reservedResources() = buildingRequests.sumBy { it.type.cost() }

    private fun requestBuilding(type: EntityType) {
        buildingRequests.add(BuildingRequest(type, findPosition(type)))
    }

    //TODO perfect place for index usage
    private fun monitorBuildingsRequests() {
        val finishedRequests = buildingRequests.filter { br ->
            myBuildings(br.type).firstOrNull { it.position == br.coordinate && it.active } != null
        }
        buildingRequests.removeAll(finishedRequests)

        buildingRequests.filter { br ->
            entities().any {
                if (br.type == it.entityType && br.coordinate == it.position) false
                else
                    intersects(
                        br.coordinate.x, br.coordinate.x + br.type.size(),
                        br.coordinate.y, br.coordinate.y + br.type.size(),
                        if (it.isBuilding()) it.position - 1 else it.position,
                        if (it.isBuilding()) it.size() + 2 else it.size()
                    )
            }
        }.forEach { it.coordinate = findPosition(it.type) }
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
                        if (it.isBuilding() && x != 0) it.position - 1 else it.position,
                        if (it.isBuilding() && x != 0) it.size() + 2 else it.size()
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
                        if (it.isBuilding() && y != 0) it.position - 1 else it.position,
                        if (it.isBuilding() && y != 0) it.size() + 2 else it.size()
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