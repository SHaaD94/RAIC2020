package impl.micro.army

import impl.*
import impl.global.State
import impl.global.State.totalSupply
import impl.util.algo.distance
import impl.util.attackAction
import impl.util.attackingMove
import impl.util.moveAction
import model.AutoAttack
import model.EntityAction
import model.EntityType
import model.Vec2Int
import kotlin.math.roundToInt

object ArmyMovementManager : ActionProvider {
    private const val minGroupSize = 10

    override fun provideActions(): Map<Int, EntityAction> {
        val resultActions = mutableMapOf<Int, EntityAction>()

        autoAttack(resultActions)

        myArmy().filter { !resultActions.containsKey(it.id) }.mapNotNull { u ->
            // don't rush into fight while we don't have income
            val mainBase = myBuildings(EntityType.BUILDER_BASE).firstOrNull()
            if (myWorkers().count() < 35 && mainBase != null) {
                val e = enemies().map { it to it.distance(mainBase) }.filter { it.second < 20 }
                    .minByOrNull { it.second }?.first ?: return@mapNotNull (u.id to moveAction(Vec2Int(20, 20), true))
                u.id to u.attackingMove(e)
            } else {
                val e = enemies().minByOrNull { u.distance(it) } ?: return@mapNotNull null
                u.id to u.attackingMove(e)
            }
        }.forEach { resultActions[it.first] = it.second }

        return resultActions
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
