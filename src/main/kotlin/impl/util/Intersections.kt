package impl.util

import impl.util.algo.CellIndex
import model.Entity
import model.Vec2Int

fun cellOccupied(x: Int, y: Int, entityToExclude: Entity? = null): Boolean {
    val cellOwner = CellIndex.index[x][y] ?: return false
    return cellOwner != entityToExclude
}

fun cellOccupied(v: Vec2Int, entityToExclude: Entity? = null) = cellOccupied(v.x, v.y, entityToExclude)