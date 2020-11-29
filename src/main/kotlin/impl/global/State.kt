package impl.global

import impl.myEntities
import model.EntityType
import model.PlayerView
import java.util.*

object State {
    //--------- System state
    private var initialized = false
    var availableSupply = 0
    var playerView: PlayerView = PlayerView()

//    val buildingsInConstruction = LinkedList<Int>()
//    val finishedBuildings = LinkedList<EntityType>()

    fun update(playerView: PlayerView) {
        fun init() {
        }
        if (initialized) {
            init()
            initialized = true
        }

        this.playerView = playerView

        this.availableSupply = myEntities()
            //TODO disabling this stuff, because of chaotic supply depo production
//            .filter { it.active }
            .map {
                val eInfo = entityStats[it.entityType]!!
                eInfo.populationProvide - eInfo.populationUse
            }.sum()
    }
}
