package fp

import sidev.lib.check.isNull
import sidev.lib.check.notNull
import sidev.lib.check.notNullTo
import sidev.lib.collection.array.forEachIndexed
import sidev.lib.collection.find
import sidev.lib.collection.forEachIndexed
import sidev.lib.collection.joinToString
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

data class Course(val id: Int, val studentCount: Int, var degree: Int = 0){
    override fun toString(): String = "C$id"
}
data class Student(val id: Int, val courses: List<Int>){
    override fun toString(): String = "S$id"
}
data class Timeslot(val no: Int, val name: String = "T$no"){
    override fun toString(): String = name
}
///*
data class Assignment(val timeslot: Timeslot, val courses: List<Course> = mutableListOf()){
    fun hasCourse(courseId: Int): Boolean = courses.any { it.id == courseId }
    override fun toString(): String = "$timeslot: $courses"
}
// */
data class ScheduleTag(var algo: Algo= Algo.UNSORTED, var fileName: String?= null){
    fun miniString()= "$fileName - $algo"
}
data class Schedule(
    val assignments: MutableMap<Timeslot, MutableList<Course>> = mutableMapOf(),
//    var title: String = "<schedule>",
//    var fileName: String? = "",
//    var algo: Any?= null,
    var penalty: Double = -1.0, //Nilai default
    val tag: ScheduleTag= ScheduleTag(),
) {
    val timeslotCount: Int
        get()= assignments.size

    fun getAssignment(no: Int): Assignment? = assignments.find { it.key.no == no }?.let {
        Assignment(it.key, it.value)
    } //assignments.find { it.timeslot.no == no }
    fun getCourseTimeslot(courseId: Int): Timeslot? = assignments.find { it.value.any { it.id == courseId } }?.key

    operator fun get(timeslot: Timeslot): MutableList<Course>? = assignments[timeslot]
    operator fun get(course: Course): Timeslot? = getCourseTimeslot(course.id)
    operator fun set(timeslot: Timeslot, courses: MutableList<Course>) {
        assignments[timeslot]= courses
    }

    /**
     * Return [Timeslot] sebelumnya.
     */
    operator fun set(course: Course, timeslot: Timeslot): Timeslot? {
        assignments[timeslot].notNull { it += course }
            .isNull { assignments[timeslot]= mutableListOf(course) }

        return assignments.find { course in it.value }.notNullTo {
            it.value.remove(course)
            it.key
        }
    }

    operator fun iterator(): Iterator<Pair<Course, Timeslot>> = object: Iterator<Pair<Course, Timeslot>> {
        val mapItr= assignments.iterator()
        var currEntry: Map.Entry<Timeslot, List<Course>>?= if(mapItr.hasNext()) mapItr.next() else null
        var currCourseIndex: Int = 0

        val currTimeslot: Timeslot
            get()= currEntry!!.key
        val currCourses: List<Course>
            get()= currEntry!!.value

        override fun hasNext(): Boolean {
            if(currEntry == null) return false
            if(currCourseIndex >= currCourses.size){
                if(!mapItr.hasNext()) return false
                currEntry= mapItr.next()
                currCourseIndex= 0
            }
            return true
        }
        override fun next(): Pair<Course, Timeslot> = currCourses[currCourseIndex++] to currTimeslot
    }

    fun miniString(): String = "Schedule - ${tag.miniString()} : penalty=$penalty timeslots=$timeslotCount"
    override fun toString(): String = assignments.joinToString("\n") { "${it.key}: ${it.value}" }
}

data class TestResult<T> @OptIn(ExperimentalTime::class) constructor(val result: T, val duration: Duration)

@OptIn(ExperimentalTime::class)
infix fun <T> T.withTime(duration: Duration): TestResult<T> = TestResult(this, duration)

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