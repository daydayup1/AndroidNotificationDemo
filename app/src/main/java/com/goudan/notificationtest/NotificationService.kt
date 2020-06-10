package com.goudan.notificationtest

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.annotation.Nullable

class NotificationService : Service() {

    private var notificationId: Int = 0

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // 获取RemoteInput中的Result
        Log.e("NotificationService", "onStartCommand")

        notificationId = intent.getIntExtra(Util.NOTIFICATION_ID, 0)

        var replyBundle: Bundle? = null
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            replyBundle = android.app.RemoteInput.getResultsFromIntent(intent)
        }
        if (replyBundle != null) {
            // 根据key拿到回复的内容
            val reply = replyBundle.getString(Util.RESULT_KEY)
            reply(reply)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun reply(reply: String?) {
        Thread(Runnable {
            // 模拟延迟1000ms，然后调用onReply
            SystemClock.sleep(1000)
            Log.e("reply", "reply: " + reply!!)
            onReply()
        }).start()
    }

    private fun onReply() {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val handler = Handler(mainLooper)
        handler.post {
            // 更新通知为“回复成功”
            val builder = Util.createBaseNotificationBuilder(MyApplication.getApplication(), notificationId)
            builder.setContentText("回复成功")
            val notification = builder.build()
            manager.notify(Util.NOTIFICATION_TAG, notificationId, notification)
        }

        // 最后将通知取消
        handler.postDelayed({ manager.cancel(Util.NOTIFICATION_TAG, notificationId) }, 2000)
    }
}