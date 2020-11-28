package impl.global

import impl.myEntities
import model.PlayerView

object State {
    private var initialized = false
    var populationAvailable = 0
    var playerView: PlayerView = PlayerView()
    fun update(playerView: PlayerView) {
        fun init() {
        }
        if (initialized) {
            init()
            initialized = true
        }

        this.playerView = playerView

        this.populationAvailable = myEntities().map {
            val eInfo = entityStats[it.entityType]!!
            eInfo.populationProvide - eInfo.populationUse
        }.sum()
    }
}
