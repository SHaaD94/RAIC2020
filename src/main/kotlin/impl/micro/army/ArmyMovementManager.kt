package impl.micro.army

import impl.*
import impl.global.State
import impl.global.State.totalSupply
import impl.util.algo.distance
import impl.util.attackAction
import impl.util.attackingMove
import impl.util.moveAction
import model.*
import kotlin.math.roundToInt

object ArmyMovementManager : ActionProvider {
    override fun provideActions(): Map<Int, EntityAction> {
        val resultActions = mutableMapOf<Int, EntityAction>()

        autoAttack(resultActions)

        val mainBase = myBuildings(EntityType.BUILDER_BASE).firstOrNull()
        // don't rush into fight while we don't have income
        if (currentTick() < 150 && mainBase != null) {
            // gather at one point and defend against early aggression
            earlyGame(resultActions, mainBase)
        } else {
            myArmy().filter { !resultActions.containsKey(it.id) }.mapNotNull { u ->

                val closestEnemy = enemies().minByOrNull { u.distance(it) } ?: return@mapNotNull null
                u.id to
                        if (u.enemiesWithinDistance(10).any()) {
                            if (FightSimulation.predictResult(
                                    u.alliesWithinDistance(10).toList(),
                                    u.enemiesWithinDistance(10).toList()
                                ) == Win
                            ) u.attackingMove(closestEnemy)
                            else moveAction(Vec2Int(0, 0), true)
                        } else u.attackingMove(closestEnemy)

            }.forEach { resultActions[it.first] = it.second }
        }
        return resultActions
    }

    private fun earlyGame(resultActions: MutableMap<Int, EntityAction>, mainBase: Entity) {
        myArmy().filter { !resultActions.containsKey(it.id) }.mapNotNull { u ->
            val e = enemies().map { it to it.distance(mainBase) }.filter { it.second < 40 }
                .minByOrNull { it.second }
                ?.first ?: return@mapNotNull (u.id to moveAction(Vec2Int(15, 15), true))
            u.id to u.attackingMove(e)
        }.forEach { resultActions[it.first] = it.second }
    }

    private fun autoAttack(resultActions: MutableMap<Int, EntityAction>) {
        myArmy().map { u ->
            u to enemies()
                .map { it to it.distance(u) }
                .filter { (e, dist) -> dist <= u.attackRange() }
                .minByOrNull { (e, dist) -> dist }
                ?.first
        }
            .filter { it.second != null }
            .forEach { (u, e) -> resultActions[u.id] = attackAction(e!!, null) }
    }
}
