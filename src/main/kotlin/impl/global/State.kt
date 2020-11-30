package impl.global

import impl.myEntities
import model.PlayerView
import model.populationProvide

object State {
    //--------- System state
    var availableSupply = 0
    var totalSupply = 0
    var playerView: PlayerView = PlayerView()

    fun update(playerView: PlayerView) {
        this.playerView = playerView

        this.totalSupply = 0
        this.availableSupply = myEntities()
            //TODO disabling this stuff, because of chaotic supply depo production
            .filter { it.active }
            .onEach { this.totalSupply += it.entityType.populationProvide() }
            .map {
                val eInfo = entityStats[it.entityType]!!
                eInfo.populationProvide - eInfo.populationUse
            }.sum()
    }
}
