package fp
/*
import fp.Config.COURSE_INDEX_OFFSET
import sidev.lib.`val`.SuppressLiteral
import sidev.lib.exception.IllegalArgExc
import sidev.lib.math.random.DistributedRandom
import sidev.lib.math.random.distRandomOf
import sidev.lib.math.random.randomBoolean
import sidev.lib.progression.domain
import sidev.lib.progression.range
import kotlin.math.exp


enum class Optimize(val code: String /*val evaluation: Evaluation? = null,*/ /*vararg val lowLevels: LowLevel*/) {
    NOT_YET("_0"),
    HC_MOVE("hc_m"),
    SA_MOVE("sa_m"),
    GD_MOVE("gd_m"),
    HC_MOVEn("hc_mN"),
    SA_MOVEn("sa_mN"),
    GD_MOVEn("gd_mN"),
    HC_SWAP("hc_s"),
    SA_SWAP("sa_s"),
    GD_SWAP("gd_s"),
    HC_SWAPn("hc_sN"),
    SA_SWAPn("sa_sN"),
    GD_SWAPn("gd_sN"),
    Hyper_HC("hyp_hc"),
    Hyper_SA("hyp_sa"),
    Hyper_GD("hyp_gd")
    ;

    sealed class LowLevel(val code: String) {
        companion object {
            val all: List<LowLevel> by lazy { listOf(MOVE, SWAP, MOVE_N.Default, SWAP_N.Default) }
            fun getRandom(maxN: Int = 3): LowLevel {
                if(maxN < 1)
                    throw IllegalArgExc(
                        paramExcepted = arrayOf("maxN"),
                        detailMsg = "Param `maxN` ($maxN) < 1"
                    )
                val rand= all.random()
                return when(rand){
                    is MOVE_N -> if(maxN >= 2) MOVE_N((2..maxN).random()) else MOVE
                    is SWAP_N -> if(maxN >= 2) SWAP_N((2..maxN).random()) else SWAP
                    else -> rand
                }
            }
        }

        abstract fun calculate(i: Int, currentSchedule: Schedule, courseAdjacencyMatrix: Array<IntArray>): Array<CourseMove>?
        operator fun invoke(i: Int, currentSchedule: Schedule, courseAdjacencyMatrix: Array<IntArray>): Array<CourseMove>? =
            calculate(i, currentSchedule, courseAdjacencyMatrix)

        override fun toString(): String = this::class.simpleName!!
        override fun equals(other: Any?): Boolean = other is LowLevel && toString() == other.toString()
        override fun hashCode(): Int = toString().hashCode()

