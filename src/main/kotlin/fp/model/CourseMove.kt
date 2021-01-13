package fp.model

data class CourseMove(val id: Int, val from: Int, val to: Int)
//infix fun Int.movesTo(to: Int): CourseMove = CourseMove(this, to)