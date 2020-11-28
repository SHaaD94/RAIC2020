package impl.util

import impl.global.State
import model.Entity
import model.EntityType
import model.Vec2Int
import model.size

fun cellsAround(e: Entity) = cellsAround(e.entityType, e.position)

fun cellsAround(type: EntityType, pos: Vec2Int): Set<Vec2Int> {
    //TODO check of outer bounds, and intersection with entities
    return (0 until type.size()).asSequence().flatMap { i ->
        sequenceOf(
            pos.copy(x = pos.x - 1, y = pos.y + i),
            pos.copy(x = pos.x + i, y = pos.y - 1),
            pos.copy(x = pos.x + type.size(), y = pos.y + i),
            pos.copy(x = pos.x + i, y = pos.y + type.size())
        )
    }.filter { it.x in (0..State.playerView.mapSize) && it.y in (0..State.playerView.mapSize) }
        .toSet()
}