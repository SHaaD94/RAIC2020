package impl.production.units

import impl.*
import impl.util.algo.math.FastMath.max
import model.Entity
import model.EntityType
import model.Vec2Int
import kotlin.math.min

object UnitProductionDecisionMaker {
    private const val maxWorkers = 70
    private const val defenseDistance = 50

    fun shouldProduceUnit(entity: Entity): Boolean {
        if (entity.entityType == EntityType.MELEE_BASE) return false

        return if (currentTick() < 200 && myWorkers().count() <= maxWorkers) {
            val allies = Vec2Int(0, 0).alliesWithinDistance(defenseDistance).filter { it.damage() > 1 }.count()
            val enemies = Vec2Int(0, 0).enemiesWithinDistance(defenseDistance).filter { it.damage() > 1 }.count()
            val underAttackButWillManage = allies > (enemies * 1.1)
            when (entity.entityType) {
                EntityType.RANGED_BASE -> !underAttackButWillManage
                EntityType.BUILDER_BASE -> underAttackButWillManage && myWorkers().count() < min(
                    resources().count(),
                    maxWorkers
                )
                else -> true
            }
        } else {
            when (entity.entityType) {
                EntityType.RANGED_BASE -> myArmy().count() < enemies().filter { it.damage() > 1 }.count() * 2.0
                EntityType.BUILDER_BASE -> myWorkers().count() < min(resources().count(), maxWorkers)
                else -> true
            }
        }
    }
}