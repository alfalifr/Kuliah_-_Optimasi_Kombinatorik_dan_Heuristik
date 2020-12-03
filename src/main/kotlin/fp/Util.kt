package fp

import fp.Config.COURSE_INDEX_OFFSET
import sidev.lib.number.pow
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

object Util {
/*
=========== Ketentuan ========= :
1. Format data:
  A. .crs
    i.   Berisi data course.
    ii.  Berisi 2 kolom: kolom 1 berisi id, kolom 2 berisi jml student yg ngambil.
    iii. Id course dimulai dari 1.
  B. .stu
    i.   Berisi data student.
    ii.  Berisi banyak kolom (jml tidak dapat dipastikan).
    iii. Tiap baris merepresentasikan tiap student.
    iv.  Tiap kolom per baris merepresentasikan course yg diambil student tersebut.
  C. .sol
    i.   Berisi data penyelesaian permsalahan timetabling.
    ii.  Berisi 2 kolom: kolom 1 berisi id course, kolom 2 berisi no timeslot.
*/
    const val FILE_EXTENSION_STUDENT = ".stu"
    const val FILE_EXTENSION_COURSE = ".crs"
    /**
     * Return table yang direpresentasikan sbg nested list dg ukuran List[i][u].
     * [i] merupakan jml baris, sedangkan [u] jml kolom tiap baris.
     */
    fun readIntTableFromFile(dir: String, extension: String, delimiter: String = " "): List<List<Int>> {
        val file= File(dir)
        if(!file.exists() || file.extension != extension.removePrefix("."))
            throw IllegalArgumentException()

        val res= mutableListOf<List<Int>>()
        val inn= Scanner(System.`in`)

        while(inn.hasNextLine()){
            val line= inn.nextLine()
            res += line.split(delimiter).map { it.toInt() }
        }
        return res
    }
    /**
     * Return 2 kolom list, kolom 1 berisi id course, kolom 2 berisi jml mhs yg ngambil course itu.
     */
    fun readCourse(dir: String): List<List<Int>> = readIntTableFromFile(
        if(dir.endsWith(FILE_EXTENSION_COURSE)) dir else "$dir$FILE_EXTENSION_COURSE", FILE_EXTENSION_COURSE
    )

    /**
     * Return nested list yg berisi kolom ganda. Tiap baris merupakan tiap mhs.
     * Tiap kolom adalah id course yg diambilnya.
     */
    fun readStudent(dir: String): List<List<Int>> = readIntTableFromFile(
        if(dir.endsWith(FILE_EXTENSION_STUDENT)) dir else "$dir$FILE_EXTENSION_STUDENT", FILE_EXTENSION_STUDENT
    )

    fun toListOfCourses(courseTable: List<List<Int>>, degreeList: List<Int>?= null): List<Course> =
        if(degreeList == null) courseTable.mapIndexed { i, list -> Course(list.first(), list[1]) }
        else courseTable.mapIndexed { i, list -> Course(list.first(), list[1], degreeList[i]) }

    fun toListOfStudents(studentTable: List<List<Int>>): List<Student> =
        studentTable.mapIndexed { i, list -> Student(i, list) }

    /**
     * Menghitung matrix yg berisi jumlah mhs yg mengambil 2 atau lebih course pada course C[i][j].
     * Fungsi ini sama dg [createCourseAdjacencyMatrix_Raw], namun dg parameter [List<Student>].
     * Hasil return adalah matrix yang hanya berisi int.
     */
    fun createCourseAdjacencyMatrix(courseCount: Int, studList: List<Student>): Array<IntArray> {
        val adjacencyMatrix= Array(courseCount){ IntArray(courseCount) }
        studList.forEachIndexed { i, student ->
            val row= student.courses
            if(row.size > 1){
                for((u, fromCourseId_) in row.withIndex()){
                    val fromCourseId= fromCourseId_ - COURSE_INDEX_OFFSET //Karena courseId dimulai dari 1.
                    for(o in u+1 until row.size){
                        val toCourseId= row[o] - COURSE_INDEX_OFFSET
                        adjacencyMatrix[fromCourseId][toCourseId]++
                        adjacencyMatrix[toCourseId][fromCourseId]++
                    }
                }
            }
        }
        return adjacencyMatrix
    }