        object MOVE: LowLevel("m"){
            override fun calculate(
                i: Int,
                currentSchedule: Schedule,
                courseAdjacencyMatrix: Array<IntArray>
            ): Array<CourseMove>? {
                val range= 0 until currentSchedule.timeslotCount

                var res: Array<CourseMove>?= null
                var loop= true
                var u= -1
                while(loop){
                    if(++u >= 7) break
                    try {
                        val fromTimeslotNo= range.random()
                        val fromTimeslot= currentSchedule.getTimeslot(fromTimeslotNo)
                        var toTimeslotNo= fromTimeslotNo //range.random()

                        if(range.range > 0)
                            while(toTimeslotNo == fromTimeslotNo)
                                toTimeslotNo= range.random()

                        val movedCourseId= currentSchedule[fromTimeslot]!!.random().id

                        res= if(currentSchedule.checkConflictInTimeslot(
                                toTimeslotNo, courseAdjacencyMatrix, courseId = movedCourseId
                            )) {
                            //val penalty= Util.getPenaltyComponentAt(movedCourseId, courseAdjacencyMatrix, currentSchedule)
                            arrayOf(CourseMove(movedCourseId, fromTimeslotNo, toTimeslotNo))
                        } else null
                        loop= false
                    } catch(e: NoSuchElementException){
                        // Abaikan
                    }
                }
                return res
            }
        }
        open class MOVE_N private constructor(val n: Int): LowLevel("mN_$n"){
            internal object Default: MOVE_N(-1)
            companion object {
                operator fun invoke(n: Int): MOVE_N = MOVE_N(n)
            }
            override fun toString(): String = "MOVE_$n"
            override fun calculate(
                i: Int,
                currentSchedule: Schedule,
                courseAdjacencyMatrix: Array<IntArray>,
            ): Array<CourseMove>? {
                val range= 0 until currentSchedule.timeslotCount
                val rangeSize= range.domain

                val fromTimeslotNoList= mutableListOf<Int>()
                val moveList= mutableListOf<CourseMove>()
                //val toTimeslotNoList= mutableListOf<Int>()

                for_@ for(u in 0 until n){
                    var loop= true
                    var o= -1
                    while_@ while(loop){
                        if(++o >= 7) break
                        try {
                            val rangeSizeItr= rangeSize - u
                            var fromTimeslotNo: Int
                            do {
                                fromTimeslotNo= range.random()
                            } while(rangeSizeItr > 0 && fromTimeslotNo in fromTimeslotNoList)

                            var toTimeslotNo= fromTimeslotNo
                            while(rangeSize > 0 && toTimeslotNo == fromTimeslotNo){
                                toTimeslotNo= range.random()
                            }

                            val fromTimeslot= currentSchedule.getTimeslot(fromTimeslotNo)
                            val fromCourseList= currentSchedule[fromTimeslot]!!
                            var movedCourseId= fromCourseList.random().id
                            val fromCourseListRemainSize= fromCourseList.size - u
                            while(moveList.any { it.id == movedCourseId }){
                                if(fromCourseListRemainSize > 0)
                                    continue@while_
                                movedCourseId= fromCourseList.random().id
                            }

                            if(currentSchedule.checkConflictInTimeslot(
                                    toTimeslotNo, courseAdjacencyMatrix, courseId = movedCourseId
                                )) {
                                //val penalty= Util.getPenaltyComponentAt(movedCourseId, courseAdjacencyMatrix, currentSchedule)
                                moveList += CourseMove(movedCourseId, fromTimeslotNo, toTimeslotNo)
                            } else {
                                continue@while_
                            }
                            fromTimeslotNoList += fromTimeslotNo
                            loop= false
                        } catch(e: NoSuchElementException){
                            // Abaikan
                        }
                    }
                }
                return if(moveList.isNotEmpty()) moveList.toTypedArray() else null
            }
        }

