package fp

import fp.Config.COURSE_INDEX_OFFSET
import sidev.lib.collection.array.filterIndexed
import sidev.lib.console.prine

enum class Optimize(val code: String, vararg val lowLevels: LowLevel) {
    NOT_YET("_0"),
    HC_MOVE("hc_m", LowLevel.MOVE),
    HC_SWAP("hc_m", LowLevel.SWAP),
    ;

    enum class LowLevel(val code: String) {
        MOVE("m"),
        MOVE_2("m2"),
        MOVE_3("m3"),
        SWAP("s"),
        SWAP_3("s3"),
    }

    enum class Evaluation(
        val evaluate: (
            prevDistanceMatrix: DistanceMatrix, //Array<Array<Pair<Int, Int>>>,
            moves: Array<CourseMove>
        ) -> Boolean
    ) {
        BETTER ({ prevDistanceMatrix, moves ->
            var prevPenaltySum= 0.0
            var currentPenaltySum= 0.0
            //var untilEnd= true
            for((i, move) in moves.withIndex()){
                //prine("Evaluation.BETTER move=$move prevPenaltyComp[move.to]=${prevPenaltyComp[move.to]} penalty= $penalty")
                val otherMoves= moves.filterIndexed { u, move2 -> u != i }
                val index= move.id - COURSE_INDEX_OFFSET
                val prevPenalty= prevDistanceMatrix.getLinePenalty(index)
                val currPenalty= prevDistanceMatrix.getLinePenalty(index, move.to, otherMoves)
                /*
                if(prevPenalty >= penalty){
                    untilEnd= false
                    break
                }
                 */
                prevPenaltySum += prevPenalty
                currentPenaltySum += currPenalty
            }
            //prine("Evaluation.BETTER currentPenaltySum=$currentPenaltySum prevPenaltySum=$prevPenaltySum")
            /*untilEnd &&*/ currentPenaltySum < prevPenaltySum
        }),
        ;

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

    companion object {
        private fun DoubleArray.assignPenaltyComp(penaltyComp: Array<Pair<CourseMove, Double>>){
            for((move, comp) in penaltyComp){
                this[move.to]= comp
            }
        }
        private fun DoubleArray.penalty(studentCount: Int): Double = sum() / studentCount / 2

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

        fun move(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
            evaluation: Evaluation,
        ): Pair<Schedule, Double>? = optimize(
            init, courseAdjacencyMatrix, studentCount, iterations, evaluation,
        ) { i, currentSchedule ->
/*
            val timeslotRange= 0 until timeslotCount
            val courseRange= 0 until currentSchedule.courseCount
            val toTimeslot= timeslotRange.random()
            val movedCourseId= courseRange.random() + COURSE_INDEX_OFFSET //Karena id course pada sistem ini dimulai dari 1.

            currentSchedule[movedCourseId] = toTimeslot
            currentSchedule.checkConflictInSameTimeslot(
                courseAdjacencyMatrix, movedCourseId
            )
// */
///
            val range= 0 until currentSchedule.timeslotCount

            val fromTimeslotNo= range.random()
            val fromTimeslot= currentSchedule.getTimeslot(fromTimeslotNo)
            val toTimeslot= range.random()
            var res: Array<CourseMove>?= null
            var loop= true
            var u= -1
            while(loop){
                if(++u >= 7) break
                try {
                    val movedCourseId= currentSchedule[fromTimeslot]!!.random().id

                    res= if(currentSchedule.checkConflictInTimeslot(
                            toTimeslot, courseAdjacencyMatrix, courseId = movedCourseId
                        )) {
                        //val penalty= Util.getPenaltyComponentAt(movedCourseId, courseAdjacencyMatrix, currentSchedule)
                        arrayOf(CourseMove(movedCourseId, fromTimeslotNo, toTimeslot))
                    } else null
                    loop= false
                } catch(e: NoSuchElementException){
                    // Abaikan
                }
            }
            res
/*
            notDone.value= false

            var bool= false
            val notDone= true.asBoxed()
            //val fromTimeslot= timeslotRange.random()
            try {
                // Pake while karena ada kemungkinan kalo timeslot yang dituju kosong.
                whileAndWait({ notDone.value }, 0, { it is NoSuchElementException }, maxRep = 10) {
                    val fromTimeslot= range.random()
                    val toTimeslot= range.random()
                    val movedCourse= currentSchedule.moveAny(fromTimeslot, toTimeslot)

                    currentSchedule.moveAny(fromTimeslot, toTimeslot)
                    bool = currentSchedule.checkConflictInTimeslot(
                        toTimeslot, courseAdjacencyMatrix, course = movedCourse
                    )
                    notDone.value= false
                }
            } catch (e: IllegalStateExc) {
                // Abaikan karena artinya optimasi ini gakda yang lebih optimal.
            }
            bool
// */
        }

        fun swap(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
            evaluation: Evaluation,
        ): Pair<Schedule, Double>? = optimize(
            init, courseAdjacencyMatrix, studentCount, iterations, evaluation,
        ) { i, currentSchedule ->
/*
            val timeslotRange= 0 until timeslotCount
            val courseRange= 0 until currentSchedule.courseCount
            val fromTimeslot= timeslotRange.random()
            val toTimeslot= timeslotRange.random()
            val movedCourseIdFrom= courseRange.random() + COURSE_INDEX_OFFSET //Karena id course pada sistem ini dimulai dari 1.
            val movedCourseIdTo= currentSchedule.entries.find { it.value == toTimeslot }!!.key.id //Mestinya kalo swap, antara asal dan tujuan tidak terjadi perubahan domain.

            currentSchedule[movedCourseIdFrom] = toTimeslot
            currentSchedule[movedCourseIdTo] = fromTimeslot
            currentSchedule.checkConflictInSameTimeslot(
                courseAdjacencyMatrix, movedCourseIdFrom
            ) && currentSchedule.checkConflictInSameTimeslot(
                courseAdjacencyMatrix, movedCourseIdTo
            )
// */
            val range= 0 until currentSchedule.timeslotCount
            val fromTimeslotNo= range.random()
            val fromTimeslot= currentSchedule.getTimeslot(fromTimeslotNo)
            val toTimeslotNo= range.random()
            val toTimeslot= currentSchedule.getTimeslot(toTimeslotNo)
            val movedSrcCourseId= currentSchedule[fromTimeslot]!!.random().id
            val movedDestCourseId= currentSchedule[toTimeslot]!!.random().id

            //val (fromCourse, toCourse)= currentSchedule.swapAny(fromTimeslot, toTimeslot)
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

            if(srcTimeslotValid && destTimeslotValid) {
/*
                val srcPenalty= Util.getPenaltyComponentAt(movedSrcCourseId, courseAdjacencyMatrix){
                    if(it == movedDestCourseId) fromTimeslotNo
                    else currentSchedule.getCourseTimeslot(it)!!.no
                }
                val destPenalty= Util.getPenaltyComponentAt(movedDestCourseId, courseAdjacencyMatrix){
                    if(it == movedSrcCourseId) toTimeslotNo
                    else currentSchedule.getCourseTimeslot(it)!!.no
                }
 */
                arrayOf(
                    CourseMove(movedSrcCourseId, fromTimeslotNo, toTimeslotNo),
                    CourseMove(movedDestCourseId, toTimeslotNo, fromTimeslotNo),
                )
            } else null
        }

        fun move_hillClimbing(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = move(
            init, courseAdjacencyMatrix, studentCount, iterations, Evaluation.BETTER
        )?.apply {
            first.tag.optimization= HC_MOVE
        }

        fun swap_hillClimbing(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = swap(
            init, courseAdjacencyMatrix, studentCount, iterations, Evaluation.BETTER
        )?.apply {
            first.tag.optimization= HC_SWAP
        }
    }
}