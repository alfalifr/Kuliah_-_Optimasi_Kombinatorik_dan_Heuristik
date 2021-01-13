package fp.model

data class Timeslot(val no: Int, val name: String = "T$no"){
    override fun toString(): String = name
}