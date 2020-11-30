package impl.production.units

import impl.myWorkers
import impl.resources
import model.EntityType

object UnitProductionGenerator {
    private const val earlyGameWorkers = 8
    private const val middleGame = 35
    private const val lateGame = 70
    private const val uberLateGame = 100

    val nextUnitToProduce = sequence {
        while (myWorkers().count() <= earlyGameWorkers) {
            yield(EntityType.BUILDER_UNIT)
        }

        while (true) {
            yield(EntityType.RANGED_UNIT)
            yield(EntityType.RANGED_UNIT)
//            yield(EntityType.MELEE_UNIT)

            if (myWorkers().count() < middleGame && resources().isNotEmpty()) {
                yield(EntityType.BUILDER_UNIT)
                yield(EntityType.BUILDER_UNIT)
                yield(EntityType.BUILDER_UNIT)
            }

            if (myWorkers().count() < lateGame && resources().isNotEmpty()) {
                yield(EntityType.BUILDER_UNIT)
                yield(EntityType.BUILDER_UNIT)
            }

            if (myWorkers().count() < uberLateGame && resources().isNotEmpty()) {
                yield(EntityType.BUILDER_UNIT)
            }
        }
    }.iterator()
}