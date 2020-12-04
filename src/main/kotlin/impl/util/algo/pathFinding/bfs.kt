package impl.util.algo.pathFinding

import impl.util.algo.CellIndex
import impl.util.algo.distance
import model.Entity
import model.EntityType
import model.Vec2Int
import java.util.*

private val visited = BitSet(80 * 80)
private fun BitSet.contains(v: Vec2Int): Boolean {
    return visited.get(v.x * 80 + v.y)
}

private fun BitSet.add(v: Vec2Int) = visited.set(v.x * 80 + v.y)


//----- BFS
fun findClosestResource(startingPoint: Vec2Int, maxCells: Int = 10): Entity? =
    findClosestEntity(startingPoint, EntityType.RESOURCE)

fun findClosestEntity(startingPoint: Vec2Int, type: EntityType, maxCells: Int = 10): Entity? {
    visited.clear()

    val toVisit = LinkedList<Vec2Int>()

    toVisit.add(startingPoint)

    var iter = 0
    while (toVisit.isNotEmpty()) {
        val cur = toVisit.poll()

        if (cur.distance(startingPoint) >= maxCells) continue

        val curUnit = CellIndex.getUnit(cur)
        if (curUnit?.entityType == type) return curUnit

        cur.validCellsAround()
            .filter { !visited.contains(it) }
            .filter { CellIndex.getUnit(it) == null || CellIndex.getUnit(it)?.entityType == type }
            .forEach { c ->
                toVisit.add(c)
            }

        visited.add(cur)
        iter += 1
    }

    return null
}