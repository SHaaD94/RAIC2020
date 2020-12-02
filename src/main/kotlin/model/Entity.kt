package model

import impl.buildingTypes
import impl.global.entityStats
import impl.myPlayerId
import impl.unitTypes
import impl.util.algo.distance
import util.StreamUtil
import java.util.*

data class Entity(
    var id: Int = 0,
    var playerId: Int? = null,
    var entityType: model.EntityType = EntityType.WALL,
    var position: model.Vec2Int = Vec2Int(0, 0),
    var health: Int = 0,
    var active: Boolean = false
) {
    companion object {

        fun readFrom(stream: java.io.InputStream): Entity {
            val result = Entity()
            result.id = StreamUtil.readInt(stream)
            if (StreamUtil.readBoolean(stream)) {
                result.playerId = StreamUtil.readInt(stream)
            } else {
                result.playerId = null
            }
            when (StreamUtil.readInt(stream)) {
                0 -> result.entityType = model.EntityType.WALL
                1 -> result.entityType = model.EntityType.HOUSE
                2 -> result.entityType = model.EntityType.BUILDER_BASE
                3 -> result.entityType = model.EntityType.BUILDER_UNIT
                4 -> result.entityType = model.EntityType.MELEE_BASE
                5 -> result.entityType = model.EntityType.MELEE_UNIT
                6 -> result.entityType = model.EntityType.RANGED_BASE
                7 -> result.entityType = model.EntityType.RANGED_UNIT
                8 -> result.entityType = model.EntityType.RESOURCE
                9 -> result.entityType = model.EntityType.TURRET
                else -> throw java.io.IOException("Unexpected tag value")
            }
            result.position = model.Vec2Int.readFrom(stream)
            result.health = StreamUtil.readInt(stream)
            result.active = StreamUtil.readBoolean(stream)
            return result
        }
    }

    fun writeTo(stream: java.io.OutputStream) {
        StreamUtil.writeInt(stream, id)
        val playerId = playerId;
        if (playerId == null) {
            StreamUtil.writeBoolean(stream, false)
        } else {
            StreamUtil.writeBoolean(stream, true)
            StreamUtil.writeInt(stream, playerId)
        }
        StreamUtil.writeInt(stream, entityType.tag)
        position.writeTo(stream)
        StreamUtil.writeInt(stream, health)
        StreamUtil.writeBoolean(stream, active)
    }

    fun size() = entityStats[entityType]!!.size

    fun intersects(e: Entity): Boolean {
        val e1Size = entityStats[this.entityType]!!.size
        val e2Size = entityStats[e.entityType]!!.size

        return impl.util.intersects(this.position, e1Size, e.position, e2Size)
    }

    fun contains(v: Vec2Int): Boolean {
        return v.x in (this.position.x until this.position.x + size()) && v.y in (this.position.y until this.position.y + size())
    }

    fun maxHP() = this.entityType.maxHP()
    fun attackRange() = this.entityType.attackRange()
    fun damage() = this.entityType.damage()

    fun isBuilding() = buildingTypes.contains(this.entityType)
    fun isUnit() = unitTypes.contains(this.entityType)

    fun validCellsAround() = this.position.validCellsAround()

    fun cellsWithinDistance(distance: Int): Sequence<Vec2Int> = this.position.cellsWithinDistance(distance)

    fun cellsCovered(): Sequence<Vec2Int> =
        this.position.cellsCovered(this.size())


    fun isEnemy() = this.playerId != null && this.playerId != myPlayerId()
}
