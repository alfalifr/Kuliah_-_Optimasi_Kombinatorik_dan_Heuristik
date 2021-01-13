package fp.model

data class ScheduleConflict(val sc: Schedule, val conflicts: List<Pair<Timeslot, Int>>){
    override fun toString(): String = sc.miniString() +"\nConflicts= \n" +conflicts.joinToString { it.first.toString() +": " +it.second.toString() }
}