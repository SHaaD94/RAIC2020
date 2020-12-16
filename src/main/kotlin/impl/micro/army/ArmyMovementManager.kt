package impl.micro.army

import debug.drawRoute
import debug.globalDebugInterface
import impl.*
import impl.util.algo.CellIndex
import impl.util.algo.distance
import impl.util.algo.pathFinding.findRoute
import impl.util.attackAction
import impl.util.moveAction
import model.*

object ArmyMovementManager : ActionProvider {
    override fun provideActions(): Map<Int, EntityAction> {
        val resultActions = mutableMapOf<Int, EntityAction>()

        autoAttack(resultActions)

        val mainBase = myBuildings(EntityType.BUILDER_BASE).firstOrNull()
        // don't rush into fight while we don't have income
        if (currentTick() < 200 && mainBase != null) {
            // gather at one point and defend against early aggression
            earlyGame(resultActions, mainBase)
        } else {
            myArmy().sortedBy { Vec2Int(79,79).distance(it) }.filter { !resultActions.containsKey(it.id) }.map { u ->
                if (u.enemiesWithinDistance(10).none()) {
                    val attractionPoint = enemies().minByOrNull { it.distance(u) }?.position ?: Vec2Int(40, 40)

                    u.id to u.moveAction(attractionPoint, true, true)
                } else {
                    val cell = u.cellsWithinDistance(1)
                        .filter { CellIndex.getUnitForNextIndex(it)?.let { it.playerId != myPlayerId() } ?: true }
                        .map { it to ArmyPF.getMeleeScore(it).score }
                        .maxByOrNull { it.second }
                        ?.let {
                            CellIndex.setNextUnit(u.position, it.first, u)
                            it
                        }
                        ?.first ?: Vec2Int()
                    u.id to u.moveAction(cell, true, true)
                }
            }.forEach { resultActions[it.first] = it.second }
        }
        return resultActions
    }

    private fun earlyGame(resultActions: MutableMap<Int, EntityAction>, mainBase: Entity) {
        myArmy().filter { !resultActions.containsKey(it.id) }.mapNotNull { u ->
            val e = enemies().map { it to it.distance(mainBase) }.filter { it.second < 40 }
                .minByOrNull { it.second }
                ?.first ?: return@mapNotNull (u.id to u.moveAction(Vec2Int(15, 15), true, true))
            u.id to u.moveAction(e.position, true, true)
        }.forEach { resultActions[it.first] = it.second }
    }

    private fun autoAttack(resultActions: MutableMap<Int, EntityAction>) {
        myArmy()
            .map { u ->
                u to enemies()
                    .map { it to it.distance(u) }
                    .filter { (e, dist) -> dist <= u.attackRange() }
                    .filter { it.first.health > 0 }
                    .minByOrNull { (e, _) -> e.health }
                    ?.first
            }
            .filter { it.second != null }
            .forEach { (u, e) -> resultActions[u.id] = u.attackAction(e!!, autoAttack = null) }
    }
}
