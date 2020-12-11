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

data class PFScore(val score: Double)
object ArmyPF {
    private val simulationLooseImpulse = Impulse(-2000.0) { dist, score ->
        score + dist * 50
    }
    var failedSimulationPoints = listOf<Vec2Int>()

    fun clearCachesAndUpdate() {
        failedSimulationPoints = listOf()
        myArmy().flatMap { u ->
            val enemiesInThePoint = u.enemiesWithinDistance(10).filter { it.damage() > 1 }

            if (enemiesInThePoint.none()) return@flatMap sequenceOf<Vec2Int>()

            val closestEnemy = enemiesInThePoint.map { it to it.distance(u) }.minByOrNull { it.second }!!.first
            val enemies =
                closestEnemy.enemiesWithinDistance(5).filter { it.damage() > 1 }.filter { it.active }.distinct()
                    .toList()
            val allies = u.alliesWithinDistance(5).filter { it.damage() > 1 }.filter { it.active }.distinct().toList()

            if (FightSimulation.predictResultFast(
                    allies,
                    enemies
                ) == Loose
            ) closestEnemy.cellsCovered() else sequenceOf()
        }.toList()
    }

    fun getRangeScore(v: Vec2Int, nextRoutePoint: Vec2Int?): PFScore {
        val commonScore = calcCommonScore(v, nextRoutePoint)

        val rangeScore = calcRangeScoreInternal(v)
        return PFScore(commonScore.score + rangeScore.score)
    }

    fun getMeleeScore(v: Vec2Int): PFScore {
        val commonScore = calcCommonScore(v, null)

        val meleeCache = calcMeleeScoreInternal(v)
        return PFScore(commonScore.score + meleeCache.score)
    }


    private fun calcCommonScore(v: Vec2Int, nextRoutePoint: Vec2Int?): PFScore {
        var current = 0.0
        // -- 0. Remove cell if is busy by building
        val unitInCell = CellIndex.getUnit(v)

        if (unitInCell?.isBuilding() == true) {
            return PFScore(Double.MIN_VALUE)
        }

        // -- 3. enemy attraction
        val attractionPointScore = nextRoutePoint?.let { p ->
//            if (CellIndex.getUnit(v)?.entityType == EntityType.RESOURCE || CellIndex.getUnit(v) == null) {
            1000 - p.distance(v).toInt() * 100
//            } else 0
        } ?: 0


        current += attractionPointScore
        return PFScore(current)
    }

    private fun calcRangeScoreInternal(v: Vec2Int): PFScore {
        var current = 0.0

        // -- 1. simulation score
        failedSimulationPoints.map { it to it.distance(v) }.filter { it.second < 7 }.minByOrNull { it.second }
            ?.let { (failSimPoint, _) ->
                current +=
                    simulationLooseImpulse.valueForDistance(failSimPoint.distance(v))
            }

        return PFScore(current)
    }

    private fun calcMeleeScoreInternal(v: Vec2Int): PFScore {
        var current = 0.0

        // -- 1. simulation score


        return PFScore(current)
    }

//    fun getRange(v: Vec2Int) = range[v.x][v.y]
}
