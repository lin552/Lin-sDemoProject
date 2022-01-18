package com.lin552.linsdemoproject.KotlinCoroutineUse

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*


/**
 * 协程通道
 */

fun main() {
    testChannel10()
}

/**
 * 通道基础
 */
fun testChannel1() = runBlocking {

    val channel = Channel<Int>()
    launch {
        //这里可能是消耗大量CPU运算的异步逻辑，我们将仅仅做5次整数的平方并发送
        for (x in 1..5) channel.send(x * x)
    }
    //这里我们打印了5次被接收的整数;
    repeat(5) {
        println(channel.receive())
    }
    println("Done!")
}

/**
 * 关闭与迭代通道
 */
fun testChannel2() = runBlocking {
    val channel = Channel<Int>()

    launch {
        for (x in 1..5) channel.send(x * x)
        channel.close() //我们结束发送
    }
    //这里我们使用'for'循环来打印所有被接收到的元素（直到通道被关闭）
    for (y in channel) println(y)
    println("Done!")
}

fun CoroutineScope.produceSquares(): ReceiveChannel<Int> = produce {
    for (x in 1..5) send(x * x)
}

/**
 * 构建通道生产者
 */
fun testChannel3() = runBlocking {
    val squares = produceSquares()
    squares.consumeEach { println(it) }
    println("Done!")

}

//在一个协程中生成多个流
fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1
    while (true) send(x++) //infinite stream of integers starting form 1
}

//在此协程中可以消费通道生成的流
fun CoroutineScope.square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
    for (x in numbers) send(x * x)
}

fun testChannel4() = runBlocking {
    val numbers = produceNumbers() //从1开始生成整数
    val squares = square(numbers) //整数求平方
    repeat(5) {
        println(squares.receive()) //输出前五个
    }
    println("Done!") //至此已完成
    coroutineContext.cancelChildren() //取消子协程

}


/**
 * 使用管道的素数
 */
fun CoroutineScope.numbersFrom(start: Int) = produce<Int> {
    var x = start
    while (true) send(x++) //开启了一个无限的整数流
}


fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce<Int> {
    for (x in numbers) if (x % prime != 0) send(x)
}

fun testChannel5() = runBlocking {
    var cur = numbersFrom(2)
    repeat(10) {
        val prime = cur.receive()
        println(prime)
        cur = filter(cur, prime)
    }
    coroutineContext.cancelChildren() //取消所有的子协程来让主协程结束
}

/**
 * 扇出
 */
fun CoroutineScope.produceNumbers1() = produce<Int> {
    var x = 1 //从1开始
    while (true) {
        send(x++) //产生下一个数字
        delay(100) //等待0.1秒
    }
}

fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    for (msg in channel) {
        println("Processor #$id received $msg")
    }
}

/**
 * 这个 for 循环是安全完美地使用多个协程的。如果其中一个处理器协程执行失败，
 * 其它的处理器协程仍然会继续处理通道，而通过 consumeEach 编写的处理器始终在正常或非正常完成时消耗（取消）底层通道。
 */
fun testChannel6() = runBlocking {
    val producer = produceNumbers1()
    repeat(5) {
        launchProcessor(it, producer)
    }
    delay(950)
    producer.cancel() //取消协程生产者从而将它们全部杀死
}

/**
 * 扇入
 */
suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
    while (true) {
        delay(time)
        channel.send(s)
    }
}

fun testChannel7() = runBlocking {
    val channel = Channel<String>()

    launch { sendString(channel, "foo", 200L) }
    launch { sendString(channel, "BAR!", 500L) }
    repeat(6) { //接收前6个
        println(channel.receive())
    }
    coroutineContext.cancelChildren() //取消所有子协程来主协程结束

}

/**
 * 带缓冲的通道
 */
fun testChannel8() = runBlocking {
    val channel = Channel<Int>(4) //启动带缓冲的通道
    val sender = launch { //启动发送者协程
        repeat(10) {
            println("Sening $it")
            channel.send(it)
        }
    }
    //没有接收到东西.....只是等待
    delay(1000)
    sender.cancel() //取消发送者协程
}

data class Ball(var hits: Int)

/**
 * 通道是公平的
 *
 * 发送和接收操作是 公平的并且尊重调用它们的多个协程。它们遵守先进先出原则，可以看到第一个协程调用
 * receive 并得到了元素。在下面的例子中两个协程"ping"和"pong"都从共享的"table"通道接收到这个"ball"元素
 */
fun testChannel9() = runBlocking {
    val table = Channel<Ball>()
    launch { player("ping", table) }
    launch { player("pong", table) }
    table.send(Ball(0)) //乒乓球
    delay(1000) //延迟1秒钟
    coroutineContext.cancelChildren() //游戏结束，取消它们
}

suspend fun player(name: String, table: Channel<Ball>) {
    for (ball in table) { //在循环中接收球
        ball.hits++
        println("$name $ball")
        delay(300) //等待一段时间
        table.send(ball) //将球发送回去
    }
}

/**
 * 计时器通道
 */
fun testChannel10() = runBlocking {
    val tickerChannel = ticker(delayMillis = 100, initialDelayMillis = 0)
    var nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
    println("Initial element is available immediately: $nextElement")

    nextElement = withTimeoutOrNull(50) { tickerChannel.receive() }
    println("Next element is not ready in 50 ms: $nextElement")

    nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
    println("Next element is ready in 100 ms: $nextElement")

    //模拟大量消费延迟
    println("Consumer pauses for 150ms")
    delay(150)
    //下一个元素立即可用
    nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
    println("Next element is avaiable immediately after large consumer delay: $nextElement")
    //请注意 'reveive' 调用之间的暂停被考虑在内，下一个元素的到达速度更快
    nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
    println("Next element is ready in 50ms after consumer pause in 150ms: $nextElement")

    tickerChannel.cancel() //表明不再需要更多的元素
}