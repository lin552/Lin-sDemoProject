package com.lin552.linsdemoproject.KotlinCoroutineUse

import android.app.Activity
import kotlinx.coroutines.*

/**
 * 协程上下文
 */

@DelicateCoroutinesApi
suspend fun main() {
    test31()
}


// launch{}和launch(Dispatchers.Default) 使用同一个构造器
@DelicateCoroutinesApi
fun test23() = runBlocking<Unit> {
    launch { // context of the parent, main runBlocking coroutine
        println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
        println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher
        println("Default               : I'm working in thread ${Thread.currentThread().name}")
    }

    //为协程启动一个线程来执行 该专用线程是一种昂贵的
    launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
        println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
    }
}


fun test24() = runBlocking {
    launch(Dispatchers.Unconfined) { // 非受限的——将和主线程一起工作
        println("Unconfined      : I'm working in thread ${Thread.currentThread().name}")
        delay(500)
        println("Unconfined      : After delay in thread ${Thread.currentThread().name}")
    }
    launch { // 父协程的上下文，主 runBlocking 协程
        println("main runBlocking: I'm working in thread ${Thread.currentThread().name}")
        delay(1000)
        println("main runBlocking: After delay in thread ${Thread.currentThread().name}")
    }
}

//打印当前线程名称
fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")


//其中一个使用 runBlocking 来显式指定了一个上下文，并且另一个使用 withContext 函数来改变协程的上下文，而仍然驻留在相同的协程中，正如可以在下面的输出中所见到的：
@DelicateCoroutinesApi
fun test25() {
    newSingleThreadContext("Ctx1").use { ctx1 ->
        newSingleThreadContext("Ctx2").use { ctx2 ->
            runBlocking(ctx1) {
                log("Started in ctx1")
                withContext(ctx2) {
                    log("Working in ctx2")
                }
                log("Back to ctx1")
            }
        }
    }
}

//上下文中的job 通过coroutineContext[job] 来检索
fun test26() = runBlocking<Unit> {
    println("My job is ${coroutineContext[Job]}")
}

//使用GlobalScope启动的协程不会成为其他线程的子协程
//父协程取消后，子协程会递归取消
//launch{} 继承了 父类的上下文
@DelicateCoroutinesApi
fun test27() = runBlocking {
    // 启动一个协程来处理某种传入请求（request）
    val request = launch {
        // 孵化了两个子作业, 其中一个通过 GlobalScope 启动
        GlobalScope.launch {
            println("job1: I run in GlobalScope and execute independently!")
            delay(1000)
            println("job1: I am not affected by cancellation of the request")
        }
        // 另一个则承袭了父协程的上下文
        launch {
            delay(100)
            println("job2: I am a child of the request coroutine")
            delay(1000)
            println("job2: I will not execute this line if my parent request is cancelled")
        }
    }
    delay(500)
    request.cancel() // 取消请求（request）的执行
    delay(1000) // 延迟一秒钟来看看发生了什么
    println("main: Who has survived request cancellation?")
}

//父协程会等待所有子协程处理完成
fun test28() = runBlocking {
    // 启动一个协程来处理某种传入请求（request）
    val request = launch {
        repeat(3) { i -> // 启动少量的子作业
            launch  {
                delay((i + 1) * 200L) // 延迟 200 毫秒、400 毫秒、600 毫秒的时间
                println("Coroutine $i is done")
            }
        }
        println("request: I'm done and I don't explicitly join my children that are still active")
    }
    request.join() // 等待请求的完成，包括其所有子协程
    println("Now processing of the request is complete")
}

//通过给协程取名可以更好的调试
fun test29()= runBlocking{
    log("Started main coroutine")
     // 运行两个后台值计算
    val v1 = async(CoroutineName("v1coroutine")) {
        delay(500)
        log("Computing v1")
        252
    }
    val v2 = async(CoroutineName("v2coroutine")) {
        delay(1000)
        log("Computing v2")
        6
    }
    log("The answer for v1 / v2 = ${v1.await() / v2.await()}")

    //当然也可以设置调度器的同时，显式指定一个命名
    launch(Dispatchers.Default + CoroutineName("test")) {
        println("I'm working in thread ${Thread.currentThread().name}")
    }
}

//协程处理生命周期
//通过MainScope() 启动和取消 Application也相同
class TestActivity : Activity() {
    private val mainScope = MainScope()

    fun doSomething() {
        // 在示例中启动了 10 个协程，且每个都工作了不同的时长
        repeat(10) { i ->
            mainScope.launch {
                delay((i + 1) * 200L) // 延迟 200 毫秒、400 毫秒、600 毫秒等等不同的时间
                println("Coroutine $i is done")
            }
        }
    }

    fun destroy() {
        mainScope.cancel()
    }
}

suspend fun test30() {
    val activity = TestActivity()
    activity.doSomething() // 运行测试函数
    println("Launched coroutines")
    delay(500L) // 延迟半秒钟
    println("Destroying activity!")
    activity.destroy() // 取消所有的协程
    delay(1000) // 为了在视觉上确认它们没有工作
}

val threadLocal = ThreadLocal<String?>() // declare thread-local variable

//不是太理解，还得多看看，具体使用场景时才看一眼

//线程局部数据
//可以通过threadLocal存储线程数据，每次协程切换后恢复正常的值
fun test31() = runBlocking {
    threadLocal.set("main")
    println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
        threadLocal.set("launch change")
        println("Launch start, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
        yield()
        println("After yield, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    }
    job.join()
    println("Post-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
}