    /**
     * Menghitung matrix yg berisi jumlah mhs yg mengambil 2 atau lebih course pada course C[i][j].
     * Fungsi ini sama dg [createCourseAdjacencyMatrix_Raw], namun dg parameter [List<List<Int>].
     * Hasil return adalah matrix yang hanya berisi int.
     */
    fun createCourseAdjacencyMatrix_Raw(courseCount: Int, studCourseTable: List<List<Int>>): Array<IntArray> {
        val adjacencyMatrix= Array(courseCount){ IntArray(courseCount) }
        studCourseTable.forEachIndexed { i, row ->
            if(row.size > 1){
                for((u, fromCourseId_) in row.withIndex()){
                    val fromCourseId= fromCourseId_ - COURSE_INDEX_OFFSET //Karena courseId dimulai dari 1.
                    for(o in u+1 until row.size){
                        val toCourseId= row[o] - COURSE_INDEX_OFFSET
                        adjacencyMatrix[fromCourseId][toCourseId]++
                        adjacencyMatrix[toCourseId][fromCourseId]++
                    }
                }
            }
        }
        return adjacencyMatrix
    }

    /**
     * Menghitung matrix yg berisi course yang tidak boleh dijadwalkan bersamaan karena
     * ada mhs yg mengambil 2 atau lebih course pada course C[i][j].
     *
     * Hasil return adalah matrix yang hanya berisi 0 / 1.
     */
    fun createCourseConflictMatrix(courseCount: Int, studCourseTable: List<List<Int>>): Array<IntArray> =
        createCourseAdjacencyMatrix_Raw(courseCount, studCourseTable).copyOf().let {
            for(i in it.indices){
                for(u in i+1 until it.size){
                    if(it[i][u] > 0)
                        it[i][u]= 1
                }
            }
            it
        }

    /**
     * Menghitung degree dari tiap course, yaitu banyaknya course lain yg tidak boleh dijadwalkan
     * bersamaan dg course tersebut dikarenakan ada mhs yg sama yg ngambil.
     */
    fun getDegreeList(courseAdjacencyMatrix: Array<IntArray>): List<Int> {
        val res= ArrayList<Int>(courseAdjacencyMatrix.size)

        courseAdjacencyMatrix.forEachIndexed { i, row ->
            res += 0
            row.forEachIndexed { u, node ->
                if(node > 0)
                    res[i]++
            }
        }
        return res
    }

    //w0=16, w1=8, w2=4, w3=2 and w4=1 -> Berdasarkan di FP.
    //Penalti msh berdasarkan rumus di UAS.
    /**
     * Menghitung penalty yang dihasilkan oleh [schedule].
     * Penalty yang bagus adalah penalty yang lebih rendah.
     */
    fun getPenalty(schedule: Schedule, courseAdjacencyMatrix: Array<IntArray>, studentCount: Int): Double {
        var sum= 0.0
        val weightRange= 1.0 .. 5.0
        for((i, row) in courseAdjacencyMatrix.withIndex()){
            val t1= schedule.getCourseTimeslot(i)!!
            for(u in i+1 until row.size){
                val t2= schedule.getCourseTimeslot(u)!!
                val conflict= courseAdjacencyMatrix[i][u].toDouble()
                val timeslotDistance= (t1.no - t2.no).absoluteValue.toDouble()

                val weight= if(timeslotDistance in weightRange){
                    (2 pow (5 - timeslotDistance)).toDouble()
                } else 0.0

                sum += conflict * weight
            }
        }
        return sum / studentCount
    }
}