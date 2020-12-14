package impl.util

import model.Entity
import model.EntityType
import model.Vec2Int
import model.size

fun cellsAround(e: Entity) = cellsAround(e.entityType, e.position)


fun cellsAround(type: EntityType, pos: Vec2Int): Set<Vec2Int> {
    return cellsAround(type.size(), pos)
}

fun cellsAround(size: Int, pos: Vec2Int): Set<Vec2Int> {
    return (0 until size).asSequence().flatMap { i ->
        sequenceOf(
            pos.copy(x = pos.x - 1, y = pos.y + i),
            pos.copy(x = pos.x + i, y = pos.y - 1),
            pos.copy(x = pos.x + size, y = pos.y + i),
            pos.copy(x = pos.x + i, y = pos.y + size)
        )
    }.filter { it.isValid() }.toSet()
}
