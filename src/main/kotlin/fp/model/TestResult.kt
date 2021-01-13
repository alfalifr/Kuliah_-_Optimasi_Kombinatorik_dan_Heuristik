package fp.model

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

data class TestResult<T> @OptIn(ExperimentalTime::class) constructor(val result: T, val duration: Duration)