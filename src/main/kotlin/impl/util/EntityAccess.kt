package impl

import impl.global.OnceSeenEntities
import impl.global.State.playerView
import impl.micro.scouts.ScoutsMovementManager
import impl.production.buildings.BuildingProductionManager
import impl.util.algo.CellIndex
import model.Entity
import model.EntityType
import model.EntityType.*
import model.Vec2Int

val unitTypes = setOf(BUILDER_UNIT, MELEE_UNIT, RANGED_UNIT)
val buildingTypes = setOf(WALL, HOUSE, BUILDER_BASE, MELEE_BASE, RANGED_BASE, TURRET)

fun myPlayerId() = playerView.myId

fun currentTick(): Int = playerView.currentTick

//---------------------------------------

fun entities(): Sequence<Entity> = playerView.entities.asSequence() + OnceSeenEntities.maybeEntities.asSequence()

fun entities(type: EntityType): Sequence<Entity> = entities().filter { it.entityType == type }

fun units(): Sequence<Entity> = entities().filter { it.entityType != RESOURCE }

fun myEntities(type: EntityType? = null): Sequence<Entity> = units()
    .filter { it.playerId == myPlayerId() }
    .let { u -> type?.let { u.filter { it.entityType == type } } ?: u }

fun myUnits(type: EntityType? = null) = myEntities(type).filter { unitTypes.contains(it.entityType) }

fun myBuildings(type: EntityType? = null) = myEntities(type).filter { buildingTypes.contains(it.entityType) }

fun myWorkers() = myUnits(BUILDER_UNIT)
    .filter { !ScoutsMovementManager.isScout(it) }

fun resources(): Sequence<Entity> = entities().filter { it.entityType == RESOURCE }

fun myArmy() = myUnits().filter { it.entityType == RANGED_UNIT || it.entityType == MELEE_UNIT }

fun enemies() = entities().filter { it.playerId != null && it.playerId != myPlayerId() }

fun Vec2Int.entitiesWithinDistance(d: Int) =
    this.cellsWithinDistance(d).mapNotNull { CellIndex.getUnit(it) }.distinctBy { it.id }

fun Entity.entitiesWithinDistance(d: Int) = this.position.entitiesWithinDistance(d)

fun Vec2Int.enemiesWithinDistance(d: Int) = this.entitiesWithinDistance(d).filter { it.isEnemy() }

fun Entity.enemiesWithinDistance(d: Int) = this.position.enemiesWithinDistance(d)

fun Vec2Int.alliesWithinDistance(d: Int) = this.entitiesWithinDistance(d).filter { it.playerId == myPlayerId() }

fun Entity.alliesWithinDistance(d: Int) = this.position.alliesWithinDistance(d)

//--------------------------------------------------------

fun availableResources() = playerView.players
    .first { it.id == myPlayerId() }.resource - BuildingProductionManager.reservedResources()