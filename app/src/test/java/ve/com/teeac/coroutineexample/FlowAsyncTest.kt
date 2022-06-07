package ve.com.teeac.coroutineexample

import androidx.test.core.app.ActivityScenario.launch
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.lang.IllegalStateException
import kotlin.system.measureTimeMillis

@ExperimentalCoroutinesApi
class FlowAsyncTest {

    private fun simple(): Flow<Int> = flow { // flow builder
        for (i in 1..3) {
            delay(100) // pretend we are doing something useful here
            emit(i) // emit next value
        }
    }

    @Test
    fun `get data of coroutine run`() = runTest {
        launch {
            for (k in 1..3) {
                println("I'm not blocked $k")
                delay(100)
            }
        }
        // Collect the flow
        simple().collect { value -> println(value) }
    }

    private fun otherSimple(): Flow<Int> = flow {
        println("Flow started")
        for (i in 1..3) {
            delay(100)
            println("Emitting $i")
            emit(i)
        }
    }

    @Test
    fun `the flow are cold`() = runTest{
        println("Calling simple function...")
        val flow = otherSimple()
        println("Calling collect...")
        flow.collect { value -> println(value) }
        println("Calling collect again...")
        flow.collect { value -> println(value) }
    }

    @Test
    fun `canceled flow`() = runBlocking<Unit> {
        withTimeoutOrNull(250) { // Timeout after 250ms
            simple().collect { value -> println(value) }
        }
        println("Done")
    }

    private suspend fun performRequest(request: Int): String {
        delay(1000) // imitate long-running asynchronous work
        return "response $request"
    }

    @Test
    fun `used intermediate operator`() = runTest {
        (1..3).asFlow() // a flow of requests
            .map { request -> performRequest(request) }
            .collect { response -> println(response) }
    }

    @Test
    fun `used Transform operator`() = runTest {
        (1..3).asFlow() // a flow of requests
            .transform { request ->
                emit("Making request $request")
                emit(performRequest(request))
            }
            .collect { response -> println(response) }
    }

    @Test
    fun `terminal operator`() = runTest {
        val sum = (1..5).asFlow()
            .map { it * it } // squares of numbers from 1 to 5
            .reduce { a, b -> a + b } // sum them (terminal operator)
        println(sum)
    }

    private fun simpleInOtherContext(): Flow<Int> = flow {
        for (i in 1..3) {
            Thread.sleep(100) // pretend we are computing it in CPU-consuming way
            println("Emitting $i")
            println("Flow run in context ${Thread.currentThread().name}")
            emit(i) // emit next value
        }
    }.flowOn(Dispatchers.IO) // RIGHT way to change context for CPU-consuming code in flow builder

    @Test
    fun `Flow run in other context`() = runBlocking {
        simpleInOtherContext().collect { value ->
            println("Consume in context ${Thread.currentThread().name}")
            println("Collected $value")
        }
    }

    @Test
    fun `Run flow sequential and with buffer`() = runTest {
        val timeS = measureTimeMillis {
            simple().collect { value ->
                delay(300) // pretend we are processing it for 300 ms
                println(value)
            }
        }
        println("Collected sequential in $timeS ms")

        val timeB = measureTimeMillis {
            simple()
                .buffer() // buffer emissions, don't wait
                .collect { value ->
                    delay(300) // pretend we are processing it for 300 ms
                    println("With Buffer $value")
                }
        }
        println("Collected with buffer in $timeB ms")
    }

    @Test
    fun `Multiples flows with zip`() = runTest {
        val strs = flowOf("one", "two", "three")

        simple().zip(strs) { a, b -> "$a -> $b" }
            .collect { value -> println(value) }
    }

    @Test
    fun `combine flow `() = runTest {
        val strs = flowOf("one", "two", "three").onEach { delay(50) } // strings every 400 ms
        val startTime = System.currentTimeMillis() // remember the start time
        simple().combine(strs) { a, b -> "$a -> $b" } // compose a single string with "combine"
            .collect { value -> // collect and print
                println("$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }

    private fun requestFlow(i: Int): Flow<String> = flow {
        emit("$i: First")
        delay(500) // wait 500 ms
        emit("$i: Second")
    }

    @OptIn(FlowPreview::class)
    @Test
    fun `flattening flows with flatMapConcat`() = runTest{
        val startTime = System.currentTimeMillis() // remember the start time
        (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
            .flatMapConcat { requestFlow(it) }
            .collect { value -> // collect and print
                println("$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }

    @OptIn(FlowPreview::class)
    @Test
    fun `flattening flows with flatMapMerge`() = runTest{
        val startTime = System.currentTimeMillis() // remember the start time
        (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
            .flatMapMerge { requestFlow(it) }
            .collect { value -> // collect and print
                println("$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
    }

    @Test
    fun `error with try catch`() = runTest {
        try {
            simple().collect { value ->
                println(value)
                check(value <= 1) { "Collected $value" }
            }
        } catch (e: Throwable) {
            println("Caught $e")
        }
    }

    private fun simpleFlow(): Flow<String> =
        flow {
            for (i in 1..3) {
                println("Emitting $i")
                emit(i) // emit next value
            }
        }
            .map { value ->
                check(value <= 1) { "Crashed on $value" }
                "string $value"
            }

    @Test
    fun `remit error`() = runTest {
        simpleFlow()
            .catch { e -> emit(e.toString()) }
            .collect { value ->
            println(value)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `does not catch error in collect`() = runTest{
        simple()
            .catch { e -> println (e) }
            .collect{ value ->
                check(value <= 1) { "value $value collect"}
                println(value)
            }

    }

    @Test
    fun `catch error in collect`() = runTest{
        simple()
            .onCompletion { cause -> if (cause != null) println("Flow completed exceptionally") }
            .onEach{ value ->
                check(value <= 1) { "value $value collect"}
                println(value)
            }
            .catch { e -> println (e) }
            .collect()

    }
}