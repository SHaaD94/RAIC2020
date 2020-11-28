package impl.production

import impl.myWorkers
import model.EntityType

object UnitProductionGenerator {
    private const val earlyGameWorkers = 8
    private const val middleGameWorkers = 25
    private const val lateGameWorkers = 35

    private var earlyGame = true

    val nextUnitToProduce = sequence {
//        if (availableSupply < 5 && !inQueue.contains(EntityType.HOUSE)) yield(EntityType.HOUSE)

        if (earlyGame && myWorkers().count() <= earlyGameWorkers) yield(EntityType.BUILDER_UNIT)

        if (myWorkers().count() >= earlyGameWorkers && earlyGame) earlyGame = true

        while (true) {
            // Current idea is to build 2 workers, 2 ranged unit, 1 melee unit
            // melee unit just in case
            yield(EntityType.RANGED_UNIT)
            yield(EntityType.RANGED_UNIT)
            yield(EntityType.MELEE_UNIT)

/*            if (myWorkers().count() < middleGameWorkers) {
                yield(EntityType.BUILDER_UNIT)
                yield(EntityType.BUILDER_UNIT)
            } else */if (myWorkers().count() < lateGameWorkers) {
                yield(EntityType.BUILDER_UNIT)
            }
        }
    }.iterator()
}