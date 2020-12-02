package m2_binPacking

import `val`.TaggedInt

//TODO 26 Nov 2020: ganti [content] dg tipe data [TaggedInt].
data class Bin(var content: List<Int>, var remCap: Int)