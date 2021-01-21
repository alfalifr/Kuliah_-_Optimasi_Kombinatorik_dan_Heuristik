@file:OptIn(ExperimentalTime::class)
package fp

import fp.Config.COURSE_INDEX_OFFSET
import fp.Config.DATASET_DIR
import fp.Config.FILE_EXTENSION_COURSE
import fp.Config.FILE_EXTENSION_COURSE_DETAILED
import fp.Config.FILE_EXTENSION_EXM
import fp.Config.FILE_EXTENSION_MAT
import fp.Config.FILE_EXTENSION_RES
import fp.Config.FILE_EXTENSION_SLN
import fp.Config.FILE_EXTENSION_SOLUTION
import fp.Config.FILE_EXTENSION_STUDENT
import fp.algo.construct.Construct
import fp.algo.optimize.Optimize
import fp.model.*
import sidev.lib.`val`.SuppressLiteral
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
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Objek utilitas yang berisi berbagai fungsi helper.
 */
object Util {
//    var courseAdjacencyMatrix: Array<IntArray> = Array(0) { IntArray(0) }
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
    fun readCourseRaw(dir: String): List<List<Int>> = readIntTableFromFile(
        if(dir.endsWith(FILE_EXTENSION_COURSE)) dir else "$dir$FILE_EXTENSION_COURSE", FILE_EXTENSION_COURSE
    )

    /**
     * Membaca data [Course] langsung dari file. Data yang dimaksud adalah data [Course] yang lengkap.
     * Bukan hanya id dan jumlah student-nya.
     */
    fun readCourse(dir: String): List<Course> = readIntTableFromFile(
        if(dir.endsWith(FILE_EXTENSION_COURSE_DETAILED)) dir else "$dir$FILE_EXTENSION_COURSE_DETAILED", FILE_EXTENSION_COURSE_DETAILED
    ).run {
        map {
            Course(it[0], it[1], it[2], it[3])
        }
    }

    /**
     * Return nested list yg berisi kolom ganda. Tiap baris merupakan tiap mhs.
     * Tiap kolom adalah id course yg diambilnya.
     */
    fun readStudent(dir: String): List<List<Int>> = readIntTableFromFile(
        if(dir.endsWith(FILE_EXTENSION_STUDENT)) dir else "$dir$FILE_EXTENSION_STUDENT", FILE_EXTENSION_STUDENT
    )

    /**
     * Return nested list yg berisi kolom ganda. Tiap baris merupakan tiap mhs.
     * Tiap kolom adalah id course yg diambilnya.
     */
    fun readScheduleRaw(dir: String): List<List<Int>> = readIntTableFromFile(
        if(dir.endsWith(FILE_EXTENSION_SOLUTION)) dir else "$dir$FILE_EXTENSION_SOLUTION", FILE_EXTENSION_SOLUTION
    )

    /**
     * Membaca `.sol` dan `.crsx` untuk mendapatkan data [Schedule].
     */
    fun readSchedule(fileNameIndex: Int): Schedule {
        val schDir= Config.getSolutionFileDir(fileNameIndex)
        val schMat= readScheduleRaw(schDir)
        val crsDir= Config.getDetailedCourseFileDir(fileNameIndex)
        val crsList= readCourse(crsDir)

        val sch= Schedule()
        for(ls in schMat) {
            val cId = ls[0] - COURSE_INDEX_OFFSET
            val tId = ls[1]
            val timeslot = (sch.getTimeslot(tId) ?: Timeslot(tId).apply {
                sch.assignments[this] = mutableListOf()
            })
            sch.assignments[timeslot]!! += crsList[cId] //Anggapannya `crsList` dan course raw urut id nya.
        }
        val stucFile= File(Config.getStudentCountFileDir(fileNameIndex))
        if(stucFile.exists()){
            val stuc= stucFile.readLines()[0].toInt()
            sch.initStudentCount= stuc
        }
        val schInfoFile= File(Config.getScheduleInfoFileDir(fileNameIndex))
        //prine("readSchedule() schInfoFile.exists()= ${schInfoFile.exists()}")
        if(schInfoFile.exists()){
            val schInfo= schInfoFile.readLines()[0].split(" ")
            val initStudCount= schInfo[0].toInt()
            val penalty= schInfo[1].toDouble()
            val constructCode= schInfo[2]
            val optCode= schInfo[3]
            val fileName= Config.fileNames[fileNameIndex]

            sch.initStudentCount = initStudCount
            sch.penalty = penalty
            sch.tag.construct = Construct[constructCode]
            sch.tag.optimization = Optimize[optCode]
            sch.tag.fileName = fileName
            //prine("readSchedule() schInfo= $schInfo penalty= $penalty")
        }
        return sch
    }

