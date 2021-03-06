package com.lin552.linsdemoproject.CoordinatorLayoutUse

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.lin552.linsdemoproject.R
import com.lin552.linsdemoproject.databinding.ActivityGlideTestBinding
import com.lin552.linsdemoproject.databinding.ConsraintLayoutTestBinding
import com.lin552.linsdemoproject.databinding.FragmentFirstBinding
import com.lin552.linsdemoproject.databinding.FragmentSecondBinding

/**
 * CoordinatorLayout 使用实例
 * 通过嵌套实现列表和动画同步的效果
 */
class CoordinatorLayoutActivity : Activity() {

    private lateinit var _binding: ConsraintLayoutTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "CoordinatorLayoutActivity onCreate: ")
        _binding = ConsraintLayoutTestBinding.inflate(layoutInflater)
        setContentView(_binding.root)
    }

    override fun onStart() {
        Log.d(TAG, "CoordinatorLayoutActivity onStart: ")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "CoordinatorLayoutActivity onResume: ")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "CoordinatorLayoutActivity onPause: ")
        super.onPause()
    }

    override fun onRestart() {
        Log.d(TAG, "CoordinatorLayoutActivity onRestart: ")
        super.onRestart()
    }

    override fun onStop() {
        Log.d(TAG, "CoordinatorLayoutActivity onStop: ")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "CoordinatorLayoutActivity onDestroy: ")
        super.onDestroy()
    }

    companion object {
        private const val TAG = "Activity LifeCycle"
    }

}