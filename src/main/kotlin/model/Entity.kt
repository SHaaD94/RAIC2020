package model

import impl.buildingTypes
import impl.global.entityStats
import util.StreamUtil

class Entity {
    var id: Int = 0
    var playerId: Int? = null
    lateinit var entityType: model.EntityType
    lateinit var position: model.Vec2Int
    var health: Int = 0
    var active: Boolean = false

    constructor() {}
    constructor(
        id: Int,
        playerId: Int?,
        entityType: model.EntityType,
        position: model.Vec2Int,
        health: Int,
        active: Boolean
    ) {
        this.id = id
        this.playerId = playerId
        this.entityType = entityType
        this.position = position
        this.health = health
        this.active = active
    }

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

    fun isBuilding() = buildingTypes.contains(this.entityType)
}