    /**
     * Return nested list yg berisi kolom ganda. Tiap baris merupakan tiap mhs.
     * Tiap kolom adalah id course yg diambilnya.
     */
    fun readAdjMatrix(dir: String): Array<IntArray> = readIntTableFromFile(
        if(dir.endsWith(FILE_EXTENSION_MAT)) dir else "$dir$FILE_EXTENSION_MAT", FILE_EXTENSION_MAT
    ).run {
        val size= size
        Array(size){
            val ls= this[it]
            IntArray(size){ ls[it] }
        }
    }

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
//            prine("createCourseAdjacencyMatrix_Raw row= $row")
            if(row.size > 1){
//                prine("createCourseAdjacencyMatrix_Raw row= $row ==== lebih dari 1")
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
    fun createCourseAdjacencyMatrix_Raw(courses: List<Course>, studCourseTable: List<List<Int>>): Array<IntArray> {
        val courseCount= courses.size
        val adjacencyMatrix= Array(courseCount){ IntArray(courseCount) }

        courses.forEach { it.studentCount= 0 }
        studCourseTable.forEachIndexed { i, row ->
//                prine("createCourseAdjacencyMatrix_Raw row= $row ==== lebih dari 1")
            for((u, fromCourseId_) in row.withIndex()){
                val fromCourseId= fromCourseId_ - COURSE_INDEX_OFFSET //Karena courseId dimulai dari 1.
                courses[fromCourseId].studentCount++
                for(o in u+1 until row.size){ //Otomatis gak jalan kalo cuma 1 size-nya.
                    val toCourseId= row[o] - COURSE_INDEX_OFFSET
                    adjacencyMatrix[fromCourseId][toCourseId]++
                    adjacencyMatrix[toCourseId][fromCourseId]++
                }
            }
//            prine("createCourseAdjacencyMatrix_Raw row= $row")
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

    /**
     * Menghitung jml mhs yang setidaknya mengambil 2 course sehingga tidak dapat dijadwalkan pada timeslot yg sama.
     * Fungsi ini tidak hanya menghitung jml course lain yang bentrok, namun juga jml mhs nya.
     */
    fun setConflictingEnrollmentCount(courses: List<Course>, courseAdjacencyMatrix: Array<IntArray>) {
        if(courses.size != courseAdjacencyMatrix.size)
            throw IllegalArgumentException()
        courses.forEachIndexed { i, course ->
            course.conflictingStudentCount= courseAdjacencyMatrix[i].sum()
        }
    }

    fun getPairDistanceMatrix(
        schedule: Schedule,
        courseAdjacencyMatrix: Array<IntArray>,
    ): Array<Array<Pair<Int, Int>>> {
        val res= Array(courseAdjacencyMatrix.size) {
            arrayOfNulls<Pair<Int, Int>>(courseAdjacencyMatrix.size)
        }
        for((i, row) in courseAdjacencyMatrix.withIndex()){
            val t1= schedule.getCourseTimeslot(i + COURSE_INDEX_OFFSET)!!.no
            for(u in i+1 until row.size){
                val t2= schedule.getCourseTimeslot(u + COURSE_INDEX_OFFSET)!!.no
                res[i][u]= t1 to t2
                res[u][i]= t2 to t1
            }
        }
        @Suppress(SuppressLiteral.UNCHECKED_CAST)
        return res as Array<Array<Pair<Int, Int>>>
    }

    fun getFullDistanceMatrix(
        schedule: Schedule,
        courseAdjacencyMatrix: Array<IntArray>,
    ): DistanceMatrix {
        val res= Array(courseAdjacencyMatrix.size) {
            arrayOfNulls<Pair<Int, Int>>(courseAdjacencyMatrix.size)
        }
        for((i, row) in courseAdjacencyMatrix.withIndex()){
            val t1= schedule.getCourseTimeslot(i + COURSE_INDEX_OFFSET)!!.no
            for(u in i+1 until row.size){
                val t2= schedule.getCourseTimeslot(u + COURSE_INDEX_OFFSET)!!.no
                res[i][u]= t1 to t2
                res[u][i]= t2 to t1
            }
        }
        @Suppress(SuppressLiteral.UNCHECKED_CAST)
        return DistanceMatrix(
            res as Array<Array<Pair<Int, Int>>>,
            Array(courseAdjacencyMatrix.size){ courseAdjacencyMatrix[it].copyOf() }
        )
    }

    fun getDistanceMatrix(
        schedule: Schedule,
        courseAdjacencyMatrix: Array<IntArray>,
    ): Array<IntArray> {
        val res= Array(courseAdjacencyMatrix.size) { IntArray(courseAdjacencyMatrix.size) }
        for((i, row) in courseAdjacencyMatrix.withIndex()){
            val t1= schedule.getCourseTimeslot(i + COURSE_INDEX_OFFSET)!!.no
            for(u in i+1 until row.size){
                val t2= schedule.getCourseTimeslot(u + COURSE_INDEX_OFFSET)!!.no
                res[i][u]= (t1 - t2).absoluteValue
            }
        }
        return res
    }

    fun getDistanceAndConflictMatrix(
        schedule: Schedule,
        courseAdjacencyMatrix: Array<IntArray>,
    ): Array<Array<Pair<Int, Int>>> {
        val res= Array(courseAdjacencyMatrix.size) {
            arrayOfNulls<Pair<Int, Int>>(courseAdjacencyMatrix.size)
        }
        for((i, row) in courseAdjacencyMatrix.withIndex()){
            val t1= schedule.getCourseTimeslot(i + COURSE_INDEX_OFFSET)!!.no
            for(u in i+1 until row.size){
                val conflict= courseAdjacencyMatrix[i][u]
                if(conflict < 1) continue
                val t2= schedule.getCourseTimeslot(u + COURSE_INDEX_OFFSET)!!.no
                val distance= (t1 - t2).absoluteValue
                res[i][u]= conflict to distance
            }
        }
        @Suppress(SuppressLiteral.UNCHECKED_CAST)
        return res as Array<Array<Pair<Int, Int>>>
    }

    fun getPenaltyComponentAt(
        courseId: Int,
        courseAdjacencyMatrix: Array<IntArray>,
        schedule: Schedule?= null,
        timeslotGetter: ((courseId: Int) -> Int)?= null,
    ): Double {
        var sum= 0.0
        val t1= if(schedule != null) schedule.getCourseTimeslot(courseId)!!.no
            else timeslotGetter!!(courseId)
        val weightRange= 0.0 .. 4.0 //1.0 .. 5.0
        val courseIndex= courseId - COURSE_INDEX_OFFSET
        val t2Getter= if(schedule != null) { it: Int -> schedule.getCourseTimeslot(it)!!.no }
            else timeslotGetter!!
        val courseNeighbors= courseAdjacencyMatrix[courseIndex]
        for(u in courseNeighbors.indices){
            val t2= t2Getter(u + COURSE_INDEX_OFFSET) //schedule.getCourseTimeslot(u + COURSE_INDEX_OFFSET)!!
            val conflict= courseNeighbors[u].toDouble()
            if(conflict == 0.0) continue
            val timeslotDistance= (t1 - t2).absoluteValue.toDouble()
            val weight= if(timeslotDistance in weightRange){
                (2 pow (4 - timeslotDistance)).toDouble()
            } else 0.0
            sum += conflict * weight
        }
        return sum
    }

    /**
     * Menghitung komponen penalty untuk tiap course terhadap course lainnya secara tunggal.
     * Tiap cell dalam [Array<DoubleArray>] merupakan hasil dari conflict * weight.
     */
    fun getPenaltyComponent(
        schedule: Schedule,
        courseAdjacencyMatrix: Array<IntArray>,
        //studentCount: Int
    ): Array<DoubleArray> {
        //var sum= 0.0
        val resArray= Array(courseAdjacencyMatrix.size){ DoubleArray(courseAdjacencyMatrix.size) }
        val weightRange= 0.0 .. 4.0 //1.0 .. 5.0
        for((i, row) in courseAdjacencyMatrix.withIndex()){
            val t1= schedule.getCourseTimeslot(i + COURSE_INDEX_OFFSET)!!
            for(u in i+1 until row.size){
                val t2= schedule.getCourseTimeslot(u + COURSE_INDEX_OFFSET)!!
                val conflict= courseAdjacencyMatrix[i][u].toDouble()
                if(conflict == 0.0) continue
                val timeslotDistance= (t1.no - t2.no).absoluteValue.toDouble()

                val weight= if(timeslotDistance in weightRange){
//                    (2 pow (5 - timeslotDistance)).toDouble()
                    (2 pow (4 - timeslotDistance)).toDouble()
                } else 0.0

                resArray[i][u]= conflict * weight
            }
        }
        return resArray
    }
    //w0=16, w1=8, w2=4, w3=2 and w4=1 -> Berdasarkan di FP.
    //Penalti msh berdasarkan rumus di UAS.
    /**
     * Menghitung penalty yang dihasilkan oleh [schedule].
     * Penalty yang bagus adalah penalty yang lebih rendah.
     */
    fun getPenalty(schedule: Schedule, courseAdjacencyMatrix: Array<IntArray>, studentCount: Int): Double {
        var sum= 0.0
        val weightRange= 0 .. 4 //1.0 .. 5.0
        val weightRangeLast= weightRange.last
        for((i, row) in courseAdjacencyMatrix.withIndex()){
            val t1= schedule.getCourseTimeslot(i + COURSE_INDEX_OFFSET)!!
            for(u in i+1 until row.size){
                //val c2Id= u + COURSE_INDEX_OFFSET
                val t2= schedule.getCourseTimeslot(u + COURSE_INDEX_OFFSET)!!
                val conflict= courseAdjacencyMatrix[i][u] //.toDouble()
                if(conflict < 1) continue
                val timeslotDistance= (t1.no - t2.no).absoluteValue

                val weight= if(timeslotDistance in weightRange){
//                    (2 pow (5 - timeslotDistance)).toDouble()
                    (2 pow (weightRangeLast - timeslotDistance)).toDouble()
                } else 0.0

                //sum += weight * schedule.getCourse(c2Id)!!.studentCount / studentCount //conflict * weight / studentCount
                sum += conflict * weight
            }
        }
        return (sum / studentCount).also { //(sum / 2)
            schedule.penalty= it
            schedule.initStudentCount= studentCount
        }
    }
    fun getPenalty(schedule: FlatSchedule, courseAdjacencyMatrix: Array<IntArray>, studentCount: Int): Double {
        var sum= 0.0
        val weightRange= 0.0 .. 4.0 //1.0 .. 5.0
        for((i, row) in courseAdjacencyMatrix.withIndex()){
            val t1= schedule.getCourseTimeslot(i + COURSE_INDEX_OFFSET)
            for(u in i+1 until row.size){
                val t2= schedule.getCourseTimeslot(u + COURSE_INDEX_OFFSET)
                val conflict= courseAdjacencyMatrix[i][u].toDouble()
                if(conflict == 0.0) continue
                val timeslotDistance= (t1 - t2).absoluteValue.toDouble()

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
///*
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
// */
    @OptIn(ExperimentalTime::class)
    fun getLeastPenaltyAndTimeSchedule(vararg scs: TestResult<Schedule>, maxTimeslot: Int = 0): TestResult<Schedule>? {
        if(scs.isEmpty()) throw IllegalArgExc(
            paramExcepted = arrayOf("scs"), detailMsg = "Param `scs` gak boleh kosong."
        )
        val filtered= if(maxTimeslot < 1) scs.asList()
            else scs.filter { it.result.assignments.size <= maxTimeslot }
        return if(filtered.isEmpty()) {
            prinw("Tidak ada schedule '${scs.first().result.tag.fileName}' yang tidak melebihi maxTimeslot='$maxTimeslot', return `null`")
            null
        }
        else filtered.reduce { acc, e ->
            val scAcc= acc.result
            val scE= e.result
            if(scAcc.penalty < scE.penalty || scAcc.penalty == scE.penalty && acc.duration <= e.duration) acc else e
        }.also {
            prinr("Schedule dg penalty paling sedikit yg tidak melebihi maxTimeslot='$maxTimeslot' adalah : \n${it.result.miniString()}")
        }
    }

//    fun createGraph(adjacencyMatrix: Array<IntArray>, courses: List<Course>): Graph<Course> = Graph(adjacencyMatrix, courses)

    //TODO 10 Des 2020: Jadikan return hasil semua algo.
    fun runScheduling(fileName: String, printEachScheduleRes: Boolean = true): List<TestResult<Schedule>> =
        runScheduling(Config.getFileNameIndex(fileName), printEachScheduleRes)

    @OptIn(ExperimentalTime::class)
    fun runScheduling(
        nameIndex: Int, printEachScheduleRes: Boolean = true,
        adjMatContainer: MutableMap<String, Array<IntArray>>?= null,
        studentCountContainer: MutableMap<String, Int>?= null,
    ): List<TestResult<Schedule>> {
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
        val courses= toListOfCourses(readCourseRaw(courseDir))

        prin("students.size= ${students.size}")
        prin("courses.size= ${courses.size}")
/*
        val crsDups= courses.countDuplicationBy { it.id }
        val crsGaps= courses.gapsBy { it.id }
        prin("course dups= $crsDups")
        prin("course any dups= ${crsDups.any { it.value > 0 }}")
        prin("course gaps= $crsGaps")
// */

        val adjacencyMatrix= createCourseAdjacencyMatrix_Raw(courses, students)
        val density= getDensity(adjacencyMatrix)

        studentCountContainer?.put(fileName, students.size)
        adjMatContainer?.put(fileName, adjacencyMatrix)

//        courseAdjacencyMatrix= adjacencyMatrix

        prin("density= $density")

//        setDegreeList(courses, adjacencyMatrix)
///*
        val degreeList= getDegreeList(adjacencyMatrix)
        degreeList.forEachIndexed { i, degree ->
            courses[i].degree= degree
        }
// */
        setConflictingEnrollmentCount(courses, adjacencyMatrix)

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
        val sc11: Schedule
        val sc12: Schedule
        val sc13: Schedule
        val sc14: Schedule
        val sc15: Schedule
        val sc16: Schedule
        val sc17: Schedule //x
        val sc18: Schedule //x
        val sc19: Schedule //x
        val sc20: Schedule //x
        val sc21: Schedule //x
        val sc22: Schedule //x
        val sc23: Schedule //x
        val sc24: Schedule //x
        val sc25: Schedule //x
        val sc26: Schedule //x
        val sc27: Schedule //x
        val sc28: Schedule //x
        val sc29: Schedule //x
        val sc30: Schedule //x
        val sc31: Schedule //x
        val sc32: Schedule //x

        val t1= measureTime { sc1= Construct.assignToTimeslot(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t2= measureTime { sc2= Construct.laD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t3= measureTime { sc3= Construct.laE(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t4= measureTime { sc4= Construct.laWD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t5= measureTime { sc5= Construct.laS_D(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t6= measureTime { sc6= Construct.laS_WD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t7= measureTime { sc7= Construct.laS_E(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t8= measureTime { sc8= Construct.laD_S_D(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t9= measureTime { sc9= Construct.laWD_S_WD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t10= measureTime { sc10= Construct.laS_E(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t11= measureTime { sc11= Construct.laWD_E(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t12= measureTime { sc12= Construct.laE_WD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t13= measureTime { sc13= Construct.laS_WD_E(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t14= measureTime { sc14= Construct.laS_E_WD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t15= measureTime { sc15= Construct.laE_WD_S_E_WD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t16= measureTime { sc16= Construct.laE_WD_S_E_WD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        //x
        val t17= measureTime { sc17= Construct.laCE_S_CE(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t18= measureTime { sc18= Construct.laWCD_S_WCD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t19= measureTime { sc19= Construct.laWCD_D_S_WCD_D(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t20= measureTime { sc20= Construct.laWCD_CE_S_WCD_CE(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t21= measureTime { sc21= Construct.laWCD_S_CE(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t22= measureTime { sc22= Construct.laWCD_CE_S_CE(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t23= measureTime { sc23= Construct.laS_WCD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t24= measureTime { sc24= Construct.laS_CE(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t25= measureTime { sc25= Construct.laS_WCD_CE(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t26= measureTime { sc26= Construct.laWCD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t27= measureTime { sc27= Construct.laCE(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t28= measureTime { sc28= Construct.laWCxD(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t29= measureTime { sc29= Construct.laWCxD_S_CE(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t30= measureTime { sc30= Construct.laWCxD_S_D(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t31= measureTime { sc31= Construct.laWCxD_S_WCD_CE(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
        val t32= measureTime { sc32= Construct.laWCxD_S_WCD_D(courses, adjacencyMatrix).apply { tag.fileName = fileName } }
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
        val penalty11= getPenalty(sc11, adjacencyMatrix, students.size)
        val penalty12= getPenalty(sc12, adjacencyMatrix, students.size)
        val penalty13= getPenalty(sc13, adjacencyMatrix, students.size)
        val penalty14= getPenalty(sc14, adjacencyMatrix, students.size)
        val penalty15= getPenalty(sc15, adjacencyMatrix, students.size)
        val penalty16= getPenalty(sc16, adjacencyMatrix, students.size)
        //x
        val penalty17= getPenalty(sc17, adjacencyMatrix, students.size)
        val penalty18= getPenalty(sc18, adjacencyMatrix, students.size)
        val penalty19= getPenalty(sc19, adjacencyMatrix, students.size)
        val penalty20= getPenalty(sc20, adjacencyMatrix, students.size)
        val penalty21= getPenalty(sc21, adjacencyMatrix, students.size)
        val penalty22= getPenalty(sc22, adjacencyMatrix, students.size)
        val penalty23= getPenalty(sc23, adjacencyMatrix, students.size)
        val penalty24= getPenalty(sc24, adjacencyMatrix, students.size)
        val penalty25= getPenalty(sc25, adjacencyMatrix, students.size)
        val penalty26= getPenalty(sc26, adjacencyMatrix, students.size)
        val penalty27= getPenalty(sc27, adjacencyMatrix, students.size)
        val penalty28= getPenalty(sc28, adjacencyMatrix, students.size)
        val penalty29= getPenalty(sc29, adjacencyMatrix, students.size)
        val penalty30= getPenalty(sc30, adjacencyMatrix, students.size)
        val penalty31= getPenalty(sc31, adjacencyMatrix, students.size)
        val penalty32= getPenalty(sc32, adjacencyMatrix, students.size)
// */
/*
        val conflict1= checkConflicts(sc1, adjacencyMatrix)
        val conflict2= checkConflicts(sc2, adjacencyMatrix)
        val conflict3= checkConflicts(sc3, adjacencyMatrix)
        val conflict4= checkConflicts(sc4, adjacencyMatrix)
        val conflict5= checkConflicts(sc5, adjacencyMatrix)
        val conflict6= checkConflicts(sc6, adjacencyMatrix)
        val conflict7= checkConflicts(sc7, adjacencyMatrix)
        val conflict8= checkConflicts(sc8, adjacencyMatrix)
        val conflict9= checkConflicts(sc9, adjacencyMatrix)
        val conflict10= checkConflicts(sc10, adjacencyMatrix)
 */

        printRes(sc1, penalty1, t1, printEachScheduleRes)
        printRes(sc2, penalty2, t2, printEachScheduleRes)
        printRes(sc3, penalty3, t3, printEachScheduleRes)
        printRes(sc4, penalty4, t4, printEachScheduleRes)
        printRes(sc5, penalty5, t5, printEachScheduleRes)
        printRes(sc6, penalty6, t6, printEachScheduleRes)
        printRes(sc7, penalty7, t7, printEachScheduleRes)
        printRes(sc8, penalty8, t8, printEachScheduleRes)
        printRes(sc9, penalty9, t9, printEachScheduleRes)
        printRes(sc10, penalty10, t10, printEachScheduleRes)
        printRes(sc11, penalty11, t11, printEachScheduleRes)
        printRes(sc12, penalty12, t12, printEachScheduleRes)
        printRes(sc13, penalty13, t13, printEachScheduleRes)
        printRes(sc14, penalty14, t14, printEachScheduleRes)
        printRes(sc15, penalty15, t15, printEachScheduleRes)
        printRes(sc16, penalty16, t16, printEachScheduleRes)
        printRes(sc17, penalty17, t17, printEachScheduleRes)
        printRes(sc18, penalty18, t18, printEachScheduleRes)
        printRes(sc19, penalty19, t19, printEachScheduleRes)
        printRes(sc20, penalty20, t20, printEachScheduleRes)
        printRes(sc21, penalty21, t21, printEachScheduleRes)
        printRes(sc22, penalty22, t22, printEachScheduleRes)
        printRes(sc23, penalty23, t23, printEachScheduleRes)
        printRes(sc24, penalty24, t24, printEachScheduleRes)
        printRes(sc25, penalty25, t25, printEachScheduleRes)
        printRes(sc26, penalty26, t26, printEachScheduleRes)
        printRes(sc27, penalty27, t27, printEachScheduleRes)
        printRes(sc28, penalty28, t28, printEachScheduleRes)
        printRes(sc29, penalty29, t29, printEachScheduleRes)
        printRes(sc30, penalty30, t30, printEachScheduleRes)
        printRes(sc31, penalty31, t31, printEachScheduleRes)
        printRes(sc32, penalty32, t32, printEachScheduleRes)
// */
/*
//        Util.printFinalSol(fileName, sc1, sc2, sc3, sc4)
        return getLeastPenaltySchedule(sc1, sc2, sc3, sc4, maxTimeslot = maxTimeslot).let {
            (it ?: sc1).tag to it
        }
 */
        return listOf(
            sc1 withTime t1, sc2 withTime t2, sc3 withTime t3, sc4 withTime t4, sc5 withTime t5,
            sc6 withTime t6, sc7 withTime t7, sc8 withTime t8, sc9 withTime t9, sc10 withTime t10,
            sc11 withTime t11, sc12 withTime t12, sc13 withTime t13, sc14 withTime t14, sc15 withTime t15,
            sc16 withTime t16, sc17 withTime t17, sc18 withTime t18, sc19 withTime t19, sc20 withTime t20,
            sc21 withTime t21, sc22 withTime t22, sc23 withTime t23, sc24 withTime t24, sc25 withTime t25,
            sc26 withTime t26, sc27 withTime t27, sc28 withTime t28, sc29 withTime t29, sc30 withTime t30,
            sc31 withTime t31, sc32 withTime t32,
        )
    }

    fun runAndGetBestScheduling(fileName: String, maxTimeslot: Int= 0, printEachScheduleRes: Boolean = true): Pair<ScheduleTag, TestResult<Schedule>?> =
        runAndGetBestScheduling(Config.getFileNameIndex(fileName), maxTimeslot, printEachScheduleRes)
    fun runAndGetBestScheduling(nameIndex: Int, maxTimeslot: Int= 0,  printEachScheduleRes: Boolean = true): Pair<ScheduleTag, TestResult<Schedule>?> =
        runScheduling(nameIndex, printEachScheduleRes).let { list ->
            return getLeastPenaltyAndTimeSchedule(*list.toTypedArray(), maxTimeslot = maxTimeslot).let {
//                var testRes: TestResult<Schedule>?= null
//                (it.also { testRes= list.find { it2 -> it2.result == it } } ?: list.first().result).tag to testRes
                (it ?: list.first()).result.tag to it
            }
        }

    fun runAllScheduling(
        adjMatContainer: MutableMap<String, Array<IntArray>>?= null,
        studentCountContainer: MutableMap<String, Int>?= null,
    ): Map<String, List<TestResult<Schedule>>>{
        val res= mutableMapOf<String, List<TestResult<Schedule>>>()
        for((i, fileName) in Config.fileNames.withIndex()){
        //for(i in 0 until 2){ //TODO!!!
            //val fileName= Config.fileNames[i]
            res[fileName]= runScheduling(i, false, adjMatContainer, studentCountContainer) //Config.maxTimeslot[i],
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
            res += getLeastPenaltyAndTimeSchedule(*testResults.toTypedArray(), maxTimeslot = maxTimeslot).let {
//                var testRes: TestResult<Schedule>?= null
//                (it.also { testRes= testResults.find { it2 -> it2.result == it } } ?: testResults.first().result).tag to testRes
                (it ?: testResults.first()).result.tag to it
            }
        }
        return res
    }

    @OptIn(ExperimentalTime::class)
    fun printRes(sc: Schedule, penalty: Double, duration: Duration, printSchedule: Boolean= true){
        prin("\n\n ============ ${sc.tag.construct} =============== \n")
        if(printSchedule){
            prin("Time table:")
            prin(sc)
        }
        prin("Timeslots= ${sc.timeslotCount}")
        prin("Penalty: $penalty")
        prin("Duration: $duration")
    }

    fun checkConflicts(result: Map<String, List<TestResult<Schedule>>>, adjacencyMatrix: Array<IntArray>): Map<String, List<ScheduleConflict>>{
        val map= mutableMapOf<String, List<ScheduleConflict>>()
        for((fileName, results) in result){
            val perFileList= mutableListOf<ScheduleConflict>()
//            val sc= results.first().result
//            val tag= results.first().result.tag
            for(res in results){
                val sc= res.result
                val conflicts= checkConflicts(sc, adjacencyMatrix)
                perFileList += conflicts
            }
            map[fileName]= perFileList
        }
        return map
    }
    fun checkConflicts(sc: Schedule, adjacencyMatrix: Array<IntArray>): ScheduleConflict{
        val list= mutableListOf<Pair<Timeslot, Int>>()
        for((timeslot, courses) in sc.assignments){
            var conflicts= 0
            for((i, c1) in courses.withIndex()){
                for(u in i+1 until courses.size){
                    val c2= courses[u]
                    if(adjacencyMatrix[c1.id - COURSE_INDEX_OFFSET][c2.id - COURSE_INDEX_OFFSET] > 0)
                        conflicts++
                }
            }
            list += timeslot to conflicts
        }
        return ScheduleConflict(sc, list)
    }

/*
    fun checkConflictInTimeslot(schedule: Schedule, timeslotNo: Int, adjacencyMatrix: Array<IntArray>): Boolean {
        val assign= schedule.getAssignmentAssert(timeslotNo)
        val courses= assign.courses
        for((i, c1) in courses.withIndex()){
            for(u in i+1 until courses.size){
                val c2= courses[u]
                if(adjacencyMatrix[c1.id - COURSE_INDEX_OFFSET][c2.id - COURSE_INDEX_OFFSET] > 0)
                    return false
            }
        }
        return true
    }
    fun checkConflictInTimeslot(
        courses: List<Course>,
        course: Course,
        adjacencyMatrix: Array<IntArray>
    ): Boolean {
        for(c1 in courses){
            if(adjacencyMatrix[course.id - COURSE_INDEX_OFFSET][c1.id - COURSE_INDEX_OFFSET] > 0)
                return false
        }
        return true
    }
    fun checkConflictInSameTimeslot(schedule: Schedule, course: Course, adjacencyMatrix: Array<IntArray>): Boolean {
        val assign= schedule.assignments.find { it.value.any { it.id == course.id } }
            ?: throw IllegalArgExc(
                paramExcepted = arrayOf("course"),
                detailMsg = "Course ($course) tidak terdapat pada schedule ini."
            )
        val courses= assign.value
        return checkConflictInTimeslot(courses, course, adjacencyMatrix)
    }
    fun checkConflictInSameTimeslot(
        schedule: Schedule,
        timeslotNo: Int,
        courseOrder: Int,
        adjacencyMatrix: Array<IntArray>
    ): Boolean {
        val assign= schedule.assignments.find { it.key.no == timeslotNo }
            ?: throw IllegalArgExc(
                paramExcepted = arrayOf("timeslotNo"),
                detailMsg = "Timeslot ($timeslotNo) tidak terdapat pada schedule ini."
            )
        val course= assign.value.find { it.id == courseOrder }
            ?: throw IllegalArgExc(
                paramExcepted = arrayOf("courseOrder"),
                detailMsg = "Course ($courseOrder) tidak terdapat pada schedule ini."
            )
        val courses= assign.value
        return checkConflictInTimeslot(courses, course, adjacencyMatrix)
    }
 */

    fun saveSol(sc: Schedule, solFile: File, withNaturalOrder: Boolean= true): Boolean {
        solFile.delete()
        val itr= if(withNaturalOrder) sc.iterator() else {
            val orderedList= sc.toMutableList()
            orderedList.sortBy { it.first.id }
            orderedList.iterator()
        }
        var (course, timeslot) = itr.next()
        if(
            !FileUtil.saveln(
                solFile,
                "${course.id} ${timeslot.no}",
                false
            )
        ) return false

        while(itr.hasNext()) {
//        for((course, timeslot) in sc){
            val next= itr.next()
            course= next.first
            timeslot= next.second
            if(
                !FileUtil.saveln(
//                    FileUtil.getAvailableFile(solFile),
                    solFile,
                    "${course.id} ${timeslot.no}",
                    true
                )
            ) return false
        }
        return true
    }
    fun saveRes(sc: Schedule, resFile: File): Boolean{
        resFile.delete()
        return FileUtil.saveln(
//            FileUtil.getAvailableFile(resFile),
            resFile,
            sc.assignments.size.toString(), false
        )
    }
    fun saveExm(sc: Schedule, exmFile: File, withNaturalOrder: Boolean= true): Boolean{
        exmFile.delete()
        val itr= if(withNaturalOrder) sc.iterator() else {
            val orderedList= sc.toMutableList()
            orderedList.sortBy { it.first.id }
            orderedList.iterator()
        }
        var (course, _) = itr.next()
        if(
            !FileUtil.saveln(
                exmFile,
                "${course.id} ${course.studentCount}",
                false
            )
        ) return false

        while(itr.hasNext()) {
//        for((course, timeslot) in sc){
            val next= itr.next()
            course= next.first
//            timeslot= next.second
            if(
                !FileUtil.saveln(
//                    FileUtil.getAvailableFile(solFile),
                    exmFile,
                    "${course.id} ${course.studentCount}",
                    true
                )
            ) return false
        }
        return true
    }

    fun saveAdjMat(matFile: File, mat: Array<IntArray>): Boolean {
        matFile.delete()
        for(arr in mat) {
            val strLine= arr.joinToString(separator = " ")
            if(
                !FileUtil.saveln(
                    matFile,
                    strLine,
                    true
                )
            ) return false
        }
        return true
    }

    fun saveDetailedCourses(crsxFile: File, crss: List<Course>, newFile: Boolean= true): Boolean {
        if(newFile) crsxFile.delete()
        for(crs in crss) {
            val strLine= "${crs.id} ${crs.studentCount} ${crs.degree} ${crs.conflictingStudentCount}"
            if(
                !FileUtil.saveln(
                    crsxFile,
                    strLine,
                    true
                )
            ) return false.also {
                prinw("Terjadi kesalahan saat menyimpan data saveDetailedCourses()")
            }
        }
        return true
    }

    fun saveAllAdjMatrix(
        adjMatContainer: Map<String, Array<IntArray>>,
        isFileNameOrdered: Boolean = true
    ){
        if(isFileNameOrdered){
            for((i, e) in adjMatContainer.iterator().withIndex()) {
                val file= File(Config.getAdjMatFileDir(i))
                saveAdjMat(file, e.value)
            }
        } else {
            for((key, value) in adjMatContainer) {
                val i= Config.getFileNameIndex(key)
                val file= File(Config.getAdjMatFileDir(i))
                saveAdjMat(file, value)
            }
        }
    }

    fun saveBestSchedulingRes(bestSchedulings: List<Pair<ScheduleTag, TestResult<Schedule>?>>){
        for((tag, testRes) in bestSchedulings) {
            if(testRes != null){
                val fileDir= DATASET_DIR +"\\${tag.fileName!!}_init_sol.csv"
                val file= File(fileDir)
                file.delete()
                FileUtil.saveln(file, "penalty\tduration", false)
                FileUtil.saveln(file, "${testRes.result.penalty}\t${testRes.duration.inMicroseconds}", true)
            }
        }
    }

    fun saveBestSchedulings(
        bestSchedulings: List<Pair<ScheduleTag, TestResult<Schedule>?>>,
        isFileNameOrdered: Boolean = true
    ){
        if(isFileNameOrdered){
            for((i, e) in bestSchedulings.withIndex()) {
                val file= File(Config.getScheduleInfoFileDir(i))
                e.second?.also { (sch, durr) ->
                    saveSchedulingInfo(file, sch)
                }
            }
        } else {
            for((tag, testRes) in bestSchedulings) {
                if(testRes != null){
                    val i= Config.getFileNameIndex(tag.fileName!!)
                    val file= File(Config.getScheduleInfoFileDir(i))
                    saveSchedulingInfo(file, testRes.result)
                }
            }
        }
    }
    fun saveSchedulingInfo(schiFile: File, sch: Schedule): Boolean {
        val penalty= sch.penalty
        val initStudCount= sch.initStudentCount
        val (construct, opt, fileName)= sch.tag
        val strLine= "$initStudCount $penalty ${construct.code} ${opt.code} $fileName"
        //prine("saveSchedulingInfo() penalty= $penalty sch= ${sch.miniString()}")
        return FileUtil.saveln(schiFile, strLine, false)
    }

    fun saveDetailedCourses(
        bestSchedulings: List<Pair<ScheduleTag, out TestResult<Schedule>?>>,
        isFileNameOrdered: Boolean = true,
        startIndex: Int = COURSE_INDEX_OFFSET
    ){
        //prine("saveDetailedCourses() isFileNameOrdered= $isFileNameOrdered bestSchedulings $bestSchedulings")
        if(isFileNameOrdered){
            for((i, e) in bestSchedulings.withIndex()) {
                val file= File(Config.getDetailedCourseFileDir(i))
                file.delete()
                //prine("for i= $i e= $e file= $file")
                e.second?.also { (res, durr) ->
                    //prine("i= $i MASUK!!! res= $res durr= $durr")
                    val crsList= mutableListOf<Course>()
                    res.iterator(true, startIndex).forEach {
                        crsList.add(it.first)
                    }
                    //prine("i= $i crsList= $crsList")
                    saveDetailedCourses(file, crsList, false)
                }
            }
        } else {
            for((tag, res) in bestSchedulings) {
                val i= Config.getFileNameIndex(tag.fileName!!)
                val file= File(Config.getDetailedCourseFileDir(i))
                file.delete()
                res?.also { (res, durr) ->
                    val crsList= mutableListOf<Course>()
                    res.iterator(true, startIndex).forEach {
                        crsList.add(it.first)
                    }
                    saveDetailedCourses(file, crsList, false)
                }
            }
        }
    }

    fun saveAllStudentCounts(
        studentCountContainer: Map<String, Int>,
        isFileNameOrdered: Boolean = true
    ){
        if(isFileNameOrdered){
            for((i, e) in studentCountContainer.iterator().withIndex()) {
                val file= File(Config.getStudentCountFileDir(i))
                saveStudentCount(file, e.value)
            }
        } else {
            for((fileName, res) in studentCountContainer) {
                val i= Config.getFileNameIndex(fileName)
                val file= File(Config.getStudentCountFileDir(i))
                saveStudentCount(file, res)
            }
        }
    }

    fun saveStudentCount(stucFile: File, count: Int): Boolean =
        FileUtil.saveln(stucFile, count.toString(), false)

    fun saveStudentCount(fileNameIndex: Int, count: Int): Boolean =
        FileUtil.saveln(Config.getStudentFileDir(fileNameIndex), count.toString(), false)

    fun saveSln(slnFile: File): Boolean {
        slnFile.delete()
        val itr= Config.fileNames.iterator()
        var fileName = itr.next()
        if(
            !FileUtil.saveln(
                slnFile,
                fileName,
                false
            )
        ) return false

        while(itr.hasNext()) {
//        for((course, timeslot) in sc){
            fileName= itr.next()
//            timeslot= next.second
            if(
                !FileUtil.saveln(
//                    FileUtil.getAvailableFile(solFile),
                    slnFile,
                    fileName,
                    true
                )
            ) return false
        }
        return true
    }

    fun saveFinalSol(fileName: String, vararg scs: Schedule, maxTimeslot: Int = 0, withNaturalOrder: Boolean= true): Boolean{
        val sc= getLeastPenaltySchedule(*scs, maxTimeslot = maxTimeslot) ?: return false

        val nameIndex= Config.getFileNameIndex(fileName)
        val fileDir= Config.getFileDir(nameIndex)
        val solDir= "$fileDir$FILE_EXTENSION_SOLUTION"
        val resDir= "$fileDir$FILE_EXTENSION_RES"
        val solFile= File(solDir)
        val resFile= File(resDir)

        return saveSol(sc, solFile, withNaturalOrder) && saveRes(sc, resFile)
    }
///*
    fun saveFinalSol(results: List<Pair<ScheduleTag, TestResult<Schedule>?>>, withNaturalOrder: Boolean= true): Boolean {
//        val map= if(result?.isNotEmpty() == true) result else runAllScheduling()
        var bool= true
        for((tag, res) in results){
            val fileName= tag.fileName!!
            if(res != null){
                val nameIndex= Config.getFileNameIndex(fileName)
                val fileDir= Config.getFileDir(nameIndex)
                val solDir= "$fileDir$FILE_EXTENSION_SOLUTION"
                val resDir= "$fileDir$FILE_EXTENSION_RES"
                val exmDir= "$fileDir$FILE_EXTENSION_EXM"
                val solFile= File(solDir)
                val resFile= File(resDir)
                val exmFile= File(exmDir)
                val sc= res.result
                bool= bool && saveSol(sc, solFile, withNaturalOrder) && saveRes(sc, resFile) && saveExm(sc, exmFile, false)
            } else {
                prinw("Dataset $fileName tidak memiliki solusi")
            }
        }

        val slnDir= "$DATASET_DIR/all$FILE_EXTENSION_SLN"
        val slnFile= File(slnDir)

        return bool && saveSln(slnFile)
    }
// */

    /**
     * Mengeprint semua hasil menjadi dua file, penalty.csv dan timeslots.csv.
     * [result.value] merupakan [List] dg size yang sama semua.
     */
    fun saveAllResult(result: Map<String, List<TestResult<Schedule>>>?= null): Boolean{
        val map= if(result?.isNotEmpty() == true) result else runAllScheduling()

        val penaltyFile= File("${Config.DATASET_DIR}\\penalty.csv")
        val timeslotFile= File("${Config.DATASET_DIR}\\timeslots.csv")
        val timeFile= File("${Config.DATASET_DIR}\\times.csv")
        val feasibilityFile= File("${Config.DATASET_DIR}\\feasibility.csv")
        val itr= map.iterator()

        var (fileName, list) = itr.next()
        var header= "'file_name';"
        var penaltyRowStr= "'$fileName';"
        var timeslotRowStr= "'$fileName';"
        var timeRowStr= "'$fileName';"
        var feasibilityRowStr= "'$fileName';"
        list.forEach { (schedule, durr) ->
            val timeslotCount= schedule.timeslotCount
            header += "'${schedule.tag.construct.code}';"
            penaltyRowStr += "'${schedule.penalty}';"
            timeslotRowStr += "'$timeslotCount';"
            timeRowStr += "'$durr';"
            feasibilityRowStr += "'${timeslotCount <= Config.maxTimeslot[Config.getFileNameIndex(fileName)]}';"
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

        feasibilityFile.delete() //.also { prine("timeslotFile.delete()= $it") }
        FileUtil.saveln(
            FileUtil.getAvailableFile(feasibilityFile),
            header, false
        )
        FileUtil.saveln(
            FileUtil.getAvailableFile(feasibilityFile),
            feasibilityRowStr, true
        )

        while(itr.hasNext()){
            val next = itr.next()
            fileName= next.key
            list= next.value

            penaltyRowStr= "'$fileName';"
            timeslotRowStr= "'$fileName';"
            timeRowStr= "'$fileName';"
            feasibilityRowStr= "'$fileName';"
            list.forEach { (schedule, durr) ->
//                val schedule= testRes.result
                val timeslotCount= schedule.timeslotCount
                penaltyRowStr += "'${schedule.penalty}';"
                timeslotRowStr += "'$timeslotCount';"
                timeRowStr += "'$durr';"
                feasibilityRowStr += "'${timeslotCount <= Config.maxTimeslot[Config.getFileNameIndex(fileName)]}';"
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
                ) ||
                !FileUtil.saveln(
                    FileUtil.getAvailableFile(feasibilityFile),
                    feasibilityRowStr, true
                )
            ) return false
        }
        return true
    }
}