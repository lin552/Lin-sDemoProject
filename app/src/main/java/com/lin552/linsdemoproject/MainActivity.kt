package com.lin552.linsdemoproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.lin552.linsdemoproject.CoordinatorLayoutUse.CoordinatorLayoutActivity
import com.lin552.linsdemoproject.RxJavaTest.RxJavaTestActivity
import com.lin552.linsdemoproject.WorkManagerUse.WorkManagerTestActivity

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(Companion.TAG, "onCreate: ")
        setContentView(R.layout.main_activity_layout)

        findViewById<TextView>(R.id.text1).setOnClickListener {
            startActivity(Intent(this@MainActivity, CoordinatorLayoutActivity::class.java))
        }

        findViewById<TextView>(R.id.text2).setOnClickListener {
            startActivity(Intent(this@MainActivity, RxJavaTestActivity::class.java))
        }

        findViewById<TextView>(R.id.text3).setOnClickListener {
            startActivity(Intent(this@MainActivity, WorkManagerTestActivity::class.java))
        }
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
    }
}