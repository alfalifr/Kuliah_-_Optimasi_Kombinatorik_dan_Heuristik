package fp.model

data class Course(val id: Int, var studentCount: Int, var degree: Int = 0, var conflictingStudentCount: Int = 0){
    override fun toString(): String = "C$id"
    override fun equals(other: Any?): Boolean = other is Course && other.id == id
            || other is Int
    override fun hashCode(): Int = id
}