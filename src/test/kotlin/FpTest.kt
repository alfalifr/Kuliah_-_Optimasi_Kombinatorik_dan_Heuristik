import fp.*
import fp.Config.DATASET_DIR
import fp.Config.FILE_EXTENSION_COURSE
import fp.Config.FILE_EXTENSION_RES
import fp.Config.FILE_EXTENSION_SOLUTION
import fp.Config.FILE_EXTENSION_STUDENT
import org.junit.Test
import sidev.lib.check.isNull
import sidev.lib.collection.copy
import sidev.lib.collection.forEachIndexed
import sidev.lib.console.prin
import sidev.lib.console.prine
import sidev.lib.console.prinw
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

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


        val sc1= Construct.assignToTimeslot(courses, adjacencyMatrix1)
        val sc2= Construct.assignToTimeslot(courses, adjacencyMatrix2)
        val scLDF= Construct.laD(coursesWithDegree, adjacencyMatrix2)
        val scLSCF= Construct.laE(courses, adjacencyMatrix2)
        val scLSDF= Construct.laS_D(coursesWithDegree, adjacencyMatrix2)

        prin(sc1)
        prin(sc2)
        prin(scLDF)
        prin(scLSCF)
        prin(scLSDF)

        val penalty1= Util.getPenalty(sc1, adjacencyMatrix2, students.size)
        val penalty2= Util.getPenalty(sc2, adjacencyMatrix2, students.size)
        val penalty3= Util.getPenalty(scLDF, adjacencyMatrix2, students.size)
        val penalty4= Util.getPenalty(scLSCF, adjacencyMatrix2, students.size)
        val penalty5= Util.getPenalty(scLSDF, adjacencyMatrix2, students.size)

        prin(penalty1)
        prin(penalty2)
        prin(penalty3)
        prin(penalty4)
        prin(penalty5)
    }

    @Test
    fun realAssignmentTest(){
        val folderDir= DATASET_DIR //"D:\\DataCloud\\OneDrive\\OneDrive - Institut Teknologi Sepuluh Nopember\\Kuliah\\SMT 7\\OKH-A\\M10\\OKH - Dataset - Toronto"
        val courseDir= "$folderDir\\car-f-92.crs" //hec-s-92.crs" //car-s-91.crs" //car-f-92.crs" car-s-91
        val studentDir= "$folderDir\\car-f-92.crs" //hec-s-92.stu" //car-s-91.stu" //car-f-92.stu"

        val students= Util.readStudent(studentDir)
        val courses= Util.toListOfCourses(Util.readCourse(courseDir))

        prin(students.size)
        prin(courses.size)

        val adjacencyMatrix= Util.createCourseAdjacencyMatrix_Raw(courses.size, students)
//        val adjacencyMatrix= Util.createCourseAdjacencyMatrix_Raw(courses.size, students)

        val sc1= Construct.assignToTimeslot(courses, adjacencyMatrix)
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
        val nameIndex= Config.getFileNameIndex(fileName) //Scanner(System.`in`).next().toInt()
        val maxTimeslot= Config.maxTimeslot[nameIndex] //18 //42

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
        prin("courses= $courses")

        val adjacencyMatrix= Util.createCourseAdjacencyMatrix_Raw(courses.size, students)
        val density= Util.getDensity(adjacencyMatrix)

        prin("density= $density")

        val degreeList= Util.getDegreeList(adjacencyMatrix)
        degreeList.forEachIndexed { i, degree ->
            courses[i].degree= degree
        }

//        val adjacencyMatrix= Util.createCourseAdjacencyMatrix_Raw(courses.size, students)

        val sc1= Construct.assignToTimeslot(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val penalty1= Util.getPenalty(sc1, adjacencyMatrix, students.size)
        prin("\n\n ============ First Order =============== \n")
        prin("Time table:")
        prin(sc1)
        prin("Penalty: $penalty1")

        val sc2= Construct.laD(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val penalty2= Util.getPenalty(sc2, adjacencyMatrix, students.size)
        prin("\n\n ============ Largest Degree First =============== \n")
        prin("Time table:")
        prin(sc2)
        prin("Penalty: $penalty2")

        val sc3= Construct.laE(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val penalty3= Util.getPenalty(sc3, adjacencyMatrix, students.size)
        prin("\n\n ============ Largest Enrollment First =============== \n")
        prin("Time table:")
        prin(sc3)
        prin("Penalty: $penalty3")

        val sc4= Construct.laWD(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val penalty4= Util.getPenalty(sc4, adjacencyMatrix, students.size)
        prin("\n\n ============ Largest Weighted Degree First =============== \n")
        prin("Time table:")
        prin(sc4)
        prin("Penalty: $penalty4")


        prin("courses akhir= $courses")

        val sc5= Construct.laS_D(courses, adjacencyMatrix).apply { tag.fileName = fileName }
        val penalty5= Util.getPenalty(sc5, adjacencyMatrix, students.size)
        prin("\n\n ============ Largest Saturated Degree First =============== \n")
        prin("Time table:")
        prin(sc5)
        prin("Penalty: $penalty5")

//        Util.printFinalSol(fileName, sc1, sc2, sc3, sc4)
        Util.getLeastPenaltySchedule(sc1, sc2, sc3, sc4, sc5, maxTimeslot = maxTimeslot)
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
    fun readTest(){
        val crsFile= Config.getCourseFileDir(0)
        val stuFile= Config.getStudentFileDir(0)

        prin("\n\n ========= stu file ============= \n\n")
        Util.readStudent(stuFile).forEach {
            prin(it)
        }
        prin("\n\n ========= crs file ============= \n\n")
        Util.readCourse(crsFile).forEach {
            prin(it)
        }
    }

    @Test
    fun matrixTest(){
        val crsFile= Config.getCourseFileDir(0)
        val stuFile= Config.getStudentFileDir(0)

        val students= Util.readStudent(stuFile)

        val courses= Util.readCourse(crsFile)

        val adj= Util.createCourseAdjacencyMatrix_Raw(courses.size, students)

        prin("\n\n ========= stu file ============= \n\n")
        students.forEach {
            prin(it)
        }
/*
        prin("\n\n ========= crs file ============= \n\n")
        courses.forEach {
            prin(it)
        }
 */
/*
        prin("\n\n ========= adj matrix ============= \n\n")
        adj.forEach {
            prin(it.joinToString())
        }
 */
    }

    @Test
    fun testReal(){
        val scs= Util.runScheduling(0)
        val sc= scs[2]

        Util.saveRes(sc.result, File(Config.getResFileDir(0)))
        Util.saveSol(sc.result, File(Config.getSolutionFileDir(0)).also { prine("saveSol file= $it") }, false).also { prin("Hasil saveSol= $it") }
    }

    @Test
    fun matrixTest_manual(){
        val courses= listOf(
            listOf(1, 4),
            listOf(2, 4),
            listOf(3, 1),
            listOf(4, 1),
            listOf(5, 1),
        ) //Config.getCourseFileDir(0)
        val students= listOf(
            listOf(2),
            listOf(2, 1),
            listOf(1, 3),
            listOf(1, 2),
            listOf(1, 2, 5, 4),
        ) //Config.getStudentFileDir(0)

        val studs= Util.toListOfStudents(students)
        val cour= Util.toListOfCourses(courses)

        val adj= Util.createCourseAdjacencyMatrix(courses.size, studs)
        val sc= Construct.assignToTimeslot(cour, adj)
        val penalty= Util.getPenalty(sc, adj, studs.size)

/*
        prin("\n\n ========= stu file ============= \n\n")
        students.forEach {
            prin(it)
        }
 */
/*
        prin("\n\n ========= crs file ============= \n\n")
        courses.forEach {
            prin(it)
        }
 */
///*
        prin("\n\n ========= adj matrix ============= \n\n")
        adj.forEach {
            prin(it.joinToString())
        }

        prin("Schedule: $sc")
        prin("Penalty: $penalty")

        val dir= DATASET_DIR
        val fileName= "$dir\\_cob-1"

        Util.saveSol(sc, File("$fileName.sol"))
        Util.saveRes(sc, File("$fileName.res"))
        Util.saveExm(sc, File("$fileName.exm"))
// */
    }

    @Test
    fun realAssignmentTest_3(){
        for(i in Config.fileNames.indices){
            Util.runAndGetBestScheduling(i, Config.maxTimeslot[i], printEachScheduleRes = false)
        }
    }

    @Test
    fun realAssignmentTest_4(){
        Util.runAllAndGetBestScheduling()
            .also { prin("\n\n\n=============== Hasil Semua Scheduling ==========") }
            .forEach { (tag, sc) -> prin("fileName= ${tag.fileName} sc= ${sc?.result?.miniString()} duration= ${sc?.duration}") }
    }

    @Test
    fun realAssignmentTest_5(){
        val adjMatContainer = mutableMapOf<String, Array<IntArray>>()
        val results= Util.runAllScheduling(adjMatContainer)
        prin("\n")
        val bestSchedulings= Util.getBestSchedulings(results)

        prin("\n\n\n=============== Hasil Semua Scheduling ==========")
        bestSchedulings.forEach { (tag, sc) -> prin("fileName= ${tag.fileName} sc= ${sc?.result?.miniString()} duration= ${sc?.duration}") }
/*
        prin("\n\n\n=============== Hasil Semua Scheduling ==========")
        val conflicts= Util.checkConflicts(results, )
        bestSchedulings.forEach { (tag, sc) -> prin("fileName= ${tag.fileName} sc= ${sc?.result?.miniString()} duration= ${sc?.duration}") }
 */
        Util.saveAllResult(results)
        Util.saveFinalSol(bestSchedulings)
    }

    @ExperimentalTime
    @Test
    fun realAssignmentTest_6(){
        val fileIndex= 12
        val fileName= Config.fileNames[fileIndex]
        val maxTimeslot= Config.maxTimeslot[fileIndex]
        val adjMatContainer = mutableMapOf<String, Array<IntArray>>()
        val studCountContainer = mutableMapOf<String, Int>()
        val result= Util.runScheduling(fileIndex, false, adjMatContainer, studCountContainer)
        val (bestSch, bestDurr)= Util.getLeastPenaltyAndTimeSchedule(*result.toTypedArray(), maxTimeslot = maxTimeslot)!!

        val adjMat= adjMatContainer[fileName]!!
        val studCount= studCountContainer[fileName]!!

        val initPenalty= Util.getPenalty(bestSch, adjMat, studCount)

        prin("\n")
        prin("================ Melakukan Optimasi ===================")
        val opt1: Pair<Schedule, Double>?
        val opt2: Pair<Schedule, Double>?
//        val opt3: Pair<Schedule, Double>?
        val opt4: Pair<Schedule, Double>?
        val opt5: Pair<Schedule, Double>?
        val opt6: Pair<Schedule, Double>?
        val opt7: Pair<Schedule, Double>?
        val opt8: Pair<Schedule, Double>?
        val opt9: Pair<Schedule, Double>?
        val opt10: Pair<Schedule, Double>?

        val optList= mutableListOf<TestResult<Schedule>>()

        prin("================ Optimasi - hc_swap ===================")
        val t1= measureTime { opt1= Optimize.swap_hillClimbing(bestSch, adjMat, studCount, 1_000_000) }
        prin("================ Optimasi - hc_move ===================")
        val t2= measureTime { opt2= Optimize.move_hillClimbing(bestSch, adjMat, studCount, 1_000_000) }
//        prin("================ Optimasi - hc_move2 ===================")
//        val t3= measureTime { opt3= Optimize.move2_hillClimbing(bestSch, adjMat, studCount, 1_000_000) }
        val n= 5
        prin("================ Optimasi - hc_moveN n=$n ===================")
        val t4= measureTime { opt4= Optimize.moveN_hillClimbing(bestSch, adjMat, studCount, n, 1_000_000) }
        prin("================ Optimasi - hc_swapN n=$n ===================")
        val t5= measureTime { opt5= Optimize.swapN_hillClimbing(bestSch, adjMat, studCount, n, 1_000_000) }
        prin("================ Optimasi - sa_moveN n=$n ===================")
        val temp= 43.0
        val decayRate= 0.02
        val t6= measureTime { opt6= Optimize.moveN_simulatedAnnealing(bestSch, adjMat, studCount, n, temp, decayRate, 1_000_000) }
        prin("================ Optimasi - sa_swapN n=$n ===================")
        val t7= measureTime { opt7= Optimize.swapN_simulatedAnnealing(bestSch, adjMat, studCount, n, temp, decayRate, 1_000_000) }
        prin("================ Optimasi - gd_moveN n=$n ===================")
        val t8= measureTime { opt8= Optimize.moveN_greatDeluge(bestSch, adjMat, studCount, n, decayRate = decayRate, iterations = 1_000_000) }
        prin("================ Optimasi - gd_swapN n=$n ===================")
        val t9= measureTime { opt9= Optimize.swapN_greatDeluge(bestSch, adjMat, studCount, n, decayRate = decayRate, iterations = 1_000_000) }
        prin("================ Optimasi - hyper maxN=$n ===================")
        val t10= measureTime {
            val optAlgo= Optimize.HighLevel.SELECTION(n, Optimize.Evaluation.SIMULATED_ANNEALING(temp, decayRate))
            opt10= optAlgo.optimize(bestSch, adjMat, studCount, 1_000_000)
        }

        prin("\n\n\n=============== Scheduling _ ${bestSch.miniString()} _ duration= $bestDurr _ initPenalty= $initPenalty ==========")
        opt1?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t1 conflict=$conflict ==============")
            optList += TestResult(optSch, t1)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t1 ==============")
        }
        opt2?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t2 conflict=$conflict ==============")
            optList += TestResult(optSch, t2)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
/*
        opt3?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t3 conflict=$conflict ==============")
            optList += TestResult(optSch, t3)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
 */
        opt4?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t4 conflict=$conflict ==============")
            optList += TestResult(optSch, t4)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
        opt5?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t5 conflict=$conflict ==============")
            optList += TestResult(optSch, t5)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
        opt6?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t6 conflict=$conflict ==============")
            optList += TestResult(optSch, t6)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
        opt7?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t7 conflict=$conflict ==============")
            optList += TestResult(optSch, t7)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
        opt8?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t8 conflict=$conflict ==============")
            optList += TestResult(optSch, t8)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
        opt9?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t9 conflict=$conflict ==============")
            optList += TestResult(optSch, t9)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
        opt10?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t10 conflict=$conflict ==============")
            optList += TestResult(optSch, t10)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
/*
        val betterSch= when{
            opt1 == null -> opt2
            opt2 == null -> opt1
            else -> if(opt1.first.penalty <= opt2.first.penalty) opt1 else opt2
        }
 */
        if(optList.isNotEmpty()){
            val (bestOptSch, bestOptDurr)= Util.getLeastPenaltyAndTimeSchedule(*optList.toTypedArray(), maxTimeslot = maxTimeslot)!!
            val file= File(Config.getFileDir(fileIndex) +"_opt_hc.sol")
            Util.saveSol(bestOptSch, file)
        }
    }

    @ExperimentalTime
    @Test
    fun realAssignmentTest_car91(){
        val fileIndex= 1
        val fileName= Config.fileNames[fileIndex]
        val maxTimeslot= Config.maxTimeslot[fileIndex]
        val adjMatContainer = mutableMapOf<String, Array<IntArray>>()
        val studCountContainer = mutableMapOf<String, Int>()
        val result= Util.runScheduling(fileIndex, false, adjMatContainer, studCountContainer)
        val (bestSch, bestDurr)= Util.getLeastPenaltyAndTimeSchedule(*result.toTypedArray(), maxTimeslot = maxTimeslot)!!

        val adjMat= adjMatContainer[fileName]!!
        val studCount= studCountContainer[fileName]!!

        val initPenalty= Util.getPenalty(bestSch, adjMat, studCount)

        prin("\n")
        prin("\n\n\n=============== Scheduling _ ${bestSch.miniString()} _ duration= $bestDurr _ initPenalty= $initPenalty ==========")
/*
        prin("================ Melakukan Optimasi ===================")
        val opt1: Pair<Schedule, Double>?
        val opt2: Pair<Schedule, Double>?

        prin("================ Optimasi - hc_swap ===================")
        val t1= measureTime { opt1= Optimize.swap_hillClimbing(bestSch, adjMat, studCount) }
        prin("================ Optimasi - hc_move ===================")
        val t2= measureTime { opt2= Optimize.move_hillClimbing(bestSch, adjMat, studCount) }

        prin("\n\n\n=============== Scheduling _ ${bestSch.miniString()} _ duration= $bestDurr _ initPenalty= $initPenalty ==========")
        opt1?.also { (optSch, optPenalty) ->
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t1 ==============")
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t1 ==============")
        }
        opt2?.also { (optSch, optPenalty) ->
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t2 ==============")
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
 */
    }

    @Test
    fun courseIdTest(){
        val c1= Course(1, 10)
        prin(c1.hashCode().hashCode())
        val map= HashMap<Course, Int>()
        map[c1]= 120
        prin(map[c1])
        prin(map[1])
        prin(map[Course(1, 391721826)])

        prin(Course(1, 9237193) == c1)
        prin(c1.equals(1))
    }
    @Test
    fun moveCourseTest(){
        val t1= Timeslot(1)
        val t2= Timeslot(2)
        val c1= Course(1, 10)
        val c2= Course(2, 11)
        val c3= Course(3, 14)

        val ls1= mutableListOf(c1)
        val ls2= mutableListOf(c3, c2)

        val map= mutableMapOf(t1 to ls1, t2 to ls2,)
        val sch= Schedule(map)

        prin(sch)
        prin("======== after move ===========")
        sch.moveById(3, 1)
        prin(sch)
    }
}