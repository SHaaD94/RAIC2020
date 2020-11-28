package model

import util.StreamUtil

data class Vec2Float(val x: Float = 0.0F, val y: Float = 0.0F) {
    companion object {
        fun readFrom(stream: java.io.InputStream): Vec2Float =
            Vec2Float(StreamUtil.readFloat(stream), StreamUtil.readFloat(stream))
    }

    fun writeTo(stream: java.io.OutputStream) {
        StreamUtil.writeFloat(stream, x)
        StreamUtil.writeFloat(stream, y)
    }

    operator fun minus(v: Vec2Float) = Vec2Float(this.x - v.x, this.y - v.y)
    operator fun plus(v: Vec2Float) = Vec2Float(this.x + v.x, this.y + v.y)
}
