package com.lin552.linsdemoproject.KotlinCoroutineUse

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 协程异步流
 * 流创建
 * 各种操作符
 * 切换上下文
 **/


suspend fun main() {
//    simple2().forEach { value -> println(value) }
    test51()
}

//--------------------------------------------------------

fun simple(): List<Int> = listOf(1, 2, 3)

fun simple1(): Sequence<Int> = sequence { // 序列构建器
    for (i in 1..3) {
        Thread.sleep(100) // 假装我们正在计算
        yield(i) // 产生下一个值
    }
}
/**
 * 计算过程阻塞运行该代码的主线程。 当这些值由异步代码计算时，我们可以使用 suspend 修饰符标记函数 simple
 * 这样它就可以在不阻塞的情况下执行其工作并将结果作为列表返回：
 */
suspend fun simple2(): List<Int> {
    delay(1000) // 假装我们在这里做了一些异步的事情
    return listOf(1, 2, 3)
}

//--------------------------------------------------------
/**
 * 流构建器
 */
fun simple3(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(100) // 假装我们在这里做了一些有用的事情
        emit(i) // 发送下一个值
    }
}

fun test40() = runBlocking<Unit> {
    // 启动并发的协程以验证主线程并未阻塞
    launch {
        for (k in 1..3) {
            println("I'm not blocked $k")
            delay(100)
        }
    }
    // 收集这个流
    simple3().collect { value -> println(value) }
}

//--------------------------------------------------------

fun simple4(): Flow<Int> = flow {
    println("Flow started")
    for (i in 1..3) {
        delay(100)
        emit(i)
    }
}
/**
 * 流是冷的
 */
fun test41() = runBlocking<Unit> {
    println("Calling simple function...")
    val flow = simple4()
    println("Calling collect...")
    flow.collect { value -> println(value) }
    println("Calling collect again...")
    flow.collect { value -> println(value) }
}

//--------------------------------------------------------
/**
 * 流取消
 */
fun simple5(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(100)
        println("Emitting $i")
        emit(i)
    }
}

fun test42() = runBlocking<Unit> {
    withTimeoutOrNull(250) { // 在 250 毫秒后超时
        simple5().collect { value -> println(value) }
    }
    println("Done")
}

//--------------------------------------------------------
/**
 * 流构建器 通过asFlow可以将集合与序列转换成流
 */
fun test43() = runBlocking {
    // Convert an integer range to a flow
    (1..3).asFlow().collect { value -> println(value) }
}

//--------------------------------------------------------
/**
 * 过渡流操作符 map
 */
suspend fun performRequest(request: Int): String {
    delay(1000) // 模仿长时间运行的异步工作
    return "response $request"
}

fun test44() = runBlocking<Unit> {
    (1..3).asFlow() // 一个请求流
        .map { request -> performRequest(request) }
        .collect { response -> println(response) }
}

//--------------------------------------------------------
/**
 * 转换操作符  使用 transform 操作符，我们可以 发射 任意值任意次。
 */
fun test45() = runBlocking<Unit> {
    (1..3).asFlow() // a flow of requests
        .transform { request ->
            emit("Making request $request")
            emit(performRequest(request))
            emit(performRequest(request))
            emit(performRequest(request))
        }
        .collect { response -> println(response) }
}

//--------------------------------------------------------
/**
 * 限长操作符
 */
fun numbers(): Flow<Int> = flow {
    try {
        emit(1)
        emit(2)
        println("This line will not execute")
        emit(3)
    } finally {
        println("Finally in numbers")
    }
}

fun test46() = runBlocking<Unit> {
    numbers()
        .take(2) // 只获取前两个
        .collect { value -> println(value) }
}

//--------------------------------------------------------
/**
 * 末端流操作符
 */
fun test47() = runBlocking<Unit> {
    val sum = (1..5).asFlow()
        .map { it * it } // squares of numbers from 1 to 5
        .reduce { a, b -> a + b } // sum them (terminal operator)
    println(sum)
}

//--------------------------------------------------------
/**
 * 流是连续的
 * 流的每次单独收集都是按顺序执行的，除非进行特殊操作的操作符使用多个流。
 * 该收集过程直接在协程中运行，该协程调用末端操作符。
 * 默认情况下不启动新协程。 从上游到下游每个过渡操作符都会处理每个发射出的值然后再交给末端操作符。
 */
fun test48() = runBlocking<Unit> {
    (1..5).asFlow()
        .filter {
            println("Filter $it")
            it % 2 == 0
        }
        .map {
            println("Map $it")
            "string $it"
        }.collect {
            println("Collect $it")
        }
}

//--------------------------------------------------------
/**
 * 流的上下文
 * 可以通过制定上下文的方式来约定流在什么地方执行
 */
fun simple6(): Flow<Int> = flow {
    log("Started simple flow")
    for (i in 1..3) {
        emit(i)
    }
}

fun test49() = runBlocking<Unit> {
    simple6().collect { value -> log("Collected $value") }
}

//--------------------------------------------------------
/**
 * withContext 发出错误
 * 在flow构建器中必须依照flow构建器
 * 不能通过指定其他上下文的方式，进行emit
 */
fun simple7(): Flow<Int> = flow {
    // / 在流构建器中更改消耗 CPU 代码的上下文的错误方式
    withContext(Dispatchers.Default) {
        for (i in 1..3) {
            Thread.sleep(100) //  假装我们以消耗 CPU 的方式进行计算
            emit(i) // 发射下一个值
        }
    }
}

fun test50() = runBlocking<Unit> {
    simple7().collect { value -> println(value) }
}

//--------------------------------------------------------
/**
 * flowOn操作符
 * 用于修改上下文
 */
fun simple8(): Flow<Int> = flow {
    for (i in 1..3) {
        Thread.sleep(100) // pretend we are computing it in CPU-consuming way
        log("Emitting $i")
        emit(i) // emit next value
    }
}.flowOn(Dispatchers.Default) // RIGHT way to change context for CPU-consuming code in flow builder

fun test51() = runBlocking<Unit> {
    simple8().collect { value ->
        log("Collected $value")
    }
}

//--------------------------------------------------------