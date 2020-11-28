package impl.util

import impl.global.entityStats
import model.*

fun moveAndAttackAction(target: Entity) = EntityAction(
    moveAction = MoveAction(target.position, true, true),
    attackAction = AttackAction(target.id, AutoAttack(50))
)

fun buildUnit(builder: Entity, unitType: EntityType): EntityAction {
    val topRightBorder = builder.position + entityStats[builder.entityType]!!.size
    return EntityAction(
        buildAction = BuildAction(unitType, topRightBorder.copy(y = topRightBorder.y - 1))
    )
}