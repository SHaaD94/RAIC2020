package impl.util.algo

import impl.util.algo.math.FastMath
import model.Entity
import model.Vec2Float
import model.Vec2Int

fun Vec2Float.distance(v: Vec2Float): Double =
        FastMath.hypot((v.x - this.x).toDouble(), (v.y - this.y).toDouble())

fun Vec2Int.distance(v: Vec2Int): Double =
        FastMath.hypot((v.x - this.x).toDouble(), (v.y - this.y).toDouble())

fun Entity.distance(entity: Entity): Double = this.position.distance(entity.position)
