package impl.util.algo

import impl.util.algo.math.FastMath
import model.Entity
import model.Vec2Float
import model.Vec2Int
import kotlin.math.abs

private fun manhattanDistance(x1: Int, x2: Int, y1: Int, y2: Int) =
    abs(x1 - x2) + abs(y1 - y2)

fun Vec2Float.distance(v: Vec2Float): Double =
    manhattanDistance(this.x.toInt(), v.x.toInt(), this.y.toInt(), v.y.toInt()).toDouble()

fun Vec2Int.distance(v: Vec2Int): Double =
    manhattanDistance(this.x, v.x, this.y, v.y).toDouble()

fun Vec2Int.distance(e: Entity): Double =
    manhattanDistance(this.x, e.position.x, this.y, e.position.y).toDouble()

fun Vec2Int.distance(x: Int, y: Int): Double =
    manhattanDistance(this.x, x, this.y, y).toDouble()

fun Entity.distance(entity: Entity): Double = this.position.distance(entity.position)

fun Entity.distance(v: Vec2Int): Double = this.position.distance(v)
fun Entity.distance(x: Int, y: Int): Double = this.position.distance(x, y)

fun Vec2Float.euclidDistance(v: Vec2Float): Double =
    FastMath.hypot((v.x - this.x).toDouble(), (v.y - this.y).toDouble())

fun Vec2Int.euclidDistance(v: Vec2Int): Double =
    FastMath.hypot((v.x - this.x).toDouble(), (v.y - this.y).toDouble())

fun Entity.euclidDistance(entity: Entity): Double = this.position.euclidDistance(entity.position)

fun Entity.euclidDistance(v: Vec2Int): Double = this.position.euclidDistance(v)
