package impl.util.algo

import java.util.*
import kotlin.collections.ArrayList

object ConvexHull {
    // Returns a new list of points representing the convex hull of
    // the given set of points. The convex hull excludes collinear points.
    // This algorithm runs in O(n log n) time.
    fun makeHull(points: List<Point>): List<Point> {
        return makeHullPresorted(ArrayList(points).sorted())
    }

    // Returns the convex hull, assuming that each points[i] <= points[i + 1]. Runs in O(n) time.
    private fun makeHullPresorted(points: List<Point>): List<Point> {
        if (points.size <= 1)
            return ArrayList(points)

        // Andrew's monotone chain algorithm. Positive y coordinates correspond to "up"
        // as per the mathematical convention, instead of "down" as per the computer
        // graphics convention. This doesn't affect the correctness of the result.

        val upperHull = ArrayList<Point>()
        for (p in points) {
            while (upperHull.size >= 2) {
                val q = upperHull[upperHull.size - 1]
                val r = upperHull[upperHull.size - 2]
                if ((q.x - r.x) * (p.y - r.y) >= (q.y - r.y) * (p.x - r.x))
                    upperHull.removeAt(upperHull.size - 1)
                else
                    break
            }
            upperHull.add(p)
        }
        upperHull.removeAt(upperHull.size - 1)

        val lowerHull = ArrayList<Point>()
        for (i in points.indices.reversed()) {
            val p = points[i]
            while (lowerHull.size >= 2) {
                val q = lowerHull[lowerHull.size - 1]
                val r = lowerHull[lowerHull.size - 2]
                if ((q.x - r.x) * (p.y - r.y) >= (q.y - r.y) * (p.x - r.x))
                    lowerHull.removeAt(lowerHull.size - 1)
                else
                    break
            }
            lowerHull.add(p)
        }
        lowerHull.removeAt(lowerHull.size - 1)

        if (!(upperHull.size == 1 && upperHull == lowerHull))
            upperHull.addAll(lowerHull)
        return upperHull
    }

}


class Point(val x: Double, val y: Double) : Comparable<Point> {
    override fun toString(): String {
        return String.format("Point(%g, %g)", x, y)
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj !is Point)
            false
        else {
            val other = obj as Point?
            x == other!!.x && y == other.y
        }
    }


    override fun hashCode(): Int {
        return Objects.hash(x, y)
    }


    override fun compareTo(other: Point): Int {
        return if (x != other.x)
            java.lang.Double.compare(x, other.x)
        else
            java.lang.Double.compare(y, other.y)
    }

}