        object SWAP: LowLevel("s"){
            override fun calculate(
                i: Int,
                currentSchedule: Schedule,
                courseAdjacencyMatrix: Array<IntArray>
            ): Array<CourseMove>? {
                val range= 0 until currentSchedule.timeslotCount
                val fromTimeslotNo= range.random()
                val fromTimeslot= currentSchedule.getTimeslot(fromTimeslotNo)
                val toTimeslotNo= range.random()
                val toTimeslot= currentSchedule.getTimeslot(toTimeslotNo)
                val movedSrcCourseId= currentSchedule[fromTimeslot]!!.random().id
                val movedDestCourseId= currentSchedule[toTimeslot]!!.random().id

                val srcTimeslotValid = currentSchedule.checkConflictInTimeslot(
                    fromTimeslotNo, courseAdjacencyMatrix, courseId = movedDestCourseId
                ){
                    it.id != movedSrcCourseId
                }
                val destTimeslotValid = currentSchedule.checkConflictInTimeslot(
                    toTimeslotNo, courseAdjacencyMatrix, courseId = movedSrcCourseId
                ){
                    it.id != movedDestCourseId
                }

                //prine("Optimize.swap() fromTimeslotNo=$fromTimeslotNo toTimeslotNo=$toTimeslotNo movedSrcCourseId=$movedSrcCourseId movedDestCourseId=$movedDestCourseId srcTimeslotValid=$srcTimeslotValid destTimeslotValid=$destTimeslotValid")

                return if(srcTimeslotValid && destTimeslotValid) {
                    arrayOf(
                        CourseMove(movedSrcCourseId, fromTimeslotNo, toTimeslotNo),
                        CourseMove(movedDestCourseId, toTimeslotNo, fromTimeslotNo),
                    )
                } else null
            }
        }
        open class SWAP_N private constructor(val n: Int): LowLevel("sN_$n"){
            internal object Default: SWAP_N(-1)
            companion object {
                operator fun invoke(n: Int): SWAP_N = SWAP_N(n)
            }
            override fun toString(): String = "SWAP_$n"
            override fun calculate(
                i: Int,
                currentSchedule: Schedule,
                courseAdjacencyMatrix: Array<IntArray>
            ): Array<CourseMove>? {
                val range= 0 until currentSchedule.timeslotCount
                val rangeSize= range.domain

                val fromTimeslotNoList= mutableListOf<Int>()
                val moveList= mutableListOf<CourseMove>()

                //prine("Optimize.swap() fromTimeslotNo=$fromTimeslotNo toTimeslotNo=$toTimeslotNo movedSrcCourseId=$movedSrcCourseId movedDestCourseId=$movedDestCourseId srcTimeslotValid=$srcTimeslotValid destTimeslotValid=$destTimeslotValid")

                val lastIndex= n-1
                for_@ for(u in 0 until n){
                    val rangeSizeItr= rangeSize - u
                    val prevMove= if(moveList.isNotEmpty()) moveList[moveList.lastIndex] else null

                    var loop= true
                    var o= -1
                    while_@ while(loop){
                        if(++o >= 7) break
                        try {
                            var fromTimeslotNo: Int
                            if(u < lastIndex || prevMove == null){
                                do {
                                    fromTimeslotNo= range.random()
                                } while(rangeSizeItr > 0 && fromTimeslotNo in fromTimeslotNoList)
                            } else {
                                fromTimeslotNo= moveList[0].to
                            }

                            var toTimeslotNo= prevMove?.from ?: fromTimeslotNo
                            if(prevMove == null){
                                while(rangeSize > 0 && toTimeslotNo == fromTimeslotNo){
                                    toTimeslotNo= range.random()
                                }
                            }

                            val fromTimeslot= currentSchedule.getTimeslot(fromTimeslotNo)
                            val fromCourseList= currentSchedule[fromTimeslot]!!
//                        val toTimeslot= currentSchedule.getTimeslot(fromTimeslotNo)

                            var movedCourseId= currentSchedule[fromTimeslot]!!.random().id
//                        val movedDestCourseId= currentSchedule[toTimeslot]!!.random().id

                            val fromCourseListRemainSize= fromCourseList.size - u
                            while(moveList.any { it.id == movedCourseId }){
                                if(fromCourseListRemainSize > 0)
                                    continue@while_
                                movedCourseId= fromCourseList.random().id
                            }

                            val prevCourseId= prevMove?.id
/*
                        when {
                            u < lastIndex -> prevMove?.id
                            moveList.isNotEmpty() -> moveList[0].id
                            else -> null
                        }
 */
                            val timeslotValid = currentSchedule.checkConflictInTimeslot(
                                toTimeslotNo, courseAdjacencyMatrix, courseId = movedCourseId
                            ){
                                it.id != prevCourseId
                            }

                            if(timeslotValid) {
                                //val penalty= Util.getPenaltyComponentAt(movedCourseId, courseAdjacencyMatrix, currentSchedule)
                                moveList += CourseMove(movedCourseId, fromTimeslotNo, toTimeslotNo)
                            } else {
                                continue@while_
                            }
                            fromTimeslotNoList += fromTimeslotNo
                            loop= false
                        } catch(e: NoSuchElementException){
                            // Abaikan
                        }
                    }
                }
                return if(moveList.isNotEmpty()) moveList.toTypedArray() else null
            }
        }
    }

