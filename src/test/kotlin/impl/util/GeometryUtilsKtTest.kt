package impl.util

import org.junit.jupiter.api.Test
import model.Vec2Int
import org.junit.jupiter.api.Assertions.*

class GeometryUtilsKtTest {
    @Test
    fun `intersects1`() {
        assertTrue(intersects(Vec2Int(0, 0), 2, Vec2Int(1, 1), 2))
    }

    @Test
    fun `intersects2`() {
        assertFalse(intersects(Vec2Int(0, 0), 2, Vec2Int(4, 4), 2))
    }

    @Test
    fun `intersects3`() {
        assertTrue(intersects(Vec2Int(0, 0), 2, Vec2Int(0, 1), 2))
    }

    @Test
    fun `intersects4`() {
        assertTrue(intersects(Vec2Int(0, 0), 2, Vec2Int(0, -1), 2))
    }

    @Test
    fun `intersects5`() {
        assertTrue(intersects(Vec2Int(0, 0), 2, Vec2Int(-1, -1), 2))
    }

    @Test
    fun `intersects6`() {
        assertTrue(intersects(Vec2Int(0, 0), 2, Vec2Int(1, -1), 2))
    }

    @Test
    fun `intersects7`() {
        assertFalse(intersects(Vec2Int(0, 0), 2, Vec2Int(-10, -10), 2))
    }

    @Test
    fun `intersects8`() {
        assertFalse(intersects(Vec2Int(0, 0), 2, Vec2Int(2, 2), 2))
    }

}