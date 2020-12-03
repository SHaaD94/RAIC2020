package impl.global

import impl.myEntities
import model.EntityType
import model.PlayerView
import model.populationProvide
import java.util.*

object State {
    //--------- System state
    var availableSupply = 0
    var totalSupply = 0
    var playerView: PlayerView = PlayerView()

    var maxPathfindNodes = 0

    var actualEntityCost: Map<EntityType, Int> = EnumMap(EntityType::class.java)

    fun update(playerView: PlayerView) {
        this.playerView = playerView
        this.maxPathfindNodes = playerView.maxPathfindNodes

        this.totalSupply = 0

        actualEntityCost = sequenceOf(EntityType.BUILDER_UNIT, EntityType.RANGED_UNIT, EntityType.MELEE_UNIT).map {
            it to entityStats[it]!!.cost + myEntities(it).count()
        }.toMap()

        this.availableSupply = myEntities()
            .filter { it.active }
            .onEach { this.totalSupply += it.entityType.populationProvide() }
            .map {
                val eInfo = entityStats[it.entityType]!!
                eInfo.populationProvide - eInfo.populationUse
            }.sum()
    }
}
