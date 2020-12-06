package model

import impl.global.State
import impl.util.algo.distance
import util.StreamUtil
import java.util.*

data class Vec2Int(val x: Int = 0, val y: Int = 0) : Comparable<Vec2Int> {
    companion object {
        fun readFrom(stream: java.io.InputStream): Vec2Int =
            Vec2Int(StreamUtil.readInt(stream), StreamUtil.readInt(stream))
    }

    fun writeTo(stream: java.io.OutputStream) {
        StreamUtil.writeInt(stream, x)
        StreamUtil.writeInt(stream, y)
    }

    fun toVecFloat() = Vec2Float(x.toFloat(), y.toFloat())

    operator fun minus(v: Vec2Int) = Vec2Int(this.x - v.x, this.y - v.y)
    operator fun minus(s: Int) = Vec2Int(this.x - s, this.y - s)
    operator fun plus(v: Vec2Int) = Vec2Int(this.x + v.x, this.y + v.y)
    operator fun plus(s: Int) = Vec2Int(this.x + s, this.y + s)

    operator fun div(s: Int) = Vec2Int(this.x / s, this.y / s)

    override fun compareTo(other: Vec2Int): Int {
        if (x < other.x) return -1
        if (x > other.x) return 1
        if (y < other.y) return -1
        if (y > other.y) return 1
        return 0
    }

    fun isValid() = this.x in 0 until State.playerView.mapSize && this.y in 0 until State.playerView.mapSize

    fun cellsWithinDistance(distance: Int): Sequence<Vec2Int> {
        val res = LinkedList<Vec2Int>()
        for (x in this.x - distance..this.x + distance) {
            for (y in this.y - distance..this.y + distance) {
                val c = Vec2Int(x, y)
                if (!c.isValid()) continue

                if (this.distance(c) <= distance) res.add(c)
            }
        }
        return res.asSequence()
    }

    fun validCellsAround() = sequenceOf(
        Vec2Int(this.x - 1, this.y),
        Vec2Int(this.x, this.y - 1),
        Vec2Int(this.x + 1, this.y),
        Vec2Int(this.x, this.y + 1)
    ).filter { it.isValid() }

    fun cellsCovered(size: Int): Sequence<Vec2Int> {
        return (this.x until this.x + size).asSequence()
            .flatMap { x -> (this.y until this.y + size).asSequence().map { y -> Vec2Int(x, y) } }
            .filter { it.isValid() }
    }
}
