package impl.micro.army

import DebugInterface
import debug.drawLine
import debug.drawText
import debug.globalDebugInterface
import impl.*
import impl.util.algo.CellIndex
import impl.util.algo.distance
import impl.util.algo.pathFinding.findRoute
import impl.util.attackAction
import impl.util.moveAction
import model.*
import kotlin.math.roundToInt

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
            myArmy()
                .sortedBy { it.distance(79, 79) }
                .filter { !resultActions.containsKey(it.id) }
                .mapNotNull { u ->
                    fun DebugInterface.drawRoute(route: List<Vec2Int>) {
                        route.windowed(2).forEach { (from, to) ->
                            this.drawLine(from, to, Color(1.0F, 0F, 0F, 1F))
                        }
                    }

                    fun findInterestPoint(): Vec2Int {
                        if (u.cellsWithinDistance(7).any { cc ->
                                ArmyPF.failedSimulationPoints.contains(cc)
                            }) {
                            return Vec2Int(0, 0)
                        }
                        if (u.enemiesWithinDistance(12).any()) {
                            val cellForFire = u.cellsWithinDistance(7)
                                .filter { CellIndex.getUnit(it)?.isBuilding() == false }
                                .filter { CellIndex.getUnit(it)?.entityType != EntityType.RESOURCE }
                                .filter { c ->
                                    c.enemiesWithinDistance(5).any()
                                }.minByOrNull { u.distance(it) }
                            if (cellForFire != null) return cellForFire
                        }
                        return enemies().minByOrNull { u.distance(it) }?.position ?: Vec2Int(40, 40)
                    }

                    val interestPoint = findInterestPoint()
                    val route = findRoute(u.position, interestPoint, u)

                    globalDebugInterface?.drawRoute(route)
                    val cell = u.cellsWithinDistance(1)
                        .filter {
                            if (CellIndex.getUnitForNextIndex(u.position) != u) {
                                if (it == u.position) return@filter false
                                val position = u.position.validCellsAround().map { it to CellIndex.getUnit(it) }
                                    .filter { it.second != null }
                                    .filter { it.second == CellIndex.getUnitForNextIndex(u.position) }
                                    .first().second!!.position
                                if (position == it) return@filter false
                            }
                            true
                        }
                        .map {
                            it to
                                    if (u.entityType == EntityType.RANGED_UNIT) {
                                        ArmyPF.getRangeScore(it, route.getOrNull(2)).score
                                    } else {
                                        ArmyPF.getMeleeScore(it).score
                                    }
                        }
                        .onEach { (v, score) ->
                            globalDebugInterface?.drawText(v, ((score * 10000).roundToInt() / 10000.0).toString())
                        }
                        .maxByOrNull { it.second }
                        ?.first ?: Vec2Int()

                    CellIndex.setNextUnit(u.position, cell, u)
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
