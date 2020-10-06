package template

object Const {
    const val ABC_LOWER_UNICODE_START= 97
    const val ABC_LOWER_UNICODE_END= 122
    const val ABC_UPPER_UNICODE_START= 65
    const val ABC_UPPER_UNICODE_END= 90
///*
    val ABC= arrayOf(
        'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l',
        'm', 'n', 'o', 'p', 'q', 'r',
        's', 't', 'u', 'v', 'w', 'x',
        'y', 'z',
    )
// */

    fun createAbc(len: Int): Array<String> = Array(len){ getAbcStr(it) }

    fun getAbcStr(index: Int): String{
        return if(index < ABC.size) ABC[index].toString()
        else getAbcStr((index / ABC.size) -1) +ABC[index % ABC.size].toString()
    }
}

fun main(){
    println('a'.toInt())
    println('z'.toInt())
    println(122-97)
    println('A'.toInt())
    println('Z'.toInt())
    println(90-65)

    println(Const.getAbcStr(52))
}