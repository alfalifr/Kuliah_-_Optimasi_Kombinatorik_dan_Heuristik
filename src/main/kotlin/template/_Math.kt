package template


fun medianNode(len: Int): Int = (len / 2) +(len % 2)

fun factorial(n: Int): Int= if(n <= 1) 1 else n * factorial(n-1)

fun permutation(n: Int, r: Int= n): Int = factorial(n) / factorial(n-r)
fun combination(n: Int, r: Int= n): Int = factorial(n) / (factorial(n-r) * factorial(r))
fun possibleHandshake(n: Int): Int = (n * (n-1)) / 2
fun possibleRoute(n: Int): Int = factorial(n-1) / 2