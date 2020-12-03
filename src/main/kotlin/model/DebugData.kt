package model

import util.StreamUtil

abstract class DebugData {

    abstract fun writeTo(stream: java.io.OutputStream)
    companion object {

        fun readFrom(stream: java.io.InputStream): DebugData {
            when (StreamUtil.readInt(stream)) {
                Log.TAG -> return Log.readFrom(stream)
                Primitives.TAG -> return Primitives.readFrom(stream)
                PlacedText.TAG -> return PlacedText.readFrom(stream)
                else -> throw java.io.IOException("Unexpected tag value")
            }
        }
    }

    class Log : DebugData {
        lateinit var text: String
        constructor() {}
        constructor(text: String) {
            this.text = text
        }
        companion object {
            val TAG = 0

            fun readFrom(stream: java.io.InputStream): Log {
                val result = Log()
                result.text = StreamUtil.readString(stream)
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
            StreamUtil.writeString(stream, text)
        }
    }

    class Primitives : DebugData {
        lateinit var vertices: Array<model.ColoredVertex>
        lateinit var primitiveType: model.PrimitiveType
        constructor() {}
        constructor(vertices: Array<model.ColoredVertex>, primitiveType: model.PrimitiveType) {
            this.vertices = vertices
            this.primitiveType = primitiveType
        }
        companion object {
            val TAG = 1

            fun readFrom(stream: java.io.InputStream): Primitives {
                val result = Primitives()
                result.vertices = Array(StreamUtil.readInt(stream), {
                    var verticesValue: model.ColoredVertex
                    verticesValue = model.ColoredVertex.readFrom(stream)
                    verticesValue
                })
                when (StreamUtil.readInt(stream)) {
                0 ->result.primitiveType = model.PrimitiveType.LINES
                1 ->result.primitiveType = model.PrimitiveType.TRIANGLES
                else -> throw java.io.IOException("Unexpected tag value")
                }
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
            StreamUtil.writeInt(stream, vertices.size)
            for (verticesElement in vertices) {
                verticesElement.writeTo(stream)
            }
            StreamUtil.writeInt(stream, primitiveType.tag)
        }
    }

    class PlacedText : DebugData {
        lateinit var vertex: model.ColoredVertex
        lateinit var text: String
        var alignment: Float = 0.0f
        var size: Float = 0.0f
        constructor() {}
        constructor(vertex: model.ColoredVertex, text: String, alignment: Float, size: Float) {
            this.vertex = vertex
            this.text = text
            this.alignment = alignment
            this.size = size
        }
        companion object {
            val TAG = 2

            fun readFrom(stream: java.io.InputStream): PlacedText {
                val result = PlacedText()
                result.vertex = model.ColoredVertex.readFrom(stream)
                result.text = StreamUtil.readString(stream)
                result.alignment = StreamUtil.readFloat(stream)
                result.size = StreamUtil.readFloat(stream)
                return result
            }
        }

        override fun writeTo(stream: java.io.OutputStream) {
            StreamUtil.writeInt(stream, TAG)
            vertex.writeTo(stream)
            StreamUtil.writeString(stream, text)
            StreamUtil.writeFloat(stream, alignment)
            StreamUtil.writeFloat(stream, size)
        }
    }
}


class ColorGradient(private val loColor: Color, private val hiColor: Color) {
    fun getColor(mix: Double): Color {
        if (mix < 0 || mix > 1) {
            throw RuntimeException("")
        }

        //Apply inverse sRGB companding to convert each channel into linear light
        val loColorSRGB = sRGBInverseCompanding(loColor)
        val hiColorSRGB = sRGBInverseCompanding(hiColor)

        //Linearly interpolate r, g, b values using mix (0..1)
        var interpolated = linearInterpolation(loColorSRGB, hiColorSRGB, mix)

        //Compute a measure of brightness of the two colors using empirically determined gamma
        val gamma = 0.43f
        val loBrightness = Math.pow(getRGBSum(loColorSRGB).toDouble(), gamma.toDouble())
        val hiBrightness = Math.pow(getRGBSum(hiColorSRGB).toDouble(), gamma.toDouble())

        //Interpolate a new brightness value, and convert back to linear light
        val brightness = linearInterpolation(loBrightness, hiBrightness, mix)
        val intensity = Math.pow(brightness.toDouble(), (1 / gamma).toDouble())

        //Apply adjustment factor to each rgb value based
        if (getRGBSum(interpolated) != 0f) {
            val factor = intensity.toFloat() / getRGBSum(interpolated)
            interpolated = scaleColor(interpolated, factor.toDouble())
        }

        //Apply sRGB companding to convert from linear to perceptual light
        return sRGBCompanding(interpolated)
    }

    private fun getRGBSum(c: Color): Float {
        return c.r + c.g + c.b
    }

    private fun scaleColor(c: Color, factor: Double): Color {
        return Color(
            (c.r * factor).toFloat(),
            (c.g * factor).toFloat(),
            (c.b * factor).toFloat(),
            1f
        )
    }

    private fun linearInterpolation(f1: Double, f2: Double, fraction: Double): Float {
        return (f1 * (1 - fraction) + f2 * fraction).toFloat()
    }

    private fun linearInterpolation(c1: Color, c2: Color, fraction: Double): Color {
        val r = linearInterpolation(c1.r.toDouble(), c2.r.toDouble(), fraction)
        val g = linearInterpolation(c1.g.toDouble(), c2.g.toDouble(), fraction)
        val b = linearInterpolation(c1.b.toDouble(), c2.b.toDouble(), fraction)
        return Color(r, g, b, 1f)
    }

    private fun sRGBInverseCompanding(c: Color): Color {
        //Inverse Red, Green, and Blue
        var r = c.r
        var g = c.g
        var b = c.b
        r = if (r > 0.04045) {
            StrictMath.pow((r + 0.055) / 1.055, 2.4).toFloat()
        } else {
            r / 12.92f
        }
        g = if (g > 0.04045) {
            StrictMath.pow((g + 0.055) / 1.055, 2.4).toFloat()
        } else {
            g / 12.92f
        }
        b = if (b > 0.04045) {
            StrictMath.pow((b + 0.055) / 1.055, 2.4).toFloat()
        } else {
            b / 12.92f
        }
        return Color(r, g, b, 1f)
    }

    private fun sRGBCompanding(c: Color): Color {
        //Apply companding to Red, Green, and Blue
        var r = c.r
        var g = c.g
        var b = c.b
        r = if (r > 0.0031308) {
            (1.055 * Math.pow(r.toDouble(), 1 / 2.4) - 0.055).toFloat()
        } else {
            r * 12.92f
        }
        g = if (g > 0.0031308) {
            (1.055 * Math.pow(g.toDouble(), 1 / 2.4) - 0.055).toFloat()
        } else {
            g * 12.92f
        }
        b = if (b > 0.0031308) {
            (1.055 * Math.pow(b.toDouble(), 1 / 2.4) - 0.055).toFloat()
        } else {
            b * 12.92f
        }
        return Color(r, g, b, 1f)
    }
}