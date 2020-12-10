package fp

import fp.Config.COURSE_INDEX_OFFSET
import fp.Config.FILE_EXTENSION_COURSE
import fp.Config.FILE_EXTENSION_RES
import fp.Config.FILE_EXTENSION_SOLUTION
import fp.Config.FILE_EXTENSION_STUDENT
import sidev.lib.collection.array.forEachIndexed
import sidev.lib.collection.forEachIndexed
import sidev.lib.console.prin
import sidev.lib.console.prine
import sidev.lib.console.prinr
import sidev.lib.console.prinw
import sidev.lib.exception.IllegalArgExc
import sidev.lib.jvm.tool.util.FileUtil
import sidev.lib.number.pow
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

object Util {
/*
=========== Ketentuan ========= :
1. Format data:
  A. .crs
    i.   Berisi data course.
    ii.  Berisi 2 kolom: kolom 1 berisi id, kolom 2 berisi jml student yg ngambil.
    iii. Id course dimulai dari 1.
  B. .stu
    i.   Berisi data student.
    ii.  Berisi banyak kolom (jml tidak dapat dipastikan).
    iii. Tiap baris merepresentasikan tiap student.
    iv.  Tiap kolom per baris merepresentasikan course yg diambil student tersebut.
  C. .sol
    i.   Berisi data penyelesaian permsalahan timetabling.
    ii.  Berisi 2 kolom: kolom 1 berisi id course, kolom 2 berisi no timeslot.
*/
    /**
     * Return table yang direpresentasikan sbg nested list dg ukuran List[i][u].
     * [i] merupakan jml baris, sedangkan [u] jml kolom tiap baris.
     */
    fun readIntTableFromFile(dir: String, extension: String, delimiter: String = " "): List<List<Int>> {
        val file= File(dir)
        prine("readFile() file= $file file.exists()= ${file.exists()} file.extension= ${file.extension}")
        if(!file.exists() || file.extension != extension.removePrefix("."))
            throw IllegalArgumentException()

        val res= mutableListOf<List<Int>>()
        val inn= Scanner(file)

        while(inn.hasNextLine()){
            val line= inn.nextLine()
//            prine("readFile() line= '$line'")
            if(line.isNotBlank())
                res += line.trimEnd().split(delimiter)
//                    .also { prine("readFile() line.split= '$it'") }
                    .map { it.toInt() }
        }
        return res
    }
    /**
     * Return 2 kolom list, kolom 1 berisi id course, kolom 2 berisi jml mhs yg ngambil course itu.
     */
    fun readCourse(dir: String): List<List<Int>> = readIntTableFromFile(
        if(dir.endsWith(FILE_EXTENSION_COURSE)) dir else "$dir$FILE_EXTENSION_COURSE", FILE_EXTENSION_COURSE
    )

    /**
     * Return nested list yg berisi kolom ganda. Tiap baris merupakan tiap mhs.
     * Tiap kolom adalah id course yg diambilnya.
     */
    fun readStudent(dir: String): List<List<Int>> = readIntTableFromFile(
        if(dir.endsWith(FILE_EXTENSION_STUDENT)) dir else "$dir$FILE_EXTENSION_STUDENT", FILE_EXTENSION_STUDENT
    )

    fun toListOfCourses(courseTable: List<List<Int>>, degreeList: List<Int>?= null): List<Course> =
        if(degreeList == null) courseTable.mapIndexed { i, list -> Course(list.first(), list[1]) }
        else courseTable.mapIndexed { i, list -> Course(list.first(), list[1], degreeList[i]) }

    fun toListOfStudents(studentTable: List<List<Int>>): List<Student> =
        studentTable.mapIndexed { i, list -> Student(i, list) }

    /**
     * Menghitung matrix yg berisi jumlah mhs yg mengambil 2 atau lebih course pada course C[i][j].
     * Fungsi ini sama dg [createCourseAdjacencyMatrix_Raw], namun dg parameter [List<Student>].
     * Hasil return adalah matrix yang hanya berisi int.
     */
    fun createCourseAdjacencyMatrix(courseCount: Int, studList: List<Student>): Array<IntArray> {
        val adjacencyMatrix= Array(courseCount){ IntArray(courseCount) }
        studList.forEachIndexed { i, student ->
            val row= student.courses
            if(row.size > 1){
                for((u, fromCourseId_) in row.withIndex()){
                    val fromCourseId= fromCourseId_ - COURSE_INDEX_OFFSET //Karena courseId dimulai dari 1.
                    for(o in u+1 until row.size){
                        val toCourseId= row[o] - COURSE_INDEX_OFFSET
                        adjacencyMatrix[fromCourseId][toCourseId]++
                        adjacencyMatrix[toCourseId][fromCourseId]++
                    }
                }
            }
        }
        return adjacencyMatrix
    }

