package impl.util.algo.dbscan

import java.util.*
import kotlin.collections.HashSet


/**
 * Implementation of density-based clustering algorithm DBSCAN.
 *
 * Original Publication:
 * Ester, Martin; Kriegel, Hans-Peter; Sander, Jörg; Xu, Xiaowei (1996).
 * Simoudis, Evangelos; Han, Jiawei; Fayyad, Usama M., eds.
 * A density-based algorithm for discovering clusters in large spatial
 * databases with noise. Proceedings of the Second International Conference
 * on Knowledge Discovery and Data Mining (KDD-96). AAAI Press. pp. 226-231
 *
 * Usage:
 * - Identify type of input values.
 * - Implement metric for input value type using DistanceMetric interface.
 * - Instantiate using [.DBSCANClusterer].
 * - Invoke [.performClustering].
 *
 * See tests and metrics for example implementation and use.
 *
 * @author [Christopher Frantz](mailto:cf@christopherfrantz.org)
 * @version 0.1
 *
 * @param <V> Input value element type
</V> */
class DBSCANClusterer<V>(
    inputValues: Collection<V>?,
    minNumElements: Int,
    maxDistance: Double,
    metric: DistanceMetric<V>
) {
    /** maximum distance of values to be considered as cluster  */
    private var epsilon = 1.0

    /** minimum number of members to consider cluster  */
    private var minimumNumberOfClusterMembers = 2

    /** distance metric applied for clustering  */
    private var metric: DistanceMetric<V>? = null

    /** internal list of input values to be clustered  */
    private var inputValues: ArrayList<V>? = null

    /** index maintaining visited points  */
    private val visitedPoints = HashSet<V>()

    /**
     * Sets the distance metric
     *
     * @param metric
     * @throws DBSCANClusteringException
     */
    @Throws(DBSCANClusteringException::class)
    fun setDistanceMetric(metric: DistanceMetric<V>?) {
        if (metric == null) {
            throw DBSCANClusteringException("DBSCAN: Distance metric has not been specified (null).")
        }
        this.metric = metric
    }

    /**
     * Sets a collection of input values to be clustered.
     * Repeated call overwrite the original input values.
     *
     * @param collection
     * @throws DBSCANClusteringException
     */
    @Throws(DBSCANClusteringException::class)
    fun setInputValues(collection: Collection<V>?) {
        if (collection == null) {
            throw DBSCANClusteringException("DBSCAN: List of input values is null.")
        }
        inputValues = ArrayList(collection)
    }

    /**
     * Sets the minimal number of members to consider points of close proximity
     * clustered.
     *
     * @param minimalNumberOfMembers
     */
    fun setMinimalNumberOfMembersForCluster(minimalNumberOfMembers: Int) {
        minimumNumberOfClusterMembers = minimalNumberOfMembers
    }

    /**
     * Sets the maximal distance members of the same cluster can have while
     * still be considered in the same cluster.
     *
     * @param maximalDistance
     */
    fun setMaximalDistanceOfClusterMembers(maximalDistance: Double) {
        epsilon = maximalDistance
    }

    /**
     * Determines the neighbours of a given input value.
     *
     * @param inputValue Input value for which neighbours are to be determined
     * @return list of neighbours
     * @throws DBSCANClusteringException
     */
    @Throws(DBSCANClusteringException::class)
    private fun getNeighbours(inputValue: V): ArrayList<V> {
        val neighbours = ArrayList<V>()
        for (i in inputValues!!.indices) {
            val candidate = inputValues!![i]
            if (metric!!.calculateDistance(inputValue, candidate) <= epsilon) {
                neighbours.add(candidate)
            }
        }
        return neighbours
    }

    /**
     * Merges the elements of the right collection to the left one and returns
     * the combination.
     *
     * @param neighbours1 left collection
     * @param neighbours2 right collection
     * @return Modified left collection
     */
    private fun mergeRightToLeftCollection(
        neighbours1: ArrayList<V>,
        neighbours2: ArrayList<V>
    ): ArrayList<V> {
        for (i in neighbours2.indices) {
            val tempPt = neighbours2[i]
            if (!neighbours1.contains(tempPt)) {
                neighbours1.add(tempPt)
            }
        }
        return neighbours1
    }

    /**
     * Applies the clustering and returns a collection of clusters (i.e. a list
     * of lists of the respective cluster members).
     *
     * @return
     * @throws DBSCANClusteringException
     */
    @Throws(DBSCANClusteringException::class)
    fun performClustering(): ArrayList<ArrayList<V>> {
        if (inputValues == null) {
            throw DBSCANClusteringException("DBSCAN: List of input values is null.")
        }
        if (inputValues!!.isEmpty()) {
            throw DBSCANClusteringException("DBSCAN: List of input values is empty.")
        }
        if (inputValues!!.size < 2) {
            throw DBSCANClusteringException("DBSCAN: Less than two input values cannot be clustered. Number of input values: " + inputValues!!.size)
        }
        if (epsilon < 0) {
            throw DBSCANClusteringException("DBSCAN: Maximum distance of input values cannot be negative. Current value: $epsilon")
        }
        if (minimumNumberOfClusterMembers < 2) {
            throw DBSCANClusteringException("DBSCAN: Clusters with less than 2 members don't make sense. Current value: $minimumNumberOfClusterMembers")
        }
        val resultList = ArrayList<ArrayList<V>>()
        visitedPoints.clear()
        var neighbours: ArrayList<V>
        var index = 0
        while (inputValues!!.size > index) {
            val p = inputValues!!.get(index)
            if (!visitedPoints.contains(p)) {
                visitedPoints.add(p)
                neighbours = getNeighbours(p)
                if (neighbours.size >= minimumNumberOfClusterMembers) {
                    var ind = 0
                    while (neighbours.size > ind) {
                        val r = neighbours[ind]
                        if (!visitedPoints.contains(r)) {
                            visitedPoints.add(r)
                            val individualNeighbours = getNeighbours(r)
                            if (individualNeighbours.size >= minimumNumberOfClusterMembers) {
                                neighbours = mergeRightToLeftCollection(
                                    neighbours,
                                    individualNeighbours
                                )
                            }
                        }
                        ind++
                    }
                    resultList.add(neighbours)
                }
            }
            index++
        }
        return resultList
    }

    /**
     * Creates a DBSCAN clusterer instance.
     * Upon instantiation, call [.performClustering]
     * to perform the actual clustering.
     *
     * @param inputValues Input values to be clustered
     * @param minNumElements Minimum number of elements to constitute cluster
     * @param maxDistance Maximum distance of elements to consider clustered
     * @param metric Metric implementation to determine distance
     * @throws DBSCANClusteringException
     */
    init {
        setInputValues(inputValues)
        setMinimalNumberOfMembersForCluster(minNumElements)
        setMaximalDistanceOfClusterMembers(maxDistance)
        setDistanceMetric(metric)
    }
}

interface DistanceMetric<V> {
    @Throws(DBSCANClusteringException::class)
    fun calculateDistance(val1: V, val2: V): Int
}

class DBSCANClusteringException(string: String?) : Exception(string) {
    companion object {
        /**
         *
         */
        private const val serialVersionUID = -7264461537718433384L
    }
}