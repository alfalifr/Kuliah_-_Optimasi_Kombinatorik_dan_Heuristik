package fp.model

import fp.Config
import sidev.lib.`val`.SuppressLiteral
import sidev.lib.collection.find
import sidev.lib.exception.IllegalArgExc
import sidev.lib.structure.data.Cloneable


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
        for((i, adj) in adjacencyMatrix[courseId - Config.COURSE_INDEX_OFFSET].withIndex()){
            if(adj > 0 && getCourseTimeslot(i + Config.COURSE_INDEX_OFFSET) == c1t){
                return false
            }
        }
        return true
    }
}