    /**
     * Menghitung matrix yg berisi jumlah mhs yg mengambil 2 atau lebih course pada course C[i][j].
     * Fungsi ini sama dg [createCourseAdjacencyMatrix_Raw], namun dg parameter [List<List<Int>].
     * Hasil return adalah matrix yang hanya berisi int.
     */
    fun createCourseAdjacencyMatrix_Raw(courseCount: Int, studCourseTable: List<List<Int>>): Array<IntArray> {
        val adjacencyMatrix= Array(courseCount){ IntArray(courseCount) }
        studCourseTable.forEachIndexed { i, row ->
            if(row.size > 1){
                for((u, fromCourseId_) in row.withIndex()){
                    val fromCourseId= fromCourseId_ - COURSE_INDEX_OFFSET //Karena courseId dimulai dari 1.
                    for(o in u+1 until row.size){
                        val toCourseId= row[o] - COURSE_INDEX_OFFSET
                        adjacencyMatrix[fromCourseId][toCourseId]++
                        adjacencyMatrix[toCourseId][fromCourseId]++
                    }
                }
            }
        }
        return adjacencyMatrix
    }

    /**
     * Menghitung matrix yg berisi course yang tidak boleh dijadwalkan bersamaan karena
     * ada mhs yg mengambil 2 atau lebih course pada course C[i][j].
     *
     * Hasil return adalah matrix yang hanya berisi 0 / 1.
     */
    fun createCourseConflictMatrix(courseCount: Int, studCourseTable: List<List<Int>>): Array<IntArray> =
        createCourseAdjacencyMatrix_Raw(courseCount, studCourseTable).copyOf().let {
            for(i in it.indices){
                for(u in i+1 until it.size){
                    if(it[i][u] > 0)
                        it[i][u]= 1
                }
            }
            it
        }

    /**
     * Menghitung degree dari tiap course, yaitu banyaknya course lain yg tidak boleh dijadwalkan
     * bersamaan dg course tersebut dikarenakan ada mhs yg sama yg ngambil.
     */
    fun getDegreeList(courseAdjacencyMatrix: Array<IntArray>): List<Int> {
        val res= ArrayList<Int>(courseAdjacencyMatrix.size)

        courseAdjacencyMatrix.forEachIndexed { i, row ->
            res += 0
            row.forEachIndexed { u, node ->
                if(node > 0)
                    res[i]++
            }
        }
        return res
    }

    //w0=16, w1=8, w2=4, w3=2 and w4=1 -> Berdasarkan di FP.
    //Penalti msh berdasarkan rumus di UAS.
    /**
     * Menghitung penalty yang dihasilkan oleh [schedule].
     * Penalty yang bagus adalah penalty yang lebih rendah.
     */
    fun getPenalty(schedule: Schedule, courseAdjacencyMatrix: Array<IntArray>, studentCount: Int): Double {
        var sum= 0.0
        val weightRange= 1.0 .. 4.0 //1.0 .. 5.0
        for((i, row) in courseAdjacencyMatrix.withIndex()){
            val t1= schedule.getCourseTimeslot(i + COURSE_INDEX_OFFSET)!!
            for(u in i+1 until row.size){
                val t2= schedule.getCourseTimeslot(u + COURSE_INDEX_OFFSET)!!
                val conflict= courseAdjacencyMatrix[i][u].toDouble()
                val timeslotDistance= (t1.no - t2.no).absoluteValue.toDouble()

                val weight= if(timeslotDistance in weightRange){
//                    (2 pow (5 - timeslotDistance)).toDouble()
                    (2 pow (4 - timeslotDistance)).toDouble()
                } else 0.0

                sum += conflict * weight
            }
        }
        return (sum / studentCount).also {
            schedule.penalty= it
        }
    }

    fun getDensity(courseAdjacencyMatrix: Array<IntArray>): Double {
        var conflicts= 0
        courseAdjacencyMatrix.forEachIndexed { i, row ->
            row.forEachIndexed { u, arc ->
                if(arc > 0)
                    conflicts++
            }
        }
        return conflicts / (courseAdjacencyMatrix.size pow 2).toDouble()
    }

