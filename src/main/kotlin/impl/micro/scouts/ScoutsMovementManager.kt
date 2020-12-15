package impl.micro.scouts

import impl.ActionProvider
import impl.currentTick
import impl.global.Round1
import impl.global.RoundInfo.currentRound
import impl.global.State
import impl.global.State.playerView
import impl.global.Vision
import impl.global.Vision.isVisible
import impl.micro.workers.WorkersPF
import impl.myWorkers
import impl.util.algo.distance
import impl.util.algo.pathFinding.findRoute
import impl.util.moveAction
import model.Entity
import model.EntityAction
import model.Vec2Int
import java.util.*
import kotlin.math.roundToInt

object ScoutsMovementManager : ActionProvider {
    private val currentScouts = mutableSetOf<Int>()

//    private val scoutsPF = Array(80) { Array(80) { -1 } }

    override fun provideActions(): Map<Int, EntityAction> {
        if (currentRound() == Round1) return mapOf()
        if (currentTick() < 100) return mapOf()

        val requiredNumberOfScouts = (myWorkers().count() * 0.1).roundToInt()

        val myCurrentScouts = myScouts().toMutableList()
        if (myCurrentScouts.size < requiredNumberOfScouts) {
            myWorkers()
                .filter { !myCurrentScouts.contains(it) }
                .sortedBy { it.distance(Vec2Int(40, 40)) }
                .take(requiredNumberOfScouts - myCurrentScouts.size)
                .forEach {
                    myCurrentScouts.add(it)
                    currentScouts.add(it.id)
                }
        }

//        updatePF(myCurrentScouts)

        return myCurrentScouts.mapNotNull { s ->
            s.id to s.moveAction(bestScoutPoint(s), true, true)
        }.toMap()
    }

    private fun bestScoutPoint(e: Entity): Vec2Int {
        return e.cellsWithinDistance(e.visionRange())
            .filter { WorkersPF.getScore(it) >= 0 }
            .map { v ->
                v to v.cellsWithinDistance(e.visionRange()).filter { !isVisible(it) }
                    .map { currentTick() - Vision.lastVisible(it) }
                    .sum()
            }.maxByOrNull { it.second }?.first ?: sequenceOf(
            Vec2Int(0, 80),
            Vec2Int(80, 80),
            Vec2Int(80, 0)
        ).minByOrNull { it.distance(e) }!!
    }

//    private fun updatePF(myCurrentScouts: MutableList<Entity>) {
//        for (x in 0 until playerView.mapSize) {
//            Arrays.setAll(WorkersPF.field[x]) { 0 }
//        }
//    }

    fun isScout(entity: Entity) = currentScouts.contains(entity.id)

    private fun myScouts() = currentScouts.asSequence().mapNotNull { State.entityId2Entity[it] }
}
