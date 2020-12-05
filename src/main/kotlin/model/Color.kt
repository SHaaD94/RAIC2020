package model

import util.StreamUtil

data class Color(
    var r: Float = 0.0f,
    var g: Float = 0.0f,
    var b: Float = 0.0f,
    var a: Float = 0.0f
) {

    companion object {
        fun readFrom(stream: java.io.InputStream): Color {
            val result = Color()
            result.r = StreamUtil.readFloat(stream)
            result.g = StreamUtil.readFloat(stream)
            result.b = StreamUtil.readFloat(stream)
            result.a = StreamUtil.readFloat(stream)
            return result
        }
    }

    fun writeTo(stream: java.io.OutputStream) {
        StreamUtil.writeFloat(stream, r)
        StreamUtil.writeFloat(stream, g)
        StreamUtil.writeFloat(stream, b)
        StreamUtil.writeFloat(stream, a)
    }
}