    fun getLeastPenaltySchedule(vararg scs: Schedule, maxTimeslot: Int = 0): Schedule? {
        if(scs.isEmpty()) throw IllegalArgExc(
            paramExcepted = arrayOf("scs"), detailMsg = "Param `scs` gak boleh kosong."
        )
        val filtered= if(maxTimeslot < 1) scs.asList()
            else scs.filter { it.assignments.size <= maxTimeslot }
        return if(filtered.isEmpty()) {
            prinw("Tidak ada schedule '${scs.first().tag}' yang tidak melebihi maxTimeslot='$maxTimeslot', return `null`")
            null
        }
        else filtered.reduce { acc, schedule -> if(acc.penalty <= schedule.penalty) acc else schedule }.also {
            prinr("Schedule dg penalty paling sedikit yg tidak melebihi maxTimeslot='$maxTimeslot' adalah : \n${it.miniString()}")
        }
    }

    fun printSol(sc: Schedule, solFile: File): Boolean{
        solFile.delete()
        for((course, timeslot) in sc){
            if(
                !FileUtil.saveln(
                    FileUtil.getAvailableFile(solFile),
                    "${course.id} ${timeslot.no}",
                    true
                )
            )
                return false
        }
        return true
    }
    fun printRes(sc: Schedule, resFile: File): Boolean{
        resFile.delete()
        return FileUtil.saveln(
            FileUtil.getAvailableFile(resFile),
            sc.assignments.size.toString()
        )
    }

    fun printFinalSol(fileName: String, vararg scs: Schedule, maxTimeslot: Int = 0): Boolean{
        val sc= getLeastPenaltySchedule(*scs, maxTimeslot = maxTimeslot) ?: return false

        val nameIndex= Config.getFileNameIndex(fileName)
        val fileDir= Config.getFileDir(nameIndex)
        val solDir= "$fileDir$FILE_EXTENSION_SOLUTION"
        val resDir= "$fileDir$FILE_EXTENSION_RES"
        val solFile= File(solDir)
        val resFile= File(resDir)

        return printSol(sc, solFile) && printRes(sc, resFile)
    }

    //TODO 10 Des 2020: Jadikan return hasil semua algo.
    fun runScheduling(fileName: String, maxTimeslot: Int = 0, printEachScheduleRes: Boolean = true): Pair<ScheduleTag, Schedule?> =
        runScheduling(Config.getFileNameIndex(fileName), maxTimeslot, printEachScheduleRes)
    fun runScheduling(nameIndex: Int, maxTimeslot: Int = 0, printEachScheduleRes: Boolean = true): Pair<ScheduleTag, Schedule?> {
//        val fileName= "hec-s-92" //"pur-s-93"
//        val maxTimeslot= 18 //42
//        val nameIndex= Config.getFileNameIndex(fileName) //Scanner(System.`in`).next().toInt()
        val fileName= Config.fileNames[nameIndex] //Scanner(System.`in`).next().toInt()

        val fileDir= Config.getFileDir(nameIndex)
        val courseDir= "$fileDir$FILE_EXTENSION_COURSE" //hec-s-92.crs" //car-s-91.crs" //pur-s-93.crs" //\\car-f-92.crs"
        val studentDir= "$fileDir$FILE_EXTENSION_STUDENT" //hec-s-92.stu" //car-s-91.stu" //pur-s-93.stu" //\\car-f-92.stu"
//        val solDir= "$fileDir$FILE_EXTENSION_SOLUTION" //car-f-92.sol"
//        val resDir= "$fileDir$FILE_EXTENSION_RES" //car-f-92.sol"
//        val solFile= File(solDir)
//        val resFile= File(resDir)

        prin("\n\n ================================ ")
        prin(" ============== fileName= $fileName index= $nameIndex ====================")
        prin(" ================================ \n\n")

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
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc1)
        }
        prin("Timeslots= ${sc1.timeslotCount}")
        prin("Penalty: $penalty1")

        prin("\n\n ============ Largest Degree First =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc2)
        }
        prin("Timeslots= ${sc2.timeslotCount}")
        prin("Penalty: $penalty2")

        prin("\n\n ============ Largest Student Count First =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc3)
        }
        prin("Timeslots= ${sc3.timeslotCount}")
        prin("Penalty: $penalty3")

        prin("\n\n ============ Largest Weighted Degree First =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc4)
        }
        prin("Timeslots= ${sc4.timeslotCount}")
        prin("Penalty: $penalty4")

//        Util.printFinalSol(fileName, sc1, sc2, sc3, sc4)
        return getLeastPenaltySchedule(sc1, sc2, sc3, sc4, maxTimeslot = maxTimeslot).let {
            (it ?: sc1).tag to it
        }
    }

    fun runAllScheduling(): List<Pair<ScheduleTag, Schedule?>>{
        val res= mutableListOf<Pair<ScheduleTag, Schedule?>>()
        for(i in Config.fileNames.indices){
            res += Util.runScheduling(i, Config.maxTimeslot[i], false)
        }
        return res
    }
}