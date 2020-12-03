package fp

import sidev.lib.check.isNull
import sidev.lib.check.notNull
import sidev.lib.check.notNullTo
import sidev.lib.collection.find
import sidev.lib.collection.joinToString

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
data class Schedule(val assignments: MutableMap<Timeslot, MutableList<Course>> = mutableMapOf()){
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

    override fun toString(): String = assignments.joinToString("\n") { "${it.key}: ${it.value}" }
}

//data class CourseAdjacencyMatrix(val coursesPair: )