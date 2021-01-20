import fp.Config
import fp.Util
import fp.algo.optimize.Optimize
import fp.model.Schedule
import org.junit.Test
import sidev.lib.check.isNull
import sidev.lib.console.prin
import sidev.lib.jvm.tool.util.FileUtil
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class FpTestForReport {
    val iterations= 1_000_000
    val llhN= 3
    val initTemp= 15.0
    val initLevel= -1.0
    val decayRateSA= 0.2
    val decayRateGD= 0.2
    val tabuMoveSize= -1
    val tabuLlhSize= 3

    @ExperimentalTime
    @Test
    fun allTest(){
        initSolution()
        linearHC()
    }

    @ExperimentalTime
    @Test
    fun allOptTest(){
        prin("\n\n ================ linearHC() ============== \n\n")
        linearHC()
        prin("\n\n ================ linearSA() ============== \n\n")
        linearSA()
        prin("\n\n ================ linearGD() ============== \n\n")
        linearGD()
        prin("\n\n ================ linearGD() ============== \n\n")
        linearTA()

        prin("\n\n ================ rw() ============== \n\n")
        rw()
        prin("\n\n ================ ta() ============== \n\n")
        ta()
    }

    @ExperimentalTime
    @Test
    fun initSolution(){
        val adjMatContainer = mutableMapOf<String, Array<IntArray>>()
        val stucContainer = mutableMapOf<String, Int>()
        val results= Util.runAllScheduling(adjMatContainer, stucContainer)
        prin("\n")
        val bestSchedulings= Util.getBestSchedulings(results)

        prin("\n\n\n=============== Hasil Semua Scheduling ==========")
        bestSchedulings.forEach { (tag, sc) -> prin("fileName= ${tag.fileName} sc= ${sc?.result?.miniString()} duration= ${sc?.duration}") }
/*
        prin("\n\n\n=============== Hasil Semua Scheduling ==========")
        val conflicts= Util.checkConflicts(results, )
        bestSchedulings.forEach { (tag, sc) -> prin("fileName= ${tag.fileName} sc= ${sc?.result?.miniString()} duration= ${sc?.duration}") }
 */
        Util.saveAllAdjMatrix(adjMatContainer)
        Util.saveAllStudentCounts(stucContainer)
        Util.saveDetailedCourses(bestSchedulings)
        Util.saveBestSchedulings(bestSchedulings)
        Util.saveBestSchedulingRes(bestSchedulings)
        Util.saveAllResult(results)
        Util.saveFinalSol(bestSchedulings)

        val initSchFileDir= Config.DATASET_DIR + "\\init_solution.csv"
        val initSchFile= File(initSchFileDir)
        FileUtil.saveln(initSchFile, "file_name\tpenalty\tduration\tconstruct", false)
        for((tag, testRes) in bestSchedulings){
            testRes?.also { (sch, durr) ->
                val penalty= sch.penalty
                val constr= tag.construct.code
                val strLine= "${tag.fileName!!}\t$penalty\t${durr.inMicroseconds}\t$constr"
                FileUtil.saveln(initSchFile, strLine, true)
            }
        }

    }

    @ExperimentalTime
    @Test
    fun linearHC(){
        val durrResMap= mutableMapOf<String, String>()
        val penaltyResMap= mutableMapOf<String, String>()
        var header: String?= null
        for((i, fileName) in Config.fileNames.withIndex()){
        //run {
            //val fileName= "sta-f-83"
            //val i= Config.getFileNameIndex(fileName)
            val adjMat= Util.readAdjMatrix(Config.getAdjMatFileDir(i))
            val initSch= Util.readSchedule(i)

            //prin("initSch= $initSch")
            //prin("adjMat= ${adjMat.joinToString(separator = "\n"){ it.joinToString() }}")
            //prin("")

            prin("=============== initSch= ${initSch.miniString()} ===============")
            prin("=============== Mulai Optimasi linearHC - $fileName n= $llhN =================")

            val opt1: Pair<Schedule, Double>?
            val opt2: Pair<Schedule, Double>?
            val opt3: Pair<Schedule, Double>?
            val opt4: Pair<Schedule, Double>?

            prin("=============== Optimasi - lin_move_hc ============")
            val t1 = measureTime { opt1 = Optimize.lin_move_hc(initSch, adjMat, initSch.initStudentCount, iterations) }
            prin("=============== Optimasi - lin_swap_hc ============")
            val t2 = measureTime { opt2 = Optimize.lin_swap_hc(initSch, adjMat, initSch.initStudentCount, iterations) }
            prin("=============== Optimasi - lin_moveN_hc ============")
            val t3 = measureTime { opt3 = Optimize.lin_moveN_hc(initSch, adjMat, initSch.initStudentCount, llhN, iterations) }
            prin("=============== Optimasi - lin_swapN_hc ============")
            val t4 = measureTime { opt4 = Optimize.lin_swapN_hc(initSch, adjMat, initSch.initStudentCount, llhN, iterations) }


            val fileDir= Config.getFileDir(i)
            val durationMap= mutableMapOf<Optimize, Duration?>()
            val penaltyMap= mutableMapOf<Optimize, Double?>()
            durationMap[Optimize.HC_MOVE] = null
            penaltyMap[Optimize.HC_MOVE] = null
            opt1?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t1 ============")
                val fileNameOpt= "${fileDir}_move_hc"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t1
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.HC_SWAP] = null
            penaltyMap[Optimize.HC_SWAP] = null
            opt2?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t2 ============")
                val fileNameOpt= "${fileDir}_swap_hc"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t2
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.HC_MOVEn] = null
            penaltyMap[Optimize.HC_MOVEn] = null
            opt3?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t3 ============")
                val fileNameOpt= "${fileDir}_moveN_hc"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t3
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.HC_SWAPn] = null
            penaltyMap[Optimize.HC_SWAPn] = null
            opt4?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t4 ============")
                val fileNameOpt= "${fileDir}_swapN_hc"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t4
                penaltyMap[sch.tag.optimization]= penalty
            }

            val timeFileDir= "${fileDir}_optimization_duration_linearHC.csv"
            val timeFile= File(timeFileDir)

            val penaltyFileDir= "${fileDir}_optimization_penalty_linearHC.csv"
            val penaltyFile= File(penaltyFileDir)

            //if(!timeFile.exists()){
            if(header == null){
                header= durationMap.keys.joinToString(separator = "\t"){ it.code }
            }
            FileUtil.saveln(timeFile, header, false)
            FileUtil.saveln(penaltyFile, header, false)
            val durrLine= durationMap.values.joinToString(separator = "\t"){ it?.inMicroseconds?.toString() ?: "" }
            val penaltyLine= penaltyMap.values.joinToString(separator = "\t"){ it?.toString() ?: "" }
            durrResMap[fileName] = durrLine
            penaltyResMap[fileName] = penaltyLine
            FileUtil.saveln(timeFile, durrLine, true)
            FileUtil.saveln(penaltyFile, penaltyLine, true)
        }

        val durrFileDir= Config.DATASET_DIR +"\\linearHC_duration.csv"
        val durrFile= File(durrFileDir)
        FileUtil.saveln(durrFile, "file_name\t${header!!}", false)
        for((fileName, strLine) in durrResMap){
            FileUtil.saveln(durrFile, "$fileName\t$strLine", true)
        }

        val penaltyFileDir= Config.DATASET_DIR +"\\linearHC_penalty.csv"
        val penaltyFile= File(penaltyFileDir)
        FileUtil.saveln(penaltyFile, "file_name\t$header", false)
        for((fileName, strLine) in penaltyResMap){
            FileUtil.saveln(penaltyFile, "$fileName\t$strLine", true)
        }
    }

    @ExperimentalTime
    @Test
    fun linearSA(){
        val durrResMap= mutableMapOf<String, String>()
        val penaltyResMap= mutableMapOf<String, String>()
        var header: String?= null
        for((i, fileName) in Config.fileNames.withIndex()){
            val adjMat= Util.readAdjMatrix(Config.getAdjMatFileDir(i))
            val initSch= Util.readSchedule(i)

            val opt1: Pair<Schedule, Double>?
            val opt2: Pair<Schedule, Double>?
            val opt3: Pair<Schedule, Double>?
            val opt4: Pair<Schedule, Double>?

            prin("=============== initSch= ${initSch.miniString()} ===============")
            prin("=============== Mulai Optimasi linearSA - $fileName n= $llhN =================")

            val t1 = measureTime { opt1 = Optimize.lin_move_sa(initSch, adjMat, initSch.initStudentCount, initTemp, decayRateSA, iterations) }
            val t2 = measureTime { opt2 = Optimize.lin_swap_sa(initSch, adjMat, initSch.initStudentCount, initTemp, decayRateSA, iterations) }
            val t3 = measureTime { opt3 = Optimize.lin_moveN_sa(initSch, adjMat, initSch.initStudentCount, llhN, initTemp, decayRateSA, iterations) }
            val t4 = measureTime { opt4 = Optimize.lin_swapN_sa(initSch, adjMat, initSch.initStudentCount, llhN, initTemp, decayRateSA, iterations) }

            val fileDir= Config.getFileDir(i)
            val durationMap= mutableMapOf<Optimize, Duration?>()
            val penaltyMap= mutableMapOf<Optimize, Double?>()
            durationMap[Optimize.SA_MOVE] = null
            penaltyMap[Optimize.SA_MOVE] = null
            opt1?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t1 ============")
                val fileNameOpt= "${fileDir}_move_sa"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t1
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.SA_SWAP] = null
            penaltyMap[Optimize.SA_SWAP] = null
            opt2?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t2 ============")
                val fileNameOpt= "${fileDir}_swap_sa"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t2
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.SA_MOVEn] = null
            penaltyMap[Optimize.SA_MOVEn] = null
            opt3?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t3 ============")
                val fileNameOpt= "${fileDir}_moveN_sa"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t3
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.SA_SWAPn] = null
            penaltyMap[Optimize.SA_SWAPn] = null
            opt4?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t4 ============")
                val fileNameOpt= "${fileDir}_swapN_sa"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t4
                penaltyMap[sch.tag.optimization]= penalty
            }

            val timeFileDir= "${fileDir}_optimization_duration_linearSA.csv"
            val timeFile= File(timeFileDir)

            val penaltyFileDir= "${fileDir}_optimization_penalty_linearSA.csv"
            val penaltyFile= File(penaltyFileDir)

            //if(!timeFile.exists()){
            if(header == null){
                header= durationMap.keys.joinToString(separator = "\t"){ it.code }
            }
            FileUtil.saveln(timeFile, header, false)
            FileUtil.saveln(penaltyFile, header, false)
            val durrLine= durationMap.values.joinToString(separator = "\t"){ it?.inMicroseconds?.toString() ?: "" }
            val penaltyLine= penaltyMap.values.joinToString(separator = "\t"){ it?.toString() ?: "" }
            durrResMap[fileName] = durrLine
            penaltyResMap[fileName] = penaltyLine
            FileUtil.saveln(timeFile, durrLine, true)
            FileUtil.saveln(penaltyFile, penaltyLine, true)
        }

        val durrFileDir= Config.DATASET_DIR +"\\linearSA_duration.csv"
        val durrFile= File(durrFileDir)
        FileUtil.saveln(durrFile, "file_name\t${header!!}", false)
        for((fileName, strLine) in durrResMap){
            FileUtil.saveln(durrFile, "$fileName\t$strLine", true)
        }

        val penaltyFileDir= Config.DATASET_DIR +"\\linearSA_penalty.csv"
        val penaltyFile= File(penaltyFileDir)
        FileUtil.saveln(penaltyFile, "file_name\t$header", false)
        for((fileName, strLine) in penaltyResMap){
            FileUtil.saveln(penaltyFile, "$fileName\t$strLine", true)
        }
    }

    @ExperimentalTime
    @Test
    fun linearGD(){
        val durrResMap= mutableMapOf<String, String>()
        val penaltyResMap= mutableMapOf<String, String>()
        var header: String?= null
        for((i, fileName) in Config.fileNames.withIndex()){
            val adjMat= Util.readAdjMatrix(Config.getAdjMatFileDir(i))
            val initSch= Util.readSchedule(i)

            val opt1: Pair<Schedule, Double>?
            val opt2: Pair<Schedule, Double>?
            val opt3: Pair<Schedule, Double>?
            val opt4: Pair<Schedule, Double>?

            prin("=============== initSch= ${initSch.miniString()} ===============")
            prin("=============== Mulai Optimasi linearGD - $fileName n= $llhN =================")

            val t1 = measureTime { opt1 = Optimize.lin_move_gd(initSch, adjMat, initSch.initStudentCount, initLevel, decayRateGD, iterations) }
            val t2 = measureTime { opt2 = Optimize.lin_swap_gd(initSch, adjMat, initSch.initStudentCount, initLevel, decayRateGD, iterations) }
            val t3 = measureTime { opt3 = Optimize.lin_moveN_gd(initSch, adjMat, initSch.initStudentCount, llhN, initLevel, decayRateGD, iterations) }
            val t4 = measureTime { opt4 = Optimize.lin_swapN_gd(initSch, adjMat, initSch.initStudentCount, llhN, initLevel, decayRateGD, iterations) }


            val fileDir= Config.getFileDir(i)
            val durationMap= mutableMapOf<Optimize, Duration?>()
            val penaltyMap= mutableMapOf<Optimize, Double?>()
            durationMap[Optimize.GD_MOVE] = null
            penaltyMap[Optimize.GD_MOVE] = null
            opt1?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t1 ============")
                val fileNameOpt= "${fileDir}_move_gd"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t1
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.GD_SWAP] = null
            penaltyMap[Optimize.GD_SWAP] = null
            opt2?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t2 ============")
                val fileNameOpt= "${fileDir}_swap_gd"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t2
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.GD_MOVEn] = null
            penaltyMap[Optimize.GD_MOVEn] = null
            opt3?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t3 ============")
                val fileNameOpt= "${fileDir}_moveN_gd"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t3
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.GD_SWAPn] = null
            penaltyMap[Optimize.GD_SWAPn] = null
            opt4?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t4 ============")
                val fileNameOpt= "${fileDir}_swapN_gd"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t4
                penaltyMap[sch.tag.optimization]= penalty
            }

            val timeFileDir= "${fileDir}_optimization_duration_linearGD.csv"
            val timeFile= File(timeFileDir)

            val penaltyFileDir= "${fileDir}_optimization_penalty_linearGD.csv"
            val penaltyFile= File(penaltyFileDir)

            //if(!timeFile.exists()){
            if(header == null){
                header= durationMap.keys.joinToString(separator = "\t"){ it.code }
            }
            FileUtil.saveln(timeFile, header, false)
            FileUtil.saveln(penaltyFile, header, false)
            val durrLine= durationMap.values.joinToString(separator = "\t"){ it?.inMicroseconds?.toString() ?: "" }
            val penaltyLine= penaltyMap.values.joinToString(separator = "\t"){ it?.toString() ?: "" }
            durrResMap[fileName] = durrLine
            penaltyResMap[fileName] = penaltyLine
            FileUtil.saveln(timeFile, durrLine, true)
            FileUtil.saveln(penaltyFile, penaltyLine, true)
        }

        val durrFileDir= Config.DATASET_DIR +"\\linearGD_duration.csv"
        val durrFile= File(durrFileDir)
        FileUtil.saveln(durrFile, "file_name\t${header!!}", false)
        for((fileName, strLine) in durrResMap){
            FileUtil.saveln(durrFile, "$fileName\t$strLine", true)
        }

        val penaltyFileDir= Config.DATASET_DIR +"\\linearGD_penalty.csv"
        val penaltyFile= File(penaltyFileDir)
        FileUtil.saveln(penaltyFile, "file_name\t$header", false)
        for((fileName, strLine) in penaltyResMap){
            FileUtil.saveln(penaltyFile, "$fileName\t$strLine", true)
        }
    }

    @ExperimentalTime
    @Test
    fun linearTA(){
        val durrResMap= mutableMapOf<String, String>()
        val penaltyResMap= mutableMapOf<String, String>()
        var header: String?= null
        for((i, fileName) in Config.fileNames.withIndex()){
            val adjMat= Util.readAdjMatrix(Config.getAdjMatFileDir(i))
            val initSch= Util.readSchedule(i)

            val opt1: Pair<Schedule, Double>?
            val opt2: Pair<Schedule, Double>?
            val opt3: Pair<Schedule, Double>?
            val opt4: Pair<Schedule, Double>?

            prin("=============== initSch= ${initSch.miniString()} ===============")
            prin("=============== Mulai Optimasi linearTA - $fileName n= $llhN =================")

            val t1 = measureTime { opt1 = Optimize.lin_move_ta(initSch, adjMat, initSch.initStudentCount, tabuMoveSize, iterations) }
            val t2 = measureTime { opt2 = Optimize.lin_swap_ta(initSch, adjMat, initSch.initStudentCount, tabuMoveSize, iterations) }
            val t3 = measureTime { opt3 = Optimize.lin_moveN_ta(initSch, adjMat, initSch.initStudentCount, llhN, tabuMoveSize, iterations) }
            val t4 = measureTime { opt4 = Optimize.lin_swapN_ta(initSch, adjMat, initSch.initStudentCount, llhN, tabuMoveSize, iterations) }


            val fileDir= Config.getFileDir(i)
            val durationMap= mutableMapOf<Optimize, Duration?>()
            val penaltyMap= mutableMapOf<Optimize, Double?>()
            durationMap[Optimize.TA_MOVE] = null
            penaltyMap[Optimize.TA_MOVE] = null
            opt1?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t1 ============")
                val fileNameOpt= "${fileDir}_move_ta"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t1
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.TA_SWAP] = null
            penaltyMap[Optimize.TA_SWAP] = null
            opt2?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t2 ============")
                val fileNameOpt= "${fileDir}_swap_ta"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t2
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.TA_MOVEn] = null
            penaltyMap[Optimize.TA_MOVEn] = null
            opt3?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t3 ============")
                val fileNameOpt= "${fileDir}_moveN_ta"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t3
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.TA_SWAPn] = null
            penaltyMap[Optimize.TA_SWAPn] = null
            opt4?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t4 ============")
                val fileNameOpt= "${fileDir}_swapN_ta"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t4
                penaltyMap[sch.tag.optimization]= penalty
            }

            val timeFileDir= "${fileDir}_optimization_duration_linearTA.csv"
            val timeFile= File(timeFileDir)

            val penaltyFileDir= "${fileDir}_optimization_penalty_linearTA.csv"
            val penaltyFile= File(penaltyFileDir)

            //if(!timeFile.exists()){
            if(header == null){
                header= durationMap.keys.joinToString(separator = "\t"){ it.code }
            }
            FileUtil.saveln(timeFile, header, false)
            FileUtil.saveln(penaltyFile, header, false)
            val durrLine= durationMap.values.joinToString(separator = "\t"){ it?.inMicroseconds?.toString() ?: "" }
            val penaltyLine= penaltyMap.values.joinToString(separator = "\t"){ it?.toString() ?: "" }
            durrResMap[fileName] = durrLine
            penaltyResMap[fileName] = penaltyLine
            FileUtil.saveln(timeFile, durrLine, true)
            FileUtil.saveln(penaltyFile, penaltyLine, true)
        }

        val durrFileDir= Config.DATASET_DIR +"\\linearTA_duration.csv"
        val durrFile= File(durrFileDir)
        FileUtil.saveln(durrFile, "file_name\t${header!!}", false)
        for((fileName, strLine) in durrResMap){
            FileUtil.saveln(durrFile, "$fileName\t$strLine", true)
        }

        val penaltyFileDir= Config.DATASET_DIR +"\\linearTA_penalty.csv"
        val penaltyFile= File(penaltyFileDir)
        FileUtil.saveln(penaltyFile, "file_name\t$header", false)
        for((fileName, strLine) in penaltyResMap){
            FileUtil.saveln(penaltyFile, "$fileName\t$strLine", true)
        }
    }

    @ExperimentalTime
    @Test
    fun rw(){
        val durrResMap= mutableMapOf<String, String>()
        val penaltyResMap= mutableMapOf<String, String>()
        var header: String?= null
        for((i, fileName) in Config.fileNames.withIndex()){
            val adjMat= Util.readAdjMatrix(Config.getAdjMatFileDir(i))
            val initSch= Util.readSchedule(i)

            val opt1: Pair<Schedule, Double>?
            val opt2: Pair<Schedule, Double>?
            val opt3: Pair<Schedule, Double>?
            val opt4: Pair<Schedule, Double>?

            prin("=============== initSch= ${initSch.miniString()} ===============")
            prin("=============== Mulai Optimasi rw - $fileName n= $llhN =================")

            val t1 = measureTime { opt1 = Optimize.rw_hc(initSch, adjMat, initSch.initStudentCount, llhN, iterations) }
            val t2 = measureTime { opt2 = Optimize.rw_sa(initSch, adjMat, initSch.initStudentCount, llhN, initTemp, decayRateSA, iterations) }
            val t3 = measureTime { opt3 = Optimize.rw_gd(initSch, adjMat, initSch.initStudentCount, llhN, initLevel, decayRateGD, iterations) }
            val t4 = measureTime { opt4 = Optimize.rw_ta(initSch, adjMat, initSch.initStudentCount, llhN, tabuMoveSize, iterations) }

            val fileDir= Config.getFileDir(i)
            val durationMap= mutableMapOf<Optimize, Duration?>()
            val penaltyMap= mutableMapOf<Optimize, Double?>()
            durationMap[Optimize.RW_HC] = null
            penaltyMap[Optimize.RW_HC] = null
            opt1?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t1 ============")
                val fileNameOpt= "${fileDir}_rw_hc"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t1
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.RW_SA] = null
            penaltyMap[Optimize.RW_SA] = null
            opt2?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t2 ============")
                val fileNameOpt= "${fileDir}_rw_sa"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t2
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.RW_GD] = null
            penaltyMap[Optimize.RW_GD] = null
            opt3?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t3 ============")
                val fileNameOpt= "${fileDir}_rw_gd"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t3
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.RW_TA] = null
            penaltyMap[Optimize.RW_TA] = null
            opt4?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t4 ============")
                val fileNameOpt= "${fileDir}_rw_ta"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t4
                penaltyMap[sch.tag.optimization]= penalty
            }

            val timeFileDir= "${fileDir}_optimization_duration_RW.csv"
            val timeFile= File(timeFileDir)

            val penaltyFileDir= "${fileDir}_optimization_penalty_RW.csv"
            val penaltyFile= File(penaltyFileDir)

            //if(!timeFile.exists()){
            if(header == null){
                header= durationMap.keys.joinToString(separator = "\t"){ it.code }
            }
            FileUtil.saveln(timeFile, header, false)
            FileUtil.saveln(penaltyFile, header, false)
            val durrLine= durationMap.values.joinToString(separator = "\t"){ it?.inMicroseconds?.toString() ?: "" }
            val penaltyLine= penaltyMap.values.joinToString(separator = "\t"){ it?.toString() ?: "" }
            durrResMap[fileName] = durrLine
            penaltyResMap[fileName] = penaltyLine
            FileUtil.saveln(timeFile, durrLine, true)
            FileUtil.saveln(penaltyFile, penaltyLine, true)
        }

        val durrFileDir= Config.DATASET_DIR +"\\RW_duration.csv"
        val durrFile= File(durrFileDir)
        FileUtil.saveln(durrFile, "file_name\t${header!!}", false)
        for((fileName, strLine) in durrResMap){
            FileUtil.saveln(durrFile, "$fileName\t$strLine", true)
        }

        val penaltyFileDir= Config.DATASET_DIR +"\\RW_penalty.csv"
        val penaltyFile= File(penaltyFileDir)
        FileUtil.saveln(penaltyFile, "file_name\t$header", false)
        for((fileName, strLine) in penaltyResMap){
            FileUtil.saveln(penaltyFile, "$fileName\t$strLine", true)
        }
    }

    @ExperimentalTime
    @Test
    fun ta(){
        val durrResMap= mutableMapOf<String, String>()
        val penaltyResMap= mutableMapOf<String, String>()
        var header: String?= null
        for((i, fileName) in Config.fileNames.withIndex()){
            val adjMat= Util.readAdjMatrix(Config.getAdjMatFileDir(i))
            val initSch= Util.readSchedule(i)

            val opt1: Pair<Schedule, Double>?
            val opt2: Pair<Schedule, Double>?
            val opt3: Pair<Schedule, Double>?
            val opt4: Pair<Schedule, Double>?

            prin("=============== initSch= ${initSch.miniString()} ===============")
            prin("=============== Mulai Optimasi ta - $fileName n= $llhN =================")

            val t1 = measureTime { opt1 = Optimize.tabu_hc(initSch, adjMat, initSch.initStudentCount, llhN, tabuLlhSize, iterations) }
            val t2 = measureTime { opt2 = Optimize.tabu_sa(initSch, adjMat, initSch.initStudentCount, llhN, tabuLlhSize, initTemp, decayRateSA, iterations) }
            val t3 = measureTime { opt3 = Optimize.tabu_gd(initSch, adjMat, initSch.initStudentCount, llhN, tabuLlhSize, initLevel, decayRateGD, iterations) }
            val t4 = measureTime { opt4 = Optimize.tabu_ta(initSch, adjMat, initSch.initStudentCount, llhN, tabuLlhSize, tabuMoveSize, iterations) }

            val fileDir= Config.getFileDir(i)
            val durationMap= mutableMapOf<Optimize, Duration?>()
            val penaltyMap= mutableMapOf<Optimize, Double?>()
            durationMap[Optimize.TA_HC] = null
            penaltyMap[Optimize.TA_HC] = null
            opt1?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t1 ============")
                val fileNameOpt= "${fileDir}_ta_hc"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t1
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.TA_SA] = null
            penaltyMap[Optimize.TA_SA] = null
            opt2?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t2 ============")
                val fileNameOpt= "${fileDir}_ta_sa"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t2
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.TA_GD] = null
            penaltyMap[Optimize.TA_GD] = null
            opt3?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t3 ============")
                val fileNameOpt= "${fileDir}_ta_gd"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t3
                penaltyMap[sch.tag.optimization]= penalty
            }
            durationMap[Optimize.TA_TA] = null
            penaltyMap[Optimize.TA_TA] = null
            opt4?.also { (sch, penalty) ->
                prin("=============== Optimasi - ${sch.miniString()} - durr= $t4 ============")
                val fileNameOpt= "${fileDir}_ta_ta"
                val fileNameOptSol= fileNameOpt +Config.FILE_EXTENSION_SOLUTION
                val fileNameOptInfo= fileNameOpt +Config.FILE_EXTENSION_SCHEDULE_INFO

                Util.saveSol(sch, File(fileNameOptSol), false)
                Util.saveSchedulingInfo(File(fileNameOptInfo), sch)
                durationMap[sch.tag.optimization]= t4
                penaltyMap[sch.tag.optimization]= penalty
            }

            val timeFileDir= "${fileDir}_optimization_duration_TA.csv"
            val timeFile= File(timeFileDir)

            val penaltyFileDir= "${fileDir}_optimization_penalty_TA.csv"
            val penaltyFile= File(penaltyFileDir)

            //if(!timeFile.exists()){
            if(header == null){
                header= durationMap.keys.joinToString(separator = "\t"){ it.code }
            }
            FileUtil.saveln(timeFile, header, false)
            FileUtil.saveln(penaltyFile, header, false)
            val durrLine= durationMap.values.joinToString(separator = "\t"){ it?.inMicroseconds?.toString() ?: "" }
            val penaltyLine= penaltyMap.values.joinToString(separator = "\t"){ it?.toString() ?: "" }
            durrResMap[fileName] = durrLine
            penaltyResMap[fileName] = penaltyLine
            FileUtil.saveln(timeFile, durrLine, true)
            FileUtil.saveln(penaltyFile, penaltyLine, true)
        }

        val durrFileDir= Config.DATASET_DIR +"\\TA_duration.csv"
        val durrFile= File(durrFileDir)
        FileUtil.saveln(durrFile, "file_name\t${header!!}", false)
        for((fileName, strLine) in durrResMap){
            FileUtil.saveln(durrFile, "$fileName\t$strLine", true)
        }

        val penaltyFileDir= Config.DATASET_DIR +"\\TA_penalty.csv"
        val penaltyFile= File(penaltyFileDir)
        FileUtil.saveln(penaltyFile, "file_name\t$header", false)
        for((fileName, strLine) in penaltyResMap){
            FileUtil.saveln(penaltyFile, "$fileName\t$strLine", true)
        }
    }
}