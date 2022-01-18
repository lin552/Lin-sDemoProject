package com.lin552.linsdemoproject.KotlinCoroutineUse

import kotlinx.coroutines.*
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import kotlin.AssertionError

/**
协程异常处理
 **/

fun main() {
    testException9()
}

/**
 * 协程构建器两种形式
 * 1.自动传播异常(launch与actor)
 * 2.向用户暴露异常(async与produce)
 *
 * 根协程的异常无法捕获，属于未捕获异常
 * 后者依赖用户最终来消费异常，通过await或receive
 */
fun testException1() = runBlocking {
    val job = GlobalScope.launch {  //launch 根协程
        println("Throwing exception from launch ")
        throw IndexOutOfBoundsException() //我们将在控制台打印 Thread.defaultUncaughtExceptionHandler
    }
    job.join()
    println("Joined failed job")
    val deferred = GlobalScope.async {  //async 根协程
        println("Throwing exception from async") //没有打印任何东西，依赖用户去调用等待
    }
    try {
        deferred.await()
        println("Unreached")
    } catch (e: ArithmeticException) {
        println("Caught ArithmeticException")
    }

}

/**
 * CoroutineExceptionHandler
 * 未捕获的异常，
 */
fun testException2() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }
    val job = GlobalScope.launch(handler) { //根协程，运行在GlobalScope 中
        throw AssertionError()
    }
    val deferred = GlobalScope.async(handler) { //同样是根协程，但使用async代替了launch
        throw ArithmeticException() //没有打印任何东西，依赖用户去调用deferred.await()
    }
    joinAll(job, deferred)

}

/**
 * 取消与异常
 * 协程内部使用CancellationException 来进行取消
 * 当一个协程使用job.cancel 取消的时候，它会被终止，但是它不会取消它的父协程
 */
fun testException3() = runBlocking {
    val job = launch {
        val child = launch {
            try {
                delay(Long.MAX_VALUE)
            } finally {
                println("Child is cancelled")
            }
        }
        yield()
        println("Child is cancelled")
        child.cancel()
        child.join()
        yield()
        println("Parent is not cancelled")
    }
    job.join()
}

/**
 * 当父协程的所有子协程都结束后，原始的异常才会被父协程处理
 */
 fun testException4() = runBlocking {
    val handler = CoroutineExceptionHandler { _, throwable ->
        println("CoroutineExceptionHandler got $throwable")
    }
    val job = GlobalScope.launch(handler) {
        launch {  //第一个子协程
            try {
                delay(Long.MAX_VALUE)
            } finally {
                withContext(NonCancellable){
                    println("Children are cancelled ,but exception is not handler until all childer")
                    delay(100)
                    println("The first child finished its non cancellable block")
                }
            }
        }
        launch {  //第二个子协程
            delay(10)
            println("Second child throws an exception")
        }
    }
    //取消父协程
    job.join()

}

/**
 * 异常聚合
 * 当协程的多个子协程因异常而失败时，一般规则是"取第一个异常，因此此处理第一个异常。
 * 在第一个异常之后发生的所有其他异常都作为被抑制的异常绑定至第一异常。
 */
fun testException5() = runBlocking {
    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        println("CoroutineException got $throwable with suppressed ${throwable.suppressed.contentToString()}")
    }
    val job = GlobalScope.launch(handler) {
        launch {
            try {
                delay(Long.MAX_VALUE) //当另一个同级的协程因 IOException 失败时，它将被取消
            } finally {
                throw  ArithmeticException() //第二个异常
            }
        }
        launch {
            delay(100)
            throw IOException() //首个异常
        }
        delay(Long.MAX_VALUE)
    }
    job.join()

}

/**
 * 取消异常是透明的，默认情况下是未包装的
 */
fun testException6() = runBlocking {
    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        println("CoroutineExceptionHandler got $throwable")
    }
    val job = GlobalScope.launch(handler) {
        val inner = launch {  //该栈内的协程都将被取消
            launch {
                launch {
                    throw IOException() //原始异常
                }
            }
        }
        try {
            inner.join()
        } catch (e: Exception) {
            println("Rethrowing CancellationException with original cause")
            throw e //取消异常被重新抛出，但原始 IOException 得到了处理
        }
    }
    job.join()
}

/**
 * 监督作业
 *
 * supervisorJob 类似常规的Job
 * 不同的是 SupervisorJob的取消只会向下传播
 */
fun testException7() = runBlocking {
    val supervisor = SupervisorJob()
    with(CoroutineScope(coroutineContext + supervisor)) {
        //启动第一个子作业  这个示例将会忽略它的异常（切勿在实践中这样）
        val firstChild = launch(CoroutineExceptionHandler { _, _ ->  }){
            println("The first is failing")
            throw AssertionError("The first child is cancelled")
        }
        //启动第二个子作业
        val secondChild = launch {
            firstChild.join()
            //取消了第一个子作业且没有传播给第二个子作业
            println("The first child is cancelled: ${firstChild.isCancelled}, but the second one is still active")
            try {
                delay(Long.MAX_VALUE)
            } finally {
                //但是取消了监督的传播
                println("The second child is cancelled because the supervisor was cancelled")
            }
        }
        //等待知道第一个字作业失败且执行完成
        firstChild.join()
        println("Cancelling the supervisor")
        supervisor.cancel()
        secondChild.join()
    }
}

/**
 * 监督作用域
 *
 * 对于作用域的并发，可以用 supervisorScope 来替代 coroutineScope来实现相同的目的
 * 他只会单向的传播并且当作业自身执行失败的时候将所有的子作业全部取消。作业自身也会在所有
 * 的子作业执行结束前等待，就像coroutineScope所做的那样
 */
fun testException8() = runBlocking {
    try {
        supervisorScope {
            val child = launch {
                try {
                    println("The child is sleeping")
                    delay(Long.MAX_VALUE)
                } finally {
                    println("The child is cancelled")
                }
            }
            //使用yield 来给我们的子作业一个机会来执行打印
            yield()
            println("Throwing an exception from the scope")
            throw AssertionError()
        }
    } catch (e: Exception) {
        println("Caught an assertion error")
    }
}

/**
 * 监督协程中的异常
 *
 * 常规的作业和监督作业之间的另一个重要区别是异常处理。监督协程中的每一个子作业应该通过异常处理机制
 * 处理自身的异常。这种差异来自于子作业的执行失败不会传播给它的父作业的事实。这意味着在supervisorScope内部直接
 * 启动的协程确实使用了设置在它们作用域内的CoroutineExceptionHandler，与父协程的方式相同
 */
fun testException9() = runBlocking {
    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        println("CoroutineExceptionHandler got $throwable")
    }
    supervisorScope {
        val child = launch(handler) {
            println("The child throws an exception")
            throw AssertionError()
        }
        println("The scope is completing")
    }
    println("The scope is completed")
}