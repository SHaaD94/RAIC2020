package impl.micro.workers

import impl.enemies
import impl.util.algo.distance
import model.EntityType
import model.PlayerView
import model.Vec2Int
import kotlin.math.roundToInt

object WorkersPF {
    val field = Array(80) { Array(80) { 0 } }

    /*
    Enemy entities with not zero attack generate very bad score
     */
    fun update(playerView: PlayerView) {
        for (x in 0 until playerView.mapSize) {
            for (y in 0 until playerView.mapSize) {
                field[x][y] = 0
            }
        }

        val enemyRangeThreshold = 5

        enemies()
            .filter { it.damage() != 0 }
            .filter { it.entityType != EntityType.BUILDER_UNIT }
            .forEach { e ->
                for (x in -enemyRangeThreshold until enemyRangeThreshold) {
                    for (y in -enemyRangeThreshold until enemyRangeThreshold) {
                        val c = Vec2Int(e.position.x + x, e.position.y + y)
                        if (!c.isValid()) continue
                        field[c.x][c.y] += (-e.damage() * (e.distance(c) / enemyRangeThreshold)).roundToInt()
                    }
                }
            }
    }

    fun getScore(v: Vec2Int) = field[v.x][v.y]
}