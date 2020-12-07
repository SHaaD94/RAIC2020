package impl.micro.army

import model.Entity
import model.EntityType
import kotlin.math.max

object FightSimulation {
    fun predictResultFast(myUnits: List<Entity>, enemies: List<Entity>): Result {

        fun scoreFunction(units: List<Entity>) =
            units
                .asSequence()
                .map {
                    it.damage() * max(it.attackRange() - 1, 1) * it.health *
                            when (it.entityType) {
                                EntityType.TURRET -> 0.4
//                                EntityType.MELEE_UNIT -> if (it.playerId == myPlayerId()) 0.75 else 1.25
                                else -> 1.0
                            }
                }
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
