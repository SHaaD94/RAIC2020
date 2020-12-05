package impl.production.buildings

import impl.*
import impl.global.State.availableSupply
import impl.global.State.totalSupply
import impl.util.algo.CellIndex
import impl.util.algo.distance
import impl.util.intersects
import model.*
import model.EntityType.*
import java.util.*

data class BuildingRequest(val type: EntityType, var coordinate: Vec2Int)

//TODO probably it is not ActionProvider anymore
object BuildingProductionManager : ActionProvider {
    var wereInitialTurretsPlanned = false

    val buildingRequests = LinkedList<BuildingRequest>()

    override fun provideActions(): Map<Int, EntityAction> {
        monitorBuildingsRequests()
        when {
            !wereInitialTurretsPlanned && myWorkers().count() == 20 -> {
                requestBuilding(TURRET, Vec2Int(4, 20))
                requestBuilding(TURRET, Vec2Int(20, 4))
                wereInitialTurretsPlanned = true
            }
            totalSupply > 80 && currentTick() % 50 == 0 -> {
                myWorkers()
//                    .sortedByDescending { it.distance(Vec2Int(0, 0)) }
                    .shuffled()
                    .firstOrNull {
                        val possibleTurretPosition = it.position - TURRET.size()
                        possibleTurretPosition
                            .cellsCovered(TURRET.size())
                            .filter { CellIndex.getUnit(it) == null }.count() == TURRET.size() * 2
                    }?.let {
                        requestBuilding(TURRET, it.position - TURRET.size())
                    }
            }

            myBuildings(BUILDER_BASE).count() == 0 ->
                requestBuilding(BUILDER_BASE)
            myBuildings(RANGED_BASE).count() == 0 && myWorkers().count() > 8 ->
                requestBuilding(RANGED_BASE)
            myBuildings(MELEE_BASE).count() == 0 && myWorkers().count() > 8 ->
                requestBuilding(MELEE_BASE)
            totalSupply < 50 && availableSupply <= 5 && numbersOfBuildingsInQueue(HOUSE) < 2 -> {
                requestBuilding(HOUSE)
            }
            totalSupply >= 50 && availableSupply <= 10 && numbersOfBuildingsInQueue(HOUSE) <= 3 -> {
                requestBuilding(HOUSE)
            }
            totalSupply >= 100 && availableSupply <= 20 && numbersOfBuildingsInQueue(HOUSE) <= 5 -> {
                requestBuilding(HOUSE)
            }
        }
        return mapOf()
    }

    fun reservedResources() = buildingRequests.sumBy { it.type.cost() }

    private fun requestBuilding(type: EntityType, position: Vec2Int? = null) {
        buildingRequests.add(BuildingRequest(type, position ?: findPosition(type)))
    }

    private fun monitorBuildingsRequests() {
        val finishedRequests = buildingRequests.filter { br ->
            CellIndex.getUnit(br.coordinate)?.entityType == br.type
        }
        buildingRequests.removeAll(finishedRequests)

        //try to find new position for the collided ones, excluding turrets
        buildingRequests
            .filter { it.type != TURRET }
            .filter { br ->
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
        buildingRequests.count { it.type == type } + myBuildings(type).count { !it.active }

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