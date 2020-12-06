package impl.micro.army

import impl.*
import impl.util.algo.CellIndex
import impl.util.algo.distance
import impl.util.algo.math.FastMath.pow
import model.EntityType
import model.Vec2Int
import kotlin.math.max

data class Impulse(
    private val basicScore: Double,
    private val fadeFunction: (Double, Double) -> Double = { distance, score -> score - distance * 0.1 }
) {
    fun valueForDistance(dist: Double) = if (dist == 0.0) basicScore else fadeFunction(dist, basicScore)
}

data class PFScore(val score: Double)
object ArmyPF {
    private val allyImpulse = Impulse(10.0) { distance, score ->
        if (distance == 0.0) 0.0 else max(score - distance, 0.0)
    }
    private val nearestEnemyAttractionImpulse = Impulse(1000000.0) { dist, score ->
        score - dist * 100
    }
    private val resourceRepellingImpulse = Impulse(-100.0) { dist, score ->
        when (dist) {
            0.0 -> score
            1.0 -> score * 0.67
            2.0 -> score * 0.34
            else -> 0.0
        }
    }

    private val simulationLooseImpulse = Impulse(-1000.0) { dist, score ->
        if (dist > 10) 0.0 else score - dist * 50
    }

    private val enemyDangerImpulse = Impulse(-10.0) { dist, score ->
        max(score - dist, 0.0)
    }

    private val meleeCache = HashMap<Vec2Int, PFScore>()
    fun clearCaches() {
        meleeCache.clear()
    }

    fun getMeleeScore(v: Vec2Int): PFScore =
        meleeCache.computeIfAbsent(v) { calcMeleeScore(it) }

    private fun calcMeleeScore(v: Vec2Int): PFScore {
        var currentMelee = 0.0
        // ------------- COMMON
        // -- 1. gravity coefficient
        val unitInCell = CellIndex.getUnit(v)

        if (unitInCell?.isBuilding() == true) {
            return PFScore(Double.MIN_VALUE)
        }

        currentMelee += resources().map { it.distance(v) }.minOrNull()?.let {
            resourceRepellingImpulse.valueForDistance(it)
        } ?: 0.0

        v.alliesWithinDistance(10).filter { it.isUnit() }.filter { it.entityType != EntityType.BUILDER_UNIT }.forEach {
            val score = allyImpulse.valueForDistance(it.distance(v))
            currentMelee += score
        }

        // -- 2. enemy distance coefficient
        val attractionPointScore = nearestEnemyAttractionImpulse.valueForDistance(
            v.distance(enemies().minByOrNull { it.distance(v) }?.position ?: Vec2Int(30, 30))
        )

        currentMelee += attractionPointScore

        enemies().forEach {
            val danderScore = enemyDangerImpulse.valueForDistance(it.distance(v))
            currentMelee += danderScore
        }

        val eToDistance = enemies().map { it to it.distance(v) }
        if (eToDistance.any { it.second < 10.0 }) {
            val myUnit = myArmy().minByOrNull { it.distance(v) }
            val enemyUnit = eToDistance.minByOrNull { it.second }?.first
            if (FightSimulation.predictResultFast(
                    myUnit?.alliesWithinDistance(7)?.filter { it.damage() > 1 }?.toList() ?: emptyList(),
                    enemyUnit?.enemiesWithinDistance(7)?.filter { it.damage() > 1 }?.toList() ?: emptyList()
                ) == Loose
            ) {
                currentMelee +=
                    enemyUnit?.let {
                        simulationLooseImpulse.valueForDistance(it.distance(v.x, v.y))
                    } ?: 0.0
            }
        }

        // ------------- MELEE


        return PFScore(currentMelee)
    }

//    fun getRange(v: Vec2Int) = range[v.x][v.y]
}
