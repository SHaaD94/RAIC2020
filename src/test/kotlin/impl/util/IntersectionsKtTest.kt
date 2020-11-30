package impl.util

import impl.global.State
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import impl.global.*
import model.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*


class IntersectionsKtTest {
    init {
        val houseProperties = EntityProperties()
        houseProperties.size = 3
        entityStats = mapOf(EntityType.HOUSE to houseProperties)
    }

    @BeforeEach
    fun flushGlobal() {
        State.playerView = PlayerView()
    }

    @Test
    fun `cellOccupied1`() {
        State.playerView.entities = arrayOf(Entity(1, 1, EntityType.HOUSE, Vec2Int(1, 1), 0, true))
        assertTrue(cellOccupied(1, 1))
    }

    @Test
    fun `cellOccupied2`() {
        State.playerView.entities = arrayOf(Entity(1, 1, EntityType.HOUSE, Vec2Int(1, 1), 0, true))
        assertFalse(cellOccupied(5, 5))
    }
}