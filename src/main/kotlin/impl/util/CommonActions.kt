package impl.util

import impl.global.State
import impl.util.algo.distance
import model.*

fun attackAction(target: Entity, autoAttack: AutoAttack? = AutoAttack(State.maxPathfindNodes, arrayOf())) =
    EntityAction(
        attackAction = AttackAction(target.id, autoAttack)
    )

fun moveAction(target: Vec2Int, findClosestPosition: Boolean = false, breakThrough: Boolean = false) = EntityAction(
    moveAction = MoveAction(target, findClosestPosition, breakThrough)
)

fun Entity.attackingMove(
    target: Entity,
    findClosestPosition: Boolean = true,
    breakThrough: Boolean = false,
    autoAttack: AutoAttack = AutoAttack(State.maxPathfindNodes)
): EntityAction {
    return if (target.distance(this) < State.maxPathfindNodes)
        moveAction(target.position, findClosestPosition, breakThrough)
    else
        attackAction(target, autoAttack)
}