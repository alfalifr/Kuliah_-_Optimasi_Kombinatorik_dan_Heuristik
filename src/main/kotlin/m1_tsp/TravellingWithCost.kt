package m1_tsp

data class TravelDest(val score: Int, val cost: Int)

fun travelWithCost_firstCome(dests: List<TravelDest>, budget: Int): Pair<List<TravelDest>, Int> {
    var totalCost= 0
    var totalScore= 0
    val visited= mutableListOf<TravelDest>()

    for(dest in dests){
        totalCost += dest.cost
        if(totalCost > budget)
            break
        visited += dest
        totalScore += dest.score
    }

    return Pair(visited, totalScore)
}

fun travelWithCost_highestScoreFirst(dests: List<TravelDest>, budget: Int): Pair<List<TravelDest>, Int> =
    travelWithCost_firstCome(dests.sortedByDescending { it.score }, budget)

fun travelWithCost_leastScoreFirst(dests: List<TravelDest>, budget: Int): Pair<List<TravelDest>, Int> =
    travelWithCost_firstCome(dests.sortedBy { it.score }, budget)

fun travelWithCost_highestScoreCostRatioFirst(dests: List<TravelDest>, budget: Int): Pair<List<TravelDest>, Int> =
    travelWithCost_firstCome(dests.sortedByDescending { it.score / it.cost.toDouble() }, budget)