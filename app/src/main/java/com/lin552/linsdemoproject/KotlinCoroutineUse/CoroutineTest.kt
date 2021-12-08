package com.lin552.linsdemoproject.KotlinCoroutineUse

import kotlinx.coroutines.*

/**
 * 协程基础
 */

@DelicateCoroutinesApi
suspend fun main() {
    test7()
}

//--------------------------------------------------------

@DelicateCoroutinesApi
fun test1() {
    GlobalScope.launch { // 在后台启动一个新的协程并继续
        delay(1000L) // 非阻塞的等待 1 秒钟（默认时间单位是毫秒）
        println("World!") // 在延迟后打印输出
    }

    println("Hello,") // 主线程中的代码会立即执行
    runBlocking {     // 但是这个表达式阻塞了主线程
        delay(2000L)  // ……我们延迟 2 秒来保证 JVM 的存活
    }
}

//--------------------------------------------------------

@DelicateCoroutinesApi
fun test2() = runBlocking {
    GlobalScope.launch { // 在后台启动一个新的协程并继续
        delay(1000L)
        println("World!")
    }
    println("Hello,") // 主协程在这里会立即执行
    delay(2000L)      // 延迟 2 秒来保证 JVM 存活
}

//--------------------------------------------------------

@DelicateCoroutinesApi
suspend fun test3() {
    val job = GlobalScope.launch {
        delay(1000)
        println("World!")
    }
    println("Hello,")
    job.join() // 等待直到子协程执行结束
}

//--------------------------------------------------------
/**
 * 结构化并发
 */
fun test4() = runBlocking {
    launch { // 在 runBlocking 作用域中启动一个新协程
        delay(1000L)
        println("World!")
    }
    println("Hello,")
}

//--------------------------------------------------------
/**
 * 作用域构建器
 */
fun test5() = runBlocking {
    launch {
        delay(200L)
        println("Task from runBlocking")
    }

    coroutineScope { // 创建一个协程作用域
        launch {
            delay(500L)
            println("Task from nested launch")
        }

        delay(100L)
        println("Task from coroutine scope") // 这一行会在内嵌 launch 之前输出
    }

    println("Coroutine scope is over") // 这一行在内嵌 launch 执行完毕后才输出
}

//--------------------------------------------------------
/**
 * 启动大量协程
 */
fun test6() = runBlocking {
    repeat(100_000) {
        launch {
            delay(5000L)
            print(".")
        }
    }
}

//--------------------------------------------------------

@DelicateCoroutinesApi
suspend fun test7() {
    GlobalScope.launch {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(1300L) // 在延迟后退出
}



