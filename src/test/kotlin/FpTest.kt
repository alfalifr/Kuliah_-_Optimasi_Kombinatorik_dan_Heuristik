import fp.Algo
import fp.Course
import fp.Student
import fp.Util
import org.junit.Test
import sidev.lib.collection.copy
import sidev.lib.collection.forEachIndexed
import sidev.lib.console.prin

class FpTest {
    @Test
    fun assignmentTest(){
        val students= listOf(
            Student(0, listOf(1,2,3,4,6)),
            Student(1, listOf(4,5)),
            Student(2, listOf(1,2,6)),
            Student(3, listOf(1,2)),
            Student(4, listOf(0,1,2,3,6)),
            Student(5, listOf(4,5)),
            Student(6, listOf(1,2)),
            Student(7, listOf(0,1)),
            Student(8, listOf(7)),
            Student(9, listOf(0,4)),
        )
        val adjacencyMatrix1= arrayOf(
//            intArrayOf(0,1,1,1,0,0,1,0),
            intArrayOf(0,1,1,1,1,0,1,0),
            intArrayOf(1,0,1,1,1,0,1,0), //1
            intArrayOf(1,1,0,1,1,0,1,0),
            intArrayOf(1,1,1,0,1,0,1,0), //3
//            intArrayOf(0,1,1,1,0,1,1,0),
            intArrayOf(1,1,1,1,0,1,1,0),
            intArrayOf(0,0,0,0,1,0,0,0), //5
            intArrayOf(1,1,1,1,1,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,0), //7
        )
        val courses= listOf(
            Course(0, 3),
            Course(1, 6),
            Course(2, 5),
            Course(3, 2),
            Course(4, 4),
            Course(5, 2),
            Course(6, 3),
            Course(7, 1),
        )

        val adjacencyMatrix2= Util.createCourseAdjacencyMatrix(courses.size, students)
        val degreeList= Util.getDegreeList(adjacencyMatrix2)

        val coursesWithDegree= courses.copy().apply {
            forEachIndexed { i, course ->
                course.degree= degreeList[i]
            }
        }

        prin("======== adjacencyMatrix2 ==========")
        var i= 0
        prin(adjacencyMatrix2.joinToString("\n"){ "i= ${i++} : " + it.joinToString() })
        prin("======== adjacencyMatrix2 - Selesai ==========")


        val sc1= Algo.assignToTimeslot(courses, adjacencyMatrix1)
        val sc2= Algo.assignToTimeslot(courses, adjacencyMatrix2)
        val scLDF= Algo.largestDegreeFirst(coursesWithDegree, adjacencyMatrix2)
        val scLSCF= Algo.largestStudentCountFirst(courses, adjacencyMatrix2)

        prin(sc1)
        prin(sc2)
        prin(scLDF)
        prin(scLSCF)

        val penalty1= Util.getPenalty(sc1, adjacencyMatrix2, students.size)
        val penalty2= Util.getPenalty(sc2, adjacencyMatrix2, students.size)
        val penalty3= Util.getPenalty(scLDF, adjacencyMatrix2, students.size)
        val penalty4= Util.getPenalty(scLSCF, adjacencyMatrix2, students.size)

        prin(penalty1)
        prin(penalty2)
        prin(penalty3)
        prin(penalty4)
    }
}