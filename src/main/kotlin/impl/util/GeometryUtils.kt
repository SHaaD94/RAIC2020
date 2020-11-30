package impl.util

import model.Vec2Int

fun intersects(x11: Int, x12: Int, y11: Int, y12: Int, x21: Int, x22: Int, y21: Int, y22: Int): Boolean {
    return x11 < x22 && x12 > x21 && y11 < y22 && y12 > y21
}

fun intersects(x11: Int, x12: Int, y11: Int, y12: Int, v2: Vec2Int, r2: Int): Boolean {
    return intersects(x11, x12, y11, y12, v2.x, v2.x + r2, v2.y, v2.y + r2)
}

fun intersects(v1: Vec2Int, r1: Int, v2: Vec2Int, r2: Int): Boolean {
    return intersects(v1.x, v1.x + r1, v1.y, v1.y + r1, v2.x, v2.x + r2, v2.y, v2.y + r2)
}
