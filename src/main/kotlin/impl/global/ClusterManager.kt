package impl.global

import impl.entities
import impl.util.algo.dbscan.DBSCANClusterer
import impl.util.algo.dbscan.DistanceMetric
import impl.util.algo.distance
import model.Entity
import model.PlayerView

data class Cluster(val unitIds: Set<Int>, val units: List<Entity>, val playerId: Int)

object ClusterManager {
    var clusters: List<Cluster> = listOf()
    fun update(playerView: PlayerView) {

        this.clusters = DBSCANClusterer(
            entities().filter { it.damage() > 0 }.toList(), 5, 5.0,
            object : DistanceMetric<Entity> {
                override fun calculateDistance(val1: Entity, val2: Entity): Int {
                    if (val1.playerId != val2.playerId) return 1_000_000
                    return val1.distance(val2).toInt()
                }
            }
        )
            .performClustering()
            .map { unitArray ->
                Cluster(unitArray.map { it.id }.toSet(), unitArray.toList(), unitArray.first().playerId!!)
            }

    }
}
