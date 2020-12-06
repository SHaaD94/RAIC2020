package impl.micro.army

import model.Entity
import model.EntityType

object FightSimulation {
    fun predictResultFast(myUnits: List<Entity>, enemies: List<Entity>): Result {

        fun scoreFunction(units: List<Entity>) =
            units
                .map { it.damage() * it.attackRange() * it.health * if (it.entityType == EntityType.TURRET) 0.4 else 1.0 }
                .sum()

        return if (scoreFunction(myUnits) > scoreFunction(enemies)) Win else Loose
    }

    fun predictResultCorrectly(myUnits: List<Entity>, enemies: List<Entity>): Result {
        TODO()
    }
}

sealed class Result
object Loose : Result()
object Win : Result()
