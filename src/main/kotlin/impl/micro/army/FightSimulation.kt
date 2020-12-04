package impl.micro.army

import model.Entity

object FightSimulation {
    fun predictResult(myUnits: List<Entity>, enemies: List<Entity>): Result {

        fun scoreFunction(units: List<Entity>) = units.map { it.damage() * it.attackRange() * it.health }.sum()

        return if (scoreFunction(myUnits) > scoreFunction(enemies)) Win else Loose
    }
}

sealed class Result
object Loose : Result()
object Win : Result()
