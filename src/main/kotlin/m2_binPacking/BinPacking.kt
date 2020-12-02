package m2_binPacking

import `val`.TaggedInt

/**
 * Memecah elemen pada [costs] yang memiliki kapasitas di atas [maxCap].
 */
fun binPacking_preprocess(costs: Array<Int>, maxCap: Int): Array<Int> {
    val list= mutableListOf<Int>()

    for(cost in costs){
        if(cost <= maxCap) {
            list += cost
        } else {
            var reducedCost= cost
            while(reducedCost > 0) {
                list += reducedCost
                reducedCost -= maxCap
            }
        }
    }
    return list.toTypedArray()
}

fun binPacking_nextFit(costs: Array<Int>, maxCap: Int, doPreprocess: Boolean = true): List<Bin> {
    val costs= if(doPreprocess) binPacking_preprocess(costs, maxCap) else costs
    val bins= mutableListOf<Bin>()
    var bin= Bin(mutableListOf(), maxCap)

    for(cost in costs){
        if(cost > maxCap)
            continue
        if(bin.remCap >= cost){
            (bin.content as MutableList).add(cost)
            bin.remCap -= cost
        } else {
            bins += bin
            bin= Bin(mutableListOf<Int>().also { it += cost }, (maxCap - cost))
        }
    }
    bins += bin

    return bins
}

fun binPacking_firstFit(costs: Array<Int>, maxCap: Int, doPreprocess: Boolean = true): List<Bin> {
    val costs= if(doPreprocess) binPacking_preprocess(costs, maxCap) else costs
    val bins= mutableListOf<Bin>()

    for(cost in costs){
        if(cost > maxCap)
            continue
        var bin: Bin?= null
        for(prevBin in bins){
            val prevRemCap= prevBin.remCap
            if(prevRemCap >= cost){
                bin= prevBin
                break
            }
        }

        if(bin == null){
            bin= Bin(mutableListOf(), maxCap)
            bins += bin
        }

        (bin.content as MutableList).add(cost)
        bin.remCap -= cost
    }

    return bins
}
fun binPacking_firstFitDecreasing(costs: Array<Int>, maxCap: Int, doPreprocess: Boolean = true): List<Bin> =
    binPacking_firstFit(costs.sortedArrayDescending(), maxCap, doPreprocess)


fun binPacking_bestFit(costs: Array<Int>, maxCap: Int, doPreprocess: Boolean = true): List<Bin> {
//    val costs= costs.sortedArray()
    val costs= if(doPreprocess) binPacking_preprocess(costs, maxCap) else costs
    val bins= mutableListOf<Bin>()

    for(cost in costs){
        if(cost > maxCap)
            continue
        var leastRemCap= Int.MAX_VALUE
        var leastRemCapBin: Bin?= null
        for(prevBin in bins){
            val prevRemCap= prevBin.remCap
            if(prevRemCap in cost until leastRemCap){
                leastRemCapBin= prevBin
                leastRemCap= prevRemCap
                if(prevRemCap == cost)
                    break
            }
        }
        if(leastRemCapBin == null){
            leastRemCapBin= Bin(mutableListOf(), maxCap)
            bins += leastRemCapBin
        }

        (leastRemCapBin.content as MutableList).add(cost)
        leastRemCapBin.remCap -= cost
    }

    return bins
}
fun binPacking_bestFitDecreasing(costs: Array<Int>, maxCap: Int, doPreprocess: Boolean = true): List<Bin> =
    binPacking_bestFit(costs.sortedArrayDescending(), maxCap, doPreprocess)


fun List<Bin>.cumulativeRemCap(): Int = scan(0) { acc, bin -> acc + bin.remCap }.last()