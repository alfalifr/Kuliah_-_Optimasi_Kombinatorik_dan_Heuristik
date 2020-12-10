import fp.*
import fp.Config.DATASET_DIR
import fp.Config.FILE_EXTENSION_COURSE
import fp.Config.FILE_EXTENSION_RES
import fp.Config.FILE_EXTENSION_SOLUTION
import fp.Config.FILE_EXTENSION_STUDENT
import fp.Config.SOLUTION_DIR
import org.junit.Test
import sidev.lib.collection.copy
import sidev.lib.collection.forEachIndexed
import sidev.lib.console.prin
import sidev.lib.jvm.tool.util.FileUtil
import java.io.File
import java.util.*

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

    @Test
    fun realAssignmentTest(){
        val folderDir= DATASET_DIR //"D:\\DataCloud\\OneDrive\\OneDrive - Institut Teknologi Sepuluh Nopember\\Kuliah\\SMT 7\\OKH-A\\M10\\OKH - Dataset - Toronto"
        val courseDir= "$folderDir\\hec-s-92.crs" //car-s-91.crs" //car-f-92.crs" car-s-91
        val studentDir= "$folderDir\\hec-s-92.stu" //car-s-91.stu" //car-f-92.stu"

        val students= Util.readStudent(studentDir)
        val courses= Util.toListOfCourses(Util.readCourse(courseDir))

        prin(students.size)
        prin(courses.size)

        val adjacencyMatrix= Util.createCourseAdjacencyMatrix_Raw(courses.size, students)
//        val adjacencyMatrix= Util.createCourseAdjacencyMatrix_Raw(courses.size, students)

        val sc1= Algo.assignToTimeslot(courses, adjacencyMatrix)
        val penalty= Util.getPenalty(sc1, adjacencyMatrix, students.size)

        prin(sc1)
        prin(penalty)
    }

    @Test
    fun realAssignmentTest_2(){
//        val folderDir= "D:\\DataCloud\\OneDrive\\OneDrive - Institut Teknologi Sepuluh Nopember\\Kuliah\\SMT 7\\OKH-A\\M10\\OKH - Dataset - Toronto"
/*
        val courseDir= "$DATASET_DIR\\pur-s-93.crs" //hec-s-92.crs" //car-s-91.crs" //pur-s-93.crs" //\\car-f-92.crs"
        val studentDir= "$DATASET_DIR\\pur-s-93.stu" //hec-s-92.stu" //car-s-91.stu" //pur-s-93.stu" //\\car-f-92.stu"
        val solDir= "$SOLUTION_DIR\\pur-s-93.sol" //car-f-92.sol"
        val resDir= "$SOLUTION_DIR\\pur-s-93.res" //car-f-92.sol"
        val solFile= File(solDir)
        val resFile= File(resDir)
 */
/*
        prin("Pilih file:")
        for((i, name) in Config.fileNames.withIndex()){
            prin("$i. $name")
        }
 */
        val fileName= "hec-s-92" //"pur-s-93"
        val maxTimeslot= 18 //42
        val nameIndex= Config.getFileNameIndex(fileName) //Scanner(System.`in`).next().toInt()

        val fileDir= Config.getFileDir(nameIndex)
        val courseDir= "$fileDir$FILE_EXTENSION_COURSE" //hec-s-92.crs" //car-s-91.crs" //pur-s-93.crs" //\\car-f-92.crs"
        val studentDir= "$fileDir$FILE_EXTENSION_STUDENT" //hec-s-92.stu" //car-s-91.stu" //pur-s-93.stu" //\\car-f-92.stu"
        val solDir= "$fileDir$FILE_EXTENSION_SOLUTION" //car-f-92.sol"
        val resDir= "$fileDir$FILE_EXTENSION_RES" //car-f-92.sol"
        val solFile= File(solDir)
        val resFile= File(resDir)

        prin("fileName= $fileName index= $nameIndex")

        val students= Util.readStudent(studentDir)
        val courses= Util.toListOfCourses(Util.readCourse(courseDir))

        prin("students.size= ${students.size}")
        prin("courses.size= ${courses.size}")

        val adjacencyMatrix= Util.createCourseAdjacencyMatrix_Raw(courses.size, students)
        val density= Util.getDensity(adjacencyMatrix)

        prin("density= $density")

        val degreeList= Util.getDegreeList(adjacencyMatrix)
        degreeList.forEachIndexed { i, degree ->
            courses[i].degree= degree
        }

//        val adjacencyMatrix= Util.createCourseAdjacencyMatrix_Raw(courses.size, students)

        val sc1= Algo.assignToTimeslot(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val sc2= Algo.largestDegreeFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val sc3= Algo.largestStudentCountFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val sc4= Algo.largestWeightedDegreeFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val penalty1= Util.getPenalty(sc1, adjacencyMatrix, students.size)
        val penalty2= Util.getPenalty(sc2, adjacencyMatrix, students.size)
        val penalty3= Util.getPenalty(sc3, adjacencyMatrix, students.size)
        val penalty4= Util.getPenalty(sc4, adjacencyMatrix, students.size)

        prin("\n\n ============ First Order =============== \n")
        prin("Time table:")
        prin(sc1)
        prin("Penalty: $penalty1")

        prin("\n\n ============ Largest Degree First =============== \n")
        prin("Time table:")
        prin(sc2)
        prin("Penalty: $penalty2")

        prin("\n\n ============ Largest Student Count First =============== \n")
        prin("Time table:")
        prin(sc3)
        prin("Penalty: $penalty3")

        prin("\n\n ============ Largest Weighted Degree First =============== \n")
        prin("Time table:")
        prin(sc4)
        prin("Penalty: $penalty4")

//        Util.printFinalSol(fileName, sc1, sc2, sc3, sc4)
        Util.getLeastPenaltySchedule(sc1, sc2, sc3, sc4, maxTimeslot = maxTimeslot)
/*
        solFile.delete()
        for((course, timeslot) in sc1){
            FileUtil.saveln(
                FileUtil.getAvailableFile(solFile),
                "${course.id} ${timeslot.no}",
                true
            )
        }
        resFile.delete()
        FileUtil.saveln(
            FileUtil.getAvailableFile(resFile),
            sc1.assignments.size.toString()
        )
// */
    }

    @Test
    fun realAssignmentTest_3(){
        for(i in Config.fileNames.indices){
            Util.runScheduling(i, Config.maxTimeslot[i], false)
        }
    }

    @Test
    fun realAssignmentTest_4(){
        Util.runAllScheduling()
            .also { prin("\n\n\n=============== Hasil Semua Scheduling ==========") }
            .forEach { (tag, sc) -> prin("fileName= ${tag.fileName} sc= ${sc?.miniString()}") }
    }
}