package impl.micro.workers

import impl.*
import impl.global.State
import impl.production.buildings.BuildingProductionManager
import impl.production.buildings.BuildingRequest
import impl.util.*
import impl.util.algo.CellIndex
import impl.util.algo.distance
import impl.util.algo.pathFinding.findClosestResource
import model.*

object WorkersManager : ActionProvider {
    private const val maxWorkersToRepairBuilding = 3

    override fun provideActions(): Map<Int, EntityAction> {
        val resultActions = mutableMapOf<Int, EntityAction>()

        var currentSpendResources = 0

        fun freeWorkers() = myWorkers().filter { !resultActions.containsKey(it.id) }

        BuildingProductionManager.buildingRequests
            .asSequence()
            .filter {
                State.playerView.players
                    .find { it.id == myPlayerId() }!!.resource + currentSpendResources >= it.type.cost()
            }
            .onEach { currentSpendResources += it.type.cost() }
            .filter { CellIndex.getUnit(it.coordinate)?.entityType != it.type }
            .map { constructBuilding(it, freeWorkers()) }
            .forEach { resultActions.putAll(it) }

        resultActions.putAll(repairBuildings(freeWorkers()))

        resultActions.putAll(
            if (resources().isNotEmpty()) assignWorkersResources(freeWorkers()) else runTo00(
                freeWorkers()
            )
        )

        return resultActions
    }

    private fun constructBuilding(br: BuildingRequest, freeWorkers: Sequence<Entity>): Map<Int, EntityAction> {
        val supplyPos: Vec2Int = br.coordinate

        val worker = freeWorkers.filter {
            !intersects(supplyPos, br.type.size(), it.position, it.size())
        }.minByOrNull { it.distance(supplyPos) } ?: return mapOf()

        return mapOf(worker.id to constructBuilding(worker, br.type, supplyPos))
    }

    private fun repairBuildings(freeWorkers: Sequence<Entity>): Map<Int, EntityAction> {
        val busyWorkers = mutableSetOf<Int>()

        val buildingRequiringRepair = myBuildings()
            .filter { it.health != it.maxHP() }
            .map { it to maxWorkersToRepairBuilding }
            .toMap().toMutableMap()

        val workersAlreadyRepairingBuildings = myBuildings().filter { it.health != it.maxHP() }
            .flatMap { b ->
                b.validCellsAround().mapNotNull { CellIndex.getUnit(it) }
                    .filter { it.entityType == EntityType.BUILDER_UNIT }
                    .filter { !busyWorkers.contains(it.id) }
                    .onEach { buildingRequiringRepair[b] = buildingRequiringRepair[b]!! - 1 }
                    .onEach { busyWorkers.add(it.id) }
                    .map { it.id to it.repairAction(b) }
            }.toMap()

        return buildingRequiringRepair.entries.sortedBy { it.key.id }.filter { it.value > 0 }
            .flatMap { (b, workersToGet) ->
                freeWorkers.filter { !busyWorkers.contains(it.id) }
                    .filter { it.distance(b) < 15 }
                    .sortedBy { it.distance(b) }
                    .take(workersToGet)
                    .onEach { busyWorkers.add(it.id) }
                    .map { it.id to repairBuilding(it, b) }
            }.toMap() + workersAlreadyRepairingBuildings
    }

    private fun assignWorkersResources(freeWorkers: Sequence<Entity>): Map<Int, EntityAction> {
        val busyResources = HashSet<Vec2Int>()

        return freeWorkers.mapNotNull { w ->
            if (WorkersPF.getScore(w.position) < 0) {
                w.id to w.moveAction(Vec2Int(), true, true)
            } else {
                val closestResourceWithoutEnemies =
                    resources().filter { WorkersPF.getScore(it.position) >= 0 }
                        .minByOrNull { w.distance(it) }
                        ?: return@mapNotNull w.id to w.moveAction(
                            Vec2Int(40, 40), true, true
                        )

                if (closestResourceWithoutEnemies.distance(w) < 10) {
                    //TODO THIS MIGHT BE REDUCED LATER
                    val bestNearestResource = findClosestResource(w.position, 10) { !busyResources.contains(it) }

                    if (bestNearestResource != null) {
                        busyResources.add(bestNearestResource.position)
                        return@mapNotNull w.id to w.moveAction(bestNearestResource.position, true, true)
                    }
                }

                w.id to if (closestResourceWithoutEnemies.distance(w) > 15) {
                    w.moveAction(closestResourceWithoutEnemies.position, true, true)
                } else {
                    w.attackAction(
                        closestResourceWithoutEnemies,
                        AutoAttack(State.maxPathfindNodes, EntityType.values())
                    )
                }
            }
        }.toMap()
    }

    private fun runTo00(freeWorkers: Sequence<Entity>): Map<Int, EntityAction> =
        freeWorkers
            .map { w -> w.id to w.moveAction(Vec2Int(0, 0), findClosestPosition = true, breakThrough = true) }
            .toMap()

    private fun repairBuilding(worker: Entity, target: Entity): EntityAction {
        val borderCells = cellsAround(target)
        // if builder is on one of them repair
        return if (borderCells.contains(worker.position)) EntityAction(
            repairAction = RepairAction(target.id)
            // otherwise go to the nearest one
        ) else worker.moveAction(
            borderCells.filter { !cellOccupied(it, worker) }.minByOrNull { it.distance(worker.position) }
            //FIXME HACK WITH DEFAULT VALUE
                ?: target.position,
            true,
            true
        )
    }

    private fun constructBuilding(worker: Entity, type: EntityType, pos: Vec2Int): EntityAction {
        //find border points
        val borderCells = cellsAround(type, pos)
        // if builder is on one of them build
        return if (borderCells.contains(worker.position)) EntityAction(
            buildAction = BuildAction(type, pos)
            // otherwise go to the nearest one
        ) else worker.moveAction(borderCells.filter { !cellOccupied(it, worker) }
            .minByOrNull { it.distance(worker.position) }
        //FIXME HACK WITH DEFAULT VALUE
            ?: pos, true, true)
    }

}