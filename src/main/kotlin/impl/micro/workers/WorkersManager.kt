package impl.micro.workers

import impl.*
import impl.global.State
import impl.production.buildings.BuildingProductionManager
import impl.production.buildings.BuildingRequest
import impl.util.*
import impl.util.algo.distance
import model.*

object WorkersManager : ActionProvider {
    override fun provideActions(): Map<Int, EntityAction> {
        val resultActions = mutableMapOf<Int, EntityAction>()

        fun freeWorkers() = myWorkers().filter { !resultActions.containsKey(it.id) }

        BuildingProductionManager.buildingRequests
            .asSequence()
//            .filter { availableResources() >= it.type.cost() }
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

        val existingBuilding = myBuildings(br.type).firstOrNull { it.position == br.coordinate }
        val action = if (existingBuilding == null) {
            constructBuilding(worker, br.type, supplyPos)
        } else {
            repairBuilding(worker, existingBuilding)
        }

        return mapOf(worker.id to action)
    }

    private fun repairBuildings(freeWorkers: Sequence<Entity>): Map<Int, EntityAction> =
        myBuildings().filter { it.health != it.maxHP() }.flatMap { b ->
            freeWorkers.sortedBy { it.distance(b) }.take(b.maxHP() / 25).map { it.id to repairBuilding(it, b) }
        }.toMap()

    private fun assignWorkersResources(freeWorkers: Sequence<Entity>): Map<Int, EntityAction> =
        freeWorkers.mapNotNull { w ->
            if (WorkersPF.getScore(w.position) < 0) {
                w.id to w.moveAction(Vec2Int(), true, true)
            } else {
                val closestResourceWithoutEnemies =
                    resources().filter { WorkersPF.getScore(it.position) >= 0 }
                        .minByOrNull { w.distance(it) } ?: return@mapNotNull null

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