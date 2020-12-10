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

data class PFScore(val score: Double, val loosingFight: Boolean = false)
object ArmyPF {
    private val allyImpulse = Impulse(30.0) { distance, score ->
        max(score - distance, 0.0)
    }
    private val nearestEnemyAttractionImpulse = Impulse(10000.0) { dist, score ->
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

    private val simulationLooseImpulse = Impulse(-2000.0) { dist, score ->
        score + dist * 50
    }

    private val enemyDangerImpulse = Impulse(-100.0) { dist, score ->
        max(score + dist * dist, 0.0)
    }

    private val commonCache = HashMap<Vec2Int, PFScore>()
    private val rangeCache = HashMap<Vec2Int, PFScore>()
    private val meleeCache = HashMap<Vec2Int, PFScore>()

    private var failedSimulationPoints = listOf<Vec2Int>()


    fun clearCachesAndUpdate() {
        commonCache.clear()
        rangeCache.clear()
        meleeCache.clear()

        failedSimulationPoints =
            myArmy().mapNotNull { u ->
                val enemiesInThePoint = u.enemiesWithinDistance(10).filter { it.damage() > 1 }

                if (enemiesInThePoint.none()) return@mapNotNull null

                val closestEnemy = enemiesInThePoint.map { it to it.distance(u) }.minByOrNull { it.second }!!.first
                val enemies = closestEnemy.enemiesWithinDistance(7).filter { it.damage() > 1 }.toList()
                val allies = u.alliesWithinDistance(7).filter { it.damage() > 1 }.toList()

                if (FightSimulation.predictResultFast(allies, enemies) == Loose) closestEnemy.position else null
            }.toList()
    }

    fun getRangeScore(v: Vec2Int, nextRoutePoint: Vec2Int?): PFScore {
        val commonScore = commonCache.computeIfAbsent(v) { calcCommonScore(it, nextRoutePoint) }

        val rangeScore = calcRangeScoreInternal(v)
        return commonScore.copy(commonScore.score + rangeScore.score, rangeScore.loosingFight)
    }

    fun getMeleeScore(v: Vec2Int): PFScore =
        meleeCache.computeIfAbsent(v) {
            val commonScore = commonCache.computeIfAbsent(v) { calcCommonScore(it, null) }

            val meleeCache = calcMeleeScoreInternal(it)
            commonScore.copy(commonScore.score + meleeCache.score, meleeCache.loosingFight)
        }

    private fun calcCommonScore(v: Vec2Int, nextRoutePoint: Vec2Int?): PFScore {
        var current = 0.0
        // -- 0. Remove cell if is busy by building
        val unitInCell = CellIndex.getUnit(v)

        if (unitInCell?.isBuilding() == true) {
            return PFScore(Double.MIN_VALUE, false)
        }

        if (v.enemiesWithinDistance(10).filter { it.damage() > 1 }.any()) {
            // -- 2. gravity coefficient
            v.alliesWithinDistance(10)
                .filter { it.entityType == EntityType.RANGED_UNIT || it.entityType == EntityType.MELEE_UNIT }.forEach {
                    val score = allyImpulse.valueForDistance(it.distance(v))
                    current += score
                }
        }

        // -- 3. enemy attraction
        val attractionPointScore = nextRoutePoint?.let { p ->
            if (CellIndex.getUnit(v)?.entityType == EntityType.RESOURCE || CellIndex.getUnit(v) == null) {
                1000 - p.distance(v).toInt() * 100
            } else 0
        } ?: 0


        current += attractionPointScore
        return PFScore(current)
    }

    private fun calcRangeScoreInternal(v: Vec2Int): PFScore {
        var current = 0.0
        var loosingFight = false

        // -- 1. simulation score
        failedSimulationPoints.map { it to it.distance(v) }.filter { it.second < 7 }.minByOrNull { it.second }
            ?.let { (failSimPoint, _) ->
                loosingFight = true
                current +=
                    simulationLooseImpulse.valueForDistance(failSimPoint.distance(v))
            }

        return PFScore(current, loosingFight)
    }

    private fun calcMeleeScoreInternal(v: Vec2Int): PFScore {
        var current = 0.0
        var loosingFight = false

        // -- 1. simulation score


        return PFScore(current, loosingFight)
    }

//    fun getRange(v: Vec2Int) = range[v.x][v.y]
}
