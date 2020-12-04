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

        val enemyRangeThreshold = 6

        enemies()
            .filter { it.damage() > 1 }
            .filter { it.entityType != EntityType.BUILDER_UNIT }
            .forEach { e ->
                e.cellsWithinDistance(enemyRangeThreshold).forEach { c ->
//                    if (e.attackRange() + 2 < e.distance(c)) return@forEach
                    field[c.x][c.y] += (-e.damage() * (e.distance(c) / (e.attackRange()))).roundToInt()
                }
            }
    }

    fun getScore(v: Vec2Int) = field[v.x][v.y]
}