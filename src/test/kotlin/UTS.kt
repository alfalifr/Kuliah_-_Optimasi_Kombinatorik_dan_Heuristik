import m1_tsp.*
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
        val maxCap= 40
        val bins_nf= binPacking_nextFit(contents, maxCap)

        prin("=================== BP - NextFit ===================")
        bins_nf.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_nf.cumulativeRemCap()}")

        val bins_ff= binPacking_firstFit(contents, maxCap)

        prin("=================== BP - FirstFit ===================")
        bins_ff.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_ff.cumulativeRemCap()}")

        val bins_bf= binPacking_bestFit(contents, maxCap)

        prin("=================== BP - BestFit ===================")
        bins_bf.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_bf.cumulativeRemCap()}")

        val bins_ffd= binPacking_firstFitDecreasing(contents, maxCap)

        prin("=================== BP - FirstFitDecreasing ===================")
        bins_ffd.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_ffd.cumulativeRemCap()}")

        val bins_bfd= binPacking_bestFitDecreasing(contents, maxCap)

        prin("=================== BP - BestFitDecreasing ===================")
        bins_bfd.forEachIndexed { i, bin ->
            prin("i= $i bin= $bin")
        }
        prin("Sisa total= ${bins_bfd.cumulativeRemCap()}")
    }

    @Test
    fun utsTravelCost(){
        val dests= mutableListOf<TravelDest>()
        val budget= 1_500
        dests += TravelDest(50, 200)
        dests += TravelDest(100, 500)
        dests += TravelDest(80, 300)
        dests += TravelDest(200, 800)
        dests += TravelDest(800, 900)
        dests += TravelDest(100, 300)
        dests += TravelDest(200, 400)
        dests += TravelDest(350, 600)

        val dest_n= travelWithCost_firstCome(dests, budget)
        prin("============ FirstCome =================")
        prin("dest= ${dest_n.first} score= ${dest_n.second}")

        val dest_hsf= travelWithCost_highestScoreFirst(dests, budget)
        prin("============ HiegehstScoreFirst =================")
        prin("dest= ${dest_hsf.first} score= ${dest_hsf.second}")

        val dest_lsf= travelWithCost_leastScoreFirst(dests, budget)
        prin("============ LeastScoreFirst =================")
        prin("dest= ${dest_lsf.first} score= ${dest_lsf.second}")

        val dest_hsrf= travelWithCost_highestScoreCostRatioFirst(dests, budget)
        prin("============ HighestScoreRationFirst =================")
        prin("dest= ${dest_hsrf.first} score= ${dest_hsrf.second}")
    }
}