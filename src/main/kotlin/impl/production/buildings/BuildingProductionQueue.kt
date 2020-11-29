package impl.production.buildings

import model.EntityType
import model.Vec2Int
import java.util.*

data class BuildingRequest(val type: EntityType, val position: Vec2Int, val newCoordinateOnCollision: () -> Vec2Int)

object BuildingProductionQueue {
    private val queue = LinkedList<BuildingRequest>()

    fun nextRequest(): BuildingRequest? {
        return queue.poll()
    }

    fun addRequest(req: BuildingRequest) {
        queue.add(req)
    }
}