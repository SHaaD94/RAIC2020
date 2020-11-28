//TODO Adapt this for current RAIC, lol
//package impl.util.algo.quadTree
//
//object VehicleQuadTree {
//    private val quadSize = 32
//    private val quads = (0 until (1024 / quadSize)).map { x ->
//        (0 until (1024 / quadSize)).map { y ->
//            Box(x * quadSize, y * quadSize, x * quadSize + quadSize, y * quadSize + quadSize)
//        }.map { QuadNode(it, mutableListOf()) }
//    }
//
//    fun update() {
//        quads.forEach { it.forEach { it.clear() } }
//        Vehicles
//                .vehicles()
//                .forEach {
//                    quads[(it.x / quadSize).toInt()][(it.y / quadSize).toInt()].addVehicle(it)
//                }
//    }
//
//    fun query(pointX: Double, pointY: Double, distance: Double, ownership: Ownership? = null): Sequence<Pair<Vehicle, Double>> {
//        val minX = Math.max(0, ((pointX - distance) / quadSize).toInt() - 1)
//        val minY = Math.max(0, ((pointY - distance) / quadSize).toInt() - 1)
//        val maxX = Math.min(1024 / quadSize, ((pointX + distance) / quadSize).toInt() + 1)
//        val maxY = Math.min(1024 / quadSize, ((pointY + distance) / quadSize).toInt() + 1)
//
//        return (minX until maxX).flatMap { x -> (minY until maxY).flatMap { y -> quads[x][y].vehicles } }
//                .asSequence()
//                .filter {
//                    when {
//                        ownership != null -> it.ownership() == ownership
//                        else -> true
//                    }
//                }
//                .map { it to Coordinates.distance(it, pointX, pointY) }
//                .filter { it.second <= distance }
//    }
//
//    fun query(box: Box, ownership: Ownership? = null): Sequence<Vehicle> {
//        return query(box.minX, box.minY, box.maxX, box.maxY, ownership)
//    }
//
//    fun query(minX: Double, minY: Double, maxX: Double, maxY: Double, ownership: Ownership? = null): Sequence<Vehicle> {
//        val minQuadX = Math.max(0, (minX / quadSize).toInt())
//        val minQuadY = Math.max(0, (minY / quadSize).toInt())
//        val maxQuadX = Math.min(1024 / quadSize, (maxX / quadSize).toInt())
//        val maxQuadY = Math.min(1024 / quadSize, (maxY / quadSize).toInt())
//
//        return (minQuadX until maxQuadX).flatMap { x -> (minQuadY until maxQuadY).flatMap { y -> quads[x][y].vehicles } }
//                .asSequence()
//                .filter {
//                    when {
//                        ownership != null -> it.ownership() == ownership
//                        else -> true
//                    }
//                }
//    }
//
//    private class QuadNode(val box: Box,
//                           val vehicles: MutableList<Vehicle>) {
//        fun clear() = vehicles.clear()
//
//        fun addVehicle(vehicle: Vehicle) = vehicles.add(vehicle)
//    }
//}
