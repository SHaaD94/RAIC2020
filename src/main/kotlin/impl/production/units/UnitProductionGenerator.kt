package impl.production.units

import impl.myWorkers
import impl.resources
import model.EntityType

object UnitProductionGenerator {
    private const val earlyGameWorkers = 8
    private const val middleGameWorkers = 35
    private const val lateGameWorkers = 70

    //TODO replace with function
    val nextUnitToProduce = sequence {
        while (myWorkers().count() <= earlyGameWorkers) {
            yield(EntityType.BUILDER_UNIT)
        }

        while (true) {
            yield(EntityType.RANGED_UNIT)
            yield(EntityType.RANGED_UNIT)
//            yield(EntityType.MELEE_UNIT)

            if (myWorkers().count() < middleGameWorkers && resources().isNotEmpty()) {
                yield(EntityType.BUILDER_UNIT)
                yield(EntityType.BUILDER_UNIT)
            }

            if (myWorkers().count() < lateGameWorkers && resources().isNotEmpty()) {
                yield(EntityType.BUILDER_UNIT)
            }
        }
    }.iterator()
}