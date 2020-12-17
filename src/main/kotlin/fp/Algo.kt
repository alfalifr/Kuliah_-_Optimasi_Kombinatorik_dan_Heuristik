package fp

import fp.Config.COURSE_INDEX_OFFSET

enum class Algo(val code: String) {
    UNSORTED("n0"),
    LARGEST_DEGREE_FIRST("LaD"),
    LARGEST_WEIGHTED_DEGREE_FIRST("LaWD"),
    LARGEST_ENROLLMENT_FIRST("LaE"),
    LARGEST_SATURATED_DEGREE_FIRST("LaSD"),
    LARGEST_SATURATED_WEIGHTED_DEGREE_FIRST("LaSWD"),
    LARGEST_SATURATED_ENROLLMENT_FIRST("LaSE"),
    LARGEST_SATURATED_DEGREE_FIRST_ORDERED("LaSDO"),
    LARGEST_SATURATED_WEIGHTED_DEGREE_FIRST_ORDERED("LaSWDO"),
    LARGEST_SATURATED_ENROLLMENT_FIRST_ORDERED("LaSEO"),
    ;

    override fun toString(): String = name //.replace("_", " ") //.capitalize()

    companion object {

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
        ).apply { tag.algo = LARGEST_DEGREE_FIRST }

        /**
         * [courses] merupakan List dg isi [Course.studentCount] dan [Course.degree] yang udah diisikan.
         */
        fun largestWeightedDegreeFirst(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = assignToTimeslot(
            courses.sortedByDescending { it.degree * it.studentCount },
            adjacencyMatrix,
            availableTimeslot
        ).apply { tag.algo = LARGEST_WEIGHTED_DEGREE_FIRST }

        /**
         * [courses] merupakan List dg isi [Course.studentCount] yang udah diisikan.
         */
        fun largestEnrollmentFirst(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = assignToTimeslot(
            courses.sortedByDescending { it.studentCount },
            adjacencyMatrix,
            availableTimeslot
        ).apply { tag.algo = LARGEST_ENROLLMENT_FIRST }

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

            if(availableTimeslot == null || availableTimeslot.isEmpty()) {
                timeslots += Timeslot(0)

                c1@ for(c1 in courses){
                    var alreadyAssigned= false
                    t1@ for(t1 in timeslots){
                        val assignedCourses= schedule[t1]
                        if(assignedCourses != null){
                            var isConflicting= false
                            c2@ for(c2 in assignedCourses){
                                if(adjacencyMatrix[c1.id - COURSE_INDEX_OFFSET][c2.id - COURSE_INDEX_OFFSET] > 0){
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
                    //Jika [availableTimeslot] == `null`, artinya tidak ada batas jml timeslot,
                    // maka buat timeslot baru.
                    if(!alreadyAssigned){
                        val newT= Timeslot(timeslots.size) //+1
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
                                if(adjacencyMatrix[c1.id - COURSE_INDEX_OFFSET][c2.id - COURSE_INDEX_OFFSET] > 0){
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


        fun largestSaturationDegreeFirstOrdered(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = largestSaturationDegreeFirst(
            courses.sortedByDescending { it.degree },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.algo= LARGEST_SATURATED_DEGREE_FIRST_ORDERED }

        fun largestSaturationDegreeFirst(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = largestSaturationFirst(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            acc.degree < c.degree
        }.apply { tag.algo= LARGEST_SATURATED_DEGREE_FIRST }


        fun largestSaturationWeightedDegreeFirstOrdered(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = largestSaturationWeightedDegreeFirst(
            courses.sortedByDescending { it.degree * it.studentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.algo= LARGEST_SATURATED_WEIGHTED_DEGREE_FIRST_ORDERED }

        fun largestSaturationWeightedDegreeFirst(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = largestSaturationFirst(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            acc.degree * acc.studentCount < c.degree * c.studentCount
        }.apply { tag.algo= LARGEST_SATURATED_WEIGHTED_DEGREE_FIRST }


        fun largestSaturationEnrollmentFirstOrdered(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = largestSaturationEnrollmentFirst(
            courses.sortedByDescending { it.studentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.algo= LARGEST_SATURATED_ENROLLMENT_FIRST_ORDERED }

        fun largestSaturationEnrollmentFirst(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = largestSaturationFirst(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            acc.studentCount < c.studentCount
        }.apply { tag.algo= LARGEST_SATURATED_ENROLLMENT_FIRST }

        /**
         * [courses] merupakan List dg isi [Course.studentCount] dan [Course.degree] yang udah diisikan.
         */
        fun largestSaturationFirst(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null,
            swapFun: (acc: Course, c: Course) -> Boolean
        ): Schedule {
            val schedule= Schedule()

            val visited= BooleanArray(courses.size) //indexnya sama menunjukan index `courses`, bkn id coursenya
            val saturation= IntArray(courses.size) //indexnya sama menunjukan index `courses`, bkn id coursenya

            val timeslots= availableTimeslot?.toMutableList() ?: mutableListOf()
/*
                var iMaxDegree= 0
                var cMaxDegree= courses.first()
                for(i in 1 until courses.size){
                    val c= courses[i]
                    if(c.degree > cMaxDegree.degree){
                        cMaxDegree= c
                        iMaxDegree= i
                    }
                }

                schedule[timeslots.first()] = mutableListOf(cMaxDegree)

                for(i in courses.indices)
                    if(adjacencyMatrix[iMaxDegree][i] > 0)
                        saturation[i]++
 */
            while(true){
                // 1. Max Satur
                var maxSatur= 0
                val maxSaturIndices= mutableListOf<Int>()
                for(i in courses.indices){
//                    val i= c.id - COURSE_INDEX_OFFSET
//                    prine("Saturated deg i= $i visited[i]= ${visited[i]}")
                    if(!visited[i]){
                        val satur= saturation[courses[i].id - COURSE_INDEX_OFFSET]
                        if(satur > maxSatur){
                            maxSatur= satur
                            maxSaturIndices.apply {
                                clear()
                                add(i)
                            }
                        } else if(satur == maxSatur) {
                            maxSaturIndices += i
                        }
                    }
                }

//                prine("Saturated Degree maxSatur= $maxSatur maxSaturIndices= $maxSaturIndices timeslots= $timeslots")

                // 2. Tie Breaking //Max (Weighted) Degree
//                    maxSaturIndices.fastSortBy(Order.DESC) { courses[it].degree }
//                var c1: Course //= -1
                if(maxSaturIndices.isEmpty()){
                    break
                } else {
                    var i1= maxSaturIndices.first()
                    var c1= courses[i1]
                    for(i in 1 until maxSaturIndices.size){
                        val iSatur= maxSaturIndices[i]
                        val c= courses[iSatur]
                        if(swapFun(c1, c)){
                            c1= c
                            i1= iSatur
                        }
                    }

//                    prine("Saturated Deg maxSatur= $maxSatur i1= $i1")

//                    val i1= c1.id - COURSE_INDEX_OFFSET
                    t1@ for(t1 in timeslots){
                        val assignedCourses= schedule[t1]
                        if(assignedCourses != null){
                            var isConflicting= false
                            c2@ for(c2 in assignedCourses){
                                if(adjacencyMatrix[c1.id - COURSE_INDEX_OFFSET][c2.id - COURSE_INDEX_OFFSET] > 0){
                                    isConflicting= true
                                    break@c2
                                }
                            }
                            if(!isConflicting){
                                assignedCourses += c1
                                visited[i1]= true
                                break@t1
                            }
                        } else {
                            schedule[t1] = mutableListOf(c1)
                            visited[i1]= true
                            break@t1
                        }
                    }

                    if(!visited[i1]){
                        if((availableTimeslot == null || availableTimeslot.isEmpty())){
                            val newT= Timeslot(timeslots.size) //+1
                            timeslots += newT
                            schedule[newT]= mutableListOf(c1)
                        }
                        visited[i1]= true
                    }

                    for(c2 in courses){
                        val i= c2.id - COURSE_INDEX_OFFSET
                        if(adjacencyMatrix[i1][i] > 0)
                            saturation[i]++
                    }
                }
            }
            return schedule //.also { prine("saturation res= $it") }
        }
    }
}