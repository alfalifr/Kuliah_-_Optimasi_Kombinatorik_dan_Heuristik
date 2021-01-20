import fp.*
import fp.Config.DATASET_DIR
import fp.Config.FILE_EXTENSION_COURSE
import fp.Config.FILE_EXTENSION_RES
import fp.Config.FILE_EXTENSION_SOLUTION
import fp.Config.FILE_EXTENSION_STUDENT
import fp.algo.construct.Construct
import fp.algo.optimize.Evaluation
import fp.algo.optimize.HighLevel
import fp.algo.optimize.Optimize
import fp.model.*
import org.junit.Test
import sidev.lib.check.contentEquals
import sidev.lib.check.isNull
import sidev.lib.collection.copy
import sidev.lib.collection.forEachIndexed
import sidev.lib.console.prin
import sidev.lib.console.prine
import sidev.lib.console.prinw
import sidev.lib.math.random.probabilities
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import sidev.lib.check.contentEquals

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
        val courses= Util.toListOfCourses(Util.readCourseRaw(courseDir))

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
        val courses= Util.toListOfCourses(Util.readCourseRaw(courseDir))

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
        Util.readCourseRaw(crsFile).forEach {
            prin(it)
        }
    }

    @Test
    fun matrixTest(){
        val crsFile= Config.getCourseFileDir(0)
        val stuFile= Config.getStudentFileDir(0)

        val students= Util.readStudent(stuFile)

        val courses= Util.readCourseRaw(crsFile)

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
        val fileIndex= 5
        val fileName= Config.fileNames[fileIndex]
        val maxTimeslot= Config.maxTimeslot[fileIndex]
        val adjMatContainer = mutableMapOf<String, Array<IntArray>>()
        val studCountContainer = mutableMapOf<String, Int>()
        val result= Util.runScheduling(fileIndex, false, adjMatContainer, studCountContainer)
        val (bestSch, bestDurr)= Util.getLeastPenaltyAndTimeSchedule(*result.toTypedArray(), maxTimeslot = maxTimeslot)!!

        val adjMat= adjMatContainer[fileName]!!
        val studCount= studCountContainer[fileName]!!

        val adjMat_f= Util.readAdjMatrix(Config.getAdjMatFileDir(fileIndex))

        //var same= true
        prin(adjMat.contentDeepEquals(adjMat_f))

        val initPenalty= Util.getPenalty(bestSch, adjMat, studCount)

        prin("initSch= $bestSch")
        prin("\n")
        prin("================ Melakukan Optimasi ===================")
        val temp= 43.0
        val decayRate= 0.02
        val n= 3
        val initLevel= initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
        val tabuMoveSize= 25
        val tabuLLHSize= 3
        val iterations= 1_000_000
///*
        val opt1: Pair<Schedule, Double>?
        val opt2: Pair<Schedule, Double>?
//        val opt3: Pair<Schedule, Double>?
        val opt4: Pair<Schedule, Double>?
        val opt5: Pair<Schedule, Double>?
        val opt6: Pair<Schedule, Double>?
        val opt7: Pair<Schedule, Double>?
        val opt8: Pair<Schedule, Double>?
        val opt9: Pair<Schedule, Double>?
// */
        val opt10: Pair<Schedule, Double>?
        val opt11: Pair<Schedule, Double>?
        val opt12: Pair<Schedule, Double>?
        val opt13: Pair<Schedule, Double>?
///*
        val opt14: Pair<Schedule, Double>?
        val opt15: Pair<Schedule, Double>?
        val opt16: Pair<Schedule, Double>?
        val opt17: Pair<Schedule, Double>?
        val opt18: Pair<Schedule, Double>?
// */
        val optList= mutableListOf<TestResult<Schedule>>()
///*
        prin("================ Optimasi - hc_swap ===================")
        val t1= measureTime { opt1= Optimize.lin_swap_hc(bestSch, adjMat, studCount, iterations) }
        prin("================ Optimasi - hc_move ===================")
        val t2= measureTime { opt2= Optimize.lin_move_hc(bestSch, adjMat, studCount, iterations) }
//        prin("================ Optimasi - hc_move2 ===================")
//        val t3= measureTime { opt3= Optimize.move2_hillClimbing(bestSch, adjMat, studCount, iterations) }
        prin("================ Optimasi - hc_moveN n=$n ===================")
        val t4= measureTime { opt4= Optimize.lin_moveN_hc(bestSch, adjMat, studCount, n, iterations) }
        prin("================ Optimasi - hc_swapN n=$n ===================")
        val t5= measureTime { opt5= Optimize.lin_swapN_hc(bestSch, adjMat, studCount, n, iterations) }
        prin("================ Optimasi - sa_moveN n=$n ===================")
        val t6= measureTime { opt6= Optimize.lin_moveN_sa(bestSch, adjMat, studCount, n, temp, decayRate, iterations) }
        prin("================ Optimasi - sa_swapN n=$n ===================")
        val t7= measureTime { opt7= Optimize.lin_swapN_sa(bestSch, adjMat, studCount, n, temp, decayRate, iterations) }
        prin("================ Optimasi - gd_moveN n=$n ===================")
        val t8= measureTime { opt8= Optimize.lin_moveN_gd(bestSch, adjMat, studCount, n, decayRate = decayRate, iterations = iterations) }
        prin("================ Optimasi - gd_swapN n=$n ===================")
        val t9= measureTime { opt9= Optimize.lin_swapN_gd(bestSch, adjMat, studCount, n, decayRate = decayRate, iterations = iterations) }
// */
///*
        prin("================ Optimasi - hyper_gd maxN=$n ===================")
        val optAlgo: HighLevel.ROULLETE_WHEEL
        val t10= measureTime {
            optAlgo= HighLevel.ROULLETE_WHEEL(n, Evaluation.GREAT_DELUGE(initLevel, decayRate))
            opt10= optAlgo.optimize(bestSch, adjMat, studCount, iterations)
        }
        prin("================ Optimasi - hyper_sa maxN=$n ===================")
        val optAlgo2: HighLevel.ROULLETE_WHEEL
        val t11= measureTime {
            //val initLevel= initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            optAlgo2= HighLevel.ROULLETE_WHEEL(n, Evaluation.SIMULATED_ANNEALING(temp, decayRate))
            opt11= optAlgo2.optimize(bestSch, adjMat, studCount, iterations)
        }
// */

        prin("================ Optimasi - lin_swap_hc maxN=$n ===================")
        val t12= measureTime {
            //val initLevel= initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            opt12= Optimize.lin_swap_hc(bestSch, adjMat, studCount, iterations)
        }

        prin("================ Optimasi - hyperSelection_hc maxN=$n ===================")
        val t13= measureTime {
            //val initLevel= initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            opt13= Optimize.rw_hc(bestSch, adjMat, studCount, n, iterations)
        }
///*
        prin("================ Optimasi - tabu_sa maxN=$n tabuSize= $tabuLLHSize ===================")
        val optAlgo3: HighLevel.TABU
        val t14= measureTime {
            //val initLevel= initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            optAlgo3= HighLevel.TABU(n, tabuLLHSize, Evaluation.SIMULATED_ANNEALING(temp, decayRate))
            opt14= optAlgo3(bestSch, adjMat, studCount, iterations)
        }

        prin("================ Optimasi - tabu_gd maxN=$n tabuSize= $tabuLLHSize ===================")
        val optAlgo4: HighLevel.TABU
        val t15= measureTime {
            optAlgo4= HighLevel.TABU(n, tabuLLHSize, Evaluation.GREAT_DELUGE(initLevel, decayRate))
            opt15= optAlgo4(bestSch, adjMat, studCount, iterations)
        }

        prin("================ Optimasi - tabu_tabu maxN=$n tabuSize= $tabuLLHSize ===================")
        val optAlgo5: HighLevel.TABU
        val t16= measureTime {
            //val initLevel= initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            optAlgo5= HighLevel.TABU(n, tabuLLHSize, Evaluation.TABU(tabuMoveSize))
            opt16= optAlgo5(bestSch, adjMat, studCount, iterations)
        }

        prin("================ Optimasi - rw_tabu maxN=$n tabuSize= $tabuLLHSize ===================")
        val optAlgo6: HighLevel.ROULLETE_WHEEL
        val t17= measureTime {
            //val initLevel= initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            optAlgo6= HighLevel.ROULLETE_WHEEL(n, Evaluation.TABU(tabuMoveSize))
            opt17= optAlgo6(bestSch, adjMat, studCount, iterations)
        }

        prin("================ Optimasi - tabu_hc maxN=$n tabuSize= $tabuLLHSize ===================")
        val t18= measureTime {
            //val initLevel= initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            opt18= Optimize.tabu_hc(bestSch, adjMat, studCount, n, tabuLLHSize, iterations)
        }
// */

        prin("\n\n\n=============== Scheduling _ ${bestSch.miniString()} _ duration= $bestDurr _ initPenalty= $initPenalty ==========")
///*
        opt1?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t1 ==============")
            optList += TestResult(optSch, t1)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t1 ==============")
        }
        opt2?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t2 ==============")
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
// */
        opt4?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t4 ==============")
            optList += TestResult(optSch, t4)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
        opt5?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t5 ==============")
            optList += TestResult(optSch, t5)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
        opt6?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t6 ==============")
            optList += TestResult(optSch, t6)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
        opt7?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t7 ==============")
            optList += TestResult(optSch, t7)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
        opt8?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t8 ==============")
            optList += TestResult(optSch, t8)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
        opt9?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t9 ==============")
            optList += TestResult(optSch, t9)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t2 ==============")
        }
