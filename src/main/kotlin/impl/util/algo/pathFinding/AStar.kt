package impl.util.algo.pathFinding

import impl.util.algo.CellIndex
import impl.util.algo.distance
import model.Entity
import model.EntityType
import model.Vec2Int
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

fun findRoute(from: Vec2Int, to: Vec2Int, unit: Entity): List<Vec2Int> {
    val node2RouteNode = HashMap<Vec2Int, RouteNode>()
    val queue = PriorityQueue<RouteNode>()
    val start = RouteNode(from, null, from.distance(to))

    node2RouteNode[from] = start
    queue.add(start)

    while (!queue.isEmpty()) {
        val next = queue.poll()

        val currentList = LinkedList<Vec2Int>()
        var cur: RouteNode? = next
        while (cur != null) {
            currentList.addFirst(cur.current)
            cur = cur.previous
        }

        if (next.current == to) return currentList

        next.current.validCellsAround()
            .filter {
                if (it == from || it == to) return@filter true
                val entityInCell = CellIndex.getUnit(it)
                entityInCell == null || !entityInCell.isBuilding()
                        || entityInCell.entityType != EntityType.BUILDER_UNIT || entityInCell.entityType == EntityType.RESOURCE
            }
            .asSequence()
            // not going for same node twice
            .filter { !currentList.contains(it) }
            .forEach { neighbourVertex ->
                val nextNode = node2RouteNode.computeIfAbsent(neighbourVertex) { x -> RouteNode(x) }

                val resourceBreakThrough = CellIndex.getUnit(neighbourVertex)?.let { neighbourVertexUnit ->
                    if (neighbourVertexUnit.entityType != EntityType.RESOURCE) return@let 0
                    max(neighbourVertexUnit.health / unit.damage(), 1)
                } ?: 0

                val newScore =
                    next.current.distance(neighbourVertex) + resourceBreakThrough

                if (nextNode.routeScore > newScore) {
                    nextNode.previous = next
                    nextNode.routeScore = newScore
                    nextNode.estimatedScore = newScore + neighbourVertex.distance(to)

                    queue.add(nextNode)
                }
            }
    }

//    debug.drawPoint(start.current.coord, 0.3f, ColorFloat(r = 255f))
//    debug.drawPoint(to.coord, 0.3f, ColorFloat(g = 255f))

    println("No route found from ${start.current} to ${to}")
    return emptyList()
}

data class RouteNode(
    var current: Vec2Int,
    var previous: RouteNode? = null,
    var routeScore: Double = Double.POSITIVE_INFINITY,
    var estimatedScore: Double = Double.POSITIVE_INFINITY
) : Comparable<RouteNode> {
    override fun compareTo(other: RouteNode): Int =
        (this.estimatedScore - other.estimatedScore).toInt()
}
