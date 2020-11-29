package impl.micro.workers

import impl.*
import impl.production.buildings.BuildingProductionManager
import impl.production.buildings.BuildingRequest
import impl.util.algo.distance
import impl.util.attackAction
import impl.util.cellsAround
import impl.util.intersects
import impl.util.moveAction
import model.*

object WorkersManager : ActionProvider {
    override fun provideActions(): Map<Int, EntityAction> {
        return if (resources().isNotEmpty()) {
            assignWorkersResources()
        } else {
            runTo00()
        } + BuildingProductionManager.buildingRequests
            .map { constructBuilding(it) }
            .foldRight(mapOf()) { acc, map -> acc + map } + repairBuildings()
    }

    private fun constructBuilding(br: BuildingRequest): Map<Int, EntityAction> {
        val supplyPos: Vec2Int = br.coordinate

        val worker = findWorkerToBuild(supplyPos, br.type)

        return mapOf(worker.id to constructBuilding(worker, br.type, supplyPos))
    }

    private fun findWorkerToBuild(supplyPos: Vec2Int, buildingType: EntityType) = myWorkers().filter {
        !intersects(supplyPos, buildingType.size(), it.position, it.size())
    }.minByOrNull { it.distance(supplyPos) }!!

    private fun repairBuildings(): Map<Int, EntityAction> =
        myBuildings().filter { it.health != it.maxHP() }.flatMap { b ->
            myWorkers().sortedBy { it.distance(b) }.take(2).map { it.id to repairAction(it, b) }
        }.toMap()

    private fun assignWorkersResources(): Map<Int, EntityAction> =
        myWorkers().map { w -> w to resources().minByOrNull { w.distance(it) }!! }
            .map { (w, r) -> w.id to attackAction(r, AutoAttack(200)) }
            .toMap()

    private fun runTo00(): Map<Int, EntityAction> =
        myWorkers()
            .map { w -> w.id to moveAction(Vec2Int(0, 0), findClosestPosition = true, breakThrough = false) }
            .toMap()

    private fun repairAction(worker: Entity, target: Entity): EntityAction {
        val borderCells = cellsAround(target)
        // if builder is on one of them repair
        return if (borderCells.contains(worker.position)) EntityAction(
            repairAction = RepairAction(target.id)
            // otherwise go to the nearest one
        ) else EntityAction(
            moveAction = MoveAction(
                borderCells.minByOrNull { it.distance(worker.position) }!!, false, true
            )
        )

    }

    private fun constructBuilding(worker: Entity, type: EntityType, pos: Vec2Int): EntityAction {
        //find border points
        val borderCells = cellsAround(type, pos)
        // if builder is on one of them build
        return if (borderCells.contains(worker.position)) EntityAction(
            buildAction = BuildAction(type, pos)
            // otherwise go to the neares one
        ) else EntityAction(
            moveAction = MoveAction(
                borderCells.minByOrNull { it.distance(worker.position) }!!, false, true
            )
        )
    }

}