/*
class A{}

fun ada(){}

println("halo")

val abc=  arrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g')

/**
 * Untuk operasi generic array-to-string
 */
fun Array<*>.str(): String{
    var str= "["
    for(e in this){
        str += "$e, "
    }
    str= str.removeSuffix(", ")
    return "$str]"
}
fun medianNode(len: Int): Int = ((len / 2).toInt()) +(len % 2)

fun factorial(n: Int): Int= if(n <= 1) 1 else n * factorial(n-1)

fun permutation(n: Int, r: Int= n): Int = factorial(n) / factorial(n-r)
fun combination(n: Int, r: Int= n): Int = factorial(n) / (factorial(n-r) * factorial(r))
fun handshake(n: Int): Int = (n * (n-1)) / 2
fun possibleRoute(n: Int): Int = factorial(n-1) / 2

fun permutateChar(n: Int): List<Array<Char>>{
    println("============= permutate n= $n START ===========")
    println("======= jml = ${permutation(n)}")
    val list= Array(n){ abc[it] }

    val res= ArrayList<Array<Char>>()
    // Karena fungsi [permute] tidak dapat menambahkan array awal.
    res += list.clone()

    var counter= 0
    /**
     * Untuk membuat semua kemungkinan dari list character pada [list].
     */
    fun permutate_int(array: Array<Char>, itr: Int= 0){
        counter++
        if(itr + 1 < array.size) {
            // abc
            // acb
            // bac
            // bca
            // cab
            // cba
            permutate_int(array.clone(), itr +1)
//            res += array.clone()
            for(i in itr +1 until array.size){
                val arr= array.clone()
                val temp= arr[i]
                arr[i]= arr[itr]
                arr[itr]= temp
                res += arr
                permutate_int(arr, itr +1)
            }
        }
    }
    permutate_int(list)

    fun Array<*>.str(): String{
        var str= "["
        for(e in this){
            str += "$e, "
        }
        str= str.removeSuffix(", ")
        return "$str]"
    }

    for((i, arr) in res.withIndex())
        println("i= $i arr= ${arr.str()}")
    println("============= permutate n= $n END permute() counter= $counter ===========")
    return res
}

fun permutateInt(n: Int, init: (index: Int) -> Int): List<Array<Int>>{
    println("============= permutate n= $n START ===========")
    println("======= jml = ${permutation(n)}")
    val list= Array(n, init)

    val res= ArrayList<Array<Int>>()
    // Karena fungsi [permute] tidak dapat menambahkan array awal.
    res += list.clone()

    var counter= 0
    /**
     * Untuk membuat semua kemungkinan dari list character pada [list].
     */
    fun permutate_int(array: Array<Int>, itr: Int= 0){
        counter++
        if(itr + 1 < array.size) {
            // abc
            // acb
            // bac
            // bca
            // cab
            // cba
            permutate_int(array.clone(), itr +1)
//            res += array.clone()
            for(i in itr +1 until array.size){
                val arr= array.clone()
                val temp= arr[i]
                arr[i]= arr[itr]
                arr[itr]= temp
                res += arr
                permutate_int(arr, itr +1)
            }
        }
    }
    permutate_int(list)

    for((i, arr) in res.withIndex())
        println("i= $i arr= ${arr.str()}")
    println("============= permutate n= $n END permute() counter= $counter ===========")
    return res
}

permutateChar(3)
permutateChar(4)
permutateChar(5)


println("=== handshake(2) = ${handshake(2)} possibleRoute(2)= ${possibleRoute(2)}")
println("=== handshake(3) = ${handshake(3)} possibleRoute(3)= ${possibleRoute(3)}")
println("=== handshake(4) = ${handshake(4)} possibleRoute(4)= ${possibleRoute(4)}")

//

val distances= ArrayList<Array<Int>>()
                  // A   B   C   D
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

fun tsp(n: Int= 4 /*sama dg list dummy*/){
    val distances= distances
    val travellingNode= n-1
    val allRedundantRoute= permutateInt(travellingNode){ it +1 }
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
    for(route in allRedundantRoute){
        var currentNode= 0 // untuk titik awal
        if(route.checkRoute()){
            var distance= 0
            for(node in route){
                distance += distances[currentNode][node].also{ println("==== cehck dist= $it") }
                currentNode= node
            }
            distance += distances[currentNode][0] // kembali ke titik awal
            computedRoutes += route
            computedDistances += distance
            if(minDistance > distance)
                minDistance= distance
        }
    }

    println("========== All possible route n= $n =========")
    for((i, route) in computedRoutes.withIndex()){
        val dist= computedDistances[i]
        println("i= $i route= ${route.str()} distance= $dist")
    }
    println("Min distance= $minDistance")
}


tsp()
 */