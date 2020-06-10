package com.goudan.notificationtest

import android.annotation.TargetApi
import android.app.*
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Icon
import android.media.session.MediaSession
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.*

class Util {

    companion object {
        const val BASE_NOTIFICATION = "基础类型通知"
        const val NORMAL_CONTENT_VIEW_NOTIFICATION = "普通ContentView通知"
        const val BIG_CONTENT_VIEW_NOTIFICATION = "bigContentView通知"
        const val BIG_PICTURE_STYLE = "大图类型通知(BigPictureStyle)"
        const val BIG_TEXT_STYLE = "长文本类型通知(BigTextStyle)"
        const val INBOX_STYLE = "收件箱类型通知(InboxStyle)"
        const val MEDIA_STYLE = "媒体类型通知(MediaStyle)"
        const val HEAD_UP_NOTIFICATION = "可悬浮于其他应用之上的通知"
        const val DECORATED_CUSTOM_VIEW_STYLE = "自定义view类型通知(DecoratedCustomViewStyle)"
        const val MESSAGING_STYLE = "消息类型通知(MessagingStyle)"
        const val DECORATE_MEDIA_CUSTOM_VIEW_STYLE = "自定义媒体类型通知(DecoratedMediaCustomViewStyle)"
        const val DIRECTLY_REPLY_NOTIFICATION = "可直接回复的通知"
        const val ONLY_ONE_PICTURE_REMOTE_VIEW = "单图通知(特型push)"
        const val INCLUDE_PERSON_AND_IMAGE_NOTIFICATION = "含对方头像和图片的通知类型"
        // 用于从RemoteInput中获取信息的关键字
        const val RESULT_KEY = "key_text_reply"
        // 当只允许通知栏上存在最多一条本应用通知时，用此值代表通知的唯一id
        private const val SINGLE_NOTIFICATION_ID = 666

        // 通知栏RemoteView高度分为四种：高(300dp)、中等(150dp)、矮(30dp)、默认(match parent)
        const val REMOTEVIEW_HIGH = 3
        const val REMOTEVIEW_MIDDLE = 2
        const val REMOTEVIEW_SHOT = 1
        const val REMOTEVIEW_DEFAULT = 0

        // 用于点击通知后取消通知时，知道该取消的通知是哪个，传入notification的pendingIntent中
        internal val NOTIFICATION_ID = "notification_id"
        var NOTIFICATION_TAG = "push"
        // push默认通道
        val PUSH_CHANNEL = "push_channel"
        // 通道名称
        val CHANNAL_NAME = "推送"

        var canCreateDuplicateNotification = false
        // 设置通知栏RemoteView的高度
        var notificationRemoteViewHeight: Int = 0
        private var hasChannel: Boolean = false
        private var manager: NotificationManager? = null

        fun getAllInfo(): HashMap<String, List<String>> {
            val allInfo = HashMap<String, List<String>>()

            val listViewTag0 = "基础通知"
            val baseNotifications = ArrayList<String>()
            baseNotifications.add(BASE_NOTIFICATION)
            baseNotifications.add(NORMAL_CONTENT_VIEW_NOTIFICATION)
            baseNotifications.add(BIG_CONTENT_VIEW_NOTIFICATION)
            baseNotifications.add(ONLY_ONE_PICTURE_REMOTE_VIEW)

            val listViewTag1 = "需api 16的"
            val needApi16 = ArrayList<String>()
            needApi16.add(BIG_PICTURE_STYLE)
            needApi16.add(BIG_TEXT_STYLE)
            needApi16.add(INBOX_STYLE)

            val listViewTag2 = "需api 21的"
            val needApi21 = ArrayList<String>()
            needApi21.add(MEDIA_STYLE)
            needApi21.add(HEAD_UP_NOTIFICATION)

            val listViewTag3 = "需api 24的"
            val needApi24 = ArrayList<String>()
            needApi24.add(DECORATED_CUSTOM_VIEW_STYLE)
            needApi24.add(MESSAGING_STYLE)
            needApi24.add(DECORATE_MEDIA_CUSTOM_VIEW_STYLE)
            needApi24.add(DIRECTLY_REPLY_NOTIFICATION)

            val listViewTag4 = "需api 28的"
            val needApi28 = ArrayList<String>()
            needApi28.add(INCLUDE_PERSON_AND_IMAGE_NOTIFICATION)

            allInfo[listViewTag0] = baseNotifications
            allInfo[listViewTag1] = needApi16
            allInfo[listViewTag2] = needApi21
            allInfo[listViewTag3] = needApi24
            allInfo[listViewTag4] = needApi28

            return allInfo
        }

        private fun generateNotificationId(): Int {
            return if (canCreateDuplicateNotification) {
                Random().nextInt()
            } else {
                SINGLE_NOTIFICATION_ID
            }
        }

        fun createBaseNotificationBuilder(context: Context, notificationId: Int): Notification.Builder {
            val clickIntent = Intent(context, NotificationClickEvent::class.java)
            clickIntent.putExtra(NOTIFICATION_ID, notificationId)
            // 这里使用的是getService，如果响应的是activity，则是getActivity
            val clickPendingIntent =
                PendingIntent.getService(context, 2, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            val builder = Notification.Builder(context)
                .setContentTitle("消息标题")
                .setContentText("消息内容")
                .setSmallIcon(R.drawable.small_icon)
                .setLargeIcon(BitmapFactory.decodeResource(MyApplication.getApplication().resources, R.mipmap.person_icon))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setTicker("1111")
                .setContentIntent(clickPendingIntent)

            if (Build.VERSION.SDK_INT >= 26) {
                if (!hasChannel) {
                    createChannel()
                }
                builder.setChannelId(PUSH_CHANNEL)
            }
            return builder
        }

        @TargetApi(Build.VERSION_CODES.O)
        fun createChannel() {
            val pushChannel = NotificationChannel(
                PUSH_CHANNEL, CHANNAL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            if (manager == null) {
                manager =
                    MyApplication.getApplication().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            manager?.createNotificationChannel(pushChannel)
            hasChannel = true
        }

        /**
         * 上通知栏
         */
        private fun notifyNotification(notification: Notification, notificationId: Int) {
            if (manager == null) {
                manager = MyApplication.getApplication().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            manager?.notify(NOTIFICATION_TAG, notificationId, notification)
        }

        /**
         * 生成基础通知
         */
        fun createBaseNotification(context: Context) {
            val notificationId = generateNotificationId()
            val builder = createBaseNotificationBuilder(context, notificationId)
            val notification = builder.build()
            notifyNotification(notification, notificationId)
        }

        /**
         * 生成基本RemoteView通知
         */
        fun createNormalRemoteViewNotification(context: Context) {
            val notificationId = generateNotificationId()
            val builder = createBaseNotificationBuilder(context, notificationId)
            val notification = builder.build()
            notification.contentView = createBaseRemoteViews(notificationRemoteViewHeight, context)
            notifyNotification(notification, notificationId)
        }

        /**
         * 生成bigRemoteView通知
         */
        fun createBigRemoteViewNotification(context: Context) {
            val notificationId = generateNotificationId()
            val builder = createBaseNotificationBuilder(context, notificationId)
            val notification = builder.build()
            notification.bigContentView = createBaseRemoteViews(notificationRemoteViewHeight, context)
            notifyNotification(notification, notificationId)
        }

        fun createOnlyOnePictureRemoteView(context: Context) {
            val contentView = RemoteViews(
                context.packageName,
                R.layout.only_one_picture_type_layout
            )
            val notificationId = generateNotificationId()
            val builder = createBaseNotificationBuilder(context, notificationId)
            builder.setContent(contentView)
            val notification = builder.build()
            notification.bigContentView = contentView
            notifyNotification(notification, notificationId)
        }

        /**
         * 根据传入的[height]不同显示不同高度的通知栏remoteView
         */
        private fun createBaseRemoteViews(height: Int, context: Context): RemoteViews {
            val remoteViews: RemoteViews = when (height) {
                REMOTEVIEW_HIGH -> RemoteViews(context.packageName, R.layout.high_remote_view)
                REMOTEVIEW_MIDDLE -> RemoteViews(context.packageName, R.layout.middle_remote_view)
                REMOTEVIEW_SHOT -> RemoteViews(context.packageName, R.layout.short_remote_view)
                // 默认的，占满父布局的remoteView
                REMOTEVIEW_DEFAULT -> RemoteViews(context.packageName, R.layout.remote_view)
                else -> RemoteViews(context.packageName, R.layout.remote_view)
            }
            getSysNotificationTextColor(context.applicationContext)
            remoteViews.setTextColor(R.id.tv_title, MainActivity.sNotificationTitleColor)
            remoteViews.setTextColor(R.id.tv_title1, MainActivity.sNotificationContentColor)
            remoteViews.setTextColor(R.id.remote_view_top_text1, MainActivity.sNotificationContentColor)
            remoteViews.setTextColor(R.id.remote_view_top_text2, MainActivity.sNotificationContentColor)
            return remoteViews
        }

        /**
         * 获取系统通知栏文案默认颜色
         */
        fun getSysNotificationTextColor(context : Context) {
            var builder: Notification.Builder = Notification.Builder(context)
            builder.setContentText(MainActivity.sNotificationContent)
            builder.setContentTitle(MainActivity.sNotificationTitle)
            var notification: Notification = builder.build()
            var group = LinearLayout(context)
            var remoteView: RemoteViews = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                builder.createContentView()
            else notification.contentView
            findViewInIterator(remoteView.apply(context, group) as ViewGroup)
            group.removeAllViews()
        }

        /**
         * 递归通知栏布局，获取title和content的颜色
         */
        private fun findViewInIterator(viewGroup: ViewGroup) {
            for (i in 0..viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                if (child is TextView) {
                    val textString = child.text.toString()
                    if (MainActivity.sNotificationTitle == textString)
                        MainActivity.sNotificationTitleColor = child.textColors.defaultColor
                    else if (MainActivity.sNotificationContent == textString)
                        MainActivity.sNotificationContentColor = child.textColors.defaultColor
                    val red = Color.red(MainActivity.sNotificationTitleColor)
                    val green = Color.green(MainActivity.sNotificationTitleColor)
                    val blue = Color.blue(MainActivity.sNotificationTitleColor)
                    val alpha = Color.alpha(MainActivity.sNotificationTitleColor)
                    if (MainActivity.sNotificationContentColor != null) return
                } else if (child is ViewGroup) {
                    findViewInIterator(child)
                }
            }
        }

        /**
         * 生成大图通知，需api 16
         */
        fun createBigPictureStyle(context: Context) {
            val notificationId = generateNotificationId()
            val builder = createBaseNotificationBuilder(context, notificationId)
            val notification = builder.setStyle(
                Notification.BigPictureStyle()
                    .bigPicture(BitmapFactory.decodeResource(context.resources, R.mipmap.largeicon))
                    // 通知展开后largeIcon
                    .bigLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.largeicon))
                    // 通知展开后标题
                    .setBigContentTitle(context.getString(R.string.notification_title))
                    // 通知展开后内容
                    .setSummaryText(context.getString(R.string.notification_text))
            ).build()
            notifyNotification(notification, notificationId)
        }

        /**
         * Create big text style，需api 16,bigText中最多可显示5120字符
         */
        fun createBigTextStyle(context: Context) {
            val notificationId = generateNotificationId()
            val builder = createBaseNotificationBuilder(context, notificationId)
            val notification = builder.setStyle(
                Notification.BigTextStyle()
                    .setBigContentTitle(context.getString(R.string.notification_title))
                    // 在BigTextStyle()类型中SummaryText指的是应用名右侧的文案
                    .setSummaryText("长文本类型通知")
                    .bigText(context.getString(R.string.notification_text))
            ).build()
            notifyNotification(notification, notificationId)
        }

        /**
         * Create inbox style，需api 16
         */
        fun createInboxStyle(context: Context) {
            val notificationId = generateNotificationId()
            val builder = createBaseNotificationBuilder(context, notificationId)
            val notification = builder.setStyle(
                Notification.InboxStyle()
                    .addLine("尊敬的张三先生：").addLine("    首先感谢您注册账号。").addLine("    我们诚挚邀请您参与答题活动")
                    .addLine("    该活动目前参与者较少，人均奖金较多")
                    .addLine("祝：游戏愉快").addLine("您的小妲己").addLine("2018年1月17日")
                    // 测试只能显示7行，下面的都显示不出来了
                    .addLine("这里也可以显示？？")
                    .setBigContentTitle("应用重要通知").setSummaryText("邀请函")
            ).build()
            notifyNotification(notification, notificationId)
        }

        /**
         * 展示媒体类型的通知，最多可以有三个图片，需api 21
         */
        fun createMediaStyle(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val mIntent = Intent(context, NotificationService::class.java)
                val mPendingIntent = PendingIntent.getService(context, 1, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                val notificationId = generateNotificationId()
                val builder = createBaseNotificationBuilder(context, notificationId)
                val notification = builder
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.small_icon)
                    // 这里加的图片应该是不能太大，不然显示的就是黑的一片
                    // 添加的顺序会决定后面setShowActionsInCompactView方法设置显示的控件
                    .addAction(android.R.drawable.ic_media_previous, "Previous", mPendingIntent) // #0
                    .addAction(android.R.drawable.ic_media_play, "Pause", mPendingIntent)  // #1
                    .addAction(android.R.drawable.ic_media_next, "Next", mPendingIntent)     // #2
                    // setShowActionsInCompactView的意义是在通知没有展开的时候，显示的是哪个action按钮
                    .setStyle(
                        Notification.MediaStyle().setShowActionsInCompactView(0, 1, 2 /* 设置通知栏未展开时显示所有三个按钮 */)
                            .setMediaSession(MediaSession(context, "MediaSession").sessionToken)
                    )
                    .build()
                notifyNotification(notification, notificationId)

            } else {
                Toast.makeText(context, "您的Android版本过低，需Android LOLLIPOP（api 21）及以上版本才能使用该功能", Toast.LENGTH_LONG)
                    .show()
            }
        }

        /**
         * Create messaging style.需api 24,right
         */
        fun createMessagingStyle(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val notificationId = generateNotificationId()
                val builder = createBaseNotificationBuilder(context, notificationId)
                val notification = builder.setContentTitle("有新的消息：").setContentText("消息如下：")
                    .setStyle(
                        Notification.MessagingStyle("Zhao liu")
                            .addMessage("你在家吗？", System.currentTimeMillis(), "Wang wu")
                            .addMessage("刚Zhang san是不是找你了？", 22, "Li si")
                            .addMessage("晚饭吃什么？", 22, "Li si")
                            .addMessage("一天都没有回我信息了，你在忙什么哪？", 22, "Li si")
                    )
                    .build()
                notifyNotification(notification, notificationId)
            } else {
                Toast.makeText(context, "您的Android版本过低，需Android N（api 24）及以上版本才能使用该功能", Toast.LENGTH_LONG).show()
            }

        }

        /**
         * 是MediaStyle的子类，可以自定义布局。需api 24
         */
        fun createDecoratedMediaCustomViewStyle(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val notificationId = generateNotificationId()
                val builder = createBaseNotificationBuilder(context, notificationId)
                val remoteViews = createBaseRemoteViews(notificationRemoteViewHeight, context)
                val notification = builder.setCustomContentView(remoteViews)
                    .setStyle(Notification.DecoratedMediaCustomViewStyle()).build()
                notifyNotification(notification, notificationId)
            } else {
                Toast.makeText(context, "您的Android版本过低，需Android N（api 24）及以上版本才能使用该功能", Toast.LENGTH_LONG).show()
            }

        }

