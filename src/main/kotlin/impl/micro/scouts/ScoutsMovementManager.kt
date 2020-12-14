package impl.micro.scouts

import impl.ActionProvider
import impl.currentTick
import impl.global.Round1
import impl.global.RoundInfo.currentRound
import impl.global.State
import impl.global.Vision
import impl.myWorkers
import impl.util.algo.CellIndex
import impl.util.algo.distance
import impl.util.moveAction
import model.Entity
import model.EntityAction
import model.Vec2Int
import kotlin.math.roundToInt

object ScoutsMovementManager : ActionProvider {
    private val currentScouts = mutableSetOf<Int>()

    override fun provideActions(): Map<Int, EntityAction> {
        if (currentRound() == Round1) return mapOf()
        if (currentTick() < 100) return mapOf()

        val requiredNumberOfScouts = (myWorkers().count() * 0.1).roundToInt()

        val myCurrentScouts = myScouts().toList()
        if (myCurrentScouts.size < requiredNumberOfScouts) {
            myWorkers().sortedBy { it.distance(Vec2Int(40, 40)) }
                .take(requiredNumberOfScouts - myCurrentScouts.size)
                .forEach { currentScouts.add(it.id) }
        }

        return myCurrentScouts.mapNotNull { s ->
            val closestUnvisited = (0 until 80).flatMap { x ->
                (0 until 80).map { y ->
                    Vec2Int(x, y)
                }.filter { !Vision.isVisible(it) }
            }.minByOrNull { it.distance(s) } ?: return@mapNotNull null
            s.id to s.moveAction(closestUnvisited, true, true)
        }.toMap()
    }

    fun isScout(entity: Entity) = currentScouts.contains(entity.id)

    private fun myScouts() = currentScouts.asSequence().mapNotNull { State.entityId2Entity[it] }
}
