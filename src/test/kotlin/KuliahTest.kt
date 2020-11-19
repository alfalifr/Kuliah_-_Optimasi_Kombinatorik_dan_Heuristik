import m1_tsp.createDistances
import m1_tsp.namedTsp
import m2_binPacking.*
import m3_sat_tsp.tsp_NNM_withRoute_allPossible
//import m1_tsp.tsp
import org.junit.Test
import sidev.lib.console.prin
import template.hamiltomianCycle_withRoute
import template.str
import template.toLetterArray

//import template.str


class KuliahTest {
    @Test
    fun tspTest(){
//        tsp(4)
        val distances= ArrayList<IntArray>()
/*
//                   Snell   Planter   Gym   School  Movies
        distances += arrayOf(0, 20, 0, 0, 7) // Snell
        distances += arrayOf(20, 0, 8, 10, 12) // Planter
        distances += arrayOf(0, 8, 0, 10, 15) // Gym
        distances += arrayOf(0, 14, 10, 0, 10) // School
        distances += arrayOf(7, 12, 15, 10, 0) // Movies
 */
        distances += intArrayOf(0, 40, 10, 35) // A
        distances += intArrayOf(40, 0, 15, 15) // B
        distances += intArrayOf(10, 15, 0, 20) // C
        distances += intArrayOf(35, 15, 20, 0) // D

        val allRoutes= distances.toTypedArray() //createDistances(4, 1 .. 100)
        val (route, distance)= namedTsp(allRoutes) //arrayOf("Snell", "Planter", "Gym", "School", "Movies")
        println("======= TEST ======")
        println("=== All route ===")
        for((i, allRoute) in allRoutes.withIndex())
            println("i= $i route= ${allRoute.joinToString()}")
        println("===== Shortest Route =======")
        println("route= ${route.str()} distance= $distance")
//        1 pow 2
    }

    @Test
    fun tspTest_hamiltonianCycle(){
//        tsp(4)
        val distances= createDistances(13) //ArrayList<IntArray>()
/*
        distances += intArrayOf(0, 3, 5, 2, 3, 4, 8, 9, 9) // Snell
        distances += intArrayOf(4, 0, 8, 6, 7, 2, 5, 6, 6) // Planter
        distances += intArrayOf(2, 1, 0, 5, 3, 2, 4, 10, 6) // Gym
        distances += intArrayOf(5, 1, 2, 0, 10, 3, 8, 2, 6) // School
        distances += intArrayOf(1, 2, 10, 11, 0, 3, 9, 6, 3) // Movies
        distances += intArrayOf(4, 2, 10, 4, 2, 0, 9, 6, 7) // Movies
        distances += intArrayOf(8, 1, 2, 3, 4, 6, 0, 3, 5) // Movies
        distances += intArrayOf(1, 2, 3, 4, 5, 6, 1, 0, 5) // Movies
        distances += intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 0) // Movies
// */


        val allRoutes= distances //.toTypedArray() //createDistances(4, 1 .. 100)
        val (route, distance)= hamiltomianCycle_withRoute(allRoutes) //namedTsp(allRoutes) //arrayOf("Snell", "Planter", "Gym", "School", "Movies")
//        val distance= hamiltomianCycle(allRoutes) //namedTsp(allRoutes) //arrayOf("Snell", "Planter", "Gym", "School", "Movies")
        println("======= TEST ======")
        println("=== All route ===")
        for((i, allRoute) in allRoutes.withIndex())
            println("i= $i route= ${allRoute.joinToString()}")
        println("===== Shortest Route =======")
        println("route= ${route.joinToString()} distance= $distance")
//        println("distance= $distance")
//        1 pow 2
    }

    @Test
    fun tspTest_NNM_m3(){
//        tsp(4)
        val distances= ArrayList<IntArray>() //createDistances(30)
///*
        distances += intArrayOf(0, 3, 5, 2, 3, 4, 8, 9, 9) // Snell
        distances += intArrayOf(4, 0, 8, 6, 7, 2, 5, 6, 6) // Planter
        distances += intArrayOf(2, 1, 0, 5, 3, 2, 4, 10, 6) // Gym
        distances += intArrayOf(5, 1, 2, 0, 10, 3, 8, 2, 6) // School
        distances += intArrayOf(1, 2, 10, 11, 0, 3, 9, 6, 3) // Movies
        distances += intArrayOf(4, 2, 10, 4, 2, 0, 9, 6, 7) // Movies
        distances += intArrayOf(8, 1, 2, 3, 4, 6, 0, 3, 5) // Movies
        distances += intArrayOf(1, 2, 3, 4, 5, 6, 1, 0, 5) // Movies
        distances += intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 0) // Movies
// */

        val allRoutes= distances.toTypedArray() //createDistances(4, 1 .. 100)
        val (route, distance)= tsp_NNM_withRoute_allPossible(allRoutes) //namedTsp(allRoutes) //arrayOf("Snell", "Planter", "Gym", "School", "Movies")
//        val distance= hamiltomianCycle(allRoutes) //namedTsp(allRoutes) //arrayOf("Snell", "Planter", "Gym", "School", "Movies")
        println("======= TEST ======")
        println("=== All route ===")
        for((i, allRoute) in allRoutes.withIndex())
            println("i= $i route= ${allRoute.joinToString()}")
        println("===== Shortest Route =======")
        println("route= ${route.joinToString()}")
        println("route letter= ${route.toLetterArray().joinToString()}")
        println("distance= $distance")
//        println("distance= $distance")
//        1 pow 2
    }

    @Test
    fun binPackingTest(){
        val contents= arrayOf(5,7,5,2,4,2,5,1,6)
        val bins_nf= binPacking_nextFit(contents, 10)

        prin("=================== BP - NextFit ===================")
        bins_nf.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_nf.cumulativeRemCap()}")

        val bins_ff= binPacking_firstFit(contents, 10)

        prin("=================== BP - FirstFit ===================")
        bins_ff.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_ff.cumulativeRemCap()}")

        val bins_bf= binPacking_bestFit(contents, 10)

        prin("=================== BP - BestFit ===================")
        bins_bf.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_bf.cumulativeRemCap()}")

        val bins_ffd= binPacking_firstFitDecreasing(contents, 10)

        prin("=================== BP - FirstFitDecreasing ===================")
        bins_ffd.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_ffd.cumulativeRemCap()}")

        val bins_bfd= binPacking_bestFitDecreasing(contents, 10)

        prin("=================== BP - BestFitDecreasing ===================")
        bins_bfd.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_bfd.cumulativeRemCap()}")
    }
}