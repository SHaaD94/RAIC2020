package impl.micro

import impl.ActionProvider
import impl.enemiesWithinDistance
import impl.myBuildings
import impl.util.attackAction
import model.EntityAction
import model.EntityType

object TurretsActionProvider : ActionProvider {
    override fun provideActions(): Map<Int, EntityAction> = myBuildings(EntityType.TURRET).map { t ->
        val enemy = t.enemiesWithinDistance(t.attackRange()).minByOrNull { it.health }
        t.id to if (enemy != null) {
            t.attackAction(enemy, null)
        } else
            EntityAction()
    }.toMap()
}