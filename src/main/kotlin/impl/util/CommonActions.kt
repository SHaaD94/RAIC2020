package impl.util

import impl.global.State
import impl.util.algo.distance
import model.*

fun Entity.attackAction(target: Entity, autoAttack: AutoAttack? = AutoAttack(State.maxPathfindNodes, arrayOf())) =
    EntityAction(
        attackAction = AttackAction(target.id, autoAttack)
    )

fun Entity.moveAction(target: Vec2Int, findClosestPosition: Boolean = false, breakThrough: Boolean = false) =
    EntityAction(
        moveAction = MoveAction(target, findClosestPosition, breakThrough)
    )

fun Entity.moveAndAttack(
    target: Entity,
    findClosestPosition: Boolean = true,
    breakThrough: Boolean = true,
    autoAttack: AutoAttack? = AutoAttack(State.maxPathfindNodes, arrayOf()),
    distanceThreshold: Int = 5
) = if (this.distance(target) > distanceThreshold) {
    moveAction(target.position, findClosestPosition, breakThrough)
} else {
    attackAction(target, autoAttack)
}

