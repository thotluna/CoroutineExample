package ve.com.teeac.coroutineexample

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class ContextTest {


    @Test
    fun `show different dispatchers`() {
        runBlocking {
            launch() { // context of the parent, main o test runBlocking coroutine
                println("main runBlock: I'm working in thread ${Thread.currentThread().name}")
            }
        }
        runTest {
            launch { // context of the parent, test runBlocking coroutine
                println("test runTest: I'm working in thread ${Thread.currentThread().name}")
            }
            launch(Dispatchers.IO) { // not confined -- will work with main thread
                println("IO: I'm working in thread ${Thread.currentThread().name}")
            }
            /**
             * Module with the Main dispatcher had failed to initialize.
             * For tests Dispatchers.setMain from kotlinx-coroutines-test module can be used
             */
//            launch(Dispatchers.Main) { // not confined -- will work with main thread
//                println("Unconfined: I'm working in thread ${Thread.currentThread().name}")
//            }
            launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
                println("Unconfined: I'm working in thread ${Thread.currentThread().name}")
            }
            launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher
                println("Default: I'm working in thread ${Thread.currentThread().name}")
            }
            launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
                println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")

            }
        }
    }

    @Test
    fun `different dispatcher start and ended in test`() = runTest {
        launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
            println("Unconfined: I'm working in thread ${Thread.currentThread().name}")
            delay(500)
            println("Unconfined: After delay in thread ${Thread.currentThread().name}")

        }
        launch { // context of the parent, main runBlocking coroutine
            println("test runTest: I'm working in thread ${Thread.currentThread().name}")
            delay(1000)
            println("test runTest: After delay in thread ${Thread.currentThread().name}")
        }
    }

    @Test
    fun `get data of coroutine run`() = runTest {
        launch {
            println("My job is ${coroutineContext[Job]}")
        }
        launch(Dispatchers.Unconfined) {
            println("My job is ${coroutineContext[Job]}")
        }
    }

    @Test
    fun `father and son with different job affect cancellation of coroutine`() = runTest {
        // launch a coroutine to process some kind of incoming request
        val request = launch {
            // it spawns two other jobs
            launch(Job()) {
                println("job1: I run in my own Job and execute independently!")
                delay(1000)
                println("job1: I am not affected by cancellation of the request")
            }
            // and the other inherits the parent context
            launch {
                delay(100)
                println("job2: I am a child of the request coroutine")
                delay(1000)
                println("job2: I will not execute this line if my parent request is cancelled")
            }
        }
        delay(500)
        request.cancel() // cancel processing of the request
        println("main: Who has survived request cancellation?")
        delay(1000) // delay the main thread for a second to see what happens
    }

    @Test
    fun `wait finished child before join`() = runTest {
        // launch a coroutine to process some kind of incoming request
        val list = mutableListOf<Int>()
        val request = launch {
            repeat(3) { i -> // launch a few children jobs
                launch {
                    delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                    println("Coroutine $i is done")
                    list.add(i)
                }
            }
            println("request: I'm done and I don't explicitly join my children that are still active")
            println("List: $list")
        }
        request.join() // wait for completion of the request, including all its children
        println("List after join: $list")
        println("Now processing of the request is complete")
    }


}