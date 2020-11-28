package impl.util

import impl.entities
import model.Vec2Int

//TODO use index
fun cellOccupied(x: Int, y: Int) =
    entities().any {
        x in it.position.x until (it.position.x + it.size()) &&
                y in it.position.y until (it.position.y + it.size())
    }

fun cellOccupied(v: Vec2Int) = cellOccupied(v.x, v.y)