    sealed class Evaluation {
        abstract fun evaluate(prevDistanceMatrix: DistanceMatrix, moves: Array<CourseMove>): Boolean
        override fun toString(): String = this::class.simpleName!!

        object BETTER : Evaluation() {
            override fun evaluate(prevDistanceMatrix: DistanceMatrix, moves: Array<CourseMove>): Boolean {
                val (prevPenaltySum, currentPenaltySum) = getPenaltySum(prevDistanceMatrix, moves)
                return currentPenaltySum < prevPenaltySum
            }
        }
        class SIMULATED_ANNEALING(
            initTemperature: Double, decayRate: Double
        ): Evaluation(){
            val decayRate: Double = if(decayRate < 1) decayRate else 1 / decayRate
            var currTemperature: Double = initTemperature
                private set
            override fun evaluate(prevDistanceMatrix: DistanceMatrix, moves: Array<CourseMove>): Boolean {
                val (prevPenaltySum, currentPenaltySum) = getPenaltySum(prevDistanceMatrix, moves)

                val accept= if(currentPenaltySum < prevPenaltySum) true
                else {
                    val acc = 1 / (1 + exp((currentPenaltySum - prevPenaltySum) / currTemperature))
                    randomBoolean(acc)
                }
                currTemperature -= currTemperature * decayRate
                return accept
            }
        }
        class GREAT_DELUGE(
            initLevel: Double, decayRate: Double
        ): Evaluation(){
            val decayRate: Double = if(decayRate < 1) decayRate else 1 / decayRate
            var currLevel: Double = initLevel
                private set
            override fun evaluate(prevDistanceMatrix: DistanceMatrix, moves: Array<CourseMove>): Boolean {
                val (prevPenaltySum, currentPenaltySum) = getPenaltySum(prevDistanceMatrix, moves)

                val accept= if(currentPenaltySum < prevPenaltySum) true
                else currentPenaltySum <= currLevel
                currLevel -= currLevel * decayRate
                return accept
            }
        }

        companion object {
            fun getPenaltySum(
                prevDistanceMatrix: DistanceMatrix, //Array<Array<Pair<Int, Int>>>,
                moves: Array<CourseMove>
            ): Pair<Double, Double> {
                var prevPenaltySum= 0.0
                var currentPenaltySum= 0.0
                //var untilEnd= true
                for((i, move) in moves.withIndex()){
                    //prine("Evaluation.BETTER move=$move prevPenaltyComp[move.to]=${prevPenaltyComp[move.to]} penalty= $penalty")
                    val otherMoves= moves.filterIndexed { u, move2 -> u != i }
                    val index= move.id - COURSE_INDEX_OFFSET
                    val prevPenalty= prevDistanceMatrix.getLinePenalty(index)
                    val currPenalty= prevDistanceMatrix.getLinePenalty(index, move.to, otherMoves)
                    //prine("index= $index move= $move otherMoves= $otherMoves")
                    /*
                    if(prevPenalty >= penalty){
                        untilEnd= false
                        break
                    }
                     */
                    prevPenaltySum += prevPenalty
                    currentPenaltySum += currPenalty
                }
                return prevPenaltySum to currentPenaltySum
            }
        }

        /**
         * [penaltyChange] mamakai [CourseMove] sebagai index karena tidak memperhatikan
         * perubahan pada timeslot yang ditinggalkan. Fungsi ini hanya memperhatikan timeslot tujuan
         * karena sifat timeslot asal mirip dengan timeslot tujuan. Fungsi ini hanya menghitung
         * selisih antar 2 timeslot tujuan secara absolut.
         */
        operator fun invoke(
            prevDistanceMatrix: DistanceMatrix, //Array<Array<Pair<Int, Int>>>,
            penaltyChange: Array<CourseMove>
        ): Boolean = evaluate(prevDistanceMatrix, penaltyChange)
    }

    sealed class HighLevel(val code: String, val maxN: Int, val evaluation: Evaluation) {
        val lowLevelDist: DistributedRandom<LowLevel> = distRandomOf()
        abstract val optimizationTag: Optimize

