package fp.model

import fp.Config
import sidev.lib.`val`.SuppressLiteral
import sidev.lib.check.isNull
import sidev.lib.check.notNull
import sidev.lib.check.notNullTo
import sidev.lib.collection.find
import sidev.lib.collection.findIndexed
import sidev.lib.collection.joinToString
import sidev.lib.exception.IllegalArgExc
import sidev.lib.structure.data.Cloneable


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
                val c1Index= c1.id - Config.COURSE_INDEX_OFFSET
                for(u in i+1 until courses.size){
                    val c2= courses[u]
                    if(adjacencyMatrix[c1Index][c2.id - Config.COURSE_INDEX_OFFSET] > 0
                        && (predicate == null || predicate(c2)))
                        return false
                }
            }
        } else {
            val courseIndex= courseId - Config.COURSE_INDEX_OFFSET
            for(c2 in courses){
                if(adjacencyMatrix[courseIndex][c2.id - Config.COURSE_INDEX_OFFSET] > 0
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
    fun moveById(courseId: Int, toTimeslot: Int, trimAfter: Boolean = true): Course {
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

        //prine("toTimeslot= $toTimeslot courseId= $courseId assignments= $assignments")
        val toCourses= assignments.find { courses_ ->
            courses_.key.no == toTimeslot
        }!!.value

        toCourses += movedCourse!!
        fromCourses.removeAt(movedCourseIndex)
        if(trimAfter && fromCourses.isEmpty())
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
                //val map= hashMapOf<String, Int>()
                //map.remove("removedKey")
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