        /**
         * 自定义布局的通知栏，需要api24,right
         */
        fun createDecoratedCustomViewStyle(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val notificationId = generateNotificationId()
                val builder = createBaseNotificationBuilder(context, notificationId)
                val remoteViews = createBaseRemoteViews(notificationRemoteViewHeight, context)
                val notification = builder
                    // 和notification.contentView = remoteViews 效果同，android n以上建议用setCustomContentView
                    .setCustomContentView(remoteViews)
                    .setStyle(Notification.DecoratedCustomViewStyle())
                    .build()
                notifyNotification(notification, notificationId)
            } else {
                Toast.makeText(context, "您的Android版本过低，需Android N（api 24）及以上版本才能使用该功能", Toast.LENGTH_LONG).show()
            }

        }

        /**
         * 可直接回复的通知，需要api 23，原因是Notification.Action.Builder需要api 23
         * 不同的手机厂商有不同的表现形式，有的手机能显示出可选择的回复列表，有的手机不能
         */
        fun createReplyStyleNotification(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val notificationId = generateNotificationId()
                val builder = createBaseNotificationBuilder(context, notificationId)

                val mIntent = Intent(context, NotificationService::class.java)
                mIntent.putExtra(NOTIFICATION_ID, notificationId)
                val mPendingIntent = PendingIntent.getService(context, 1, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                // 由于createBaseNotificationBuilder中设置了点击消息体的事件，所以这里需要设置一个点击消息体不会有响应的pendingIntent
                builder.setContentIntent(mPendingIntent)

                val strings = arrayOf("吃饭呢", "睡觉呢")
                // remoteInput用于显示输入框和头像右侧的回复标识，会将action中设置的pendingIntent转移设置到用户点击发送按钮上
                // setChoices为智能回复（快捷回复）
                val remoteInput = RemoteInput.Builder(RESULT_KEY).setChoices(strings).build()
                val action = Notification.Action.Builder(null, "回复消息", mPendingIntent)
                    .addRemoteInput(remoteInput).build()
                builder.addAction(action)
                val notification = builder.build()
                notifyNotification(notification, notificationId)
            } else {
                Toast.makeText(context, "您的Android版本过低，需Android N（api 24）及以上版本才能使用该功能", Toast.LENGTH_LONG).show()
            }
        }

        /**
         * 悬浮于其他应用之上的通知。
         * 以下三种方式都会使通知变成浮动通知：
         * 1.应用activity正处于全屏模式（应用使用了fullScreenIntent）
         * 2.在api25及以下版本上，通知使用了铃声或震动且具有高优先级
         * 3.api26及以上版本，该通知对应的channel具有很高的重要程度
         *
         * 浮动通知的正确展示应该如下：浮动通知（heads up）的出现需要设置通知为高优（PRIORITY_HIGH）及以上，
         * 并且设置震动或响铃，对通知channel也设置为高优。
         *
         * 目前看vivo展示浮动通知完全由用户在系统设置页是否打开"顶部预览"决定，
         * oppo（android 7以下）即使设置震动也不会以heads up样式展示，
         * 三星和金立可以通过设置通知高优和震动（或响铃）使通知以heads up样式展示。
         *
         * 但不能直接设置震动，而是以如下设置一个震动零毫秒的模式的方式取巧使"设置震动"这一条件满足，
         * 以免用户应用内设置了不震动，但实际振动的bug出现（不过本demo不含应用内设置震动的功能）。
         * 另外需注意，android O及以上是否震动仅与系统channel设置有关，无法应用内随时更改
         */
        fun createHeadUpNotification(context: Context) {
            val notificationId = generateNotificationId()
            val builder = createBaseNotificationBuilder(context, notificationId)
            builder.setPriority(Notification.PRIORITY_MAX)
            builder.setVibrate(LongArray(0))

            val notification = builder.build()

            Thread(Runnable {
                Thread.sleep(6000)
                notifyNotification(notification, notificationId)
            }).run()
        }

        @TargetApi(Build.VERSION_CODES.P)
        fun createIncludePersonAndImageNotification(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                Toast.makeText(context, "您的Android版本过低，需Android P（api 28）及以上版本才能使用该功能", Toast.LENGTH_LONG).show()
                return
            }
            // 生成通知id
            val notificationId = generateNotificationId()

