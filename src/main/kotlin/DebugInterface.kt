import model.*
import model.DebugCommand.Add
import model.DebugData.Primitives
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files.size


class DebugInterface(private val inputStream: InputStream, private val outputStream: OutputStream) {
    fun send(command: model.DebugCommand) {
        try {
            model.ClientMessage.DebugMessage(command).writeTo(outputStream)
            outputStream.flush()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun getState(): model.DebugState {
        try {
            model.ClientMessage.RequestDebugState().writeTo(outputStream)
            outputStream.flush()
            return model.DebugState.readFrom(inputStream)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}

fun DebugInterface.drawSquare(x: Int, y: Int, size: Int, color: Color) {
    val screenOffset = Vec2Float(0.0F, 0.0F)
    this.send(
        Add(
            Primitives(
                arrayOf(
                    // 1
                    ColoredVertex(Vec2Float(x, y), screenOffset, color),
                    ColoredVertex(Vec2Float(x + size, y), screenOffset, color),
                    ColoredVertex(Vec2Float(x, y + size), screenOffset, color),
                    // 2
                    ColoredVertex(Vec2Float(x + size, y + size), screenOffset, color),
                    ColoredVertex(Vec2Float(x + size, y), screenOffset, color),
                    ColoredVertex(Vec2Float(x, y + size), screenOffset, color),
                ), PrimitiveType.TRIANGLES
            )
        )
    )
}