// */
///*
        opt10?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t10 ==============")
            optList += TestResult(optSch, t10)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t10 ==============")
        }
        opt11?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t11 ==============")
            optList += TestResult(optSch, t11)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t11 ==============")
        }
// */
        opt12?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t12 ==============")
            optList += TestResult(optSch, t12)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t12 ==============")
        }
        opt13?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t13 ==============")
            optList += TestResult(optSch, t13)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t13 ==============")
        }
///*
        opt14?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t14 ==============")
            optList += TestResult(optSch, t14)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t14 ==============")
        }
        opt15?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t15 ==============")
            optList += TestResult(optSch, t15)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t15 ==============")
        }
        opt16?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t16 ==============")
            optList += TestResult(optSch, t16)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t16 ==============")
        }
        opt17?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t17 ==============")
            optList += TestResult(optSch, t17)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t17 ==============")
        }
        opt18?.also { (optSch, optPenalty) ->
            val conflict= Util.checkConflicts(optSch, adjMat)
            prin("============== Hasil optimasi _ optSch= ${optSch.miniString()} _ optPenalty= ${optPenalty} durr= $t18 ==============")
            optList += TestResult(optSch, t18)
        }.isNull {
            prinw("============== Hasil optimasi _ Tidak ada durr= $t18 ==============")
        }
