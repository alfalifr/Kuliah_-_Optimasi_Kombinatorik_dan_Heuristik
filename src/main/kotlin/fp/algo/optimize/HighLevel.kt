package fp.algo.optimize

import fp.Config
import fp.Util
import fp.model.Schedule
import sidev.lib.math.random.DistributedRandom
import sidev.lib.math.random.distRandomOf
import sidev.lib.math.random.randomBoolean

/**
 * Kelas heuristik level tinggi yang berfungsi untuk melakukan iterasi
 * meng-invoke [LowLevel] untuk melakukan aksinya dan meng-invoke [Evaluation]
 * untuk menentukan apakah solusi yang dihasilkan [LowLevel] bisa diterima atau tidak.
 */
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

    protected fun nextLowLevel(coefficient: Double): LowLevel {
        //randomBoolean(1 / (1 + exp(1.0 / lowLevelDist.distSum)))
        return if(!lowLevelDist.isEmpty()
            && randomBoolean(1 / (1.5 + coefficient * (1 / lowLevelDist.distSum))) // Ada kemungkinan dapet lowLevel baru meskipun udah di assign yg lama.
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
            Evaluation.BETTER -> Optimize.Hyper_HC
            is Evaluation.SIMULATED_ANNEALING -> Optimize.Hyper_SA
            is Evaluation.GREAT_DELUGE -> Optimize.Hyper_GD
        }
        override fun optimize(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int,
        ): Pair<Schedule, Double>? {
            val resDistMat= Util.getFullDistanceMatrix(init, courseAdjacencyMatrix)
            //var resPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
            var acceptRes= false
            val sch= init.clone_()
            val coefficient= iterations * 40 / 100.0  // probabilitas diambil-ulangnya lowLevel yang sama mencapai hampir 50% saat iterasi mencapai 40% dari panjang total.
            for(i in 0 until iterations) {
                //val sch= resSch?.clone_() ?: init.clone_()
                val lowLevel= nextLowLevel(coefficient)
                val moves= lowLevel(i, sch, courseAdjacencyMatrix)
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
                } //else { resPenalty.value= resPenalty.value }
            }
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
                LowLevel.MOVE -> Optimize.HC_MOVE
                LowLevel.SWAP -> Optimize.HC_SWAP
                is LowLevel.MOVE_N -> Optimize.HC_MOVEn
                is LowLevel.SWAP_N -> Optimize.HC_SWAPn
            }
            is Evaluation.SIMULATED_ANNEALING -> when(lowLevel){
                LowLevel.MOVE -> Optimize.SA_MOVE
                LowLevel.SWAP -> Optimize.SA_SWAP
                is LowLevel.MOVE_N -> Optimize.SA_MOVEn
                is LowLevel.SWAP_N -> Optimize.SA_SWAPn
            }
            is Evaluation.GREAT_DELUGE -> when(lowLevel){
                LowLevel.MOVE -> Optimize.GD_MOVE
                LowLevel.SWAP -> Optimize.GD_SWAP
                is LowLevel.MOVE_N -> Optimize.GD_MOVEn
                is LowLevel.SWAP_N -> Optimize.GD_SWAPn
            }
        }
        override fun optimize(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int
        ): Pair<Schedule, Double>? {
            val resDistMat= Util.getFullDistanceMatrix(init, courseAdjacencyMatrix)
            var acceptRes= false
            val sch= init.clone_()
            val trimAfter= lowLevel != LowLevel.SWAP && lowLevel !is LowLevel.SWAP_N
            for(i in 0 until iterations) {
                //val sch= resSch?.clone_() ?: init.clone_()
                val moves= lowLevel(i, sch, courseAdjacencyMatrix)
                //val penalty= Util.getPenalty(sch, courseAdjacencyMatrix, studentCount)
                if(moves != null && evaluation(resDistMat, moves)){
                    for(move in moves){
                        resDistMat.setPositionMatrix(move)
                        sch.moveById(move.id, move.to, trimAfter)
                    }
                    acceptRes= true
                } //else { resPenalty.value= resPenalty.value }
            }
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