package debug

import DebugInterface
import impl.global.ClusterManager
import impl.micro.army.ArmyPF
import impl.micro.workers.WorkersPF
import impl.util.algo.distance
import model.*
import kotlin.math.roundToInt
import kotlin.random.Random

fun drawWorkersPf(debugInterface: DebugInterface) {
    val gradient = ColorGradient(Color(0F, 255F, 0F, 0.3F), Color(255F, 0F, 0F, 0.3F))
    val min = WorkersPF.field.flatMap { it.toList() }.minOrNull()!!

    WorkersPF.field.forEachIndexed { x, arr ->
        arr.forEachIndexed { y, _ ->
            if (WorkersPF.field[x][y] == 0) return@forEachIndexed
            debugInterface.drawSquare(
                x,
                y,
                1,
                gradient.getColor(WorkersPF.field[x][y] * 1.0 / min).copy(a = 0.3F)
            )
        }
    }
}

fun DebugInterface.drawArmyPF() {
    val gradient = ColorGradient(Color(1F, 0F, 0F, 0.3F), Color(0F, 1F, 0F, 0.3F))
    val pf = Array(80) { x -> Array(80) { y -> ArmyPF.getRangeScore(Vec2Int(x, y), null) } }
    val min = pf.flatMap { it.toList().map { it.score } }.filter { it != Double.MIN_VALUE }.minOrNull()!!
    val max = pf.flatMap { it.toList().map { it.score } }.filter { it != Double.MIN_VALUE }.maxOrNull()!!

    fun percent(v: Double) = (v - min) / (max - min)

    WorkersPF.field.forEachIndexed { x, arr ->
        arr
            .forEachIndexed { y, _ ->
//            if (WorkersPF.field[x][y] == 0) return@forEachIndexed
                if (pf[x][y].score == Double.MIN_VALUE) return@forEachIndexed
                this.drawSquare(
                    x, y, 1, gradient.getColor(percent(pf[x][y].score)).copy(a = 0.3F)
                )
                this.drawText(Vec2Int(x, y), ((pf[x][y].score * 10000).roundToInt() / 10000.0).toString())
            }
    }
}

var globalDebugInterface: DebugInterface? = null

fun DebugInterface.drawSquare(vec2Int: Vec2Int, size: Int, color: Color) {
    this.drawSquare(vec2Int.x, vec2Int.y, size, color)
}

fun DebugInterface.drawSquare(x: Int, y: Int, size: Int, color: Color) {
    val screenOffset = Vec2Float(0.0F, 0.0F)
    this.send(
        DebugCommand.Add(
            DebugData.Primitives(
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

fun DebugInterface.drawLine(v1: Vec2Int, v2: Vec2Int, c: Color) {
    val screenOffset = Vec2Float(0.0F, 0.0F)
    this.send(
        DebugCommand.Add(
            DebugData.Primitives(
                arrayOf(
                    ColoredVertex(v1.toVecFloat() + 0.5F, screenOffset, c),
                    ColoredVertex(v2.toVecFloat() + 0.5F, screenOffset, c)
                ),
                PrimitiveType.LINES
            )
        )
    )
}

fun DebugInterface.clear() {
    this.send(DebugCommand.Clear())
}

fun DebugInterface.drawText(v1: Vec2Int, text: String, c: Color = Color(0.0F, 0F, 0F, 1F)) {
    val screenOffset = Vec2Float(0.0F, 0.0F)
    this.send(
        DebugCommand.Add(
            DebugData.PlacedText(
                ColoredVertex(v1.toVecFloat(), screenOffset, c),
                text,
                0.1F,
                30F
            )
        )
    )
}


private val randomColors = (0..1000).map {
    Color(
        Random.nextFloat(),
        Random.nextFloat(),
        Random.nextFloat(),
        0.8F
    )
}

fun DebugInterface.drawClusters() {
    ClusterManager.clusters.sortedBy { Vec2Int(0, 0).distance(it.centroid) }.forEachIndexed { i, cluster ->
        cluster.units.forEach {
            this.drawSquare(
                it.position.x, it.position.y, it.size(),
                randomColors[i]
            )
        }
    }
}
