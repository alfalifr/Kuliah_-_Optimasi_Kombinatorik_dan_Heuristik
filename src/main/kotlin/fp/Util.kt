@file:OptIn(ExperimentalTime::class)
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
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

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
            prinw("Tidak ada schedule '${scs.first().tag.fileName}' yang tidak melebihi maxTimeslot='$maxTimeslot', return `null`")
            null
        }
        else filtered.reduce { acc, schedule -> if(acc.penalty <= schedule.penalty) acc else schedule }.also {
            prinr("Schedule dg penalty paling sedikit yg tidak melebihi maxTimeslot='$maxTimeslot' adalah : \n${it.miniString()}")
        }
    }

//    fun createGraph(adjacencyMatrix: Array<IntArray>, courses: List<Course>): Graph<Course> = Graph(adjacencyMatrix, courses)

    //TODO 10 Des 2020: Jadikan return hasil semua algo.
    fun runScheduling(fileName: String, printEachScheduleRes: Boolean = true): List<TestResult<Schedule>> =
        runScheduling(Config.getFileNameIndex(fileName), printEachScheduleRes)

    @OptIn(ExperimentalTime::class)
    fun runScheduling(nameIndex: Int, printEachScheduleRes: Boolean = true): List<TestResult<Schedule>> {
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

        val students= readStudent(studentDir)
        val courses= toListOfCourses(readCourse(courseDir))

        prin("students.size= ${students.size}")
        prin("courses.size= ${courses.size}")

        val adjacencyMatrix= createCourseAdjacencyMatrix_Raw(courses.size, students)
        val density= getDensity(adjacencyMatrix)

        prin("density= $density")

        val degreeList= getDegreeList(adjacencyMatrix)
        degreeList.forEachIndexed { i, degree ->
            courses[i].degree= degree
        }

//        val adjacencyMatrix= Util.createCourseAdjacencyMatrix_Raw(courses.size, students)
/*
        val degreeSorted= courses.sortedByDescending { it.degree }
        val weightedDegreeSorted= courses.sortedByDescending { it.degree * it.studentCount }
        val enrollmentSorted= courses.sortedByDescending { it.studentCount }
 */
        val sc1: Schedule
        val sc2: Schedule
        val sc3: Schedule
        val sc4: Schedule
        val sc5: Schedule
        val sc6: Schedule
        val sc7: Schedule
        val sc8: Schedule
        val sc9: Schedule
        val sc10: Schedule

        val t1= measureTime { sc1= Algo.assignToTimeslot(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t2= measureTime { sc2= Algo.largestDegreeFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t3= measureTime { sc3= Algo.largestEnrollmentFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t4= measureTime { sc4= Algo.largestWeightedDegreeFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t5= measureTime { sc5= Algo.largestSaturationDegreeFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t6= measureTime { sc6= Algo.largestSaturationWeightedDegreeFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t7= measureTime { sc7= Algo.largestSaturationEnrollmentFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t8= measureTime { sc8= Algo.largestSaturationDegreeFirstOrdered(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t9= measureTime { sc9= Algo.largestSaturationWeightedDegreeFirstOrdered(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t10= measureTime { sc10= Algo.largestSaturationEnrollmentFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
/*
        val sc1= Algo.assignToTimeslot(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val sc2= Algo.largestDegreeFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName }
//        val sc2= Algo.assignToTimeslot(degreeSorted, adjacencyMatrix).apply { tag.fileName = fileName; tag.algo= Algo.LARGEST_DEGREE_FIRST }
        val sc3= Algo.largestEnrollmentFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName }
//        val sc3= Algo.assignToTimeslot(enrollmentSorted, adjacencyMatrix).apply { tag.fileName = fileName; tag.algo= Algo.LARGEST_ENROLLMENT_FIRST }
        val sc4= Algo.largestWeightedDegreeFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName }
//        val sc4= Algo.assignToTimeslot(weightedDegreeSorted, adjacencyMatrix).apply { tag.fileName = fileName; tag.algo= Algo.LARGEST_WEIGHTED_DEGREE_FIRST }
        val sc5= Algo.largestSaturationDegreeFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val sc6= Algo.largestSaturationWeightedDegreeFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val sc7= Algo.largestSaturationEnrollmentFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName }
///*
        val sc8= Algo.largestSaturationDegreeFirstOrdered(courses, adjacencyMatrix).apply { tag.fileName = fileName }
//        val sc8= Algo.largestSaturationDegreeFirst(degreeSorted, adjacencyMatrix).apply { tag.fileName = fileName; tag.algo= Algo.LARGEST_SATURATED_DEGREE_FIRST_ORDERED }
        val sc9= Algo.largestSaturationWeightedDegreeFirstOrdered(courses, adjacencyMatrix).apply { tag.fileName = fileName }
//        val sc9= Algo.largestSaturationWeightedDegreeFirst(weightedDegreeSorted, adjacencyMatrix).apply { tag.fileName = fileName; tag.algo= Algo.LARGEST_SATURATED_WEIGHTED_DEGREE_FIRST_ORDERED }
        val sc10= Algo.largestSaturationEnrollmentFirst(courses, adjacencyMatrix).apply { tag.fileName = fileName }
//        val sc10= Algo.largestSaturationEnrollmentFirst(enrollmentSorted, adjacencyMatrix).apply { tag.fileName = fileName; tag.algo= Algo.LARGEST_SATURATED_ENROLLMENT_FIRST_ORDERED }
// */
 */
        val penalty1= getPenalty(sc1, adjacencyMatrix, students.size)
        val penalty2= getPenalty(sc2, adjacencyMatrix, students.size)
        val penalty3= getPenalty(sc3, adjacencyMatrix, students.size)
        val penalty4= getPenalty(sc4, adjacencyMatrix, students.size)
        val penalty5= getPenalty(sc5, adjacencyMatrix, students.size)
        val penalty6= getPenalty(sc6, adjacencyMatrix, students.size)
        val penalty7= getPenalty(sc7, adjacencyMatrix, students.size)
///*
        val penalty8= getPenalty(sc8, adjacencyMatrix, students.size)
        val penalty9= getPenalty(sc9, adjacencyMatrix, students.size)
        val penalty10= getPenalty(sc10, adjacencyMatrix, students.size)
// */

        prin("\n\n ============ ${sc1.tag.algo} =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc1)
        }
        prin("Timeslots= ${sc1.timeslotCount}")
        prin("Penalty: $penalty1")
        prin("Duration: $t1")

        prin("\n\n ============ ${sc2.tag.algo} =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc2)
        }
        prin("Timeslots= ${sc2.timeslotCount}")
        prin("Penalty: $penalty2")
        prin("Duration: $t2")

        prin("\n\n ============ ${sc3.tag.algo} =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc3)
        }
        prin("Timeslots= ${sc3.timeslotCount}")
        prin("Penalty: $penalty3")
        prin("Duration: $t3")

        prin("\n\n ============ ${sc4.tag.algo} =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc4)
        }
        prin("Timeslots= ${sc4.timeslotCount}")
        prin("Penalty: $penalty4")
        prin("Duration: $t4")

        prin("\n\n ============ ${sc5.tag.algo} =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc5)
        }
        prin("Timeslots= ${sc5.timeslotCount}")
        prin("Penalty: $penalty5")
        prin("Duration: $t5")

        prin("\n\n ============ ${sc6.tag.algo} =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc6)
        }
        prin("Timeslots= ${sc6.timeslotCount}")
        prin("Penalty: $penalty6")
        prin("Duration: $t6")

        prin("\n\n ============ ${sc7.tag.algo} =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc7)
        }
        prin("Timeslots= ${sc7.timeslotCount}")
        prin("Penalty: $penalty7")
        prin("Duration: $t7")
///*
        prin("\n\n ============ ${sc8.tag.algo} =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc8)
        }
        prin("Timeslots= ${sc8.timeslotCount}")
        prin("Penalty: $penalty8")
        prin("Duration: $t8")

        prin("\n\n ============ ${sc9.tag.algo} =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc9)
        }
        prin("Timeslots= ${sc9.timeslotCount}")
        prin("Penalty: $penalty9")
        prin("Duration: $t9")

        prin("\n\n ============ ${sc10.tag.algo} =============== \n")
        if(printEachScheduleRes){
            prin("Time table:")
            prin(sc10)
        }
        prin("Timeslots= ${sc10.timeslotCount}")
        prin("Penalty: $penalty10")
        prin("Duration: $t10")
// */
/*
//        Util.printFinalSol(fileName, sc1, sc2, sc3, sc4)
        return getLeastPenaltySchedule(sc1, sc2, sc3, sc4, maxTimeslot = maxTimeslot).let {
            (it ?: sc1).tag to it
        }
 */
        return listOf(
            sc1 withTime t1, sc2 withTime t2, sc3 withTime t3, sc4 withTime t4, sc5 withTime t5,
            sc6 withTime t6, sc7 withTime t7, sc8 withTime t8, sc9 withTime t9, sc10 withTime t10
        )
    }

    fun runAndGetBestScheduling(fileName: String, maxTimeslot: Int= 0, printEachScheduleRes: Boolean = true): Pair<ScheduleTag, TestResult<Schedule>?> =
        runAndGetBestScheduling(Config.getFileNameIndex(fileName), maxTimeslot, printEachScheduleRes)
    fun runAndGetBestScheduling(nameIndex: Int, maxTimeslot: Int= 0, printEachScheduleRes: Boolean = true): Pair<ScheduleTag, TestResult<Schedule>?> =
        runScheduling(nameIndex, printEachScheduleRes).let { list ->
            return getLeastPenaltySchedule(*list.map { it.result }.toTypedArray(), maxTimeslot = maxTimeslot).let {
                var testRes: TestResult<Schedule>?= null
                (it.also { testRes= list.find { it2 -> it2.result == it } } ?: list.first().result).tag to testRes
            }
        }

    fun runAllScheduling(): Map<String, List<TestResult<Schedule>>>{
        val res= mutableMapOf<String, List<TestResult<Schedule>>>()
        for((i, fileName) in Config.fileNames.withIndex()){
            res[fileName]= runScheduling(i, false) //Config.maxTimeslot[i],
        }
        return res
    }

    fun runAllAndGetBestScheduling(): List<Pair<ScheduleTag, TestResult<Schedule>?>>{
        val res= mutableListOf<Pair<ScheduleTag, TestResult<Schedule>?>>()
        for(i in Config.fileNames.indices){
            res += runAndGetBestScheduling(i, Config.maxTimeslot[i], false) //Config.maxTimeslot[i],
        }
        return res
    }

    fun getBestSchedulings(result: Map<String, List<TestResult<Schedule>>>?= null): List<Pair<ScheduleTag, TestResult<Schedule>?>> {
        val map= if(result?.isNotEmpty() == true) result else runAllScheduling()
        val res= mutableListOf<Pair<ScheduleTag, TestResult<Schedule>?>>()
        for((i, entry) in map.iterator().withIndex()){
            val testResults= entry.value
            val maxTimeslot= Config.maxTimeslot[i]
            res += getLeastPenaltySchedule(*testResults.map { it.result }.toTypedArray(), maxTimeslot = maxTimeslot).let {
                var testRes: TestResult<Schedule>?= null
                (it.also { testRes= testResults.find { it2 -> it2.result == it } } ?: testResults.first().result).tag to testRes
            }
        }
        return res
    }

    fun saveSol(sc: Schedule, solFile: File): Boolean{
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
    fun saveRes(sc: Schedule, resFile: File): Boolean{
        resFile.delete()
        return FileUtil.saveln(
            FileUtil.getAvailableFile(resFile),
            sc.assignments.size.toString()
        )
    }

    fun saveFinalSol(fileName: String, vararg scs: Schedule, maxTimeslot: Int = 0): Boolean{
        val sc= getLeastPenaltySchedule(*scs, maxTimeslot = maxTimeslot) ?: return false

        val nameIndex= Config.getFileNameIndex(fileName)
        val fileDir= Config.getFileDir(nameIndex)
        val solDir= "$fileDir$FILE_EXTENSION_SOLUTION"
        val resDir= "$fileDir$FILE_EXTENSION_RES"
        val solFile= File(solDir)
        val resFile= File(resDir)

        return saveSol(sc, solFile) && saveRes(sc, resFile)
    }

    /**
     * Mengeprint semua hasil menjadi dua file, penalty.csv dan timeslots.csv.
     * [result.value] merupakan [List] dg size yang sama semua.
     */
    fun saveAllResult(result: Map<String, List<TestResult<Schedule>>>?= null): Boolean{
        val map= if(result?.isNotEmpty() == true) result else runAllScheduling()

        val penaltyFile= File("${Config.DATASET_DIR}\\penalty.csv")
        val timeslotFile= File("${Config.DATASET_DIR}\\timeslots.csv")
        val timeFile= File("${Config.DATASET_DIR}\\times.csv")
        val itr= map.iterator()

        var (fileName, list) = itr.next()
        var header= "file_name;"
        var penaltyRowStr= "'$fileName';"
        var timeslotRowStr= "'$fileName';"
        var timeRowStr= "'$fileName';"
        list.forEach { (schedule, durr) ->
            header += "${schedule.tag.algo.code};"
            penaltyRowStr += "'${schedule.penalty}';"
            timeslotRowStr += "'${schedule.timeslotCount}';"
            timeRowStr += "'$durr';"
        }

        penaltyFile.delete() //.also { prine("penaltyFile.delete()= $it") }
        FileUtil.saveln(
            FileUtil.getAvailableFile(penaltyFile),
            header, false
        )
        FileUtil.saveln(
            FileUtil.getAvailableFile(penaltyFile),
            penaltyRowStr, true
        )

        timeslotFile.delete() //.also { prine("timeslotFile.delete()= $it") }
        FileUtil.saveln(
            FileUtil.getAvailableFile(timeslotFile),
            header, false
        )
        FileUtil.saveln(
            FileUtil.getAvailableFile(timeslotFile),
            timeslotRowStr, true
        )

        timeFile.delete() //.also { prine("timeslotFile.delete()= $it") }
        FileUtil.saveln(
            FileUtil.getAvailableFile(timeFile),
            header, false
        )
        FileUtil.saveln(
            FileUtil.getAvailableFile(timeFile),
            timeRowStr, true
        )

        while(itr.hasNext()){
            val next = itr.next()
            fileName= next.key
            list= next.value

            penaltyRowStr= "'$fileName';"
            timeslotRowStr= "'$fileName';"
            timeRowStr= "'$fileName';"
            list.forEach { (schedule, durr) ->
//                val schedule= testRes.result
                penaltyRowStr += "'${schedule.penalty}';"
                timeslotRowStr += "'${schedule.timeslotCount}';"
                timeRowStr += "'$durr';"
            }
            if(
                !FileUtil.saveln(
                    FileUtil.getAvailableFile(penaltyFile),
                    penaltyRowStr, true
                ) ||
                !FileUtil.saveln(
                    FileUtil.getAvailableFile(timeslotFile),
                    timeslotRowStr, true
                ) ||
                !FileUtil.saveln(
                    FileUtil.getAvailableFile(timeFile),
                    timeRowStr, true
                )
            ) return false
        }
        return true
    }
}