package impl.production.units

import impl.*
import impl.micro.scouts.ScoutsMovementManager
import model.Entity
import model.EntityType
import model.Vec2Int
import model.cost
import kotlin.math.min

object UnitProductionDecisionMaker {
    private const val middleGameWorkers = 40
    private const val lateGameWorkers = 60

    private const val defenseDistance = 60

    fun shouldProduceUnit(entity: Entity): Boolean {
        return if (currentTick() < 200 && myWorkers().filter { !ScoutsMovementManager.isScout(it) }
                .count() <= middleGameWorkers) {
            val allies = Vec2Int(0, 0).alliesWithinDistance(defenseDistance)
                .filter { !it.isBuilding() }
                .filter { it.damage() > 1 }.map { it.health }.sum()
            val enemies = Vec2Int(0, 0).enemiesWithinDistance(defenseDistance)
                .filter { !it.isBuilding() }
                .filter { it.damage() > 1 }.map { it.health }.sum()
            val underAttackButWillManage = allies >= (enemies * 1.1)
            when (entity.entityType) {
                EntityType.RANGED_BASE -> !underAttackButWillManage
                EntityType.MELEE_BASE -> !underAttackButWillManage
                EntityType.BUILDER_BASE -> underAttackButWillManage
                else -> true
            }
        } else {
            when (entity.entityType) {
                EntityType.RANGED_BASE -> true
                EntityType.MELEE_BASE -> false //EntityType.MELEE_UNIT.cost() <= EntityType.RANGED_UNIT.cost() / 2
                EntityType.BUILDER_BASE ->
                    myWorkers().count() <
                            min(
                                resources().count(),
                                if (currentTick() < 500) middleGameWorkers else lateGameWorkers
                            )
                else -> true
            }
        }
    }
}