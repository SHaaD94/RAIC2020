package impl.production.units

import impl.ActionProvider
import impl.availableResources
import impl.myBuildings
import impl.util.buildUnit
import model.EntityAction
import model.EntityType
import model.cost

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
            mapOf()
    }

    private fun produceUnit(builder: EntityType, unit: EntityType) =
        myBuildings(builder).map { it.id to buildUnit(it, unit) }.toMap()

    private fun produce(e: EntityType) = when (e) {
        EntityType.MELEE_UNIT -> produceUnit(EntityType.MELEE_BASE, EntityType.MELEE_UNIT)
        EntityType.RANGED_UNIT -> produceUnit(EntityType.RANGED_BASE, EntityType.RANGED_UNIT)
        EntityType.BUILDER_UNIT -> produceUnit(EntityType.BUILDER_BASE, EntityType.BUILDER_UNIT)
        else -> mapOf()
    }
}