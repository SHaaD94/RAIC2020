package model

import util.StreamUtil

data class Vec2Int(val x: Int = 0, val y: Int = 0) : Comparable<Vec2Int> {
    companion object {
        fun readFrom(stream: java.io.InputStream): Vec2Int =
            Vec2Int(StreamUtil.readInt(stream), StreamUtil.readInt(stream))
    }

    fun writeTo(stream: java.io.OutputStream) {
        StreamUtil.writeInt(stream, x)
        StreamUtil.writeInt(stream, y)
    }

    fun toVecFloat() = Vec2Float(x.toFloat(), x.toFloat())

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
}
