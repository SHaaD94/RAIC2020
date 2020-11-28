package impl.global

import model.PlayerView

private var entityStatsInitialized = false
lateinit var entityStats: Map<model.EntityType, model.EntityProperties>
fun initEntityStats(playerView: PlayerView) {
    if (entityStatsInitialized) return
    entityStatsInitialized = true

    entityStats = playerView.entityProperties.toMap()
}
