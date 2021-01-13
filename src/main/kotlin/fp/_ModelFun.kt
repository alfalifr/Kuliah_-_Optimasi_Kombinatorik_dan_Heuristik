package fp

import fp.model.AssignmentEntry
import fp.model.FlatSchedule
import fp.model.Schedule
import fp.model.TestResult
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
infix fun <T> T.withTime(duration: Duration): TestResult<T> = TestResult(this, duration)

fun Schedule.toFlat(): FlatSchedule {
    val res= FlatSchedule()
    for((course, timeslot) in this){
        res += AssignmentEntry(course, timeslot.no)
    }
    return res
}

fun FlatSchedule.toOfficial(): Schedule {
    val res= Schedule()
    for((course, timeslot) in this){
        res += AssignmentEntry(course, timeslot)
    }
    return res
}

/*
//data class CourseAdjacencyMatrix(val coursesPair: )


//data class Node<T>(val content: T, val neighbours: List<Node<T>>, var tag: Any?)
//data class Vertex<T>(val node1: Node<T>, val node2: Node<T>, var tag: Any?)

/**
 * Kelas yang mempermudah navigasi antar node dalam graph.
 * Index dari [adjacencyMatrix] merepresentasikan index [nodeElements].
 * Oleh karena itu [nodeElements.size] == [adjacencyMatrix.size]
 */
class Graph<T>(
    val adjacencyMatrix: List<List<Int>>, val nodeElements: List<T> //val getIdFun: ((T) -> Int)? = null
): Copyable<Graph<T>>, List<T> by nodeElements {
    constructor(adjacencyMatrix: Array<IntArray>, nodeElements: List<T>)
            : this(adjacencyMatrix.map { it.toList() }, nodeElements)
    init {
        if(adjacencyMatrix.size != nodeElements.size)
            throw IllegalArgExc(
                paramExcepted = arrayOf("adjacencyMatrix", "nodeElements"),
                detailMsg = "`adjacencyMatrix` dan `nodeElements` harus punya size yg sama"
            )
    }

    val nodeTag: Array<Any?> = arrayOfNulls(nodeElements.size)

    private var isInternalEdit= false
    var beforePosition: Int= -1
        private set
    var currentPosition: Int = 0
        private set
    val currentNode: T
        get()= nodeElements[currentPosition]
    val beforeNode: T?
        get()= if(beforePosition >= 0) nodeElements[beforePosition] else null

    fun setNewPosition(pos: Int){
        currentPosition= pos
        beforePosition= -1
    }

    fun neighboursOf(node: T): List<T> = neighboursOf(nodeElements.indexOf(node))
    fun neighboursOf(nodePosition: Int): List<T> {
        val res= mutableListOf<T>()
        for((i, link) in adjacencyMatrix[nodePosition].withIndex()){
            if(link > 0)
                res += nodeElements[i]
        }
        return res
    }

    fun neighbourOf(node: T, predicate: (T) -> Boolean): T? = neighbourOf(nodeElements.indexOf(node), predicate)
    fun neighbourOf(nodePosition: Int, predicate: (T) -> Boolean): T? {
        for((i, link) in adjacencyMatrix[nodePosition].withIndex()){
            if(link > 0) {
                val neighbour= nodeElements[i]
                if(predicate(neighbour)){
                    return neighbour
                }
            }
        }
        return null
    }

    fun nextNeighbour(predicate: (T) -> Boolean): T? {
        for((i, link) in adjacencyMatrix[currentPosition].withIndex()){
            if(link > 0) {
                val neighbour= nodeElements[i]
                if(predicate(neighbour)){
                    beforePosition= currentPosition
                    currentPosition= i
                    return neighbour
                }
            }
        }
        return null
    }

    fun prev(): T? {
        return if(beforePosition >= 0){
            currentPosition= beforePosition
            beforeNode.also {
                beforePosition= -1
            }
        } else null
    }

    override fun copy(): Graph<T> = Graph(adjacencyMatrix, nodeElements).apply {
        beforePosition= this@Graph.beforePosition
        currentPosition= this@Graph.currentPosition
        for(i in nodeTag.indices)
            nodeTag[i]= this@Graph.nodeTag[i]
    }

    override fun equals(other: Any?): Boolean = other is Graph<*>
            && adjacencyMatrix == other.adjacencyMatrix && nodeElements == other.nodeElements

    override fun hashCode(): Int = getHashCode(adjacencyMatrix, nodeElements)
}
 */