package impl.util

import impl.entities
import model.Entity
import model.Vec2Int

//TODO use index
fun cellOccupied(x: Int, y: Int, entityToExclude: Entity? = null) =
    entities().any {
        if (entityToExclude == it) false
        else
            x in it.position.x until (it.position.x + it.size()) &&
                    y in it.position.y until (it.position.y + it.size())
    }

fun cellOccupied(v: Vec2Int, entityToExclude: Entity? = null) = cellOccupied(v.x, v.y, entityToExclude)