// */
///*
        prin("")
        prin("optAlgo= ${optAlgo.optimizationTag}")
        prin("optAlgo.lowLevelDist= ${optAlgo.lowLevelDist}")
        prin("optAlgo.lowLevelDist.probabilities= ${optAlgo.lowLevelDist.probabilities}")
        prin("optAlgo2= ${optAlgo2.optimizationTag}")
        prin("optAlgo2.lowLevelDist= ${optAlgo2.lowLevelDist}")
        prin("optAlgo2.lowLevelDist.probabilities= ${optAlgo2.lowLevelDist.probabilities}")
///*
        prin("optAlgo3= ${optAlgo3.optimizationTag}")
        prin("optAlgo3.lowLevelDist= ${optAlgo3.lowLevelDist}")
        prin("optAlgo3.lowLevelDist.probabilities= ${optAlgo3.lowLevelDist.probabilities}")
        prin("optAlgo4= ${optAlgo4.optimizationTag}")
        prin("optAlgo4.lowLevelDist= ${optAlgo4.lowLevelDist}")
        prin("optAlgo4.lowLevelDist.probabilities= ${optAlgo4.lowLevelDist.probabilities}")
        prin("optAlgo5= ${optAlgo5.optimizationTag}")
        prin("optAlgo5.lowLevelDist= ${optAlgo5.lowLevelDist}")
        prin("optAlgo5.lowLevelDist.probabilities= ${optAlgo5.lowLevelDist.probabilities}")
        prin("optAlgo6= ${optAlgo6.optimizationTag}")
        prin("optAlgo6.lowLevelDist= ${optAlgo6.lowLevelDist}")
        prin("optAlgo6.lowLevelDist.probabilities= ${optAlgo6.lowLevelDist.probabilities}")
