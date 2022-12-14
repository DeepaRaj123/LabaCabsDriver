package com.laba.partner.widget

import android.R.attr
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.*
import com.laba.partner.R
import com.laba.partner.ui.activity.main.MainActivity
import kotlin.math.abs


class SimpleFloatingWindow constructor(private val context: Context) {

    private var windowManager: WindowManager? = null
        get() {
            if (field == null) field = (context.getSystemService(WINDOW_SERVICE) as WindowManager)
            return field
        }

    private var floatView: View =
        LayoutInflater.from(context).inflate(R.layout.widget_chathead, null)

    private lateinit var layoutParams: WindowManager.LayoutParams

    private var lastX: Int = 0
    private var lastY: Int = 0
    private var firstX: Int = 0
    private var firstY: Int = 0

    private var isShowing = false
    private var touchConsumedByMove = false
    var CLICK_ACTION_THRESHOLD = 0;
    var startX: Float? = null
    var startY: Float? = null
    private val onTouchListener = View.OnTouchListener { view, event ->
        val totalDeltaX = lastX - firstX
        val totalDeltaY = lastY - firstY

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                firstX = lastX
                firstY = lastY
                startX = event.x
                startY = event.y
            }
            MotionEvent.ACTION_UP -> {
                view.performClick()
                val endX = event.x
                val endY = event.y

                if (startX != null && startY != null) {
                    if (isAClick(startX!!.toFloat(), endX, startY!!.toFloat(), endY)) {
                        if (isAppIsInBackground(view.context)) {
                            val intent = Intent(view.context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                            view.context.startActivity(intent)
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX.toInt() - lastX
                val deltaY = event.rawY.toInt() - lastY
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                if (abs(totalDeltaX) >= 5 || abs(totalDeltaY) >= 5) {
                    if (event.pointerCount == 1) {
                        layoutParams.x += deltaX
                        layoutParams.y += deltaY
                        touchConsumedByMove = true
                        windowManager?.apply {
                            updateViewLayout(floatView, layoutParams)
                        }
                    } else {
                        touchConsumedByMove = false
                    }
                } else {
                    touchConsumedByMove = false
                }
            }
            else -> {
            }
        }
        touchConsumedByMove
    }

    private fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val differenceX = abs(startX - endX)
        val differenceY = abs(startY - endY)
        Log.e("eeee","$differenceX | $differenceY")
        Log.e("eeee!","${differenceX > CLICK_ACTION_THRESHOLD} | ${differenceY > CLICK_ACTION_THRESHOLD}")
        return !(differenceX > CLICK_ACTION_THRESHOLD /* =5 */ || differenceY > CLICK_ACTION_THRESHOLD)
    }

    init {
        with(floatView) {
            //closeImageButton.setOnClickListener { dismiss() }
            /*vFloat.setOnClickListener {
                if (isAppIsInBackground(it.context)) {
                    val intent = Intent( it.context,MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                    it.context.startActivity(intent)
                }
            }*/
            /* vFloat?.setSafeOnClickListener {
                 if (isAppIsInBackground(it.context)) {
                     val intent = Intent( it.context,MainActivity::class.java)
                     intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                     it.context.startActivity(intent)
                 }
             }*/

           // textView.text = "Labaaaa"
        }

        floatView.setOnTouchListener(onTouchListener)

        layoutParams = WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            @Suppress("DEPRECATION")
            type = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else -> WindowManager.LayoutParams.TYPE_TOAST
            }

            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
    }

    fun toggle() {
        if (isShowing){
            dismiss()
        }else{
            show()
        }
    }

    fun show() {
        if (context.canDrawOverlays && !isShowing) {
            dismiss()
            isShowing = true
            windowManager?.addView(floatView, layoutParams)
        }

    }

    fun dismiss() {
        if (isShowing) {
            windowManager?.removeView(floatView)
            isShowing = false
        }
    }

    fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }

    class SafeClickListener(
        private var defaultInterval: Int = 1000,
        private val onSafeCLick: (View) -> Unit
    ) : View.OnClickListener {
        private var lastTimeClicked: Long = 0
        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
                return
            }
            lastTimeClicked = SystemClock.elapsedRealtime()
            onSafeCLick(v)
        }
    }

    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo!!.packageName == context.packageName) {
                isInBackground = false
            }
        }
        return isInBackground
    }
}
val Context.canDrawOverlays: Boolean
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)