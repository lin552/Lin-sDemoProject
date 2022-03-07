package com.lin552.linsdemoproject

import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.util.Log
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Application
 */
open class MainApplication : Application() {


    companion object {

        //单例化的第一种方式：声明一个简单的Application属性
        //情况一：声明可空的属性
        private var instance: MainApplication? = null
        fun instance() = instance!!
        //情况二：声明延迟初始化属性
        //private lateinit var instance: MainApplication
        //fun instance() = instance


        //单例化的第二种方式：利用系统自带的Delegates生成委托属性
        private var INSTANCE_1: MainApplication by Delegates.notNull()
        fun instance1() = instance

        //单例化的第三种方式：自定义一个非空且只能一次性赋值的委托属性
        private var INSTANCE_2: MainApplication by NotNullSingleValueVar()
        fun instance2() = instance

    }

    //定义一个属性管理类，进行非空和重复赋值的判断
    private class NotNullSingleValueVar<T>() : ReadWriteProperty<Any?, T> {
        private var value: T? = null
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return value ?: throw IllegalStateException("application not initialized")
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this.value = if (this.value == null) value
            else throw IllegalStateException("application already initialized")
        }
    }

    override fun registerComponentCallbacks(callback: ComponentCallbacks?) {
        super.registerComponentCallbacks(callback)
    }

    override fun unregisterComponentCallbacks(callback: ComponentCallbacks?) {
        super.unregisterComponentCallbacks(callback)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("App", "onCreate: App被初始化")
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    override fun registerActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacks?) {
        super.registerActivityLifecycleCallbacks(callback)
    }

    override fun unregisterActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacks?) {
        super.unregisterActivityLifecycleCallbacks(callback)
    }

    override fun registerOnProvideAssistDataListener(callback: OnProvideAssistDataListener?) {
        super.registerOnProvideAssistDataListener(callback)
    }

    override fun unregisterOnProvideAssistDataListener(callback: OnProvideAssistDataListener?) {
        super.unregisterOnProvideAssistDataListener(callback)
    }
}