// */
        prin("")
// */
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

    @ExperimentalTime
    @Test
    fun realAssignmentTest_milestone2(){
        val iterations = 1_000_000
        val n= 3
        val adjMatContainer= mutableMapOf<String, Array<IntArray>>()
        val studCountContainer= mutableMapOf<String, Int>()
        val optSchs_= mutableListOf<TestResult<Schedule>?>()
        for((i, fileName) in Config.fileNames.withIndex()){
            prin("\n\n========== Mulai Scheduling i= $i fileName= $fileName ============\n")
            val result= Util.runScheduling(i, false, adjMatContainer, studCountContainer)
            val adjMat= adjMatContainer[fileName]!!
            val studCount= studCountContainer[fileName]!!
            val maxTimeslot= Config.maxTimeslot[i]
            val (initSch, initDurr)= Util.getLeastPenaltyAndTimeSchedule(*result.toTypedArray(), maxTimeslot = maxTimeslot)!!

            prin("")
            prin("========== Init Schedule sch= ${initSch.miniString()} durr= $initDurr ============")

            val optSch1: Pair<Schedule, Double>?
            val optSch2: Pair<Schedule, Double>?
            val optSch3: Pair<Schedule, Double>?
            val optSch4: Pair<Schedule, Double>?
            val optSch5: Pair<Schedule, Double>?

            val optSchs= mutableListOf<TestResult<Schedule>>()

            prin("")
            prin("========= Mulai Optimasi - lin_move_hc itr= $iterations =========")
            val t1= measureTime { optSch1= Optimize.lin_move_hc(initSch, adjMat, studCount, iterations) }
            optSch1?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t1)
                prin("============= optSch= ${sch.miniString()} durr= $t1 ===========")
            }

            prin("========= Mulai Optimasi - lin_swap_hc itr= $iterations =========")
            val t2= measureTime { optSch2= Optimize.lin_swap_hc(initSch, adjMat, studCount, iterations) }
            optSch2?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t2)
                prin("============= optSch= ${sch.miniString()} durr= $t2 ===========")
            }

            prin("========= Mulai Optimasi - lin_moveN_hc n= $n itr= $iterations =========")
            val t3= measureTime { optSch3= Optimize.lin_moveN_hc(initSch, adjMat, studCount, n, iterations) }
            optSch3?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t3)
                prin("============= optSch= ${sch.miniString()} durr= $t3 ===========")
            }

            prin("========= Mulai Optimasi - lin_swapN_hc n= $n itr= $iterations =========")
            val t4= measureTime { optSch4= Optimize.lin_swapN_hc(initSch, adjMat, studCount, n, iterations) }
            optSch4?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t4)
                prin("============= optSch= ${sch.miniString()} durr= $t4 ===========")
            }

            prin("========= Mulai Optimasi - hyperSelection_hc n= $n itr= $iterations =========")
            val t5= measureTime { optSch5= Optimize.rw_hc(initSch, adjMat, studCount, n, iterations) }
            optSch5?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t5)
                prin("============= optSch= ${sch.miniString()} durr= $t5 ===========")
            }

            val bestOptSch= Util.getLeastPenaltyAndTimeSchedule(*optSchs.toTypedArray())
            bestOptSch?.also { (sch, time) -> prin("opt durr= $time") }
            optSchs_ += bestOptSch
        }

        prin("\n\n========== Hasil Akhir Optimasi ==========\n")
        for((i, opt) in optSchs_.withIndex()){
            prin("======= i= $i file= ${Config.fileNames[i]} sch= ${opt?.result?.miniString()} durr= ${opt?.duration} ===========")
        }
    }
    @ExperimentalTime
    @Test
    fun realAssignmentTest_milestone3(){
        val iterations = 1_000_000
        val n= 3
        val initTemp= 25.0
        val decayRate= 0.07
        val tabuMoveSize= 25
        //val tabuLLHSize= 6

        val adjMatContainer= mutableMapOf<String, Array<IntArray>>()
        val studCountContainer= mutableMapOf<String, Int>()
        val optSchs_= mutableListOf<TestResult<Schedule>?>()
        for((i, fileName) in Config.fileNames.withIndex()){
            prin("\n\n========== Mulai Scheduling i= $i fileName= $fileName ============\n")
            val result= Util.runScheduling(i, false, adjMatContainer, studCountContainer)
            val adjMat= adjMatContainer[fileName]!!
            val studCount= studCountContainer[fileName]!!
            val maxTimeslot= Config.maxTimeslot[i]
            val (initSch, initDurr)= Util.getLeastPenaltyAndTimeSchedule(*result.toTypedArray(), maxTimeslot = maxTimeslot)!!

            val initPenalty= initSch.penalty
            val initLevel= initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE

            prin("")
            prin("========== Init Schedule sch= ${initSch.miniString()} durr= $initDurr ============")

            val optSch1: Pair<Schedule, Double>?
            val optSch2: Pair<Schedule, Double>?
            val optSch3: Pair<Schedule, Double>?
            val optSch4: Pair<Schedule, Double>?
            //val optSch5: Pair<Schedule, Double>?

            val optSchs= mutableListOf<TestResult<Schedule>>()

            prin("")
            prin("========= Mulai Optimasi - rw_hc itr= $iterations =========")
            val t1= measureTime { optSch1= Optimize.rw_hc(initSch, adjMat, studCount, n, iterations) }
            optSch1?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t1)
                prin("============= optSch= ${sch.miniString()} durr= $t1 ===========")
            }

            prin("========= Mulai Optimasi - rw_sa itr= $iterations initTemp= $initTemp decayRate= $decayRate =========")
            val t2= measureTime { optSch2= Optimize.rw_sa(initSch, adjMat, studCount, n, initTemp, decayRate, iterations) }
            optSch2?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t2)
                prin("============= optSch= ${sch.miniString()} durr= $t2 ===========")
            }

            prin("========= Mulai Optimasi - rw_gd n= $n itr= $iterations initLevel= $initLevel decayRate= $decayRate =========")
            val t3= measureTime { optSch3= Optimize.rw_gd(initSch, adjMat, studCount, n, initLevel, decayRate, iterations) }
            optSch3?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t3)
                prin("============= optSch= ${sch.miniString()} durr= $t3 ===========")
            }

            prin("========= Mulai Optimasi - rw_ta n= $n itr= $iterations tabuMoveSize= $tabuMoveSize =========")
            val t4= measureTime { optSch4= Optimize.rw_ta(initSch, adjMat, studCount, n, tabuMoveSize, iterations) }
            optSch4?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t4)
                prin("============= optSch= ${sch.miniString()} durr= $t4 ===========")
            }

            val bestOptSch= Util.getLeastPenaltyAndTimeSchedule(*optSchs.toTypedArray())
            bestOptSch?.also { (sch, time) -> prin("opt durr= $time") }
            optSchs_ += bestOptSch
        }

        prin("\n\n========== Hasil Akhir Optimasi ==========\n")
        for((i, opt) in optSchs_.withIndex()){
            prin("======= i= $i file= ${Config.fileNames[i]} sch= ${opt?.result?.miniString()} durr= ${opt?.duration} ===========")
        }
    }
    @ExperimentalTime
    @Test
    fun realAssignmentTest_milestone4(){
        val iterations = 1_000_000
        val n= 3
        val initTemp= 25.0
        val decayRate= 0.07
        val tabuMoveSize= 25
        val tabuLLHSize= 3

        val adjMatContainer= mutableMapOf<String, Array<IntArray>>()
        val studCountContainer= mutableMapOf<String, Int>()
        val optSchs_= mutableListOf<TestResult<Schedule>?>()
        for((i, fileName) in Config.fileNames.withIndex()){
            prin("\n\n========== Mulai Scheduling i= $i fileName= $fileName ============\n")
            val result= Util.runScheduling(i, false, adjMatContainer, studCountContainer)
            val adjMat= adjMatContainer[fileName]!!
            val studCount= studCountContainer[fileName]!!
            val maxTimeslot= Config.maxTimeslot[i]
            val (initSch, initDurr)= Util.getLeastPenaltyAndTimeSchedule(*result.toTypedArray(), maxTimeslot = maxTimeslot)!!

            val initPenalty= initSch.penalty
            val initLevel= initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE

            prin("")
            prin("========== Init Schedule sch= ${initSch.miniString()} durr= $initDurr ============")

            val optSch1: Pair<Schedule, Double>?
            val optSch2: Pair<Schedule, Double>?
            val optSch3: Pair<Schedule, Double>?
            val optSch4: Pair<Schedule, Double>?
            //val optSch5: Pair<Schedule, Double>?

            val optSchs= mutableListOf<TestResult<Schedule>>()

            prin("")
///*
            prin("========= Mulai Optimasi - tabu_hc itr= $iterations n= $n tabuLLHSize= $tabuLLHSize =========")
            val t1= measureTime { optSch1= Optimize.tabu_hc(initSch, adjMat, studCount, n, tabuLLHSize, iterations) }
            optSch1?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t1)
                prin("============= optSch= ${sch.miniString()} durr= $t1 ===========")
            }