//            // 运行时获取权限，android 6及以上有些权限需要运行时获取
//            val permissions = arrayOf("android.permission.READ_EXTERNAL_STORAGE")
//            ActivityCompat.requestPermissions(context as Activity, permissions, 1)

            // 读取本地的文件uri，作为通知栏上对方发来的图片
            val uri :Uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                    + context.packageName + "/"
                    + context.resources.getResourceTypeName(R.mipmap.largeicon) + "/"
                    + context.resources.getResourceEntryName(R.mipmap.largeicon))

            // 生成来信息的人的信息
            // 在小米android 8.1、三星android 8.0上使用非person参数的message不会显示图片，在vivo android 7.0上会将largeIcon显示出来，作为对方头像
            // 在索尼 android P上展示的是名字首字母
            val p = Person.Builder().setName("欧巴")
                .setIcon(Icon.createWithResource(context, R.mipmap.person_1)).build()

            val message = Notification.MessagingStyle.Message("干嘛呢？", 2000, p)
                // setData会覆盖上面的文案--"干嘛呢？"
                .setData("image/", uri)

            // 设置自动回复文案
            val strings = arrayOf("吃饭呢", "睡觉呢")
            val remoteInput = RemoteInput.Builder(RESULT_KEY).setChoices(strings).build()

            val mIntent = Intent(context, NotificationService::class.java)
            mIntent.putExtra(NOTIFICATION_ID, notificationId)
            val mPendingIntent = PendingIntent.getService(context, 1, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val action = Notification.Action.Builder(R.mipmap.ic_launcher, "回复消息", mPendingIntent)
                .addRemoteInput(remoteInput).build()

            val builder = createBaseNotificationBuilder(context, notificationId)
            builder.style = Notification.MessagingStyle(p).addMessage(message)
            builder.addAction(action)
            val notification = builder.build()
            notifyNotification(notification, notificationId)
        }
    }
}