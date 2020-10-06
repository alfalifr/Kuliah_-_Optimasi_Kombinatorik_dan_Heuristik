import m1_tsp.createDistances
import m1_tsp.namedTsp
import m1_tsp.tsp
import org.junit.Test
import template.str


class KuliahTest {
    @Test
    fun tspTest(){
//        tsp(4)
        val allRoutes= createDistances(6, 1 .. 100)
        val (route, distance)= namedTsp(allRoutes)
        println("======= TEST ======")
        println("=== All route ===")
        for((i, allRoute) in allRoutes.withIndex())
            println("i= $i route= ${allRoute.str()}")
        println("===== Shortest Route =======")
        println("route= ${route.str()} distance= $distance")
    }
}