// */

            prin("========= Mulai Optimasi - tabu_sa itr= $iterations tabuLLHSize= $tabuLLHSize initTemp= $initTemp decayRate= $decayRate =========")
            val t2= measureTime { optSch2= Optimize.tabu_sa(initSch, adjMat, studCount, n, tabuLLHSize, initTemp, decayRate, iterations) }
            optSch2?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t2)
                prin("============= optSch= ${sch.miniString()} durr= $t2 ===========")
            }

            prin("========= Mulai Optimasi - tabu_gd n= $n itr= $iterations tabuLLHSize= $tabuLLHSize initLevel= $initLevel decayRate= $decayRate =========")
            val t3= measureTime { optSch3= Optimize.tabu_gd(initSch, adjMat, studCount, n, tabuLLHSize, initLevel, decayRate, iterations) }
            optSch3?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t3)
                prin("============= optSch= ${sch.miniString()} durr= $t3 ===========")
            }

            prin("========= Mulai Optimasi - tabu_ta n= $n itr= $iterations tabuLLHSize= $tabuLLHSize tabuMoveSize= $tabuMoveSize =========")
            val t4= measureTime { optSch4= Optimize.tabu_ta(initSch, adjMat, studCount, n, tabuLLHSize, tabuMoveSize, iterations) }
            optSch4?.also { (sch, penalty) ->
                optSchs += TestResult(sch, t4)
                prin("============= optSch= ${sch.miniString()} durr= $t4 ===========")
            }

            val bestOptSch= Util.getLeastPenaltyAndTimeSchedule(*optSchs.toTypedArray())
            bestOptSch?.also { (sch, time) -> prin("opt durr= $time") }
            optSchs_ += bestOptSch
        }

        prin("\n\n========== Hasil Akhir Optimasi ==========\n")
        for((i, opt) in optSchs_.withIndex()){
            prin("======= i= $i file= ${Config.fileNames[i]} sch= ${opt?.result?.miniString()} durr= ${opt?.duration} ===========")
        }
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

    @Test
    fun crsxTesg(){
        val crs1= Course(0, 1, 2, 3)
        val crs2= Course(1, 11, 21, 3)
        val crs3= Course(2, 11, 21, 3)

        val sch= Schedule()
        sch.assignments[Timeslot(0)] = mutableListOf(crs3)
        sch.assignments[Timeslot(1)] = mutableListOf(crs1, crs2)

        val ls= listOf(crs1, crs2, crs3)

        //Util.saveDetailedCourses(File(Config.getDetailedCourseFileDir(0)), ls)


        val crsList= mutableListOf<Course>()
        sch.iterator(true).forEach {
            crsList.add(it.first)
        }
        Util.saveDetailedCourses(File(Config.getDetailedCourseFileDir(0)), crsList)
    }


    @ExperimentalTime
    @Test
    fun initSolution_(){
        val crs1= Course(0, 1)
        val crs2= Course(1, 11)
        val crs3= Course(2, 14)
        val crs4= Course(3, 15)
        val crs5= Course(4, 16)

        val sch1= Schedule()
        val sch2= Schedule()

        sch1.assignments[Timeslot(0)] = mutableListOf(crs3, crs2)
        sch1.assignments[Timeslot(1)] = mutableListOf(crs4)

        sch2.assignments[Timeslot(0)] = mutableListOf(crs1, crs5)
        sch2.assignments[Timeslot(1)] = mutableListOf(crs3)

        val test1= TestResult(sch1, Duration.INFINITE)
        val test2= TestResult(sch2, Duration.ZERO)

        val tag1= ScheduleTag(Construct.LaD, Optimize.GD_MOVE, Config.fileNames[0])
        val tag2= ScheduleTag(Construct.X_LaCE, Optimize.TA_GD, Config.fileNames[1])

        val pair1= tag1 to test1
        val pair2= tag2 to test2

        val ls= listOf(pair1, pair2)

        Util.saveDetailedCourses(ls)
/*
        val adjMatContainer = mutableMapOf<String, Array<IntArray>>()
        val stucContainer = mutableMapOf<String, Int>()
        val results= Util.runAllScheduling(adjMatContainer, stucContainer)
        prin("\n")
        val bestSchedulings= Util.getBestSchedulings(results)

        Duration.INFINITE

        prin("\n\n\n=============== Hasil Semua Scheduling ==========")
        bestSchedulings.forEach { (tag, sc) -> prin("fileName= ${tag.fileName} sc= ${sc?.result?.miniString()} duration= ${sc?.duration}") }

        prin("\n\n\n=============== Hasil Semua Scheduling ==========")
        val conflicts= Util.checkConflicts(results, )
        bestSchedulings.forEach { (tag, sc) -> prin("fileName= ${tag.fileName} sc= ${sc?.result?.miniString()} duration= ${sc?.duration}") }
 */
/*
        Util.saveAllAdjMatrix(adjMatContainer)
        Util.saveAllStudentCounts(stucContainer)
        Util.saveDetailedCourses(bestSchedulings)
        Util.saveBestSchedulings(bestSchedulings)
        Util.saveBestSchedulingRes(bestSchedulings)
        Util.saveAllResult(results)
        Util.saveFinalSol(bestSchedulings)
 */
    }
}