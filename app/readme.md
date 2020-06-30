#一、背景
android从4.0版本就开始支持了通知栏消息，到现在已经支持了近十种通知栏消息样式，本文通过用实例讲解各种通知栏样式的创建和使用，并简单说明“中国特色”的通知栏样式，减少初学者或者尝试将某种通知栏样式进行应用的老铁们踩坑的概率。鉴于kotlin已经成为谷歌的亲儿子，本文demo基于kotlin编写并完全开源，大家可以自行下载。
#二、消息上通知栏统一逻辑
简单来说，所有通知栏消息都首先需要使用```Notification.Builder(context)```设置通知栏消息的标题、内容、小图片等，然后像其他builder模式一样，调用下```Notification.Builder(context).build()```方法生成真正的可上通知栏的消息Notification，接着使用```getSystemService(Context.NOTIFICATION_SERVICE)```获取到NotificationManager并调用```NotificationManager.notify(id, notification)```将通知发送到通知栏上就可以了。
$\color{red}{注意：}$有的手机默认是关闭应用的通知栏权限，在发送消息上通知栏前最好判断下通知栏权限```NotificationManagerCompat.from(context).areNotificationsEnabled()```
#三、通知栏消息基本要素
其中，有以下三点是必须设置的：
3.1 通知栏的小图片是必须设置的：
```kotlin
// 必须调用setSmallIcon方法
Notification.Builder(context).setSmallIcon(R.drawable.small_icon)
```
否则在发送通知栏消息的时候会报错：
```
java.lang.IllegalArgumentException: Invalid notification (no valid small icon)
```
3.2 另外一个必设的是通知id，代表通知栏上的消息的“身份证号”：
```kotlin
// 通知栏消息上通知栏必须使用NotificationManager.notify方法，而该方法至少传入两个参数，一个是要上通知栏的消息，另一个就是通知id
(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)?.notify(notificationId, notification)
```
奇怪的是，通知栏消息的标题和内容竟然不是必须字段，没有设置这两个字段时消息仍然可以上通知栏，但会导致通知栏上标题和内容区均空白，显然也不是我们想要的样子。
最后通知栏的点击事件也不是必须设置的，但是一般正常都会去设置（谁希望自己的通知栏消息点击后没有反应呢）：
```kotlin
val clickIntent = Intent(context, NotificationClickEvent::class.java)
Notification.Builder(context).setContentIntent(clickPendingIntent)
```
3.3 如果是android 8以及上版本，则需要设置channelId(channel id是应用声明的，应用发送通知栏消息时将消息与某个channel绑定，用户可以在系统设置页面对具体的channel进行是否上通知栏、是否震动等个性化设置)：
```kotlin
Notification.Builder(context).setChannelId(PUSH_CHANNEL)
```
讲解了消息上通知栏的统一逻辑和要素后，接下来讲解下目前android支持的所有通知栏样式
#三、通知栏样式
##3.1 基础通知栏消息样式
基础通知消息样式是最简单、最常用、也是国内各大厂商支持的最好的一种样式：
```kotlin
// 更为详细和判断全面的代码请参考文末源码链接
val builder = Notification.Builder(context)
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    // small icon一般是应用的图标
    .setSmallIcon(R.drawable.small_icon)
    // largeIcon是显示在通知栏消息右侧的比smallIcon更大些的图，一般用来放和本消息强关联的图，没有large icon也可以
    .setLargeIcon(BitmapFactory.decodeResource(MyApplication.getApplication().resources, R.mipmap.person_icon))

NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
// notificationId是自己随便定义的int即可
manager?.notify(NOTIFICATION_TAG, notificationId, builder.build())
```
基础通知栏消息样式如下：
![基础通知栏.png](https://upload-images.jianshu.io/upload_images/20181727-1832c6ad43e53f75.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

有一个需要注意的点：国内有的厂商不管开发者设置的small icon是什么，都只会取该应用的图标作为small icon显示，可能其目的是为了明确当前通知是哪个应用的，避免出现假冒行为。

##3.2 remoteView类型的通知栏样式
remoteView类型的通知栏样式这里介绍三种，一种是将remoteView设置到Notification的contentView属性上，一种是设置到bigContentView，最后一种是和通知style的结合。具体我们来看下实现逻辑：
###3.2.1 设置到contentView上
```kotlin
val builder = Notification.Builder(context)
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    // 这里除了small icon必须设置以外，title和text都可以不用设置了，毕竟也不会显示出来
    .setSmallIcon(R.drawable.small_icon)
val notification = builder.build()
// 将remoteView设置在contentView属性上
notification.contentView = createBaseRemoteViews(context)
NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
manager?.notify(NOTIFICATION_TAG, notificationId, notification)

private fun createBaseRemoteViews(context: Context): RemoteViews {
    val remoteViews: RemoteViews = RemoteViews(context.packageName, R.layout.remote_view)
    // 如果想将remoteView类型的通知栏消息应用到自己上架的apk中，
    // 则最好考虑用户因为用了不同的主题，使通知栏颜色非默认色，导致通知栏上的remoteView和通知栏背景混为一体的情况，
    // 详细解决方法在demo中有写，与如下几行注释有关
    // getSysNotificationTextColor(context.applicationContext)
    // remoteViews.setTextColor(R.id.tv_title, MainActivity.sNotificationTitleColor)
    // remoteViews.setTextColor(R.id.tv_title1, MainActivity.sNotificationContentColor)
    // remoteViews.setTextColor(R.id.remote_view_top_text1, MainActivity.sNotificationContentColor)
    // remoteViews.setTextColor(R.id.remote_view_top_text2, MainActivity.sNotificationContentColor)
    return remoteViews
}
```
其中R.layout.remote_view布局样式如下：
![remoteView布局.png](https://upload-images.jianshu.io/upload_images/20181727-6e79f976635235d9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

通知栏效果图如下：
![普通ContentView通知.png](https://upload-images.jianshu.io/upload_images/20181727-bcdda277a9651384.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

可见remoteView在高度上被压缩了，这个原因是R.layout.remote_view布局高度写的是match_parent，因此在开发中务必注意自定义的remoteView高度，避免被系统压缩了。那么如果remoteView不设置高度为match_parent，而是固定死高度更小或更大行不行呢？demo中有对remoteView高度修改的尝试，大家可以利用demo进行验证：
![remoteView高度修改入口.png](https://upload-images.jianshu.io/upload_images/20181727-1fd4b66abf26cdec.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


###3.2.2 设置到bigContentView上
如果将remoteView设置在bigContentView属性上，类似上述代码，新样式代码如下：
```kotlin
val builder = Notification.Builder(context)
    // title和text都需要设置
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    .setSmallIcon(R.drawable.small_icon)
val notification = builder.build()
// 将remoteView设置在bigContentView属性上
notification.bigContentView = createBaseRemoteViews(context)
NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
manager?.notify(NOTIFICATION_TAG, notificationId, notification)
```
效果图如下：
![bigContentView.gif](https://upload-images.jianshu.io/upload_images/20181727-f5a56d376df8d28c.gif?imageMogr2/auto-orient/strip)
###3.2.3 remoteView和DecoratedCustomViewStyle结合
仍然将remoteView设置到contentView上，但使用Notification.DecoratedCustomViewStyle，如下：
```kotlin
val builder = Notification.Builder(context)
    // title和text都需要设置
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    .setSmallIcon(R.drawable.small_icon)
val notification = builder
    // 和notification.contentView = remoteViews 效果同，android n以上建议用setCustomContentView
    .setCustomContentView(remoteViews)
    .setStyle(Notification.DecoratedCustomViewStyle())
    .build()
NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
manager?.notify(NOTIFICATION_TAG, notificationId, notification)
```
通知栏表现如下：
![remoteView-和DecoratedCustomViewStyle结合.gif](https://upload-images.jianshu.io/upload_images/20181727-c25cb2dff0777f25.gif?imageMogr2/auto-orient/strip)
可以见到与没有设置style的3.2.2内所述的样式的区别就是能正常显示出整个remoteView布局了，但是Notification.DecoratedCustomViewStyle()是android N及以上才有的。

总结下使用remoteView需要注意的事项：
1.将remoteView设置给contentView属性时，务必要注意自定义remoteView的高度设置，避免被系统压缩
2.将remoteView设置给bigContentView属性时，务必要注意有的手机将通知栏边缘圆角化了，可能会影响remoteView的边缘展示
3.使用remoteView要注意主题不同可能会造成通知栏背景色和remoteView颜色冲突等问题
4.如果你的项目用户量级比较大，使用remoteView会出现线上少量崩溃，大致log为```Bad notification posted from package XXXXXX: Couldn't expand RemoteViews for: StatusBarNotification …………```，这个crash还不太好解，如果你知道如何修复，请联系我
##3.3 单图通知
这种通知类型中也包含了remoteView的使用，但因为结合了builder.setContent使得通知栏样式比较独特，单独展示一下，这个类型的通知在某些app上用的还是蛮多的，尤其应用在运营活动上。
```kotlin
val builder = Notification.Builder(context)
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    // 这里除了small icon必须设置以外，title和text都可以不用设置了，毕竟也不会显示出来
    .setSmallIcon(R.drawable.small_icon)
val contentView = RemoteViews(context.packageName,R.layout.only_one_picture_type_layout)
// 将remoteView同时设置到setContent和bigContentView上
val notification = builder.setContent(contentView).build()
notification.bigContentView = contentView
NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
manager?.notify(NOTIFICATION_TAG, notificationId, notification)
```
效果如下所示：
![单图通知.gif](https://upload-images.jianshu.io/upload_images/20181727-d8d0376d67f570ed.gif?imageMogr2/auto-orient/strip)
##3.4 大图类型通知（bigPictureStyle）
该类型通知的特点是使用了Notification.BigPictureStyle()
```kotlin
val builder = Notification.Builder(context)
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    .setSmallIcon(R.drawable.small_icon)
val notification = builder.setStyle(
    Notification.BigPictureStyle()
        .bigPicture(BitmapFactory.decodeResource(context.resources, R.mipmap.largeicon))
        // 通知展开后largeIcon
        .bigLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.largeicon))
        // 通知展开后标题
        .setBigContentTitle(context.getString(R.string.notification_title))
        // 通知展开后内容
        .setSummaryText( context.getString(R.string.notification_text))
).build()
NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
manager?.notify(NOTIFICATION_TAG, notificationId, notification)
```
通知栏表现如下：
![bigPicture类型通知.gif](https://upload-images.jianshu.io/upload_images/20181727-fbeee1052237cbdd.gif?imageMogr2/auto-orient/strip)

##3.5 长文本类型通知（bigTextStyle）
该类型允许显示内容超长的通知栏消息，核心是使用了Notification.BigTextStyle()
```kotlin
val builder = Notification.Builder(context)
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    .setSmallIcon(R.drawable.small_icon)
val notification = builder.setStyle(
    Notification.BigTextStyle()
        .setBigContentTitle(context.getString(R.string.notification_title))
        // 在BigTextStyle()类型中SummaryText指的是应用名右侧的文案
        .setSummaryText("长文本类型通知")
        .bigText(context.getString(R.string.notification_text))
).build()
NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
manager?.notify(NOTIFICATION_TAG, notificationId, notification)
```
通知栏样式如下：
![bigTextStyle.gif](https://upload-images.jianshu.io/upload_images/20181727-f0c6336ba605598c.gif?imageMogr2/auto-orient/strip)

##3.6 收件箱类型通知（inboxStyle）
该类型消息支持按行添加消息内容，每行可以是一个新闻的标题，这样用户展开通知栏消息就会看到当前应用推送过来了哪些新闻，点击后跳转到统一的落地页。该类型核心是使用了Notification.InboxStyle()
```kotlin
val builder = Notification.Builder(context)
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    .setSmallIcon(R.drawable.small_icon)
val notification = builder.setStyle(
    Notification.InboxStyle()
        .addLine("尊敬的张三先生：").addLine("    首先感谢您注册账号。").addLine("    我们诚挚邀请您参与答题活动")
        .addLine("    该活动目前参与者较少，人均奖金较多")
        .addLine("祝：游戏愉快").addLine("您的小妲己").addLine("2018年1月17日")
        // 测试只能显示7行，下面的都显示不出来了
        .addLine("这里也可以显示？？")
        .setBigContentTitle("应用重要通知").setSummaryText("邀请函")
).build()
NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
manager?.notify(NOTIFICATION_TAG, notificationId, notification)
```
样式如下：
![inboxStyle.gif](https://upload-images.jianshu.io/upload_images/20181727-a1dd8163f3d909cb.gif?imageMogr2/auto-orient/strip)
##3.7 媒体类型通知（MediaStyle）
这种类型的通知适合音乐类的应用使用，通知栏消息中可以设置多个图片，并可响应不同的点击事件：
```kotlin
val builder = Notification.Builder(context)
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    .setSmallIcon(R.drawable.small_icon)
val notification = builder
    // Show controls on lock screen even when user hides sensitive content.
    .setVisibility(Notification.VISIBILITY_PUBLIC)
    .setSmallIcon(R.drawable.small_icon)
    // 这里加的图片应该是不能太大，不然显示的就是黑的一片
    // 添加的顺序会决定后面setShowActionsInCompactView方法设置显示的控件
    // 这里对不同图片可设置不同pendingIntent，用于响应点击事件
    .addAction(android.R.drawable.ic_media_previous, "Previous", mPendingIntent) // #0
    .addAction(android.R.drawable.ic_media_play, "Pause", mPendingIntent)  // #1
    .addAction(android.R.drawable.ic_media_next, "Next", mPendingIntent)     // #2
    // setShowActionsInCompactView的意义是在通知没有展开的时候，显示的是哪个action按钮
    .setStyle(
        Notification.MediaStyle().setShowActionsInCompactView(0, 1, 2 /* 设置通知栏未展开时显示所有三个按钮 */)
            .setMediaSession(MediaSession(context, "MediaSession").sessionToken)
    )
    .build()
NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
manager?.notify(NOTIFICATION_TAG, notificationId, notification)
```
需要注意设置的图片不能过大，不然在部分手机上会显示成黑色。
通知栏显示效果如下：
![mediaStyle.gif](https://upload-images.jianshu.io/upload_images/20181727-bfaeff7eff5576cc.gif?imageMogr2/auto-orient/strip)
##3.8 浮动式通知
即使用户正在浏览其他应用，该类通知也会直接以浮动的形式显示在屏幕顶端，几秒后消息并仅显示在通知栏中。
该类通知的实现方式是通知设置高优先级（如果是api 26及以上版本，则该通知关联的channel也应该设置高优）并设置震动或响铃。
```kotlin
val builder = Notification.Builder(context)
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    .setSmallIcon(R.drawable.small_icon)
builder.setPriority(Notification.PRIORITY_MAX)
    // 这里没有直接设置震动，而是以如下设置一个震动零毫秒的模式的方式取巧使"设置震动"这一条件满足，
   // 以免用户应用内设置了不震动，但实际振动的bug出现
    builder.setVibrate(LongArray(0))
val notification = builder.build()
NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
manager?.notify(NOTIFICATION_TAG, notificationId, notification)
```
需要注意的是：部分国内厂商对是否允许某应用发送浮动通知有专门的设置项，比如vivo展示浮动通知完全由用户在系统设置页是否打开"顶部预览"决定。
通知栏样式如下：
![headup.gif](https://upload-images.jianshu.io/upload_images/20181727-6623244e91a85df7.gif?imageMogr2/auto-orient/strip)
##3.9 可直接回复的通知
该类型通知可支持在通知栏直接回复当前通知消息，其原理有两点：第一，通知栏消息上增加一个无图的action（是不是很熟悉，在3.7设置媒体类型通知的时候也用到了action，不过那个是设置了图片的），这样系统会在通知底部添加一个类似文本样式的可设置点击事件的控件，此时点击时会执行action中设置的pendingIntent；第二，使用action中的addRemoteInput方法对action重新设置点击后的行为，也就是变成输入框供用户输入，用户点击发送按钮会调用action中设置的pendingIntent，执行自定义逻辑，比如请求server回复用户消息。额外的，我们还可以用```setChoices(strings)```对remoteInput添加可选信息，即一键快捷回复。
代码如下：
```kotlin
val builder = Notification.Builder(context)
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    .setSmallIcon(R.drawable.small_icon)
val strings = arrayOf("吃饭呢", "睡觉呢")
// remoteInput用于显示输入框和头像右侧的回复标识，会将action中设置的pendingIntent转移设置到用户点击发送按钮上
// setChoices为智能回复（快捷回复）
val remoteInput = RemoteInput.Builder(RESULT_KEY).setChoices(strings).build()
val action = Notification.Action.Builder(null, "回复消息", mPendingIntent)
    .addRemoteInput(remoteInput).build()
builder.addAction(action)
val notification = builder.build()
NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
manager?.notify(NOTIFICATION_TAG, notificationId, notification)
```
在这个类型通知栏消息上，不同厂商的表现形式也不完全一样，如下分别是vivo和google的表现：
![vivo手机直接回复样式.gif](https://upload-images.jianshu.io/upload_images/20181727-bc4ca0e081556962.gif?imageMogr2/auto-orient/strip)
![google手机直接回复样式.gif](https://upload-images.jianshu.io/upload_images/20181727-fe7b873d79ffc071.gif?imageMogr2/auto-orient/strip)
在我调研后，得出以下结论：android 7及以上手机，华为、OPPO、VIVO、三星、小米、金立、魅族、谷歌、锤子这些厂商中，除了锤子之外，都支持通知栏直接回复这种类型消息，但是每个厂商具体样式不完全一样。
##3.10 含对方头像和图片的通知类型
这种通知的展现形式更为适合展示即时聊天信息，因为这种通知展现的图片是在左边的，看着更符合习惯。这种通知可以显示对方发来的文字或者图片，搭配通知栏直接回复的功能，会给用户带来很大的便利。
```kotlin
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

val builder = Notification.Builder(context)
    .setContentTitle("消息标题")
    .setContentText("消息内容")
    .setSmallIcon(R.drawable.small_icon)
builder.style = Notification.MessagingStyle(p).addMessage(message)
builder.addAction(action)
val notification = builder.build()
NotificationManager manager = MyApplication.getApplication()
    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
manager?.notify(NOTIFICATION_TAG, notificationId, notification)
```
vivo和google手机通知栏样式如下：
![vivo-person通知.gif](https://upload-images.jianshu.io/upload_images/20181727-864e59dd104e0587.gif?imageMogr2/auto-orient/strip)
![google-person通知.gif](https://upload-images.jianshu.io/upload_images/20181727-8b7456eb49baeb19.gif?imageMogr2/auto-orient/strip)
#四、总结
从android 4到目前的android 11，通知栏样式主要为以上几种，大家可以将多种样式进行组合，玩出不同的花样。但是要注意同样的代码，在不同的厂商手机上通知栏可能有不一样的表现形式，如果想应用某种通知栏样式到自己上架apk中，务必多找些国内机型占比较多的手机测一下。
大家也可以去官网了解每种通知栏样式：[https://developer.android.com/guide/topics/ui/notifiers/notifications?hl=zh-cn](https://developer.android.com/guide/topics/ui/notifiers/notifications?hl=zh-cn)
。官网不足的就是部分样式只有代码片段，没有完整的demo以及效果图，也没有更详细一些的讲解和注释。