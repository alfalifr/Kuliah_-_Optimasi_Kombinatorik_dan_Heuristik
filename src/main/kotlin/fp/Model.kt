package fp

import fp.Config.COURSE_INDEX_OFFSET
import sidev.lib.`val`.SuppressLiteral
import sidev.lib.check.isNull
import sidev.lib.check.notNull
import sidev.lib.check.notNullTo
import sidev.lib.collection.find
import sidev.lib.collection.findIndexed
import sidev.lib.collection.joinToString
import sidev.lib.exception.IllegalArgExc
import sidev.lib.number.pow
import sidev.lib.structure.data.Cloneable
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


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
            var destPairId= destPair?.id?.minus(COURSE_INDEX_OFFSET) ?: -1
            val srcPos= if(newSrcPosition >= 0) newSrcPosition
                else positionMatrix[index][0].first
            val distRangeLast= distanceRange.last
            for(i in adjacencyMatrix.indices){
                if(i == index) continue
                val dest= if(i == destPairId) {
                    val curr= destPair!!.to //- COURSE_INDEX_OFFSET
                    if(destItr!!.hasNext()) { //Karena destPair tidak akan dijadikan null setelah inisiasi.
                        destPair= destItr.next()
                        destPairId= destPair.id - COURSE_INDEX_OFFSET
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

    fun setPositionMatrix(move: CourseMove) = setPositionMatrix(move.id - COURSE_INDEX_OFFSET, move.to)
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

data class CourseMove(val id: Int, val from: Int, val to: Int)
//infix fun Int.movesTo(to: Int): CourseMove = CourseMove(this, to)

data class Course(val id: Int, var studentCount: Int, var degree: Int = 0, var conflictingStudentCount: Int = 0){
    override fun toString(): String = "C$id"
    override fun equals(other: Any?): Boolean = other is Course && other.id == id
            || other is Int
    override fun hashCode(): Int = id
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
data class ScheduleTag(
    var construct: Construct= Construct.UNSORTED,
    var optimization: Optimize= Optimize.NOT_YET,
    var fileName: String?= null
){
    fun miniString()= "$fileName - $construct - $optimization"
}
data class Schedule(
    val assignments: MutableMap<Timeslot, MutableList<Course>> = mutableMapOf(),
//    var title: String = "<schedule>",
//    var fileName: String? = "",
//    var algo: Any?= null,
    var penalty: Double = -1.0, //Nilai default
    val tag: ScheduleTag= ScheduleTag(),
): Iterable<Pair<Course, Timeslot>>, Cloneable<Schedule> {
    val timeslotCount: Int
        get()= assignments.size

    fun getAssignment(timeslotNo: Int): Assignment? = assignments.find { it.key.no == timeslotNo }?.let {
        Assignment(it.key, it.value)
    } ?: throw IllegalArgExc(paramExcepted=arrayOf("timeslotNo"), detailMsg="Tidak ada timeslot dengan `timeslotNo` ($timeslotNo) tidak terdapat pada schedule ini.")
    fun getAssignmentAssert(timeslotNo: Int): Assignment = getAssignment(timeslotNo)
        ?: throw IllegalArgExc(paramExcepted=arrayOf("timeslotNo"), detailMsg="Tidak ada timeslot dengan `timeslotNo` ($timeslotNo) tidak terdapat pada schedule ini.")
    //assignments.find { it.timeslot.no == no }
    fun getCourseTimeslot(courseId: Int): Timeslot? = assignments.find { it.value.any { it.id == courseId } }?.key
    fun getTimeslot(no: Int): Timeslot = assignments.keys.find { it.no == no }
        ?: throw IllegalArgExc(paramExcepted=arrayOf("no"), detailMsg="Tidak ada timeslot dengan `no` ($no) tidak terdapat pada schedule ini.")
    fun getCourse(courseId: Int): Course? {
        var courseIndex= -1
        return assignments.find {
            it.value.findIndexed { it.value.id == courseId }.notNullTo {
                courseIndex= it.index
                true
            } ?: false
        }?.value?.get(courseIndex)
    }

    fun getRandomCourse(timeslotNo: Int): Pair<Int, Course> {
        val assignment = assignments.find { it.key.no == timeslotNo }
            ?: throw IllegalArgExc(paramExcepted=arrayOf("timeslotNo"), detailMsg="Param `timeslotNo` ($timeslotNo) tidak terdapat pada schedule ini.")
        val courses= assignment.value
        val i= (0 until courses.size).random()
        return i to courses[i]
    }

    operator fun plusAssign(entry: AssignmentEntry){
        val timeslot= entry.timeslot
        assignments.keys.find { it.no == timeslot }
            .notNull { assignments[it]!! += entry.course }
            .isNull {
                val newTimeslot= Timeslot(timeslot)
                assignments[newTimeslot]= mutableListOf(entry.course)
            }
    }
    operator fun plusAssign(entry: Pair<Course, Int>) = plusAssign(AssignmentEntry(entry.first, entry.second))

    fun checkConflictInTimeslot(
        timeslotNo: Int, adjacencyMatrix: Array<IntArray>,
        courseOrder: Int = -1,
        course: Course?= null, courseId: Int = -1,
        predicate: ((Course) -> Boolean)?= null
    ): Boolean {
        val assign= getAssignmentAssert(timeslotNo)
        val courses= assign.courses
        @Suppress(SuppressLiteral.NAME_SHADOWING)
        val courseId= course?.id ?: run {
            when {
                courseId > 0 -> courseId
                courseOrder >= 0 -> courses[courseOrder].id
                else -> -1
            }
        }
        if(courseId < 1){
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            for((i, c1) in courses.withIndex()){
                val c1Index= c1.id - COURSE_INDEX_OFFSET
                for(u in i+1 until courses.size){
                    val c2= courses[u]
                    if(adjacencyMatrix[c1Index][c2.id - COURSE_INDEX_OFFSET] > 0
                        && (predicate == null || predicate(c2)))
                        return false
                }
            }
        } else {
            val courseIndex= courseId - COURSE_INDEX_OFFSET
            for(c2 in courses){
                if(adjacencyMatrix[courseIndex][c2.id - COURSE_INDEX_OFFSET] > 0
                    && (predicate == null || predicate(c2)))
                    return false
            }
        }
        return true
    }
    /*
    fun move(fromTimeslot: Int, courseOrder: Int, toTimeslot: Int): Course {
        return getTimeslot(fromTimeslot)
            .isNull { throw IllegalArgExc(paramExcepted=arrayOf("fromTimeslot"), detailMsg="Param `fromTimeslot` ($fromTimeslot) tidak terdapat pada schedule ini.") }
            .notNullTo { assignments[it]!!.removeAt(courseOrder) }
            .notNullTo { course ->
                getTimeslot(toTimeslot)
                    .isNull { throw IllegalArgExc(paramExcepted=arrayOf("toTimeslot"), detailMsg="Param `toTimeslot` ($toTimeslot) tidak terdapat pada schedule ini.") }
                    .notNull { assignments[it]!! += course}
                course
            }!!
    }
     */
    fun moveById(courseId: Int, toTimeslot: Int): Course {
        var movedCourse: Course?= null
        var movedCourseIndex= -1
        var movedCourseTimeslotI= -1
//        var courses: MutableList<Course>?= null
        val fromCourses= assignments.findIndexed { i, courses_ ->
            courses_.value.findIndexed { it.value.id == courseId }.notNullTo {
                movedCourseIndex= it.index
                movedCourse= it.value
                movedCourseTimeslotI= i
                true
            } ?: false
        }!!.value

        val toCourses= assignments.find { courses_ ->
            courses_.key.no == toTimeslot
        }!!.value

        toCourses += movedCourse!!
        fromCourses.removeAt(movedCourseIndex)
        if(fromCourses.isEmpty())
            trimTimeslot(movedCourseTimeslotI)
        return movedCourse!!
    }
    /*
    fun moveAny(fromTimeslot: Int, toTimeslot: Int): Course {
        return getTimeslot(fromTimeslot)
            .isNull { throw IllegalArgExc(paramExcepted=arrayOf("fromTimeslot"), detailMsg="Param `fromTimeslot` ($fromTimeslot) tidak terdapat pada schedule ini.") }
            .notNullTo {
                val list = assignments[it]!!
                val courseOrder = (0 until list.size).random()
                val removed= list.removeAt(courseOrder)
                removed
            }.notNullTo { course ->
                getTimeslot(toTimeslot)
                    .isNull { throw IllegalArgExc(paramExcepted=arrayOf("toTimeslot"), detailMsg="Param `toTimeslot` ($toTimeslot) tidak terdapat pada schedule ini.") }
                    .notNull { assignments[it]!! += course }
                course
            }!!
    }

    fun swapAny(fromTimeslot: Int, toTimeslot: Int): Pair<Course, Course> {
        @Suppress(SuppressLiteral.NAME_SHADOWING)
        val fromTimeslot= getTimeslot(fromTimeslot) //?: throw IllegalArgExc(paramExcepted=arrayOf("fromTimeslot"), detailMsg="Param `fromTimeslot` ($fromTimeslot) tidak terdapat pada schedule ini.")
        @Suppress(SuppressLiteral.NAME_SHADOWING)
        val toTimeslot= getTimeslot(toTimeslot) //?: throw IllegalArgExc(paramExcepted=arrayOf("toTimeslot"), detailMsg="Param `toTimeslot` ($toTimeslot) tidak terdapat pada schedule ini.")

        val fromCourseList= assignments[fromTimeslot]!!
        val toCourseList= assignments[toTimeslot]!!

        val from= (0 until fromCourseList.size).random()
        val to= (0 until toCourseList.size).random()

        val fromCourse= fromCourseList[from]
        val toCourse= toCourseList[to]
        fromCourseList[from]= toCourse
        toCourseList[to]= fromCourse
        return fromCourse to toCourse
        /*
            .notNullTo {
                val list = assignments[it]!!
                val courseOrder = (0 until list.size).random()
                list.removeAt(courseOrder)
            }.notNullTo { course ->
                getTimeslot(toTimeslot)
                    .isNull { throw IllegalArgExc(paramExcepted=arrayOf("toTimeslot"), detailMsg="Param `toTimeslot` ($toTimeslot) tidak terdapat pada schedule ini.") }
                    .notNull { assignments[it]!! += course}
            }
         */
    }
     */

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
/*
    fun iterator(orderedByCourse: Boolean, start: Int= 0): Iterator<Pair<Course, Timeslot>> = if(!orderedByCourse) iterator()
    else object: Iterator<Pair<Course, Timeslot>> {
        var i= start
        var next: Pair<Course, Timeslot>?= null

        override fun hasNext(): Boolean = getCourseTimeslot(i)?.also { next= assignments[it]!!.find { it.id == i }!! to it } != null
        override fun next(): Pair<Course, Timeslot> = next!!
    }
 */


    /**
     * Menghapus timeslot yang kosong.
     */
    fun trimTimeslot(from: Int = 0): List<Timeslot> {
        var destDiff= 0
        val keys= mutableListOf<Timeslot>()
        val coursesList= mutableListOf<MutableList<Course>>()
        val removedList= mutableListOf<Timeslot>()
        val entries= assignments.entries.toList()
        for(i in from until assignments.size){
            val e= entries[i]
            keys += e.key
            coursesList += e.value
        }

        for((i, c) in coursesList.withIndex()){
            if(c.isEmpty())
                destDiff++
            else if(destDiff > 0)
                assignments[keys[i-destDiff]]= c
            //coursesList[i-destDiff]= c
        }
        if(destDiff > 0){
            val lastIndex= coursesList.lastIndex
            for(i in lastIndex downTo lastIndex - destDiff + 1){
                //coursesList.removeLast()
                val removedKey= keys[i]
                assignments.remove(removedKey)
                removedList += removedKey
            }
        }
        return removedList
    }

    override operator fun iterator(): Iterator<Pair<Course, Timeslot>> = object: Iterator<Pair<Course, Timeslot>> {
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

    /**
     * Otomatis deepClone.
     */
    override fun clone_(isShallowClone: Boolean): Schedule {
        //val hm= HashMap<String, Int>()
        //hm.put("", 1)
        val newAssignments= mutableMapOf<Timeslot, MutableList<Course>>()
        for((key, value) in assignments)
            newAssignments[key]= ArrayList(value)
        return Schedule(
            newAssignments, penalty, tag.copy()
        )
    }
}

/**
 * Sama seperti [Schedule], namun entry berupa `Pair<Schedule, Int>` dengan Int merupakan no timeslot.
 */
data class FlatSchedule(
    val entries: HashMap<Course, Int> = HashMap(),
    var penalty: Double = -1.0
): Iterable<Pair<Course, Int>>, Cloneable<FlatSchedule> {
    val courseCount: Int
        get()= entries.size

    operator fun set(course: Course, timeslot: Int): Int {
        val old= entries[course] //.find { it.key.id == course.id }
            ?: throw IllegalArgExc(detailMsg = "Tidak ada course dg id (${course.id})")
        entries[course]= timeslot
        return old
    }

    @Suppress(
        "TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING",
        SuppressLiteral.UNCHECKED_CAST
    ) operator fun set(courseId: Int, timeslot: Int): Int = set(Course(courseId, -1), timeslot)
    /*
    {
        val entry= entries.find { it.key.id == courseId } //.find { it.key.id == course.id }
            ?: throw IllegalArgExc(detailMsg = "Tidak ada course dg id (${courseId})")
        entries[entry.key]= timeslot
        return entry.value
    }
     */

    /** Mengambil nomer timeslot dari [course]. */
    operator fun get(course: Course): Int = entries[course]
        ?: throw IllegalArgExc(detailMsg = "Tidak ada course dg id (${course.id})")
    fun getCourseTimeslot(course: Course): Int = get(course)
    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING") //Karena has dari Course adalah id-nya.
    fun getCourseTimeslot(courseId: Int): Int = entries.find { it.key.id == courseId }?.value
        ?: throw IllegalArgExc(detailMsg = "Tidak ada course dg id (${courseId})")

    operator fun plusAssign(entry: AssignmentEntry){ entries[entry.course]= entry.timeslot }
    operator fun plusAssign(entry: Pair<Course, Int>){ entries[entry.first]= entry.second }

    override fun clone_(isShallowClone: Boolean): FlatSchedule = FlatSchedule(
        HashMap(entries), penalty
    )

    override fun iterator(): Iterator<Pair<Course, Int>> = object : Iterator<Pair<Course, Int>>{
        val itr= entries.iterator()
        override fun hasNext(): Boolean = itr.hasNext()
        override fun next(): Pair<Course, Int> = itr.next().run {
            key to value
        }
    }

    fun checkConflictInSameTimeslot(
        adjacencyMatrix: Array<IntArray>,
        courseId: Int
    ): Boolean {
        val c1t= getCourseTimeslot(courseId)
        for((i, adj) in adjacencyMatrix[courseId - COURSE_INDEX_OFFSET].withIndex()){
            if(adj > 0 && getCourseTimeslot(i + COURSE_INDEX_OFFSET) == c1t){
                return false
            }
        }
        return true
    }
}

data class ScheduleConflict(val sc: Schedule, val conflicts: List<Pair<Timeslot, Int>>){
    override fun toString(): String = sc.miniString() +"\nConflicts= \n" +conflicts.joinToString { it.first.toString() +": " +it.second.toString() }
}

data class AssignmentEntry(val course: Course, var timeslot: Int)

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