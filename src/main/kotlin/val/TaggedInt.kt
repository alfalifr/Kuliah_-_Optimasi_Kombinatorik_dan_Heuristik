package `val`

data class TaggedInt(override val value: Int, override val tag: Any? = null): TaggedValue<Int>, Comparable<TaggedInt> {
    override operator fun compareTo(other: TaggedInt): Int = value.compareTo(other.value)
}