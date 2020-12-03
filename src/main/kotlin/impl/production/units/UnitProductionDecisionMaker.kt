package impl.production.units

import impl.*
import model.Entity
import model.EntityType
import model.Vec2Int
import kotlin.math.min

object UnitProductionDecisionMaker {
    private const val maxWorkers = 70
    private const val defenseDistance = 60

    fun shouldProduceUnit(entity: Entity): Boolean {
        return if (currentTick() < 150 && myWorkers().count() <= maxWorkers) {
            val allies = Vec2Int(0, 0).alliesWithinDistance(defenseDistance)
                .filter { it.damage() > 1 }.map { it.health }.sum()
            val enemies = Vec2Int(0, 0).enemiesWithinDistance(defenseDistance)
                .filter { it.damage() > 1 }.map { it.health }.sum()
            val underAttackButWillManage = allies > (enemies * 1.1)
            when (entity.entityType) {
                EntityType.RANGED_BASE -> !underAttackButWillManage
                EntityType.MELEE_BASE -> !underAttackButWillManage
                EntityType.BUILDER_BASE -> underAttackButWillManage && myWorkers().count() < min(
                    resources().count(),
                    maxWorkers
                )
                else -> true
            }
        } else {
            when (entity.entityType) {
                EntityType.RANGED_BASE -> true
                EntityType.MELEE_UNIT -> true
                EntityType.BUILDER_BASE -> myWorkers().count() < min(resources().count(), maxWorkers)
                else -> true
            }
        }
    }
}