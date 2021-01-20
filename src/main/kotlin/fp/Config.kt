package fp

/**
 * Berisi konstan terkait pengaturan yg ada di FP, seperti direktori, nama file, ekstensi, dan nilai default.
 */
object Config {
    const val COURSE_INDEX_OFFSET= 1
    const val DATASET_DIR = "D:\\DataCloud\\OneDrive\\OneDrive - Institut Teknologi Sepuluh Nopember\\Kuliah\\SMT 7\\OKH-A\\---FP---\\OKH - Dataset - Toronto" //"D:\\DataCloud\\OneDrive\\OneDrive - Institut Teknologi Sepuluh Nopember\\Kuliah\\SMT 7\\OKH-A\\M10\\OKH - Dataset - Toronto"
    const val SOLUTION_DIR = "$DATASET_DIR\\Solusi"
    const val DEFAULT_ITERATIONS= 1_000
    val fileNames: Array<String> = arrayOf(
        "car-f-92", "car-s-91", "ear-f-83", "hec-s-92", "kfu-s-93", //4
        "lse-f-91", "pur-s-93", "rye-s-93", "sta-f-83", "tre-s-92", //9
        "uta-s-92", "ute-s-92", "yor-f-83", //12
    )
    val maxTimeslot: IntArray = intArrayOf(
        32, 35, 24, 18, 20,
        18, 42, 23, 13 /*35 ??*/, 23,
        35, 10, 21,
    )

    const val FILE_EXTENSION_STUDENT = ".stu"
    const val FILE_EXTENSION_COURSE = ".crs"
    const val FILE_EXTENSION_SOLUTION = ".sol"
    const val FILE_EXTENSION_RES = ".res"
    const val FILE_EXTENSION_EXM = ".exm"
    const val FILE_EXTENSION_SLN = ".sln"
    /**
     * Untuk jumlah student. Ekstensi ini merupakan tambahan dari programmer.
     */
    const val FILE_EXTENSION_STUDENT_COUNT = ".stuc"
    /**
     * Untuk adjacencyMatrix. Ekstensi ini merupakan tambahan dari programmer.
     */
    const val FILE_EXTENSION_MAT = ".matr"
    /**
     * Untuk list Course yang berupa objek. Ekstensi ini merupakan tambahan dari programmer.
     */
    const val FILE_EXTENSION_COURSE_DETAILED = ".crsx"
    /**
     * Untuk informasi tambahan terkait [Schedule], seperti penalty. Ekstensi ini merupakan tambahan dari programmer.
     */
    const val FILE_EXTENSION_SCHEDULE_INFO = ".sch"

    const val DEFAULT_TEMPERATURE_INIT: Double = 35.0
    const val DEFAULT_LEVEL_INIT_PERCENTAGE: Double = 0.4
    const val DEFAULT_TABU_MOVE_PERCENTAGE: Double = 0.1
    const val DEFAULT_DECAY_RATE: Double = 0.12

    /**
     * Dijamin hasil return lebih besar dari -1.
     */
    fun getFileNameIndex(fileName: String): Int = fileNames.indexOf(fileName.toLowerCase()).let {
        if(it < 0) throw NoSuchElementException("Gakda nama file '$it'")
        it
    }
    fun getFileDir(nameIndex: Int): String = "$DATASET_DIR\\${fileNames[nameIndex]}"
    fun getCourseFileDir(nameIndex: Int): String = "${getFileDir(nameIndex)}$FILE_EXTENSION_COURSE"
    fun getDetailedCourseFileDir(nameIndex: Int): String = "${getFileDir(nameIndex)}$FILE_EXTENSION_COURSE_DETAILED"
    fun getStudentFileDir(nameIndex: Int): String = "${getFileDir(nameIndex)}$FILE_EXTENSION_STUDENT"
    fun getStudentCountFileDir(nameIndex: Int): String = "${getFileDir(nameIndex)}$FILE_EXTENSION_STUDENT_COUNT"
    fun getSolutionFileDir(nameIndex: Int): String = "${getFileDir(nameIndex)}$FILE_EXTENSION_SOLUTION"
    fun getResFileDir(nameIndex: Int): String = "${getFileDir(nameIndex)}$FILE_EXTENSION_RES"
    fun getScheduleInfoFileDir(nameIndex: Int): String = "${getFileDir(nameIndex)}$FILE_EXTENSION_SCHEDULE_INFO"
    fun getAdjMatFileDir(nameIndex: Int): String = "${getFileDir(nameIndex)}$FILE_EXTENSION_MAT"

//    val fileNameItr: Iterator<String> get()= fileNames.iterator()
}