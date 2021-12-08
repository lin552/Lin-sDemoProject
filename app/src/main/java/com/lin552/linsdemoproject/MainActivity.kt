package com.lin552.linsdemoproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.lin552.linsdemoproject.CoordinatorLayoutUse.CoordinatorLayoutActivity
import com.lin552.linsdemoproject.Multi.MultiProcessActivity
import com.lin552.linsdemoproject.RxJavaTest.RxJavaTestActivity
import com.lin552.linsdemoproject.WorkManagerUse.WorkManagerTestActivity
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.*


class MainActivity : Activity() {


    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(Companion.TAG, "onCreate: ")
        setContentView(R.layout.main_activity_layout)
        val rootDir = MMKV.initialize(application)
//        val mmkv = MMKV.defaultMMKV()

        //测试mmkv多线程支持
        val mmkv = MMKV.mmkvWithID(this.javaClass.simpleName, MMKV.MULTI_PROCESS_MODE)

        testMMKVMainProcessToProcess(mmkv)
//        testMMKVMainToThread(mmkv)
//        testMMKVThreadToThread(mmkv)

        findViewById<TextView>(R.id.text1).setOnClickListener {
            startActivity(Intent(this@MainActivity, CoordinatorLayoutActivity::class.java))
        }

        findViewById<TextView>(R.id.text2).setOnClickListener {
            startActivity(Intent(this@MainActivity, RxJavaTestActivity::class.java))
        }

        findViewById<TextView>(R.id.text3).setOnClickListener {
            startActivity(Intent(this@MainActivity, WorkManagerTestActivity::class.java))
        }

        findViewById<TextView>(R.id.text5).setOnClickListener {
            startActivity(Intent(this@MainActivity, MultiProcessActivity::class.java))
        }
    }

    private fun testMMKVMainProcessToProcess(mmkv: MMKV) {
        Log.d(MMKV_TAG, "onCreate: 主进程 获取mmkv实例 $mmkv ")
        mmkv.encode("bool2",true)
        Log.d(MMKV_TAG, "主进程 创建 bool2 为true")
    }

    @DelicateCoroutinesApi
    private fun testMMKVThreadToThread(mmkv: MMKV) = runBlocking {
        //子线程创建，主线程读取
//        val bool1 = mmkv.decodeBool("bool1")
//        Log.d(MMKV_TAG, "onCreate: 主线程查询 bool1 $bool1")
        launch (newSingleThreadContext("MyOwnThread1")){
            mmkv.encode("bool1", true)
            Log.d(MMKV_TAG, "【${Thread.currentThread().name}】: 子线程创建 bool1 true")
        }
        delay(1000)
        val bVale1 = mmkv.encode("bool1", false)
        Log.d(MMKV_TAG, "【${Thread.currentThread().name}】: 主线程 bool1 修改为 false")
        val decodeBool = mmkv.decodeBool("bool1")
        Log.d(MMKV_TAG, "【${Thread.currentThread().name}】: 主线程 bool1 结果 $decodeBool")
    }

    @DelicateCoroutinesApi
    private fun testMMKVMainToThread(mmkv: MMKV) = runBlocking {

        //主线程存，子线程拿
        mmkv.encode("bool", false)
        Log.d(MMKV_TAG, "【${Thread.currentThread().name}】:主线程 创建 bool false")

        //开启子线程
        launch(newSingleThreadContext("MyOwnThread")){
            mmkv.encode("bool", true)
            Log.d(MMKV_TAG, "【${Thread.currentThread().name}】: 子线程 修改 bool 为true")
            val bValue: Boolean = mmkv.decodeBool("bool")
            Log.d(MMKV_TAG, "【${Thread.currentThread().name}】: 子线程 bool 结果 $bValue")
        }

        delay(1000L)
        val bValueMain: Boolean = mmkv.decodeBool("bool")
        Log.d(MMKV_TAG, "【${Thread.currentThread().name}】: 主线程 bool 结果 $bValueMain\" ")

    }

    override fun onStart() {
        Log.d(Companion.TAG, "MainActivity onStart: ")
        super.onStart()
    }

    override fun onResume() {
        Log.d(Companion.TAG, "MainActivity onResume: ")
        super.onResume()
    }

    override fun onPause() {
        Log.d(Companion.TAG, "MainActivity onPause: ")
        super.onPause()
    }

    override fun onRestart() {
        Log.d(Companion.TAG, "MainActivity onRestart: ")
        super.onRestart()
    }

    override fun onStop() {
        Log.d(Companion.TAG, "MainActivity onStop: ")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(Companion.TAG, "MainActivity onDestroy: ")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "Activity LifeCycle"
        private const val MMKV_TAG = "MMKV"
    }
}