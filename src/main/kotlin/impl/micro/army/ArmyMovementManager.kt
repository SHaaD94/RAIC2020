package impl.micro.army

import impl.ActionProvider
import impl.enemies
import impl.myArmy
import impl.util.algo.distance
import impl.util.attackAction
import impl.util.moveAction
import model.EntityAction
import model.Vec2Int

object ArmyMovementManager : ActionProvider {
    override fun provideActions(): Map<Int, EntityAction> {
        val randomArmyMovementActions = myArmy().map { u ->
            val closesEnemy = enemies().map { it to it.distance(u.position) }.filter { it.second < 30.0 }
                .minByOrNull { it.second }?.first
            u.id to if (closesEnemy == null) moveAction(Vec2Int(25, 25)) else attackAction(closesEnemy)
        }.toMap()

        return randomArmyMovementActions
    }
}