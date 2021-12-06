package com.lin552.linsdemoproject.KotlinCoroutineUse

/**
 * 协程异步
 **/



fun main() {
    simple1().forEach { value -> println(value) }
}

fun simple(): List<Int> = listOf(1, 2, 3)

fun simple1(): Sequence<Int> = sequence { // 序列构建器
    for (i in 1..3) {
        Thread.sleep(100) // 假装我们正在计算
        yield(i) // 产生下一个值
    }
}