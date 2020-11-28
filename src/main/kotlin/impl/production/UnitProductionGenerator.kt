package impl.production

import impl.myWorkers
import model.EntityType
import impl.global.State.availableSupply
import impl.global.State.inQueue

object UnitProductionGenerator {
    var earlyGame = true
    val nextUnitToProduce = sequence {
        if (availableSupply < 5 && !inQueue.contains(EntityType.HOUSE)) yield(EntityType.HOUSE)

        if (earlyGame && myWorkers().count() <= 15) yield(EntityType.BUILDER_UNIT)

        if (myWorkers().count() >= 15 && earlyGame) earlyGame = true

        while (true) {
            // Current idea is to build 2 workers, 2 ranged unit, 1 melee unit
            // melee unit just in case
            yield(EntityType.RANGED_UNIT)
            yield(EntityType.RANGED_UNIT)
            yield(EntityType.MELEE_UNIT)

            if (myWorkers().count() < 30) {
                yield(EntityType.BUILDER_UNIT)
                yield(EntityType.BUILDER_UNIT)
            }
        }
    }.iterator()
}