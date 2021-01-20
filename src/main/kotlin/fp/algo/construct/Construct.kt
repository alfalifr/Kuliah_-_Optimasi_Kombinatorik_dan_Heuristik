package fp.algo.construct

import fp.Config.COURSE_INDEX_OFFSET
import fp.model.Course
import fp.model.Schedule
import fp.model.Timeslot
import sidev.lib.collection.fastSortedWith
import sidev.lib.exception.IllegalArgExc

//import fp.Algo.Component

/**
 * Enum untuk algoritma construct initial solution menggunakan graph coloring dengan
 * berbagai metode pengurutan assign ke timeslot.
 */
enum class Construct(val code: String, vararg val component: Component) {
    UNSORTED("_0"),
    LaD("LaD", Component.DEGREE),
    LaE("LaE", Component.ENROLLMENT),
    X_LaCE("LaCE", Component.COMMON_ENROLLMENT),
    LaWD("LaWD", Component.WEIGHTED_DEGREE),
    X_LaWCD("LaWCD", Component.WEIGHTED_COMMON_DEGREE),
    X_LaWCxD("LaWCxD", Component.WEIGHTED_COMPLEX_DEGREE),
    LaS_D("LaS", Component.SATURATION),
    LaWD_E("LaWD_E", Component.WEIGHTED_DEGREE, Component.ENROLLMENT),
    LaE_WD("LaE_WD", Component.ENROLLMENT, Component.WEIGHTED_DEGREE),
    LaS_WD("LaS_WD", Component.SATURATION, Component.WEIGHTED_DEGREE),
    LaS_WD_E("LaS_WD_E", Component.SATURATION, Component.WEIGHTED_DEGREE, Component.ENROLLMENT),
    LaS_E_WD("LaS_E_WD", Component.SATURATION, Component.ENROLLMENT, Component.WEIGHTED_DEGREE),
    LaS_E("LaS_E", Component.SATURATION, Component.ENROLLMENT),
    X_LaS_CE("LaS_CE", Component.SATURATION, Component.COMMON_ENROLLMENT),
    X_LaS_WCD("LaS_WCD", Component.SATURATION, Component.WEIGHTED_COMMON_DEGREE),
    X_LaS_WCD_D("LaS_WCD_D", Component.SATURATION, Component.WEIGHTED_COMMON_DEGREE, Component.DEGREE),
    X_LaS_WCD_CE("LaS_WCD_CE", Component.SATURATION, Component.WEIGHTED_COMMON_DEGREE, Component.COMMON_ENROLLMENT),
    LaD_S_D("LaD_S_D", Component.DEGREE, Component.SATURATION, Component.DEGREE),
    LaWD_S_WD("LaWD_S_WD", Component.WEIGHTED_DEGREE, Component.SATURATION, Component.WEIGHTED_DEGREE),
    LaE_S_E("LaE_S_E", Component.ENROLLMENT, Component.SATURATION, Component.ENROLLMENT),
    LaWD_E_S_WD_E("LaWD_E_S_WD_E",
        Component.WEIGHTED_DEGREE,
        Component.ENROLLMENT,
        Component.SATURATION,
        Component.WEIGHTED_DEGREE,
        Component.ENROLLMENT
    ),
    LaE_WD_S_E_WD("LaE_WD_S_E_WD",
        Component.ENROLLMENT,
        Component.WEIGHTED_DEGREE,
        Component.SATURATION,
        Component.ENROLLMENT,
        Component.WEIGHTED_DEGREE
    ),
    X_LaCE_S_CE("LaCE_S_CE", Component.COMMON_ENROLLMENT, Component.SATURATION, Component.COMMON_ENROLLMENT),
    X_LaWCD_S_WCD("LaWCD_S_WCD",
        Component.WEIGHTED_COMMON_DEGREE,
        Component.SATURATION,
        Component.WEIGHTED_COMMON_DEGREE
    ),
    X_LaWCD_D_S_WCD_D("LaWCD_D_S_WCD_D",
        Component.WEIGHTED_COMMON_DEGREE,
        Component.DEGREE,
        Component.SATURATION,
        Component.WEIGHTED_COMMON_DEGREE,
        Component.DEGREE
    ),
    X_LaWCD_CE_S_WCD_CE("LaWCD_CE_S_WCD_CE",
        Component.WEIGHTED_COMMON_DEGREE,
        Component.COMMON_ENROLLMENT,
        Component.SATURATION,
        Component.WEIGHTED_COMMON_DEGREE,
        Component.COMMON_ENROLLMENT
    ),
    X_LaWCD_S_CE("LaWCD_S_CE", Component.WEIGHTED_COMMON_DEGREE, Component.SATURATION, Component.COMMON_ENROLLMENT),
    X_LaWCxD_S_CE("LaWCxD_S_CE", Component.WEIGHTED_COMPLEX_DEGREE, Component.SATURATION, Component.COMMON_ENROLLMENT),
    X_LaWCxD_S_D("LaWCxD_S_D", Component.WEIGHTED_COMPLEX_DEGREE, Component.SATURATION, Component.DEGREE),
    X_LaWCxD_S_WCD_CE("LaWCxD_S_WCD_CE",
        Component.WEIGHTED_COMPLEX_DEGREE,
        Component.SATURATION,
        Component.WEIGHTED_COMMON_DEGREE,
        Component.COMMON_ENROLLMENT
    ),
    X_LaWCxD_S_WCD_D("LaWCxD_S_WCD_D",
        Component.WEIGHTED_COMPLEX_DEGREE,
        Component.SATURATION,
        Component.WEIGHTED_COMMON_DEGREE,
        Component.DEGREE
    ),
    X_LaWCD_CE_S_CE("LaWCD_CE_S_CE",
        Component.WEIGHTED_COMMON_DEGREE,
        Component.COMMON_ENROLLMENT,
        Component.SATURATION,
        Component.COMMON_ENROLLMENT
    ),
    ;