        abstract fun optimize(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS
        ): Pair<Schedule, Double>?
        operator fun invoke(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS
        ): Pair<Schedule, Double>? =
            optimize(init, courseAdjacencyMatrix, studentCount, iterations)

        protected fun nextLowLevel(): LowLevel {
            return if(!lowLevelDist.isEmpty()
                && randomBoolean(1 / (1 + exp(1.0 / lowLevelDist.distSum))) // Ada kemungkinan dapet lowLevel baru meskipun udah di assign yg lama.
            ) lowLevelDist.next()
            else initLowLevel()
        }
        protected fun initLowLevel(): LowLevel = LowLevel.getRandom(maxN)
        protected fun accept(lowLevel: LowLevel){
            lowLevelDist.add(lowLevel)
        }
        override fun toString(): String = this::class.simpleName!!
        override fun equals(other: Any?): Boolean = other is HighLevel && toString() == other.toString()
        override fun hashCode(): Int = toString().hashCode()

        class SELECTION(maxN: Int = 3, evaluation: Evaluation = Evaluation.BETTER)
            : HighLevel("sel", maxN, evaluation) {
            override val optimizationTag: Optimize = when(evaluation){
                Evaluation.BETTER -> Hyper_HC
                is Evaluation.SIMULATED_ANNEALING -> Hyper_SA
                is Evaluation.GREAT_DELUGE -> Hyper_GD
            }
            override fun optimize(
                init: Schedule,
                courseAdjacencyMatrix: Array<IntArray>,
                studentCount: Int,
                iterations: Int,
                //evaluation: Evaluation,
                //action: (i: Int, currentSchedule: Schedule, penaltyChange: Array<Pair<CourseMove, Double>>) -> Unit,
                //[currentSchedule] tidak boleh dimodif isinya.
                //calculation: (i: Int, currentSchedule: Schedule) -> Array<CourseMove>?, //Jika `true`, maka [currentSchedule] akan dijadikan sbg `resSch`.
            ): Pair<Schedule, Double>? {
                //val initFlat= init.toFlat()
                //val timeslotCount= init.timeslotCount
                //initFlat.forEach { prine(it) }
                //prine("initFlat.getCourseTimeslot(1)=${initFlat.getCourseTimeslot(1)}")
                val resDistMat= Util.getFullDistanceMatrix(init, courseAdjacencyMatrix)
                //var resPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
                //var resSch: Schedule? = null
                var acceptRes= false
                val sch= init.clone_()
                for(i in 0 until iterations) {
                    //val sch= resSch?.clone_() ?: init.clone_()
                    val lowLevel= nextLowLevel()
                    val moves= lowLevel(i, sch, courseAdjacencyMatrix)
                    //prine("HigLevel.SELECTION lowLevel= $lowLevel")
                    //val penalty= Util.getPenalty(sch, courseAdjacencyMatrix, studentCount)
                    //prine("penaltyChange= $penaltyChange")
                    if(moves != null && evaluation(resDistMat, moves)){
                        for(move in moves){
                            resDistMat.setPositionMatrix(move)
                            sch.moveById(
                                move.id, move.to,
                                lowLevel != LowLevel.SWAP && lowLevel !is LowLevel.SWAP_N
                            )
                        }
                        accept(lowLevel)
                        acceptRes= true
                        //prine("MASUK akhir= $resPenaltyComp")
                    } //else { resPenalty.value= resPenalty.value }
                }
                //val initFinalPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
                //prine("Optimize initFinalPenalty= $initFinalPenalty")
                return if(acceptRes) {
                    val finalPenalty= Util.getPenalty(sch, courseAdjacencyMatrix, studentCount)
                    sch.apply {
                        trimTimeslot()
                        tag.optimization= optimizationTag
                    } to finalPenalty
                } else null
            }
        }

