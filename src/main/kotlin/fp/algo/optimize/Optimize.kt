package fp.algo.optimize

import fp.*
import fp.model.Schedule
import sidev.lib.`val`.SuppressLiteral

/**
 * Entry point convenient untuk melakukan optimasi terhadap initial solution.
 */
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

    companion object {
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