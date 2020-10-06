package template


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