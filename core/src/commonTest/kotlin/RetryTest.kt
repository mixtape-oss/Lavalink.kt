import dev.schlaubi.lavakord.audio.retry.LinearRetry
import kotlinx.coroutines.launch
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.measureTime
import kotlin.time.seconds

class RetryTest {

    @JsName("testRetryMaxFail")
    @Test
    fun `check whether retry exits correctly after exceeding max`() {
        val retry = LinearRetry(Duration.seconds(1), Duration.seconds(5), 1)
        Tests.launch {
            retry.retry()

            assertFalse(retry.hasNext, "Retry::hasNext has to be false when maxTries has been exceeded")
            assertFailsWith<IllegalStateException>("Retry is supposed to throw illegalStateException when retrying after max was exceeded") { retry.retry() }
        }
    }

    @JsName("testNeverExceedsMaxBackoff")
    @Test
    fun `check linear retry never exceeds max backoff`() {
        val max = 5
        val maxBackoff = Duration.seconds(5)
        val retry = LinearRetry(Duration.seconds(1), maxBackoff, max)
        val delays = mutableListOf<Duration>()
        repeat(max) {
            delays += measureTime {
                Tests.launch {
                    retry.retry()
                }
            }
        }

        val maxDelay = delays.maxOrNull() ?: error("Could not measure delays")
        assertTrue(maxDelay <= maxBackoff, "Max delay cannot exceed max delay")
    }
}
