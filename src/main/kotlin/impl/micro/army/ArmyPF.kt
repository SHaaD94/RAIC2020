package impl.micro.army

import impl.*
import impl.util.algo.CellIndex
import impl.util.algo.distance
import model.EntityType
import model.Vec2Int
import kotlin.math.max

data class Impulse(
    private val basicScore: Double,
    private val fadeFunction: (Double, Double) -> Double = { distance, score -> score - distance * 0.1 }
) {
    fun valueForDistance(dist: Double) = if (dist == 0.0) basicScore else fadeFunction(dist, basicScore)
}

data class PFScore(val score: Double, val loosingFight: Boolean)
object ArmyPF {
    private val allyImpulse = Impulse(30.0) { distance, score ->
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
        meleeCache.computeIfAbsent(v) { calcScoreInternal(it) }

    private fun calcScoreInternal(v: Vec2Int): PFScore {
        var current = 0.0
        var loosingFight = false
        // ------------- COMMON

        // -- 0. Remove cell if is busy by building
        val unitInCell = CellIndex.getUnit(v)

        if (unitInCell?.isBuilding() == true) {
            return PFScore(Double.MIN_VALUE, false)
        }

        // -- 1. Repelling from resources
        current += resources().map { it.distance(v) }.minOrNull()?.let {
            resourceRepellingImpulse.valueForDistance(it)
        } ?: 0.0

        // -- 2. gravity coefficient
        v.alliesWithinDistance(10).filter { it.isUnit() }.filter { it.entityType != EntityType.BUILDER_UNIT }.forEach {
            val score = allyImpulse.valueForDistance(it.distance(v))
            current += score
        }

        // -- 3. enemy attraction
        val attractionPointScore = nearestEnemyAttractionImpulse.valueForDistance(
            v.distance(enemies().minByOrNull { it.distance(v) }?.position ?: Vec2Int(30, 30))
        )

        current += attractionPointScore

        // -- 4. danger score
        enemies().forEach {
            val danderScore = enemyDangerImpulse.valueForDistance(it.distance(v))
            current += danderScore
        }

        // -- 5. simulation score
        val eToDistance = enemies().map { it to it.distance(v) }
        if (eToDistance.any { it.second < 10.0 }) {
            val myUnit = myArmy().minByOrNull { it.distance(v) }
            val enemyUnit = eToDistance.minByOrNull { it.second }?.first
            if (FightSimulation.predictResultFast(
                    myUnit?.alliesWithinDistance(7)?.filter { it.damage() > 1 }?.toList() ?: emptyList(),
                    enemyUnit?.enemiesWithinDistance(7)?.filter { it.damage() > 1 }?.toList() ?: emptyList()
                ) == Loose
            ) {
                loosingFight = true
                current +=
                    enemyUnit?.let {
                        simulationLooseImpulse.valueForDistance(it.distance(v.x, v.y))
                    } ?: 0.0
            }
        }

        return PFScore(current, loosingFight)
    }

//    fun getRange(v: Vec2Int) = range[v.x][v.y]
}
