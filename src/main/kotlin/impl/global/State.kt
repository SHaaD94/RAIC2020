package impl.global

import impl.myEntities
import model.PlayerView

object State {
    //--------- System state
    var availableSupply = 0
    var playerView: PlayerView = PlayerView()

    fun update(playerView: PlayerView) {
        this.playerView = playerView

        this.availableSupply = myEntities()
            //TODO disabling this stuff, because of chaotic supply depo production
            .filter { it.active }
            .map {
                val eInfo = entityStats[it.entityType]!!
                eInfo.populationProvide - eInfo.populationUse
            }.sum()
    }
}
