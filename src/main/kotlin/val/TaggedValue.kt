package `val`

interface TaggedValue<T> {
    val tag: Any?
    val value: T
}