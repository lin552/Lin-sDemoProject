package com.lin552.linsdemoproject.KotlinCoroutineUse

import kotlinx.coroutines.*

/**
 * 协程的取消和超时
 */
suspend fun main() {
    test16()
}

//取消协程
fun test8() = runBlocking {
    val job = launch {
        repeat(1000) { i ->
            println("job: I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(1300L) // 延迟一段时间
    println("main: I'm tired of waiting!")
//    job.cancel() // 取消该作业
//    job.join() // 等待作业执行结束
    job.cancelAndJoin()
    println("main: Now I can quit.")
}

//计算循环如果没有检查协程的取消，就还有执行，直到最后完成
fun test9() = runBlocking {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) { // 一个执行计算的循环，只是为了占用 CPU
            // 每秒打印消息两次
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // 等待一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消一个作业并且等待它结束
    println("main: Now I can quit.")
}

//通过isActive判断后就会取消循环，并成功取消协程
fun test10() = runBlocking{
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (isActive) { // 可以被取消的计算循环
            // 每秒打印消息两次
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // 等待一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该作业并等待它结束
    println("main: Now I can quit.")
}

//通过finally 释放资源，当协程被取消会抛出异常
fun test11() = runBlocking {
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
    delay(1300L) // 延迟一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该作业并且等待它结束
    println("main: Now I can quit.")
}

//运行不能取消的代码块，用withContext 和 NonCancellable
fun test12() = runBlocking{
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
    delay(1300L) // 延迟一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该作业并等待它结束
    println("main: Now I can quit.")
}

//超时会抛出一个异常
fun test13() = runBlocking {
    withTimeout(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
}

//通过返回一个Null来替代超时抛出的异常
suspend fun test14() {
    val result = withTimeoutOrNull(1300L){
        repeat(1000) {
                i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
        "Done" // 在它运行得到结果之前取消它
    }
    println("Result is $result")
}


//多个协程异步执行时，每个协程有超时判断，修改资源问题
var acquired = 0

class Resource {
    init { acquired++ } // Acquire the resource
    fun close() { acquired-- } // Release the resource
}

//通过withTimeout返回
fun test15() {
    runBlocking {
        repeat(100_000) { // Launch 100K coroutines
            launch {
                val resource = withTimeout(60) { // Timeout of 60 ms
                    delay(50) // Delay for 50 ms
                    Resource() // 获取资源并从 withTimeout 块中返回
                }
                resource.close() // 释放资源
            }
        }
    }
    // 在 runBlocking 之外，所有协程都已完成
    println(acquired) // 打印仍获取的资源数量
}

//资源应该存储在变量中，不该由withTimeout返回
fun test16() {
    runBlocking {
        repeat(100_000) { // Launch 100K coroutines
            launch {
                var resource: Resource? = null // Not acquired yet
                try {
                    withTimeout(60) { // Timeout of 60 ms
                        delay(50) // Delay for 50 ms
                        resource = Resource() // Store a resource to the variable if acquired
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
}




