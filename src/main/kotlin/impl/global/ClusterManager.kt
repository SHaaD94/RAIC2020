package impl.global

import impl.util.algo.dbscan.DBSCANClusterer
import impl.util.algo.dbscan.DistanceMetric
import impl.util.algo.distance
import model.Entity
import model.PlayerView
import model.Vec2Int

data class Cluster(val unitIds: Set<Int>, val units: List<Entity>, val centroid: Vec2Int, val playerId: Int)

data class ClusterMove(val v: Vec2Int)

object ClusterManager {
    private const val clusteringDistance = 3.0
    private const val minClusterSize = 3

    var clusters: List<Cluster> = listOf()

    fun update(playerView: PlayerView) {
        val enitiesToCluster = playerView.entities
            .filter { it.isUnit() }
            .filter { it.damage() > 1 }.toList()
        this.clusters =
            if (enitiesToCluster.size > minClusterSize) {
                DBSCANClusterer(
                    enitiesToCluster, minClusterSize, clusteringDistance,
                    object : DistanceMetric<Entity> {
                        override fun calculateDistance(val1: Entity, val2: Entity): Int {
                            if (val1.playerId != val2.playerId) return 1_000_000
                            return val1.distance(val2).toInt()
                        }
                    }
                ).performClustering()
            } else {
                listOf<List<Entity>>()
            }
                .map { unitArray ->
                    Cluster(
                        unitArray.map { it.id }.toSet(),
                        unitArray.toList(),
                        unitArray.fold(Vec2Int(0, 0)) { l, r -> l + r.position } / unitArray.size,
                        unitArray.first().playerId!!
                    )
                }
    }
}
