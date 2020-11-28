package impl.util

import impl.util.algo.distance
import model.*

fun attackAction(target: Entity) = EntityAction(
    attackAction = AttackAction(target.id, AutoAttack(100))
)

fun moveAction(target: Vec2Int) = EntityAction(
    moveAction = MoveAction(target, false, true)
)

fun buildUnit(builder: Entity, unitType: EntityType): EntityAction {
    return EntityAction(
        buildAction = BuildAction(
            unitType, builder.position.copy(y = builder.position.y - 1)
        )
    )
}

fun constructBuilding(worker: Entity, type: EntityType, pos: Vec2Int): EntityAction {
    //TODO check of outer bounds, and intersection with entities
    //find border points
    val borderCells = (0 until type.size()).flatMap { i ->
        listOf(
            pos.copy(x = pos.x - 1, y = pos.y + i),
            pos.copy(x = pos.x + i, y = pos.y - 1),
            pos.copy(x = pos.x + type.size(), y = pos.y + i),
            pos.copy(x = pos.x + i, y = pos.y + type.size())
        )
    }.toSet()
    // if builder is on one of them build
    return if (borderCells.contains(worker.position)) EntityAction(
        buildAction = BuildAction(type, pos)
    // otherwise go to the neares one
    ) else EntityAction(
        moveAction = MoveAction(
            borderCells.minByOrNull { it.distance(worker.position) }!!, false, true
        )
    )
}
