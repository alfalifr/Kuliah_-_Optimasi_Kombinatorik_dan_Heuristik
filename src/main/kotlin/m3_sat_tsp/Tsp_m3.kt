package m3_sat_tsp

import sidev.lib.number.notNegativeOr
import kotlin.math.cos

fun main(){

    val distances= ArrayList<IntArray>()
//                   Snell   Planter   Gym   School  Movies
    distances += intArrayOf(0, 3, 5, 2, 3, 4, 8, 9, 9) // Snell
    distances += intArrayOf(4, 0, 8, 6, 7, 2, 5, 6, 6) // Planter
    distances += intArrayOf(2, 1, 0, 5, 3, 2, 4, 10, 6) // Gym
    distances += intArrayOf(5, 1, 2, 0, 10, 3, 8, 2, 6) // School
    distances += intArrayOf(1, 2, 10, 11, 0, 3, 9, 6, 3) // Movies
    distances += intArrayOf(4, 2, 10, 4, 2, 0, 9, 6, 7) // Movies
    distances += intArrayOf(8, 1, 2, 3, 4, 6, 0, 3, 5) // Movies
    distances += intArrayOf(1, 2, 3, 4, 5, 6, 1, 0, 5) // Movies
    distances += intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 0) // Movies

    tsp_NNM_old(distances.toTypedArray())
    println(tsp_NNM(distances.toTypedArray()))
    println(tsp_NNM_withRoute(distances.toTypedArray()).first.joinToString())
    println(tsp_NNM_withRoute_allPossible(distances.toTypedArray()).also { println(it.first.joinToString()) })
}

/**
 * Travelling Salesman Problem - Nearest Neighbor Method
 *
 * Return Pair dari rute terpendek dan jarak tempuhnya.
 *
 * Constraint:
 *   - Setidaknya tiap node terhubung ke 1 node lainnya.
 */
fun tsp_NNM_old(distances: Array<IntArray>): Pair<IntArray, Int>
{
//    fun Array<Boolean>.isAllVisited(): Boolean = reduce { acc, b -> acc && b }

    val routeList= ArrayList<IntArray>()

    var minDist= Int.MAX_VALUE
    var minInd= 0
    println("=== All Possible Route ===")
    for(i in distances.indices){
        val isVisited= BooleanArray(distances.size).also { it[i]= true }
        var distance= 0
        var neighbors= distances[i]
        var nearestNeighborInd= i
        val route= IntArray(distances.size)

        for(u in 1 until neighbors.size){
            var minNeighborDist= Int.MAX_VALUE
            for((o, node) in neighbors.withIndex()){
                if(!isVisited[o] && node in 1 until minNeighborDist){
                    minNeighborDist= node
                    nearestNeighborInd= o
                    neighbors= distances[o]
                }
            }
            route[u]= nearestNeighborInd.also {
                isVisited[it]= true
            }
            println("i= $i u= $nearestNeighborInd dist= $minNeighborDist")
            distance += minNeighborDist
        }
        distance += neighbors[i] //Kembali ke titik awal

        println("=== i= $i distance= $distance")
        routeList += route
        if(minDist > distance){
            minDist= distance
            minInd= i
        }
    }
    println("min distance= $minDist index= $minInd")
    return Pair(routeList[minInd], minDist)
}


fun tsp_NNM(
    graph: Array<IntArray>,
    isVisited: BooleanArray?= null,
    currentPos: Int= 0,
    nodeItr: Int= 1, nodeItrLimit: Int= graph.size,
    cost: Int= 0,
    backtrackingInd: Int= -1,
): Int{
    var cost= cost
    val isVisited= isVisited ?: BooleanArray(nodeItrLimit).also { it[currentPos]= true }
//    val route= (route ?: IntArray(nodeItrLimit +1)).also { it[nodeItr-1]= currentPos }

    if(nodeItr == nodeItrLimit)
        return cost + graph[currentPos][0]

    var nearestNeighbor= Int.MAX_VALUE
    var nearestNeighborInd= -1
    for((i, node) in graph[currentPos].withIndex()){
        if(!isVisited[i] && node > 0 && i != backtrackingInd && nearestNeighbor > node){
            nearestNeighbor= node
            nearestNeighborInd= i
        }
    }
    println("tsp_NNM currentPos= $currentPos nearestNeighborInd= $nearestNeighborInd nearestNeighbor= $nearestNeighbor")
    if(nearestNeighborInd >= 0)
        isVisited[nearestNeighborInd]= true

    cost= tsp_NNM(graph, isVisited, nearestNeighborInd, nodeItr +1, nodeItrLimit, cost + nearestNeighbor)
    //Jika ada yg blum di-visit, maka lakukan backtracking dg mengulangi `currentPos` dan memberi tanda `nearestNeighborInd` sbg index yg melakukan backtracking.
    if(isVisited.find { !it } != null)
        cost= tsp_NNM(graph, isVisited, currentPos, nodeItr, nodeItrLimit, cost, nearestNeighborInd)

    println("tsp_NNM end= $cost")
    return cost
}

fun tsp_NNM_withRoute_allPossible(
    graph: Array<IntArray>
): Pair<IntArray, Int> {
    var minPair= Pair(IntArray(0), -1)
    var minDist= Int.MAX_VALUE
    for(i in graph.indices)
        tsp_NNM_withRoute(graph, currentPos = i).also {
            if(minDist > it.second){
                minDist= it.second
                minPair= it
            }
        }
    return minPair
}

fun tsp_NNM_withRoute(
    graph: Array<IntArray>,
    isVisited: BooleanArray?= null,
    currentPos: Int= 0,
    nodeItr: Int= 1, nodeItrLimit: Int= graph.size,
    cost: Int= 0,
    route: IntArray?= null,
    backtrackingInd: Int= -1,
): Pair<IntArray, Int>{
    var cost= cost
    val isVisited= isVisited ?: BooleanArray(nodeItrLimit).also { it[currentPos]= true }
    var route= (route ?: IntArray(nodeItrLimit +1){ -1 }).also { it[nodeItr-1]= currentPos }

    if(nodeItr == nodeItrLimit)
        return Pair(
            route.also { it[it.lastIndex]= it.first() },
            cost + if(currentPos >= 0) graph[currentPos][route.first().notNegativeOr(0)] else cost
        )

    var nearestNeighbor= Int.MAX_VALUE
    var nearestNeighborInd= -1
    for((i, node) in graph[currentPos].withIndex()){
        if(!isVisited[i] && node > 0 && i != backtrackingInd && nearestNeighbor > node){
            nearestNeighbor= node
            nearestNeighborInd= i
        }
    }
    println("tsp_NNM currentPos= $currentPos nearestNeighborInd= $nearestNeighborInd nearestNeighbor= $nearestNeighbor")
    if(nearestNeighborInd >= 0)
        isVisited[nearestNeighborInd]= true

    tsp_NNM_withRoute(graph, isVisited, nearestNeighborInd, nodeItr +1, nodeItrLimit, cost + nearestNeighbor, route)
        .also { (r, dist) ->
            cost= dist
            route= r
        }

    //Jika ada yg blum di-visit, maka lakukan backtracking dg mengulangi `currentPos` dan memberi tanda `nearestNeighborInd` sbg index yg melakukan backtracking.
    if(isVisited.find { !it } != null)
        tsp_NNM_withRoute(graph, isVisited, currentPos, nodeItr, nodeItrLimit, cost, route, nearestNeighborInd)
            .also { (r, dist) ->
                cost= dist
                route= r
            }

    println("tsp_NNM end= $cost")
    return Pair(route, cost)
}