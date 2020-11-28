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
            unitType, cellsAround(builder).first()
        )
    )
}

fun constructBuilding(worker: Entity, type: EntityType, pos: Vec2Int): EntityAction {
    //find border points
    val borderCells = cellsAround(type, pos)
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
