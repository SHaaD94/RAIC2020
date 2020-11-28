package model

import util.StreamUtil

data class Vec2Int(val x: Int = 0, val y: Int = 0) {
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
}
