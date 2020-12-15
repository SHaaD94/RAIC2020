package impl.global

import impl.entities
import impl.global.RoundInfo.currentRound
import impl.global.Vision.isVisible
import impl.myPlayerId
import impl.util.algo.CellIndex
import model.Entity
import model.PlayerView

object OnceSeenEntities {
    var maybeEntities: List<Entity> = mutableListOf()
    fun update(playerView: PlayerView) {
        if (currentRound() == Round1) return

        // check with index if maybe entities are no more in coordinates

        val previouslyVisible = maybeEntities
            .filterNot { isVisible(it.position) && CellIndex.getUnit(it.position)?.id != it.id }
            .map { it.id to it }
            .toMap()
            .toMutableMap()

        // add current to them

        playerView.entities.asSequence().filter { it.playerId != myPlayerId() }
            .forEach {
                previouslyVisible[it.id] = it
            }

        maybeEntities = previouslyVisible.values.toList()
    }
}
