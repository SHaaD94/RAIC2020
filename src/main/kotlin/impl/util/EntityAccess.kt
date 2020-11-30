package impl

import impl.global.State.playerView
import impl.production.buildings.BuildingProductionManager
import model.Entity
import model.EntityType
import model.EntityType.*

val unitTypes = setOf(BUILDER_UNIT, MELEE_UNIT, RANGED_UNIT)
val buildingTypes = setOf(WALL, HOUSE, BUILDER_BASE, MELEE_BASE, RANGED_BASE, TURRET)

fun myPlayerId() = playerView.myId

fun currentTick(): Int = playerView.currentTick

//---------------------------------------

fun entities(): Sequence<Entity> = playerView.entities.asSequence()

fun entities(type: EntityType): Sequence<Entity> = entities().filter { it.entityType == type }

fun units(): Sequence<Entity> = playerView.entities.asSequence().filter { it.entityType != RESOURCE }

fun myEntities(type: EntityType? = null): Sequence<Entity> = units()
    .filter { it.playerId == myPlayerId() }
    .let { u -> type?.let { u.filter { it.entityType == type } } ?: u }

fun myUnits(type: EntityType? = null) = myEntities(type).filter { unitTypes.contains(it.entityType) }

fun myBuildings(type: EntityType? = null) = myEntities(type).filter { buildingTypes.contains(it.entityType) }

fun myWorkers() = myUnits(BUILDER_UNIT)

fun resources(): List<Entity> = playerView.entities.filter { it.entityType == RESOURCE }

fun myArmy() = myUnits().filter { it.entityType == RANGED_UNIT || it.entityType == MELEE_UNIT }

fun enemies() = entities().filter { it.playerId != null && it.playerId != myPlayerId() }

//--------------------------------------------------------

fun availableResources() = playerView.players
    .first { it.id == myPlayerId() }.resource - BuildingProductionManager.reservedResources()