        class LINEAR(
            val lowLevel: LowLevel,
            //maxN: Int = 3, Gak penting karena udah ada [lowLevel].
            evaluation: Evaluation = Evaluation.BETTER
        ): HighLevel("lin", -1, evaluation){
            override val optimizationTag: Optimize = when(evaluation){
                Evaluation.BETTER -> when(lowLevel){
                    LowLevel.MOVE -> HC_MOVE
                    LowLevel.SWAP -> HC_SWAP
                    is LowLevel.MOVE_N -> HC_MOVEn
                    is LowLevel.SWAP_N -> HC_SWAPn
                }
                is Evaluation.SIMULATED_ANNEALING -> when(lowLevel){
                    LowLevel.MOVE -> SA_MOVE
                    LowLevel.SWAP -> SA_SWAP
                    is LowLevel.MOVE_N -> SA_MOVEn
                    is LowLevel.SWAP_N -> SA_SWAPn
                }
                is Evaluation.GREAT_DELUGE -> when(lowLevel){
                    LowLevel.MOVE -> GD_MOVE
                    LowLevel.SWAP -> GD_SWAP
                    is LowLevel.MOVE_N -> GD_MOVEn
                    is LowLevel.SWAP_N -> GD_SWAPn
                }
            }
            override fun optimize(
                init: Schedule,
                courseAdjacencyMatrix: Array<IntArray>,
                studentCount: Int,
                iterations: Int
            ): Pair<Schedule, Double>? {
                //val initFlat= init.toFlat()
                //val timeslotCount= init.timeslotCount
                //initFlat.forEach { prine(it) }
                //prine("initFlat.getCourseTimeslot(1)=${initFlat.getCourseTimeslot(1)}")
                val resDistMat= Util.getFullDistanceMatrix(init, courseAdjacencyMatrix)
                //var resPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
                //var resSch: Schedule? = null
                var acceptRes= false
                val sch= init.clone_()
                val trimAfter= lowLevel != LowLevel.SWAP && lowLevel !is LowLevel.SWAP_N
                for(i in 0 until iterations) {
                    //val sch= resSch?.clone_() ?: init.clone_()
                    val moves= lowLevel(i, sch, courseAdjacencyMatrix)
                    //val penalty= Util.getPenalty(sch, courseAdjacencyMatrix, studentCount)
                    //prine("penaltyChange= $penaltyChange")
                    if(moves != null && evaluation(resDistMat, moves)){
                        for(move in moves){
                            resDistMat.setPositionMatrix(move)
                            sch.moveById(move.id, move.to, trimAfter)
                        }
                        acceptRes= true
                        //prine("MASUK akhir= $resPenaltyComp")
                    } //else { resPenalty.value= resPenalty.value }
                }
                //val initFinalPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
                //prine("Optimize initFinalPenalty= $initFinalPenalty")
                return if(acceptRes) {
                    val finalPenalty= Util.getPenalty(sch, courseAdjacencyMatrix, studentCount)
                    sch.apply {
                        trimTimeslot()
                        tag.optimization= optimizationTag
                    } to finalPenalty
                } else null
            }
        }
    }

