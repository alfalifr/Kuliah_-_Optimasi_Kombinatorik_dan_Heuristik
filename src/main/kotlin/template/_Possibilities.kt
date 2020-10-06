package template


inline fun <reified T> permutate(n: Int, noinline init: (index: Int) -> T): List<Array<T>>{
/*
    println("============= permutate n= $n START ===========")
    println("======= jml = ${permutation(n)}")
 */
    val list= Array(n, init)

    val res= ArrayList<Array<T>>()
    // Karena fungsi [permute] tidak dapat menambahkan array awal.
    res += list.clone()

//    var counter= 0
    permutateInner(list, resultContainer = res)
/*
    for((i, arr) in res.withIndex())
        println("i= $i arr= ${arr.str()}")
    println("============= permutate n= $n END permute() counter= $counter ===========")
 */
    return res
}

/**
 * Untuk membuat semua kemungkinan dari list character pada [list].
 */
fun <T> permutateInner(
    array: Array<T>, itr: Int= 0,
    resultContainer: MutableList<Array<T>> = ArrayList()
){
//    counter++
    if(itr + 1 < array.size) {
        // abc
        // acb
        // bac
        // bca
        // cab
        // cba
        permutateInner(array.clone(), itr +1, resultContainer)
//            res += array.clone()
        for(i in itr +1 until array.size){
            val arr= array.clone()
            val temp= arr[i]
            arr[i]= arr[itr]
            arr[itr]= temp
            resultContainer += arr
            permutateInner(arr, itr +1, resultContainer)
        }
    }
}