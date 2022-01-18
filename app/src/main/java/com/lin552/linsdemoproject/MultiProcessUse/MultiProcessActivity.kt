package com.lin552.linsdemoproject.Multi
import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.lin552.linsdemoproject.MultiProcessUse.BaseMMKVHelper
import com.lin552.linsdemoproject.R
import com.tencent.mmkv.MMKV

class MultiProcessActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multi_process_layout)
        val mmkv = BaseMMKVHelper.mmkv()
        Log.d(MMKV_TAG, "onCreate: 启动多进程页面")
        Log.d(MMKV_TAG, "onCreate: 非主进程 获取mmkv实例 $mmkv ")
        val decodeBool = mmkv.decodeBool("bool2")
        Log.d(MMKV_TAG, "onCreate: 非主进程 获取bool2 $decodeBool")
        mmkv.encode("bool3",false)
        Log.d(MMKV_TAG, "onCreate: 非主进程 创建bool3 false")
    }

    fun testMMKVProcessToProcess() {

    }

    companion object {
        private const val MMKV_TAG = "MMKV"
    }
}