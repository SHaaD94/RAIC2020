package impl.util.algo

import model.Entity
import model.PlayerView
import model.Vec2Int
import java.util.*

object CellIndex {
    val index: Array<Array<Entity?>> = Array(80) { Array(80) { null } }
    val nextIndex: Array<Array<Entity?>> = Array(80) { Array(80) { null } }
    fun update(playerView: PlayerView) {
        index.forEach { Arrays.fill(it, null) }
        nextIndex.forEach { Arrays.fill(it, null) }

        playerView.entities.forEach {
            for (x in it.position.x until it.position.x + it.size()) {
                for (y in it.position.y until it.position.y + it.size()) {
                    index[x][y] = it
                    nextIndex[x][y] = it
                }
            }
        }
    }

    fun getUnit(v: Vec2Int): Entity? = index[v.x][v.y]
    fun getUnit(x: Int, y: Int): Entity? = index[x][y]

    fun getUnitForNextIndex(v: Vec2Int): Entity? = nextIndex[v.x][v.y]

    fun setNextUnit(oldPos: Vec2Int, v: Vec2Int, e: Entity) {
        nextIndex[oldPos.x][oldPos.y] = null
        nextIndex[v.x][v.y] = e
    }
}