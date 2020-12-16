package impl.util

import impl.global.State
import impl.util.algo.distance
import model.*

fun Entity.attackAction(
    target: Entity,
    autoAttack: AutoAttack? = AutoAttack(State.maxPathfindNodes, arrayOf())
): EntityAction {
    if (autoAttack == null) {
        target.health = target.health - this.damage()
    }
    return EntityAction(
        attackAction = AttackAction(target.id, autoAttack)
    )
}

fun Entity.moveAction(target: Vec2Int, findClosestPosition: Boolean = false, breakThrough: Boolean = false) =
    EntityAction(
        moveAction = MoveAction(target, findClosestPosition, breakThrough)
    )

fun Entity.repairAction(target: Entity) =
    EntityAction(
        repairAction = RepairAction(target.id)
    )
