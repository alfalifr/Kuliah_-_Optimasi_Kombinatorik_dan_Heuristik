package fp.algo.optimize

import fp.model.CourseMove
import fp.model.Schedule
import sidev.lib.console.prine
import sidev.lib.exception.IllegalArgExc
import sidev.lib.progression.domain
import sidev.lib.progression.range

/**
 * Kelas heuristik level rendah yang digunakan untuk merubah susunan
 * course pada tiap timeslot pada tiap iterasi.
 */
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
                    val fromTimeslot= currentSchedule.getTimeslotAssert(fromTimeslotNo)
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

                        val fromTimeslot= currentSchedule.getTimeslotAssert(fromTimeslotNo)
                        val fromCourseList= currentSchedule[fromTimeslot]!!
                        var movedCourseId= fromCourseList.random().id
                        val fromCourseListRemainSize= fromCourseList.size - u
                        while(moveList.any { it.id == movedCourseId }){
                            if(fromCourseListRemainSize <= 0)
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
            return if(moveList.size == n) moveList.toTypedArray() else null
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
            val fromTimeslot= currentSchedule.getTimeslotAssert(fromTimeslotNo)
            val toTimeslotNo= range.random()
            val toTimeslot= currentSchedule.getTimeslotAssert(toTimeslotNo)
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
                    //prine("SWAPn u= $u o= $o moveList.size= ${moveList.size}")
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

                        val fromTimeslot= currentSchedule.getTimeslotAssert(fromTimeslotNo)
                        val fromCourseList= currentSchedule[fromTimeslot]!!
//                        val toTimeslot= currentSchedule.getTimeslot(fromTimeslotNo)

                        var movedCourseId= currentSchedule[fromTimeslot]!!.random().id
//                        val movedDestCourseId= currentSchedule[toTimeslot]!!.random().id

                        val fromCourseListRemainSize= fromCourseList.size - u
                        while(moveList.any { it.id == movedCourseId }){
                            if(fromCourseListRemainSize <= 0)
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
            return if(moveList.size == n) moveList.toTypedArray() else null
        }
    }
}