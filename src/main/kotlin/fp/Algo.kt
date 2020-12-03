package fp

object Algo {
    /**
     * [courses] merupakan List dg isi [Course.degree] yang udah diisikan.
     */
    fun largestDegreeFirst(
        courses: List<Course>,
        adjacencyMatrix: Array<IntArray>,
        availableTimeslot: List<Timeslot>? = null
    ): Schedule = assignToTimeslot(
        courses.sortedByDescending { it.degree },
        adjacencyMatrix,
        availableTimeslot
    )

    /**
     * [courses] merupakan List dg isi [Course.studentCount] yang udah diisikan.
     */
    fun largestStudentCountFirst(
        courses: List<Course>,
        adjacencyMatrix: Array<IntArray>,
        availableTimeslot: List<Timeslot>? = null
    ): Schedule = assignToTimeslot(
        courses.sortedByDescending { it.studentCount },
        adjacencyMatrix,
        availableTimeslot
    )

    /**
     * Memasangkan [courses] ke [availableTimeslot] jika ada.
     * Jika [availableTimeslot] == `null`, artinya [courses] dapat di-assign ke [Timeslot] sebanyak
     * mungkin yang dapat menampung semua [courses] tanpa melanggar [adjacencyMatrix].
     *
     * Fungsi ini tidak mengurutkan [courses] dan tidak memproses [adjacencyMatrix].
     * Urutan [courses] sesuai dg index baris pada [adjacencyMatrix].
     * [adjacencyMatrix] harus memiliki ukuran yg sama, yaitu n x n.
     *
     * 3 Des 2020: Tidak ada pengurangan domain.
     */
    fun assignToTimeslot(
        courses: List<Course>,
        adjacencyMatrix: Array<IntArray>,
        availableTimeslot: List<Timeslot>? = null
    ): Schedule {
        val timeslots= availableTimeslot?.toMutableList() ?: mutableListOf()
        val schedule= Schedule()

        if(availableTimeslot == null) {
            timeslots += Timeslot(1)

            c1@ for(c1 in courses){
                var alreadyAssigned= false
                t1@ for(t1 in timeslots){
                    val assignedCourses= schedule[t1]
                    if(assignedCourses != null){
                        var isConflicting= false
                        c2@ for(c2 in assignedCourses){
                            if(adjacencyMatrix[c1.id][c2.id] > 0){
                                isConflicting= true
                                break@c2
                            }
                        }
                        if(!isConflicting){
//                            prine("Assigned c= $c1 timeslot= $t1")
                            assignedCourses += c1
                            alreadyAssigned= true
                            break@t1
                        }
                    } else {
                        schedule[t1]= mutableListOf(c1)
                        alreadyAssigned= true
                        break@t1
                    }
                }
                if(!alreadyAssigned){
                    val newT= Timeslot(timeslots.size +1)
                    timeslots += newT
                    schedule[newT]= mutableListOf(c1)
                }
            }
        } else {
            c1@ for(c1 in courses){
                t1@ for(t1 in timeslots){
                    val assignedCourses= schedule[t1]
                    if(assignedCourses != null){
                        var isConflicting= false
                        c2@ for(c2 in assignedCourses){
                            if(adjacencyMatrix[c1.id][c2.id] > 0){
                                isConflicting= true
                                break@c2
                            }
                        }
                        if(!isConflicting){
//                            prine("Assigned c= $c1 timeslot= $t1")
                            assignedCourses += c1
                            break@t1
                        }
                    } else {
                        schedule[t1]= mutableListOf(c1)
                        break@t1
                    }
                }
            }
        }
        return schedule
    }
}