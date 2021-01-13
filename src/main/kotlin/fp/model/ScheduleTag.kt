package fp.model

import fp.algo.construct.Construct
import fp.algo.optimize.Optimize

data class ScheduleTag(
    var construct: Construct = Construct.UNSORTED,
    var optimization: Optimize = Optimize.NOT_YET,
    var fileName: String?= null
){
    fun miniString()= "$fileName - $construct - $optimization"
}