package impl.micro.army

import impl.*
import impl.util.algo.distance
import impl.util.attackAction
import impl.util.moveAction
import model.AutoAttack
import model.EntityAction
import model.Vec2Int

private data class ArmyGroup(val ids: Set<Int>)

object ArmyMovementManager : ActionProvider {
    private const val minGroupSize = 10
    private val groups = mutableListOf<ArmyGroup>()
    private val unitId2Group = mutableMapOf<Int, ArmyGroup>()

    override fun provideActions(): Map<Int, EntityAction> {
        if (currentTick() % 5 == 0) createGroupIfPossible()?.let { groups.add(it) }

        val resultActions = mutableMapOf<Int, EntityAction>()

        //auto attack
        myArmy().map { u ->
            u to enemies()
                .map { it to it.distance(u) }
                .filter { (e, dist) -> dist <= u.attackRange() }
                .minByOrNull { (e, dist) -> dist }
                ?.first
        }
            .filter { it.second != null }
            .forEach { (u, e) -> resultActions[u.id] = attackAction(e!!, null) }

        //fresh units movement
        myArmy()
            .filter { !resultActions.containsKey(it.id) }
            .filter { !unitId2Group.containsKey(it.id) }
            .map { u ->
                u.id to moveAction(Vec2Int(20, 20))
            }.forEach { (id, action) -> resultActions[id] = action }

        // move groups to closest enemies
        groups.toList().forEach { group ->
            val units = myUnits().filter { group.ids.contains(it.id) }.toList()
            //if group is empty, remove it
            if (units.isEmpty()) {
                groups.remove(group)
                group.ids.forEach { unitId2Group.remove(it) }
                return@forEach
            }

            val groupMiddle = units.asSequence().map { it.position }.reduce { l, r -> l + r } / units.size

//            // if group is to split out
//            val distanceThreshold = 30
//            if (units.maxByOrNull { it.position }!!.distance(units.minByOrNull { it.position }!!) < distanceThreshold) {
//                units.filter { !resultActions.containsKey(it.id) }.forEach {
//                    resultActions[it.id] =
//                        moveAction(groupMiddle, true, false)
//                }
//                return@forEach
//            }

            // move to closest enemy
            val closestEnemy = enemies().minByOrNull { it.distance(groupMiddle) }
            if (closestEnemy != null) {
                units.filter { !resultActions.containsKey(it.id) }.forEach {
                    resultActions[it.id] =
                        moveAction(closestEnemy.position, true, false)
                }
            }
        }

        return resultActions
    }

    private fun createGroupIfPossible(): ArmyGroup? {
        val usedIds = groups.flatMap { it.ids }.toSet()
        val unbindedArmy = myArmy().map { it.id }.filter { !usedIds.contains(it) }.toSet()
        if (unbindedArmy.size >= minGroupSize) {
            val armyGroup = ArmyGroup(unbindedArmy)
            armyGroup.ids.forEach { unitId2Group[it] = armyGroup }
            return ArmyGroup(unbindedArmy)
        }
        return null
    }
}

