package fp.model

data class Assignment(val timeslot: Timeslot, val courses: List<Course> = mutableListOf()){
    fun hasCourse(courseId: Int): Boolean = courses.any { it.id == courseId }
    override fun toString(): String = "$timeslot: $courses"
}