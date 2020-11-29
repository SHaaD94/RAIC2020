package impl.util

import model.*

fun attackAction(target: Entity, autoAttack: AutoAttack = AutoAttack(100, arrayOf())) = EntityAction(
    attackAction = AttackAction(target.id, autoAttack)
)

fun moveAction(target: Vec2Int) = EntityAction(
    moveAction = MoveAction(target, false, false)
)