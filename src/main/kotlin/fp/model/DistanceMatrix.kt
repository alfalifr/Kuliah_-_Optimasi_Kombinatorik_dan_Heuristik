package fp.model

import fp.Config
import sidev.lib.exception.IllegalArgExc
import sidev.lib.number.pow
import sidev.lib.structure.data.Cloneable
import kotlin.math.absoluteValue


/**
 * Dimensi [positionMatrix] dan [adjacencyMatrix] harus sama.
 */
data class DistanceMatrix(
    val positionMatrix: Array<Array<Pair<Int, Int>>>,
    val adjacencyMatrix: Array<IntArray>,
    val distanceRange: IntRange = 0 .. 4
): Cloneable<DistanceMatrix> {
    init {
        if(positionMatrix.size != adjacencyMatrix.size)
            throw IllegalArgExc(
                paramExcepted = arrayOf("positionMatrix", "adjacencyMatrix"),
                detailMsg = "Param `positionMatrix` memiliki panjang (${positionMatrix.size}) != panjang `adjacencyMatrix` (${adjacencyMatrix.size})"
            )
    }

    operator fun get(index: Int): Double = getLinePenalty(index)
    operator fun get(from: Int, to: Int): Double = getPenaltyComp(from, to)
    operator fun set(index: Int, newPosition: Int) = setPositionMatrix(index, newPosition)
    operator fun set(from: Int, to: Int, pair: Pair<Int, Int>): Pair<Int, Int> = setDistance(from, to, pair)
    operator fun set(from: Int, to: Int, adjacency: Int): Int = setAdjacency(from, to, adjacency)
    fun setDistance(from: Int, to: Int, pair: Pair<Int, Int>): Pair<Int, Int> {
        val old= positionMatrix[from][to]
        positionMatrix[from][to]= pair
        return old
    }
    fun setAdjacency(from: Int, to: Int, adjacency: Int): Int {
        val old= adjacencyMatrix[from][to]
        adjacencyMatrix[from][to]= adjacency
        return old
    }
    fun getPenaltyComp(from: Int, to: Int): Double = if(from == to) 0.0
    else getDistance(from, to).let {
        if(it in distanceRange) adjacencyMatrix[from][to] * (2 pow (distanceRange.last - it)).toDouble()
        else 0.0
    }
    fun getLinePenalty(
        index: Int,
        newSrcPosition: Int = -1,
        newDestPositions: List<CourseMove>? = null
    ): Double {
        var sum= 0.0
        if(newSrcPosition < 1 && (newDestPositions == null || newDestPositions.isEmpty())){
            for(i in adjacencyMatrix.indices){
                sum += getPenaltyComp(index, i)
            }
        } else {
            val destItr= newDestPositions?.iterator()
            var destPair= if(destItr?.hasNext() == true) destItr.next() else null
            var destPairId= destPair?.id?.minus(Config.COURSE_INDEX_OFFSET) ?: -1
            val srcPos= if(newSrcPosition >= 0) newSrcPosition
            else positionMatrix[index][0].first
            val distRangeLast= distanceRange.last
            for(i in adjacencyMatrix.indices){
                if(i == index) continue
                val dest= if(i == destPairId) {
                    val curr= destPair!!.to //- COURSE_INDEX_OFFSET
                    if(destItr!!.hasNext()) { //Karena destPair tidak akan dijadikan null setelah inisiasi.
                        destPair= destItr.next()
                        destPairId= destPair.id - Config.COURSE_INDEX_OFFSET
                    }
                    curr
                } else positionMatrix[index][i].second
                val conflict= adjacencyMatrix[index][i]
                if(conflict < 1) continue
                val dist= (srcPos - dest).absoluteValue
                val penalty= if(dist in distanceRange){
                    conflict * (2 pow (distRangeLast - dist)).toDouble()
                } else 0.0
                sum += penalty
            }
/*
            if(newDestPositions == null || newDestPositions.isEmpty()){
                for(i in adjacencyMatrix.indices){
                    if(i == index) continue
                    val coord= positionMatrix[index][i]
                    val conflict= adjacencyMatrix[index][i]
                    if(conflict < 1) continue
                    val dist= (newSrcPosition - coord.second).absoluteValue
                    val penalty= if(dist in distanceRange){
                        conflict * (2 pow dist).toDouble()
                    } else 0.0
                    sum += penalty
                }
            } else {
            }
 */
        }
        return sum
    }
    fun getDistance(from: Int, to: Int): Int = if(from == to) 0
    else positionMatrix[from][to].run {
        (first - second).absoluteValue
    }

    fun setPositionMatrix(move: CourseMove) = setPositionMatrix(move.id - Config.COURSE_INDEX_OFFSET, move.to)
    fun setPositionMatrix(index: Int, newPosition: Int){
        val line= positionMatrix[index]
        for((i, pair) in line.withIndex()){
            if(i == index) continue
            line[i]= newPosition to pair.second
            positionMatrix[i][index]= pair.second to newPosition
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DistanceMatrix

        if (!positionMatrix.contentDeepEquals(other.positionMatrix)) return false
        if (!adjacencyMatrix.contentDeepEquals(other.adjacencyMatrix)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = positionMatrix.contentDeepHashCode()
        result = 31 * result + adjacencyMatrix.contentDeepHashCode()
        return result
    }

    override fun clone_(isShallowClone: Boolean): DistanceMatrix {
        val newPosMat= Array(positionMatrix.size) { positionMatrix[it].copyOf() }
        val newAdjMat= Array(adjacencyMatrix.size) { adjacencyMatrix[it].copyOf() }
        return DistanceMatrix(newPosMat, newAdjMat, distanceRange)
    }
}