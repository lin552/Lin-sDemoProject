package com.lin552.linsdemoproject.KotlinCoroutineUse

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

/**
 * Kotlin 并发
 */
fun main() {
    testConcurrency6()
}

suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 100 //启动协程数量
    val k = 1000 //每个协程重复执行同一动作的次数
    val time = measureTimeMillis {
        coroutineScope {  //协程的作用域
            repeat(n){
                launch {
                    repeat(k){
                        action()
                    }
                }
            }
        }
    }
}

/**
 * Volatile无济于事
 */
//@Volatile
var counter = 0
fun testConcurrency1() = runBlocking {
    withContext(Dispatchers.Default){
        massiveRun {
            counter++
        }
    }
    println("Counter == $counter")
}

/**
 * 线程安全的数据结构
 */
var counter1 = AtomicInteger()
fun testConcurrency2() = runBlocking {
    withContext(Dispatchers.Default){
        massiveRun {
            counter1.incrementAndGet()
        }
    }
    println("Counter1 = $counter1")
}

/**
 * 以细粒度限制线程
 *
 */
var counterContext = newSingleThreadContext("CounterContext")
fun testConcurrency3() = runBlocking {
    withContext(Dispatchers.Default){
        massiveRun {
            //将每次自增限制在单线程上下文中
            withContext(counterContext){
                counter++
            }
        }
    }
    println("Counter2 = $counter")
}

/**
 * 以粗粒度限制线程
 */
fun testConcurrency4() = runBlocking {
    //将一切都限制在单线程上下文中
    withContext(counterContext){
        massiveRun {
            counter++
        }
    }
    println("Counter3 = $counter")
}

val mutex = Mutex()

/**
 * 互斥
 */
fun testConcurrency5() = runBlocking {
    withContext(Dispatchers.Default){
        massiveRun {
            mutex.withLock {
                counter++
            }
        }
    }
    println("Counter5 = $counter")
}

/**
 * 一个 actor 是由协程、被限制并封装到该协程中的状态以及一个与其它协程通信的通道
 * 组合而成的一个实体。一个简单的actor可以简单的写成一个函数，但是一个拥有复杂状态的actor
 * 更适合由类来表示。
 *
 * 有一个 actor 协程构建器，它可以方便地将actor的邮箱通道组合到其作用域中（用来接受消息）、
 * 组合发送channel与结果集对象，这样对actor的单个引用就可以作为其句柄持有。
 *
 * 使用actor的第一步是定义一个actor要处理的消息类。Koltin的密封类很适合这种场景。我们使用
 * IncCounter消息（从来递增计数器）和 GetCounter 消息（用来获取值）来定义CounterMsg密封类。
 * 后者需要发送回复。CompletableDeferred 通信原语表未来可知（可传达）的单个值，这里被用于此目的
 */
// 计数器 Actor 的各种类型
sealed class CounterMsg
object IncCounter:CounterMsg() //递增计数器的单向消息
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg()

//这个函数启动一个新的计数器 actor
fun CoroutineScope.counterActor() = actor<CounterMsg> {
    var counter = 0
    for (msg in channel) {
        when (msg) {
            is IncCounter -> counter++
            is GetCounter -> msg.response.complete(counter)
        }
    }
}

fun testConcurrency6() = runBlocking {
    val counter = counterActor() //创建该 actor
    withContext(Dispatchers.Default){
        massiveRun {
            counter.send(IncCounter)
        }
    }
    val response = CompletableDeferred<Int>()
    counter.send(GetCounter(response))
    println("Counter = ${response.await()}")
    counter.close() //关闭该actor
}



