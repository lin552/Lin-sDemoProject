package com.lin552.linsdemoproject.KotlinCoroutineUse

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.flow.*
import java.lang.RuntimeException
import java.lang.System.currentTimeMillis

/**
 * 协程异步流
 * 各种操作符
 **/

fun main() {
    test70()
}

//--------------------------------------------------------
/**
 * 缓存
 * 可以通过buffer提高执行效率
 * 注意，当必须更改 CoroutineDispatcher 时，flowOn 操作符使用了相同的缓冲机制， 但是我们在这里显式地请求缓冲而不改变执行上下文。
 */
fun simple9(): Flow<Int> = flow {
    for (i in 1..6) {
        delay(100) // 假装我们异步等待了 100 毫秒
        emit(i) // 发射下一个值
    }
}

fun test52() = runBlocking<Unit> {
    val time = measureTimeMillis {
        simple9()
            .buffer() // 缓冲发射项，无需等待
            .collect { value ->
                delay(300) // 假装我们花费 300 毫秒来处理它
                println(value)
            }
    }
    println("Collected in $time ms")
}

//--------------------------------------------------------
/**
 * 合并
 */
fun test53() = runBlocking {
    val time = measureTimeMillis {
        simple9()
            .conflate() // 合并发射项，不对每个值进行处理
            .collect { value ->
                delay(300) // 假装我们花费 300 毫秒来处理它
                println(value)
            }
    }
    println("Collected in $time ms")
}

//--------------------------------------------------------
/**
 * collectLatest
 * 只处理最后一个
 */
fun test54() = runBlocking<Unit> {
    val time = measureTimeMillis {
        simple9()
            .collectLatest { value -> // cancel & restart on the latest value
                println("Collecting $value")
                delay(300) // pretend we are processing it for 300 ms
                println("Done $value")
            }
    }
    println("Collected in $time ms")
}

//--------------------------------------------------------
/**
 * zip
 * 组合多个
 * 将 nums 和 strs 按照顺序组合
 */
fun test55() = runBlocking<Unit> {
    val nums = (1..3).asFlow() // numbers 1..3
    val strs = flowOf("one", "two", "three") // strings
    nums.zip(strs) { a, b -> "$a -> $b" } // compose a single string
        .collect { println(it) } // collect and print
}

//--------------------------------------------------------
/**
 * Combine
 * 组合多个
 * 利用Combine替换zip
 */
fun test56() = runBlocking<Unit> {
    val nums = (1..3).asFlow().onEach { delay(300) } // numbers 1..3 every 300 ms
    val strs = flowOf("one", "two", "three").onEach { delay(400) } // strings every 400 ms
    val startTime = currentTimeMillis() // remember the start time
    nums.combine(strs) { a, b -> "$a -> $b" } // compose a single string with "combine"
        .collect { value -> // collect and print
            println("$value at ${currentTimeMillis() - startTime} ms from start")
        }
}

//--------------------------------------------------------
/**
 * 流展平操作符
 */
fun requestFlow(i: Int): Flow<String> = flow {
    emit("$i: First")
    delay(500) // wait 500 ms
    emit("$i: Second")
}

/**
 * flatMapConcat
 * 它们在等待内部流完成之前开始收集下一个值：
 * 如图 requestFlow 执行完成后才会发送下一个
 */
