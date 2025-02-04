package impl.production.units

import impl.*
import impl.global.entityStats
import impl.production.buildings.BuildingProductionManager
import impl.util.algo.CellIndex
import impl.util.algo.pathFinding.findClosestEntity
import impl.util.algo.distance
import impl.util.algo.pathFinding.findClosestResource
import impl.util.cellOccupied
import impl.util.cellsAround
import model.*
import model.EntityType.*

object UnitProductionManager : ActionProvider {
    override fun provideActions(): Map<Int, EntityAction> {
        var currentSpends = 0

        fun buildingsWhichCanProduce() =
            myBuildings().filter {
                it.entityType == BUILDER_BASE || it.entityType == MELEE_BASE || it.entityType == RANGED_BASE
            }

        val buildActions = buildingsWhichCanProduce()
            .asSequence()
            .filter { entityStats[it.entityType]!!.build != null }
            .sortedBy { it.producingUnit()?.cost() ?: 0 }
            .filter { availableResources() + currentSpends >= it.producingUnit()?.cost() ?: 0 }
            .filter { UnitProductionDecisionMaker.shouldProduceUnit(it) }
            .mapNotNull { b ->
                val unitToProduce = b.producingUnit()!!
                val target: Vec2Int = when (unitToProduce) {
                    BUILDER_UNIT ->
//                        //use bfs to find closest minerals
                        cellsAround(b).filter { !cellOccupied(it) }
                            .mapNotNull {
                                if (it.entitiesWithinDistance(10).filter { it.entityType == RESOURCE }.any()) {
                                    findClosestResource(it) { CellIndex.getUnit(it) == null }
                                } else null
                            }
                            .firstOrNull()?.position
                        //if not successful find closest point of interest by distance
                            ?: BuildingProductionManager.buildingRequests.map { it.coordinate }
                                .plus(resources().map { it.position }).minByOrNull { b.distance(it) }
                    RANGED_UNIT, MELEE_UNIT -> enemies().minByOrNull { b.distance(it) }?.position
                    else -> null
                } ?: Vec2Int(40, 40)

                currentSpends += unitToProduce.cost()
                cellsAround(b)
                    .filter { !cellOccupied(it) }
                    .minByOrNull { it.distance(target) }
                    ?.let { b.id to buildUnitAction(unitToProduce, it) }
            }.toMap()
        val emptyActions = buildingsWhichCanProduce()
            .filter { !buildActions.containsKey(it.id) }
            .map { it.id to EntityAction() }.toMap()

        return buildActions + emptyActions
    }

    private fun buildUnitAction(unitType: EntityType, position2Build: Vec2Int): EntityAction =
        EntityAction(
            buildAction = BuildAction(unitType, position2Build)
        )
}