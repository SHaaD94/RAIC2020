package impl.production.units

import impl.ActionProvider
import impl.availableResources
import impl.myBuildings
import impl.util.cellOccupied
import impl.util.cellsAround
import model.*

object UnitProductionManager : ActionProvider {
    var currentOrder: EntityType? = null

    override fun provideActions(): Map<Int, EntityAction> {
        if (currentOrder == null) {
            currentOrder = UnitProductionGenerator.nextUnitToProduce.next()
        }

        return if (availableResources() >= currentOrder!!.cost()) {
            val actions = produce(currentOrder!!)
            currentOrder = null
            actions
        } else
            //flush building orders
            myBuildings().filter {
                it.entityType == EntityType.RANGED_BASE
                        || it.entityType == EntityType.MELEE_BASE || it.entityType == EntityType.BUILDER_BASE
            }.map { it.id to EntityAction() }.toMap()
    }

    private fun produceUnit(builder: EntityType, unit: EntityType) =
        myBuildings(builder)
            .map { b -> b to cellsAround(b).firstOrNull { !cellOccupied(it.x, it.y) } }
            .filter { it.second != null }
            .map { (b, positionToBuildUnit) -> b.id to buildUnitAction(unit, positionToBuildUnit!!) }.toMap()

    private fun produce(e: EntityType) = when (e) {
        EntityType.MELEE_UNIT -> produceUnit(EntityType.MELEE_BASE, EntityType.MELEE_UNIT)
        EntityType.RANGED_UNIT -> produceUnit(EntityType.RANGED_BASE, EntityType.RANGED_UNIT)
        EntityType.BUILDER_UNIT -> produceUnit(EntityType.BUILDER_BASE, EntityType.BUILDER_UNIT)
        else -> mapOf()
    }

    private fun buildUnitAction(unitType: EntityType, position2Build: Vec2Int): EntityAction =
        EntityAction(
            buildAction = BuildAction(unitType, position2Build)
        )
}