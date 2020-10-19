package template

fun IntArray.toLetterArray(): Array<String>{
    val letterArray= Const.createAbc(size)
    return Array(size){ letterArray[this[it]] }
}