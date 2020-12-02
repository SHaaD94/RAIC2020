package impl.util.algo.bfs

import impl.micro.workers.WorkersPF
import impl.util.algo.CellIndex
import impl.util.algo.distance
import impl.util.cellOccupied
import model.Entity
import model.EntityType
import model.Vec2Int
import java.util.*

fun fillShortestPaths(startingPoint: Vec2Int): Array<Array<Int?>> {
    val res = Array(80) { Array<Int?>(80) { null } }
    val visited = mutableSetOf<Vec2Int>()
    val toVisit = LinkedList<Vec2Int>()

    toVisit.add(startingPoint)

    var iter = 0
    while (toVisit.isNotEmpty()) {
        val cur = toVisit.poll()

        cur.validCellsAround()
            .filter { !visited.contains(it) }
            .filter { !cellOccupied(it) }
            .forEach { c ->
                toVisit.add(c)
            }

        res[cur.x][cur.y] = iter
        visited.add(cur)
        iter++
    }

    return res
}

fun findClosestMineral(startingPoint: Vec2Int, maxCells: Int = 10/*, shouldBeSafe: Boolean = true*/): Entity? {
    val visited = mutableSetOf<Vec2Int>()
    val toVisit = LinkedList<Vec2Int>()

    toVisit.add(startingPoint)

    var iter = 0
    while (toVisit.isNotEmpty()) {
        val cur = toVisit.poll()

        if (cur.distance(startingPoint) >= maxCells) continue

        val curUnit = CellIndex.getUnit(cur)
        if (curUnit?.entityType == EntityType.RESOURCE && WorkersPF.getScore(cur) == 0) return curUnit

        cur.validCellsAround()
            .filter { !visited.contains(it) }
            .filter { CellIndex.getUnit(it) == null || CellIndex.getUnit(it)?.entityType == EntityType.RESOURCE }
            .forEach { c ->
                toVisit.add(c)
            }

        visited.add(cur)
        iter += 1
    }

    return null
}