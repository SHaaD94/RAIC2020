package impl.util

import impl.entities
import model.Vec2Int

//TODO use index
fun cellOcuppied(x: Int, y: Int) =
    entities().any {
        x in it.position.x..(it.position.x + it.size()) &&
                y in it.position.y..(it.position.y + it.size())
    }

fun cellOcuppied(v: Vec2Int) = cellOcuppied(v.x, v.y)