fun test57() = runBlocking<Unit> {
    val startTime = currentTimeMillis() // remember the start time
    (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
        .flatMapConcat { requestFlow(it) }
        .collect { value -> // collect and print
            println("$value at ${currentTimeMillis() - startTime} ms from start")
        }
}

/**
 * flatMapMerge
 * 并发的收集所有传入的流，并合并到一个单独的流
 * 按照顺序执行，但是收集流时并发接收
 */
fun test58() = runBlocking {
    val startTime = currentTimeMillis() // remember the start time
    (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
        .flatMapMerge { requestFlow(it) }
        .collect { value -> // collect and print
            println("$value at ${currentTimeMillis() - startTime} ms from start")
        }
}

/**
 * flatMapLatest
 *
 * 发送新流后，立即取消先前流的收集
 */
fun test59() = runBlocking {
    val startTime = currentTimeMillis() // remember the start time
    (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
        .flatMapLatest { requestFlow(it) }
        .collect { value -> // collect and print
            println("$value at ${currentTimeMillis() - startTime} ms from start")
        }
}

/**
 * 流异常
 * 异常被捕捉以后，后面的流不再发
 */

fun test60() = runBlocking {
    try {
        simple9().collect { value ->
            println(value)
            check(value <= 1) { "Collected $value" }
        }
    } catch (e: Exception) {
        println("Caught $e")
    }
}

fun simple10(): Flow<String> =
    flow {
        for (i in 1..3) {
            println("Emitting $i")
            emit(i) // 发射下一个值
        }
    }
        .map { value ->
            check(value <= 1) { "Crashed on $value" }
            "string $value"
        }

fun test61() = runBlocking {
    try {
        simple10().collect { value ->
            println(value)
        }
    } catch (e: Exception) {
        println("Caught $e")
    }
}

/**
 * 发射器可以使用catch操作符来保留此异常的透明性并允许封装它的异常处理
 * catch操作符的代码块可以分析异常并根据捕获的异常以不同的方式对其做出反应
 *
 * 透明捕获
 * catch过渡操作符遵循异常透明性，仅捕获上游异常（catch操作符上游的异常，但是它下面的不是）。如果 collect{ ...} 块
 * (位于catch之下)抛出一个异常，那么异常会逃逸
 */
fun test62() = runBlocking {
    simple10().catch { e ->
        emit("Caught $e") //不会捕获下游异常
    }.collect { value ->
        check(value <= 1.toString()) { "Collected $value" }
        println(value)
    }
}

/**
 * 声明式捕获
 *
 * 我们可以将catch操作符的声明性与处理所有异常的期望相结合，将collect操作符的代码块移动到onEach中
 * ，并将其放到catch操作符之前。收集该流必须由调用无参的collect()触发
 */

fun test63() = runBlocking {
    simple10().onEach { value ->
        check(value <= 1.toString()) {
            "Collected $value"
        }
        println(value)
    }.catch { e ->
        println("Caught $e")
    }
        .collect()
}

/**
 * 流完成
 *
 * 当流收集完成时（普通情况或异常情况），它可能需要执行一个动作。你可能已经注意到，它可以通过两种方式完成：命令式或声明式
 *
 * 命令式finally块
 */

fun test64() = runBlocking {
    try {
        simple9().collect { value ->
            println(value)
        }
    } finally {
        println("Done")
    }
}

/**
 * 声明式处理
 * onCompletion在流完全收集时调用
 * 优点是其lambda表达式的可空参数Throwable 可以用于确定流收集时正常完成还是有异常发生
 */

fun test65() = runBlocking {
    simple9().onCompletion { println("Done") }
        .collect { value -> println(value) }
}

fun simple11(): Flow<Int> = flow {
    emit(1)
    throw RuntimeException()
}

/**
 * onCompletion 不处理异常，异常流仍然流向下流。
 * 可以后面交给catch操作符处理
 */

fun test66() = runBlocking {
    simple11().onCompletion { cause ->
        if (cause != null) {
            println("Flow completed exceptionally")
        }
    }
        .catch { cause ->
            println("Caught exception")
        }
        .collect { value ->
            println(value)
        }
}

/**
 * 与catch操作符的另一个不同点是onCompletion能观察到所有异常并且
 * 仅在上游流成功完成（没有取消或失败）的情况下接收一个null异常
 */
fun test67() = runBlocking {
    simple9().onCompletion {
        cause ->
        println("Flow completed with $cause")
    }
        .collect {
            value ->
            check(value <= 1) {
                "Collected $value"
            }
            println(value)
        }
}

/**
 * 启动流
 */

fun events():Flow<Int> = (1..3).asFlow().onEach {
    delay(100)
}

fun test68() = runBlocking {
    events()
        .onEach { event -> println("Event: $event") }
        .collect() // <--- 等待流收集
    println("Done")
}

/**
 * launchIn 必要的参数 CoroutineScope 指定了用哪一个协程来启动流的收集
 * 在先前的示例中这个作用域来自runBlocking协程构建器，在这个流运行的时候，runBlocking作用域等待它的子协程执行完毕并防止main函数返回并终止此示例
 *
 * 在实际的应用中，作用域来自于一个寿命有限的实体。在该实体的寿命终止后，相应的作用域就会被取消，即取消相应的流的收集。这种成对的onEach{...}.launchIn(scope)工作方式
 * 就像addEventListener一样。而且，这不需要相应的removeEventListener函数，因为取消于结构化并发可以达成这个目的。
 *
 * 注意，launchIn也会返回一个Job，可以在不取消整个作用域的情况下仅取消相应的流收集或对其进行join
 */
fun test69() = runBlocking {
    events()
        .onEach { event -> println("Event: $event") }
        .launchIn(this) // <--- 在单独的协程中执行流
    println("Done")
}

/**
 * 流取消检测
 *
 * 为了方便起见，流构建器对每个发射值执行附加的ensureActive检测以进行取消。这意味着从flow{...}发出的繁忙循环是可以取消的；
 */

fun foo():Flow<Int> = flow {
    for (i in 1..5) {
        println("Emitting $i")
        emit(i)

    }
}

fun test70() = runBlocking {
    foo().collect {
        value -> if (value == 3) cancel()
        println(value)
    }
}

/**
 * 让繁忙的流取消
 *
 * 在协程处于繁忙循环的情况下，必须明确检测是否取消。 可以添加 .onEach { currentCoroutineContext().ensureActive() }， 但是这里提供了一个现成的 cancellable 操作符来执行
 */
fun test71() = runBlocking<Unit> {
    (1..5).asFlow().cancellable().collect { value ->
        if (value == 3) cancel()
        println(value)
    }
}



