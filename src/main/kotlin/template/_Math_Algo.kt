package template

import kotlin.math.min

/**
 * Untuk mencari minimum weight dari semua kemungkinan jml pada [graph].
 * Constraint:
 *   - [graph] berukuran [nodeItrLimit] x [nodeItrLimit]
 */
fun hamiltomianCycle(
    graph: Array<IntArray>, isVisited: BooleanArray?= null,
    currentPosition: Int= 0,
    nodeItr: Int= 1, nodeItrLimit: Int= graph.size,
    cost: Int= 0, min: Int= Int.MAX_VALUE,
//    minRouteContainer: MutableList<Int>?= null
): Int{
    val isVisited= isVisited ?: BooleanArray(nodeItrLimit).also { it[0]= true }
    var min= min

    /*
    Jika iterasi [nodeItr] sudah mencapai titik akhir [nodeItrLimit]
    dan [currentPosition] terhubung dengan titik awal,
    maka nilai akhir adalah nilai minimal dari [cost] dan [min].
     */
    if(nodeItr == nodeItrLimit && graph[currentPosition][0] > 0){
        return min(min, cost + graph[currentPosition][0])
    }

    /*
    BACKTRACKING
    =============
    Traverse graph, loop tiap titik yang terhubung dg [currentPosition].
     */
    for(i in graph.indices){
        if(!isVisited[i] && graph[currentPosition][i] > 0){
            //Tandai sudah dikunjungi agar pada rekursif selanjutnya titik yg sama tidak dikunjungi lagi.
            isVisited[i]= true

            //Rekursif untuk menemukan segala kemungkinan rute.
            min= hamiltomianCycle(
                graph, isVisited, i, nodeItr +1, nodeItrLimit,
                cost + graph[currentPosition][i], min
            )

            //Tandai belum dikunjungi. Ini bertujuan untuk backtracking, yaitu untuk kembali ke node sebelumnya dan mencoba rute baru.
            isVisited[i]= false
        }
    }
    return min
}

/**
 * Untuk mencari minimum weight dari semua kemungkinan jml pada [graph].
 * [route] memiliki ukuran yaitu [nodeItrLimit] +1.
 * Nilai default untuk tiap position dalam [route] adalah -1.
 * Constraint:
 *   - [graph] berukuran [nodeItrLimit] x [nodeItrLimit]
 */
fun hamiltomianCycle_withRoute(
    graph: Array<IntArray>, isVisited: BooleanArray?= null,
    currentPosition: Int= 0,
    nodeItr: Int= 1, nodeItrLimit: Int= graph.size,
    cost: Int= 0, min: Int= Int.MAX_VALUE,
    route: IntArray?= null,
    minRoute: IntArray?= null,
): Pair<IntArray, Int>{
    val isVisited= isVisited ?: BooleanArray(nodeItrLimit).also { it[0]= true }
    var route= (route ?: IntArray(nodeItrLimit +1){ -1 }).clone().also {
        it[nodeItr-1]= currentPosition
    }
    var minRoute= minRoute ?: route
    var min= min

//    println("minRouteContainer Start= ${route.joinToString()}")

    /*
    Jika iterasi [nodeItr] sudah mencapai titik akhir [nodeItrLimit]
    dan [currentPosition] terhubung dengan titik awal,
    maka nilai akhir adalah nilai minimal dari [cost] dan [min].
     */
    if(nodeItr == nodeItrLimit && graph[currentPosition][0] > 0){
        (cost + graph[currentPosition][0]).also {
            if(it < min){
                min= it
                route[nodeItr]= 0
                minRoute= route
            }
        }
//        println("minRouteContainer= ${route.joinToString()}")
        return Pair(minRoute, min)
    }

    /*
    BACKTRACKING
    =============
    Traverse graph, loop tiap titik yang terhubung dg [currentPosition].
     */
    for(i in graph.indices){
        if(!isVisited[i] && graph[currentPosition][i] > 0){
            //Tandai sudah dikunjungi agar pada rekursif selanjutnya titik yg sama tidak dikunjungi lagi.
            isVisited[i]= true

            //Rekursif untuk menemukan segala kemungkinan rute.
            hamiltomianCycle_withRoute(
                graph, isVisited, i, nodeItr +1, nodeItrLimit,
                cost + graph[currentPosition][i], min, route, minRoute //.clone()//.also { it[i]=  }
            ).also { (route, dist)  ->
                minRoute= route
                min= dist
            }

            //Tandai belum dikunjungi. Ini bertujuan untuk backtracking, yaitu untuk kembali ke node sebelumnya dan mencoba rute baru.
            isVisited[i]= false
        }
    }
//    println("minRouteContainer End= ${route.joinToString()}")
    return Pair(minRoute, min)
}