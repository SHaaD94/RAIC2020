package impl.micro.army

import debug.drawRoute
import debug.globalDebugInterface
import impl.*
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
            myArmy().filter { !resultActions.containsKey(it.id) }.map { u ->
                if (u.enemiesWithinDistance(10).none()) {
                    val attractionPoint = enemies().minByOrNull { it.distance(u) }?.position ?: Vec2Int(40, 40)

//                    val route = findRoute(u.position, attractionPoint, u)
//                    globalDebugInterface?.drawRoute(route)
//                    val point2Move = route.getOrElse(1) { Vec2Int(40, 40) }
//                    u.id to u.moveAction(point2Move, false, true)
                    u.id to u.moveAction(attractionPoint, false, true)
                } else {
                    val cell = u.cellsWithinDistance(5)
                        .map { it to ArmyPF.getMeleeScore(it).score }
                        .maxByOrNull { it.second }
                        ?.first ?: Vec2Int()
                    u.id to u.moveAction(cell, true, true)
                }
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
