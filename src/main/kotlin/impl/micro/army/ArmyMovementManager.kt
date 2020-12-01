package impl.micro.army

import impl.*
import impl.global.State
import impl.util.algo.distance
import impl.util.attackAction
import impl.util.moveAction
import model.AutoAttack
import model.EntityAction
import model.Vec2Int

object ArmyMovementManager : ActionProvider {
    private const val minGroupSize = 10

    override fun provideActions(): Map<Int, EntityAction> {
        val resultActions = mutableMapOf<Int, EntityAction>()

        autoAttack(resultActions)

        myArmy().filter { !resultActions.containsKey(it.id) }.mapNotNull { u ->
            val e = enemies().minByOrNull { u.distance(it) } ?: return@mapNotNull null

            u.id to if (e.distance(u) < State.maxPathfindNodes)
                moveAction(e.position, true, false)
            else
                attackAction(e, AutoAttack(State.maxPathfindNodes))
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
