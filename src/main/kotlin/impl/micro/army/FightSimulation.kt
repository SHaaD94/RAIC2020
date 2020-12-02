package impl.micro.army

import impl.util.algo.distance
import model.Entity
import model.Vec2Int
import kotlin.math.roundToInt

object FightSimulation {
    fun predictResult(myUnits: List<Entity>, enemies: List<Entity>): Result {
        val middleOfFight =
            (myUnits.plus(enemies).fold(Vec2Int(0, 0)) { l, r -> l + r.position }) / (myUnits.size + enemies.size)

        var myUnitsSorted = myUnits.map { it.copy() }.sortedByDescending { it.distance(middleOfFight) }.toMutableList()
        var enemiesSorted =
            enemies.map { it.copy(health = (it.health * 1.2).roundToInt()) }.sortedByDescending { it.distance(middleOfFight) }
                .toMutableList()

        while (myUnitsSorted.isNotEmpty() && enemiesSorted.isNotEmpty()) {
            var myDamageTotal = myUnitsSorted.map { it.damage() }.reduce { l, r -> l + r }
            var enemiesDamageTotal = (enemiesSorted.map { it.damage() }.reduce { l, r -> l + r } * 1.2).roundToInt()

            fun calculate(units: MutableList<Entity>, damage: Int) {
                var d = damage
                while (units.isNotEmpty() && d != 0) {
                    if (units.last().health < d) {
                        d -= units.last().health
                        units.remove(units.last())
                    } else {
                        units.last().health = units.last().health - d
                        d = 0
                    }
                }
            }

            calculate(enemiesSorted, myDamageTotal)
            calculate(myUnitsSorted, enemiesDamageTotal)
        }

        return if (enemiesSorted.isEmpty()) Win else Loose
    }
}

sealed class Result
object Loose : Result()
object Win : Result()
