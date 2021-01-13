package fp.algo.optimize

import fp.Config
import fp.model.CourseMove
import fp.model.DistanceMatrix
import sidev.lib.math.random.randomBoolean
import kotlin.math.exp

/**
 * Kelas algoritma fungsi evaluasi penalty. Kelas ini berfungsi untuk menentukan
 * apakah solusi baru dapat diterima atau tidak.
 */
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
                //val acc = 1 / (1 + exp((currentPenaltySum - prevPenaltySum) / currTemperature))
                val acc = exp(-(currentPenaltySum - prevPenaltySum) / currTemperature)
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
                val index= move.id - Config.COURSE_INDEX_OFFSET
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