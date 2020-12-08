package impl.micro.army

import impl.*
import impl.util.algo.distance
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
            myArmy().filter { !resultActions.containsKey(it.id) }.mapNotNull { u ->
                val cellsToCheck = if (u.enemiesWithinDistance(30).none()) {
                    coarseCellsToCheck(u, 15)
                } else if (u.enemiesWithinDistance(10).none()) {
                    coarseCellsToCheck(u, 7)
                } else {
                    u.cellsWithinDistance(5)
                }
                val cell = cellsToCheck
                    .map { it to ArmyPF.getMeleeScore(it).score }
                    .maxByOrNull { it.second }
                    ?.first ?: Vec2Int()

                u.id to u.moveAction(cell, true, true)

            }.forEach { resultActions[it.first] = it.second }
        }
        return resultActions
    }

    private fun coarseCellsToCheck(u: Entity, coarseDist: Int) = sequenceOf(
        u.position,
        u.position.copy(x = u.position.x - coarseDist),
        u.position.copy(x = u.position.x - coarseDist, y = u.position.y + coarseDist),
        u.position.copy(x = u.position.x + coarseDist),
        u.position.copy(y = u.position.y + coarseDist),
        u.position.copy(y = u.position.y - coarseDist),
        u.position.copy(x = u.position.x + coarseDist, y = u.position.y - coarseDist),
        u.position + coarseDist,
        u.position - coarseDist
    ).filter { it.isValid() }

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
//            .filter { !ArmyPF.getMeleeScore(it.position).loosingFight }
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
