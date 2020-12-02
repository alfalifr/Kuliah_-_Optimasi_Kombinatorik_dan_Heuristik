package `val`

enum class Goal(val compareFun: (acc: Comparable<*>, x: Comparable<*>) -> Boolean) {
    MAXIMIZE( { acc, x -> acc > x as Nothing } )
}

fun afaf(){
    Goal.MAXIMIZE.compareFun(1,2)
}