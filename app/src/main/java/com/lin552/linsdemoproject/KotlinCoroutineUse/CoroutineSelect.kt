package com.lin552.linsdemoproject.KotlinCoroutineUse

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.onReceiveOrNull
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select

/**
 * Select表达式在Kotinx.coroutines 中的一个实验性的特性。这些API
 * 在Kotlinx.coroutines库即将到来的更新中可能有所改变
 */
fun main(){
    testSelect2()
}

/**
 * 在通道中的select
 */
fun CoroutineScope.fizz() = produce<String> {
    while (true){
        delay(300) //每300毫秒发送一个 "Fizz"
        send("Fizz")
    }
}

fun CoroutineScope.buzz() = produce<String>{
    while (true){
        delay(500)//每500毫秒发送一个"Buzz!"
        send("Buzz!")
    }
}

suspend fun selectFizzBuzz(fizz: ReceiveChannel<String>, buzz: ReceiveChannel<String>) {
    select<Unit> {
        fizz.onReceive { value ->
            println("fizz -> $value")
        }
        buzz.onReceive { value ->
            println("buzz -> $value")
        }
    }
}

fun testSelect1() = runBlocking {
    val fizz = fizz()
    val buzz = buzz()
    repeat(7){
        selectFizzBuzz(fizz, buzz)
    }
    coroutineContext.cancelChildren() //取消fizz 和 buzz 协程
}

///**
// * 通道关闭时 select
// */
//suspend fun selectAorB(a:ReceiveChannel<String>,b: ReceiveChannel<String>):String =
////    select<String> {
////        a.onReceiveOrNull { value ->
////            if (value == null)
////                "Channel 'a' is closed"
////            else
////                "a -> $value"
////        }
////        b.onReceiveOrNull{ value ->
////            if (value == null)
////                "Channel 'b' is closed"
////            else
////                "b -> '$value'"
////        }
////    }

fun testSelect2() = runBlocking {
    val a = produce<String> {
        repeat(4) {
            send("Hello $it")
        }
    }
    val b = produce<String> {
        repeat(4){
            send("World $it")
        }
    }
    repeat(8){
//        println(selectAorB(a, b))
    }
    coroutineContext.cancelChildren()
}