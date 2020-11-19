import m1_tsp.tsp_exhaustive
import m2_binPacking.*
import m3_sat_tsp.tsp_NNM_withRoute_allPossible
import org.junit.Test
import sidev.lib.console.prin
import template.toLetterArray

class UTS {
    @Test
    fun utsTspTest(){
//        tsp(4)
        val distances= ArrayList<IntArray>() //createDistances(30)
///*
        distances += intArrayOf(0,2,5,2,3,4,8,9,8) // Snell
        distances += intArrayOf(4,0,8,6,8,2,5,6,6) // Planter
        distances += intArrayOf(2,1,0,5,3,2,4,10,6) // Gym
        distances += intArrayOf(6,1,2,0,12,3,8,2,6) // School
        distances += intArrayOf(1,2,10,12,0,3,6,6,3) // Movies
        distances += intArrayOf(4,2,10,4,2,0,6,6,8) // Movies
        distances += intArrayOf(8,2,2,3,4,6,0,3,6) // Movies
        distances += intArrayOf(1,2,3,4,5,6,1,0,5) // Movies
        distances += intArrayOf(2,2,3,4,5,6,7,8,0) // Movies
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

        tsp_exhaustive(allRoutes).also{
            prin("Optimal route= ${it.first.joinToString { (it+1).toString() }} distance= ${it.second}")
        }
    }


    @Test
    fun utsBinPackingTest(){
        val contents= arrayOf(20,30,40,100,60,20,25,40,30,10)
        val bins_nf= binPacking_nextFit(contents, 40)

        prin("=================== BP - NextFit ===================")
        bins_nf.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_nf.cumulativeRemCap()}")

        val bins_ff= binPacking_firstFit(contents, 40)

        prin("=================== BP - FirstFit ===================")
        bins_ff.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_ff.cumulativeRemCap()}")

        val bins_bf= binPacking_bestFit(contents, 40)

        prin("=================== BP - BestFit ===================")
        bins_bf.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_bf.cumulativeRemCap()}")

        val bins_ffd= binPacking_firstFitDecreasing(contents, 40)

        prin("=================== BP - FirstFitDecreasing ===================")
        bins_ffd.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_ffd.cumulativeRemCap()}")

        val bins_bfd= binPacking_bestFitDecreasing(contents, 40)

        prin("=================== BP - BestFitDecreasing ===================")
        bins_bfd.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_bfd.cumulativeRemCap()}")
    }
}