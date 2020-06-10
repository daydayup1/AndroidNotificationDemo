package com.goudan.notificationtest

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.Nullable

class NotificationClickEvent : Service() {

    private var notificationId: Int = 0

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // 获取RemoteInput中的Result
        Log.e("NotificationClickEvent", "NotificationClickEvent.onStartCommand")

        notificationId = intent.getIntExtra(Util.NOTIFICATION_ID, 0)

        onReply()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun onReply() {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val handler = Handler(mainLooper)
        handler.post {
            // 更新通知为“回复成功”
            val builder = Util.createBaseNotificationBuilder(MyApplication.getApplication(), notificationId)
            builder.setContentText("已点击消息体，本通知即将消失")
            val notification = builder.build()
            manager.notify(Util.NOTIFICATION_TAG, notificationId, notification)
        }

        // 最后将通知取消
        handler.postDelayed({ manager.cancel(Util.NOTIFICATION_TAG, notificationId) }, 2000)
    }
}