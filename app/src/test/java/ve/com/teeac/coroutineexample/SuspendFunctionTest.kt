package ve.com.teeac.coroutineexample

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import kotlin.system.measureTimeMillis

@ExperimentalCoroutinesApi
class SuspendFunctionTest {


    private suspend fun getNumber(): Int {
        return withContext(Dispatchers.Default) {
            delay(1000)
            println("run getNumber")
            (1..100).random()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun numberAsync() = GlobalScope.async {
        getNumber()
    }

    private suspend fun getOtherNumber(): Int {
        return withContext(Dispatchers.Default) {
            delay(1000)
            println("run getOtherNumber")
            (1..100).random()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun otherNumberAsync() = GlobalScope.async {
        getOtherNumber()
    }

    @Test
    fun `run two coroutine in sequential `() {
        println("Test Initial sequential")
        runTest {
            val time = measureTimeMillis {
                val one = getNumber()
                val two = getOtherNumber()
                println("Number is: ${one + two}")


                assertTrue(one > 0)
                assertTrue(two > 0)
            }
            println("time: $time")
        }

        println("Test ended")
    }

    @Test
    fun `run two coroutine in parallel`() {
        println("Test Initial in parallel")
        runTest {
            val time = measureTimeMillis {
                val one = async { getNumber() }
                val two = async { getOtherNumber() }
                println("Number is: ${one.await() + two.await()}")

                assertTrue(one.await() > 0)
                assertTrue(two.await() > 0)
            }
            println("time: $time")
        }

        println("Test ended")
    }

    @Test
    fun `Run two coroutine in parallel and lazy`()= runTest{
        val time = measureTimeMillis {
            val one = async(start = CoroutineStart.LAZY) { getNumber() }
            val two = async(start = CoroutineStart.LAZY) { getOtherNumber() }
            // some computation
            one.start() // start the first one
            two.start() // start the second one
            println("The answer is ${one.await() + two.await()}")
        }
        println("Completed in $time ms")
    }

    @Test //this method is not recommended for GlobalScope
    fun `run async outside coroutine`(){

        val one = numberAsync()
        val two = otherNumberAsync()

        runTest {
            val time = measureTimeMillis {
                println("The answer is ${one.await() + two.await()}")
            }
            println("Completed in $time ms")
        }
    }

    private suspend fun sunNumberAsync(): Int = coroutineScope {
        val one = async { getNumber() }
        val two = async { getOtherNumber() }
        one.await() + two.await()
    }

    private suspend fun numberDeferred2(): Int = coroutineScope {
        return@coroutineScope async {
            delay(1000)
            getNumber()
        }
    }.await()

    private suspend fun otherNumberDeferred2(): Int = coroutineScope {
        return@coroutineScope async {
            delay(1000)
            throw ArithmeticException()
        }
    }.await()

    @Test
    fun `run async inside coroutine`() = runTest {
        try {
            println("Result: ${numberDeferred2() + otherNumberDeferred2()}")
        }catch (e: Exception){
            println("Computation failed with $e")
        }
    }

    @Test
    fun `should join launch with run test`() {
        runTest {
            val job = launch {
                val number = getNumber()

                println("number: $number")
            }
            job.join()
            println("Done")
        }
    }

    @Test
    fun `run multiples coroutine without problem`() = runTest {
        repeat(100_000) { // launch a lot of coroutines
            launch {
                delay(5000L)
                print(".")
            }
        }
    }

    @Test
    fun `Cancel collaboratives coroutine`() = runTest {
        val job = launch {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                delay(500L)
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancel() // cancels the job
        job.join() // waits for job's completion
        println("main: Now I can quit.")
    }

    @Test
    fun `Catch error for canceled job coroutine`() = runTest {
        val job = launch(Dispatchers.Default) {
            repeat(5) { i ->
                try {
                    println("job: I'm sleeping $i ...")
                    delay(500)
                } catch (e: Exception) {
                    println("error: $e")
                }
            }
        }
        delay(1300L)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }

    @Test
    fun `does not catch error for canceled coroutine`() = runTest {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default) {
            var nextPrintTime = startTime
            var i = 0
            while (isActive) { // cancellable computation loop
                // print a message twice a second
                if (System.currentTimeMillis() >= nextPrintTime) {
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")
    }

    @Test
    fun `does not catch error for canceled coroutine with try finally`() = runTest {
        val job = launch {
            try {
                repeat(1000) { i ->
                    println("job: I'm sleeping $i ...")
                    delay(500L)
                }
            } finally {
                println("job: I'm running finally")
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")
    }

    @Test
    fun `does not canceled job with context NonCancellable`() = runTest {
        val job = launch {
            try {
                repeat(1000) { i ->
                    println("job: I'm sleeping $i ...")
                    delay(500L)
                }
            } finally {
                withContext(NonCancellable) {
                    println("job: I'm running finally")
                    delay(1000L)
                    println("job: And I've just delayed for 1 sec because I'm non-cancellable")
                }
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for its completion
        println("main: Now I can quit.")
    }

    @Test(expected = TimeoutCancellationException::class)
    fun `should canceled after 1500 millisecons`() = runTest {
        withTimeout(1300L) {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
        }
    }

    @Test
    fun `should return null for timeout`() = runTest {
        val result = withTimeoutOrNull(1300L) {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
            "Done" // will get cancelled before it produces this result
        }
        println("Result is $result")
        assertNull(result)
    }

    companion object {
        var acquired = 0
    }

    class Resource {
        init {
            acquired++
        } // Acquire the resource

        fun close() {
            acquired--
        } // Release the resource
    }

    @Test
    fun `should return acquired mayor 0 because leak for cancel timeout`() {
        runBlocking {
            var i = 0
            repeat(1_000) { // Launch 100K coroutines
                launch {
                    val resource = withTimeout(60) { // Timeout of 60 ms
                        delay(50) // Delay for 50 ms
                        Resource() // Acquire a resource and return it from withTimeout block
                    }
                    resource.close() // Release the resource
                }
                println("ves: $i")
                i++
            }
        }
        // Outside of runBlocking all coroutines have completed
        println(acquired) // Print the number of resources still acquired
    }

    @Test
    fun `should return acquired equal 0 because not leak for cancel timeout`() {
        var resource: Resource? = null // Not acquired yet
        runBlocking {
            repeat(100_000) { // Launch 100K coroutines
                launch {
                    try {
                        withTimeout(60) { // Timeout of 60 ms
                            delay(50) // Delay for 50 ms
                            resource =
                                Resource() // Store a resource to the variable if acquired
                        }
                        // We can do something else with the resource here
                    } finally {
                        resource?.close() // Release the resource if it was acquired
                    }
                }
            }
        }
// Outside of runBlocking all coroutines have completed
        println(acquired) // Print the number of resources still acquired
        assertEquals(0, acquired)
    }

    private suspend fun failedConcurrentSum(): Int = coroutineScope {
        val one = async<Int> {
            try {
                delay(Long.MAX_VALUE) // Emulates very long computation
                42
            } finally {
                println("First child was cancelled")
            }
        }
        val two = async<Int> {
            println("Second child throws an exception")
            throw ArithmeticException()
        }
        one.await() + two.await()
    }

    @Test
        (expected = ArithmeticException::class)
    fun `Cancellation is always propagated through coroutines hierarchy`() = runTest {
        try {
            failedConcurrentSum()
        } catch(e: ArithmeticException) {
            println("Computation failed with ArithmeticException")
            throw e
        }
    }


}