    companion object {
/*
        private fun DoubleArray.assignPenaltyComp(penaltyComp: Array<Pair<CourseMove, Double>>){
            for((move, comp) in penaltyComp){
                this[move.to]= comp
            }
        }
        private fun DoubleArray.penalty(studentCount: Int): Double = sum() / studentCount / 2
 */
/*
        fun optimize(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
            evaluation: Evaluation,
            //action: (i: Int, currentSchedule: Schedule, penaltyChange: Array<Pair<CourseMove, Double>>) -> Unit,
            //[currentSchedule] tidak boleh dimodif isinya.
            calculation: (i: Int, currentSchedule: Schedule) -> Array<CourseMove>?, //Jika `true`, maka [currentSchedule] akan dijadikan sbg `resSch`.
        ): Pair<Schedule, Double>? {
            //val initFlat= init.toFlat()
            //val timeslotCount= init.timeslotCount
            //initFlat.forEach { prine(it) }
            //prine("initFlat.getCourseTimeslot(1)=${initFlat.getCourseTimeslot(1)}")
            val resDistMat= Util.getFullDistanceMatrix(init, courseAdjacencyMatrix)
            //var resPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
            //var resSch: Schedule? = null
            var acceptRes= false
            val sch= init.clone_()
            for(i in 0 until iterations) {
                //val sch= resSch?.clone_() ?: init.clone_()
                val moves= calculation(i, sch)
                //val penalty= Util.getPenalty(sch, courseAdjacencyMatrix, studentCount)
                //prine("penaltyChange= $penaltyChange")
                if(moves != null && evaluation(resDistMat, moves)){
                    for(move in moves){
                        resDistMat.setPositionMatrix(move)
                        sch.moveById(move.id, move.to)
                    }
                    acceptRes= true
                    //prine("MASUK akhir= $resPenaltyComp")
                } //else { resPenalty.value= resPenalty.value }
            }
            //val initFinalPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
            //prine("Optimize initFinalPenalty= $initFinalPenalty")
            return if(acceptRes) {
                val finalPenalty= Util.getPenalty(sch, courseAdjacencyMatrix, studentCount)
                sch to finalPenalty
            } else null
        }
 */

        /*
                fun move2(
                    init: Schedule,
                    courseAdjacencyMatrix: Array<IntArray>,
                    studentCount: Int,
                    iterations: Int = Config.DEFAULT_ITERATIONS,
                    evaluation: Evaluation,
                ): Pair<Schedule, Double>? = optimize(
                    init, courseAdjacencyMatrix, studentCount, iterations, evaluation,
                ) { i, currentSchedule ->
                    val range= 0 until currentSchedule.timeslotCount

                    var res: Array<CourseMove>?= null
                    var loop= true
                    var u= -1
                    while(loop){
                        if(++u >= 7) break
                        try {
                            val fromTimeslotNo1= range.random()
                            var toTimeslotNo1= fromTimeslotNo1 //range.random()

                            if(range.range > 0)
                                while(toTimeslotNo1 == fromTimeslotNo1)
                                    toTimeslotNo1= range.random()

                            var fromTimeslotNo2= fromTimeslotNo1 //range.random()
                            var toTimeslotNo2= toTimeslotNo1 //range.random()

                            if(range.range > 0)
                                while(fromTimeslotNo2 == fromTimeslotNo1)
                                    fromTimeslotNo2= range.random()

                            if(range.range > 0)
                                while(toTimeslotNo2 == fromTimeslotNo2)
                                    toTimeslotNo2= range.random()

                            val fromTimeslot1= currentSchedule.getTimeslot(fromTimeslotNo1)
                            val fromTimeslot2= currentSchedule.getTimeslot(fromTimeslotNo2)

                            val movedCourseId1= currentSchedule[fromTimeslot1]!!.random().id
                            val movedCourseId2= currentSchedule[fromTimeslot2]!!.random().id

                            res= if(currentSchedule.checkConflictInTimeslot(
                                    toTimeslotNo1, courseAdjacencyMatrix, courseId = movedCourseId1
                                ) && currentSchedule.checkConflictInTimeslot(
                                    toTimeslotNo2, courseAdjacencyMatrix, courseId = movedCourseId2
                                )) {
                                //val penalty= Util.getPenaltyComponentAt(movedCourseId, courseAdjacencyMatrix, currentSchedule)
                                arrayOf(
                                    CourseMove(movedCourseId1, fromTimeslotNo1, toTimeslotNo1),
                                    CourseMove(movedCourseId2, fromTimeslotNo2, toTimeslotNo2),
                                )
                            } else null
                            loop= false
                        } catch(e: NoSuchElementException){
                            // Abaikan
                        }
                    }
                    res
                }
         */
        fun lin_move(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            evaluation: Evaluation,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = HighLevel.LINEAR(LowLevel.MOVE, evaluation).optimize(
            init, courseAdjacencyMatrix, studentCount, iterations
        )

        fun lin_moveN(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            evaluation: Evaluation,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = HighLevel.LINEAR(LowLevel.MOVE_N(n), evaluation).optimize(
            init, courseAdjacencyMatrix, studentCount, iterations
        )

        fun lin_swap(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            evaluation: Evaluation,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = HighLevel.LINEAR(LowLevel.SWAP, evaluation).optimize(
            init, courseAdjacencyMatrix, studentCount, iterations
        )

        fun lin_swapN(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            evaluation: Evaluation,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = HighLevel.LINEAR(LowLevel.SWAP_N(n), evaluation).optimize(
            init, courseAdjacencyMatrix, studentCount, iterations
        )

        fun lin_move_hc(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = lin_move(
            init, courseAdjacencyMatrix, studentCount, Evaluation.BETTER, iterations
        )

        fun lin_moveN_hc(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = lin_moveN(
            init, courseAdjacencyMatrix, studentCount, n, Evaluation.BETTER, iterations
        )

        fun lin_moveN_sa(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            initTemperature: Double = Config.DEFAULT_TEMPERATURE_INIT,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = lin_moveN(
            init, courseAdjacencyMatrix, studentCount, n,
            Evaluation.SIMULATED_ANNEALING(initTemperature, decayRate),
            iterations
        )

        fun lin_moveN_gd(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            initLevel: Double = -1.0,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val initLevel= if(initLevel > 0) initLevel else {
                val initPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
                initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            }
            return lin_moveN(
                init, courseAdjacencyMatrix, studentCount, n,
                Evaluation.GREAT_DELUGE(initLevel, decayRate),
                iterations
            )
        }

        fun lin_swap_hc(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = lin_swap(
            init, courseAdjacencyMatrix, studentCount, Evaluation.BETTER, iterations
        )

        fun lin_swapN_hc(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = lin_swapN(
            init, courseAdjacencyMatrix, studentCount, n,
            Evaluation.BETTER, iterations
        )

        fun lin_swapN_sa(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            initTemperature: Double = Config.DEFAULT_TEMPERATURE_INIT,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = lin_swapN(
            init, courseAdjacencyMatrix, studentCount, n,
            Evaluation.SIMULATED_ANNEALING(initTemperature, decayRate),
            iterations
        )

        fun lin_swapN_gd(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            initLevel: Double = -1.0,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val initLevel= if(initLevel > 0) initLevel else {
                val initPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
                initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            }
            return lin_swapN(
                init, courseAdjacencyMatrix, studentCount, n,
                Evaluation.SIMULATED_ANNEALING(initLevel, decayRate),
                iterations
            )
        }

        fun hyperSelection(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            evaluation: Evaluation,
            maxN: Int = 3,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = HighLevel.SELECTION(maxN, evaluation).optimize(
            init, courseAdjacencyMatrix, studentCount, iterations
        )

        fun hyperSelection_hc(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            maxN: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = HighLevel.SELECTION(maxN, Evaluation.BETTER).optimize(
            init, courseAdjacencyMatrix, studentCount, iterations
        )

        fun hyperSelection_sa(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            maxN: Int,
            initTemperature: Double = Config.DEFAULT_TEMPERATURE_INIT,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = HighLevel.SELECTION(
            maxN, Evaluation.SIMULATED_ANNEALING(initTemperature, decayRate)
        ).optimize(
            init, courseAdjacencyMatrix, studentCount, iterations
        )

        fun hyperSelection_gd(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            maxN: Int,
            initLevel: Double = -1.0,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val initLevel= if(initLevel > 0) initLevel else {
                val initPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
                initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            }
            return HighLevel.SELECTION(
                maxN, Evaluation.GREAT_DELUGE(initLevel, decayRate)
            ).optimize(
                init, courseAdjacencyMatrix, studentCount, iterations
            )
        }
    }
}

 */