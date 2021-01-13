package fp.model


data class Student(val id: Int, val courses: List<Int>){
    override fun toString(): String = "S$id"
}