    enum class Component(val code: String) {
        DEGREE("D"), ENROLLMENT("E"),

        /**
         * Degree * studentCount
         */
        WEIGHTED_DEGREE("WD"),

        /**
         * conflictingStudentCount
         */
        COMMON_ENROLLMENT("CE"),

        /**
         * Degree * conflictingStudentCount
         */
        WEIGHTED_COMMON_DEGREE("WCD"),

        /**
         * Degree * conflictingStudentCount * studentCount
         */
        WEIGHTED_COMPLEX_DEGREE("WCxD"),

        SATURATION("S")
    }

    override fun toString(): String = name //.replace("_", " ") //.capitalize()

    companion object {
        operator fun get(code: String): Construct = enumValues<Construct>().find {
            it.code == code
        } ?: throw IllegalArgExc(
            paramExcepted = arrayOf("code"),
            detailMsg = "Enum `Construct` tidak punya entry dengan `code` ($code)"
        )

        /**
         * [courses] merupakan List dg isi [Course.degree] yang udah diisikan.
         */
        fun laD(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = assignToTimeslot(
            courses.sortedByDescending { it.degree },
            adjacencyMatrix,
            availableTimeslot
        ).apply { tag.construct = LaD }

        /**
         * [courses] merupakan List dg isi [Course.studentCount] dan [Course.degree] yang udah diisikan.
         */
        fun laWD(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = assignToTimeslot(
            courses.sortedByDescending { it.degree * it.studentCount },
            adjacencyMatrix,
            availableTimeslot
        ).apply { tag.construct = LaWD }

        /**
         * [courses] merupakan List dg isi [Course.studentCount] dan [Course.degree] yang udah diisikan.
         */
        fun laWCD(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = assignToTimeslot(
            courses.sortedByDescending { it.degree * it.conflictingStudentCount },
            adjacencyMatrix,
            availableTimeslot
        ).apply { tag.construct = X_LaWCD }

        /**
         * [courses] merupakan List dg isi [Course.studentCount] dan [Course.degree] yang udah diisikan.
         */
        fun laWCxD(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = assignToTimeslot(
            courses.sortedByDescending { it.degree * it.conflictingStudentCount * it.studentCount },
            adjacencyMatrix,
            availableTimeslot
        ).apply { tag.construct = X_LaWCxD }

        /**
         * [courses] merupakan List dg isi [Course.studentCount] yang udah diisikan.
         */
        fun laE(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = assignToTimeslot(
            courses.sortedByDescending { it.studentCount },
            adjacencyMatrix,
            availableTimeslot
        ).apply { tag.construct = LaE }

        /**
         * [courses] merupakan List dg isi [Course.studentCount] yang udah diisikan.
         */
        fun laCE(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = assignToTimeslot(
            courses.sortedByDescending { it.conflictingStudentCount },
            adjacencyMatrix,
            availableTimeslot
        ).apply { tag.construct = X_LaCE }

        /**
         * [courses] merupakan List dg isi [Course.studentCount] dan [Course.degree] yang udah diisikan.
         */
        fun laWD_E(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = assignToTimeslot(
            courses.fastSortedWith { n1, n2 ->
                val wd1= n1.degree * n1.studentCount
                val wd2= n2.degree * n2.studentCount
                if(wd1 != wd2) wd1.compareTo(wd2)
                else n1.studentCount.compareTo(n2.studentCount)
            },
            adjacencyMatrix,
            availableTimeslot
        ).apply { tag.construct = LaWD_E }

        /**
         * [courses] merupakan List dg isi [Course.studentCount] dan [Course.degree] yang udah diisikan.
         */
        fun laE_WD(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = assignToTimeslot(
            courses.fastSortedWith { n1, n2 ->
                val wd1= n1.degree * n1.studentCount
                val wd2= n2.degree * n2.studentCount
                if(n1.studentCount != n2.studentCount) n1.studentCount.compareTo(n2.studentCount)
                else wd1.compareTo(wd2)
            },
            adjacencyMatrix,
            availableTimeslot
        ).apply { tag.construct = LaE_WD }

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


        fun laD_S_D(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_D(
            courses.sortedByDescending { it.degree },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= LaD_S_D }

        fun laS_D(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            acc.degree < c.degree
        }.apply { tag.construct= LaS_D }


        fun laWD_S_WD(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_WD(
            courses.sortedByDescending { it.degree * it.studentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= LaWD_S_WD }

        fun laS_WD(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            acc.degree * acc.studentCount < c.degree * c.studentCount
        }.apply { tag.construct= LaS_WD }


        fun laWD_E_S_WD_E(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_WD_E(
            courses.fastSortedWith { n1, n2 ->
                val wd1= n1.degree * n1.studentCount
                val wd2= n2.degree * n2.studentCount
                if(wd1 != wd2) wd1.compareTo(wd2)
                else n1.studentCount.compareTo(n2.studentCount)
            }, //.sortedByDescending { it.degree * it.studentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= LaWD_E_S_WD_E }

        fun laS_WD_E(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            val wdAcc= acc.degree * acc.studentCount
            val wdC= c.degree * c.studentCount
            wdAcc < wdC || acc.studentCount < c.studentCount
        }.apply { tag.construct= LaS_WD_E }


        fun laE_WD_S_E_WD(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_E_WD(
            courses.fastSortedWith { n1, n2 ->
                val wd1= n1.degree * n1.studentCount
                val wd2= n2.degree * n2.studentCount
                if(n1.studentCount != n2.studentCount) n1.studentCount.compareTo(n2.studentCount)
                else wd1.compareTo(wd2)
            }, //.sortedByDescending { it.degree * it.studentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= LaE_WD_S_E_WD }

        fun laS_E_WD(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            val wdAcc= acc.degree * acc.studentCount
            val wdC= c.degree * c.studentCount
            acc.studentCount < c.studentCount || wdAcc < wdC
        }.apply { tag.construct= LaS_E_WD }


        fun laE_S_E(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_E(
            courses.sortedByDescending { it.studentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= LaE_S_E }

        fun laS_E(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            acc.studentCount < c.studentCount
        }.apply { tag.construct= LaS_E }


        fun laCE_S_CE(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_CE(
            courses.sortedByDescending { it.conflictingStudentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= X_LaCE_S_CE }

        fun laS_CE(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            acc.conflictingStudentCount < c.conflictingStudentCount
        }.apply { tag.construct= X_LaS_CE }


        fun laWCD_S_WCD(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_WCD(
            courses.sortedByDescending { it.degree * it.conflictingStudentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= X_LaWCD_S_WCD }

        fun laWCD_S_CE(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_CE(
            courses.sortedByDescending { it.degree * it.conflictingStudentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= X_LaWCD_S_CE }

        fun laWCxD_S_CE(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_CE(
            courses.sortedByDescending { it.degree * it.conflictingStudentCount * it.studentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= X_LaWCxD_S_CE }

        fun laWCxD_S_D(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_D(
            courses.sortedByDescending { it.degree * it.conflictingStudentCount * it.studentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= X_LaWCxD_S_D }

        fun laWCxD_S_WCD_CE(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_WCD_CE(
            courses.sortedByDescending { it.degree * it.conflictingStudentCount * it.studentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= X_LaWCxD_S_WCD_CE }

        fun laWCxD_S_WCD_D(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_WCD_D(
            courses.sortedByDescending { it.degree * it.conflictingStudentCount * it.studentCount },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= X_LaWCxD_S_WCD_D }

        fun laWCD_CE_S_CE(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_CE(
            courses.fastSortedWith { n1, n2 ->
                val wcd1= n1.degree * n1.conflictingStudentCount
                val wcd2= n2.degree * n2.conflictingStudentCount
                if(wcd1 != wcd2) wcd1.compareTo(wcd2)
                else n1.conflictingStudentCount.compareTo(n2.conflictingStudentCount)
            },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= X_LaWCD_CE_S_CE }

        fun laS_WCD(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            acc.degree * acc.conflictingStudentCount < c.degree * c.conflictingStudentCount
        }.apply { tag.construct= X_LaS_WCD }


        fun laWCD_D_S_WCD_D(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_WCD_D(
            courses.fastSortedWith { n1, n2 ->
                val wcd1= n1.degree * n1.conflictingStudentCount
                val wcd2= n2.degree * n2.conflictingStudentCount
                if(wcd1 != wcd2) wcd1.compareTo(wcd2)
                else n1.degree.compareTo(n2.degree)
            },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= X_LaWCD_D_S_WCD_D }

        fun laS_WCD_D(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            val wcd1= acc.degree * acc.conflictingStudentCount
            val wcd2= c.degree * c.conflictingStudentCount
            wcd1 < wcd2 || acc.degree < c.degree
        }.apply { tag.construct= X_LaS_WCD_D }


        fun laWCD_CE_S_WCD_CE(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS_WCD_CE(
            courses.fastSortedWith { n1, n2 ->
                val wcd1= n1.degree * n1.conflictingStudentCount
                val wcd2= n2.degree * n2.conflictingStudentCount
                if(wcd1 != wcd2) wcd1.compareTo(wcd2)
                else n1.conflictingStudentCount.compareTo(n2.conflictingStudentCount)
            },
            adjacencyMatrix, availableTimeslot
        ).apply { tag.construct= X_LaWCD_CE_S_WCD_CE }

        fun laS_WCD_CE(
            courses: List<Course>,
            adjacencyMatrix: Array<IntArray>,
            availableTimeslot: List<Timeslot>? = null
        ): Schedule = laS(courses, adjacencyMatrix, availableTimeslot) { acc, c ->
            val wcd1= acc.degree * acc.conflictingStudentCount
            val wcd2= c.degree * c.conflictingStudentCount
            wcd1 < wcd2 || acc.conflictingStudentCount < c.conflictingStudentCount
        }.apply { tag.construct= X_LaS_WCD_CE }

        /**
         * [courses] merupakan List dg isi [Course.studentCount] dan [Course.degree] yang udah diisikan.
         */
        fun laS(
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