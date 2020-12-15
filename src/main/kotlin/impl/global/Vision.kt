package impl.global


import impl.currentTick
import model.PlayerView
import model.Vec2Int
import java.util.*

object Vision {
    private val visionMap = BitSet(6400)
    private val lastVisible = Array(80) { Array(80) { -1 } }
    fun update(playerView: PlayerView) {
        visionMap.clear()
        playerView.entities.filter { it.playerId == playerView.myId }
            .flatMap { e -> e.cellsCovered().flatMap { it.cellsWithinDistance(e.visionRange()) } }
            .onEach { lastVisible[it.x][it.y] = currentTick() }
            .forEach { visionMap.set(it.x * 80 + it.y) }
    }

    fun isVisible(v: Vec2Int) = isVisible(v.x, v.y)
    fun isVisible(x: Int, y: Int) = visionMap.get(x * 80 + y)

    fun lastVisible(v: Vec2Int) = lastVisible(v.x, v.y)
    fun lastVisible(x: Int, y: Int) = lastVisible[x][y]
}
