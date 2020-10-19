package m1_tsp

import template.Const
import template.medianNode
import template.permutate
import template.str

/*
val distances= ArrayList<Array<Int>>()
Jawaban: 75 [a, c, b, d, a]
//                   A   B   C   D
distances += arrayOf(0, 40, 10, 35) // A
distances += arrayOf(40, 0, 15, 15) // B
distances += arrayOf(10, 15, 0, 20) // C
distances += arrayOf(35, 15, 20, 0) // D

// A-B-C-D-A : 0
// A-B-D-C-A : 1
// A-C-B-D-A : 2
// A-C-D-B-A : 1
// A-D-B-C-A : 2
// A-D-C-B-A : 0
 */

fun createDistances(node: Int, range: IntRange= 0 .. 100): Array<IntArray> =
    Array(node){ IntArray(node) }.apply {
        for(i in indices)
            for(u in i+1 until size)
                this[i][u]= range.random().also {
                    this[u][i]= it
                }
    }

fun tsp_exhaustive(node: Int, distanceRange: IntRange = 1 .. 100): Pair<IntArray, Int> =
    tsp_exhaustive(createDistances(node, distanceRange))

/**
 *  ** Travelling Salesman Problem **
 *  Permasalahan algoritma di mana setiap node yg saling terhubung dikunjungi hanya sekali
 *  dan perjalanan kembali ke titik semula dg jarak tempuh minimal.
 *  *****
 *
 * Mengambil rute dari [distances] yg memiliki total jarak minimal.
 * [distances] merupakan array 2 dimensi dg ukuran yg sama, contoh: 3x3, 4x4, 5x5.
 * Return Pair dari rute terpendek dan jarak tempuhnya.
 *
 * Batasan fungsi ini:
 *   1. Setiap node yg direpresentasikan oleh tiap jarak pada [distances] harus saling terhubung.
 *   2. Fungsi ini menggunakan exhaustive comparison dan akan sangat terbebani pada [distances].size == 10.
 */
fun tsp_exhaustive(distances: Array<IntArray>, isSymetric: Boolean= true): Pair<IntArray, Int>{
    val n= distances.size
    val travellingNode= n-1
    val allRedundantRoute= permutate(travellingNode){ it +1 }
    val computedRoutes= ArrayList<Array<Int>>()
    val computedDistances= ArrayList<Int>()

    /**
     * Mengecek apakah rute `this.extension` sama dg rute yg udah dihitung jaraknya.
     * Contoh: A-C-B-D-A == A-D-B-C-A
     */
    fun Array<Int>.checkRoute(): Boolean{
        for(route in computedRoutes){
            var allNodeSame= true
            for(i in 0 until medianNode(travellingNode))
                allNodeSame = allNodeSame && this[lastIndex-i] == route[i]
            if(allNodeSame)
                return false
        }
        return true
    }

    var minDistance= Int.MAX_VALUE
    var minDistanceIndex= -1
    var itrInd= -1 // Untuk indeks berjalan
    route@ for(route in allRedundantRoute){
        var currentNode= 0 // untuk titik awal
        if(route.checkRoute() || !isSymetric){
            itrInd++
            var distance= 0
            for(node in route){
                if(node <= 0) continue@route // Karena jika ada bbrp node yg tak terhubung, itu di luar fungsi ini.
                distance += distances[currentNode][node] //.also{ println("==== cehck dist= $it") }
                currentNode= node
            }
            distance += distances[currentNode][0] // kembali ke titik awal
            computedRoutes += route
            computedDistances += distance
            if(minDistance > distance){
                minDistance= distance
                minDistanceIndex= itrInd
            }
        }
    }
///*
    println("========== All possible route n= $n =========")
    for((i, route) in computedRoutes.withIndex()){
        val dist= computedDistances[i]
        println("i= $i route= ${route.str()} distance= $dist")
    }
    println("Min distance= $minDistance")
// */
    val shortestRoute= computedRoutes[minDistanceIndex]
    val rangeOfIndexForComputedRoute= 1 until n
    return Pair(
        IntArray(n+1){
            if(it in rangeOfIndexForComputedRoute) shortestRoute[it-1] else 0
        },
        minDistance
    )
}

/**
 * Sama dg fungsi [tsp_exhaustive], namun rute yg dihasilkan menggunakan string alfabet.
 */
fun namedTsp(distances: Array<IntArray>, nodeNames: Array<String>?= null): Pair<Array<String>, Int> = tsp_exhaustive(distances).let { initRes ->
    val nodeName= nodeNames ?: Const.createAbc(distances.size)
    val nodeIndex= initRes.first
    val nodeCount= distances.size
    Pair(Array(nodeIndex.size){ nodeName[nodeIndex[it] % nodeCount] }, initRes.second)
}

fun namedTsp(node: Int, distanceRange: IntRange = 1 .. 100): Pair<Array<String>, Int> =
    namedTsp(createDistances(node, distanceRange))