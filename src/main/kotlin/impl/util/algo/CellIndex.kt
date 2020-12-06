package impl.util.algo

import model.Entity
import model.PlayerView
import model.Vec2Int

object CellIndex {
    var index: Array<Array<Entity?>> = Array(0) { Array(0) { null } }
    fun update(playerView: PlayerView) {
        val newIndex = Array(80) { Array<Entity?>(80) { null } }

        playerView.entities.forEach {
            for (x in it.position.x until it.position.x + it.size()) {
                for (y in it.position.y until it.position.y + it.size()) {
                    newIndex[x][y] = it
                }
            }
        }

        index = newIndex
    }

    fun getUnit(v: Vec2Int): Entity? = index[v.x][v.y]
    fun getUnit(x: Int, y: Int): Entity? = index[x][y]
}