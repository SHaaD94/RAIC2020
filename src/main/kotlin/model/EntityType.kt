package model

import impl.global.entityStats

enum class EntityType(var tag: Int) {
    WALL(0),
    HOUSE(1),
    BUILDER_BASE(2),
    BUILDER_UNIT(3),
    MELEE_BASE(4),
    MELEE_UNIT(5),
    RANGED_BASE(6),
    RANGED_UNIT(7),
    RESOURCE(8),
    TURRET(9)
}

fun EntityType.size() = entityStats[this]!!.size
fun EntityType.maxHP() = entityStats[this]!!.maxHealth
fun EntityType.cost() = entityStats[this]!!.cost
fun EntityType.attackRange() = entityStats[this]!!.attack?.attackRange ?: 0
fun EntityType.damage() = entityStats[this]!!.attack?.damage ?: 0
fun EntityType.populationProvide() = entityStats[this]!!.populationProvide
fun EntityType.populationUse() = entityStats[this]!!.populationUse
fun EntityType.productionEntity() = entityStats
    .entries.firstOrNull { it.value.build?.options?.contains(this) ?: false }
    ?.key

