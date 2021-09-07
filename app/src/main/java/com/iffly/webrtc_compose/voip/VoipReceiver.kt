package com.iffly.webrtc_compose.voip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AsyncPlayer
import com.iffly.rtcchat.SkyEngineKit
import com.iffly.webrtc_compose.App
import com.iffly.webrtc_compose.MainActivity


class VoipReceiver : BroadcastReceiver() {
    private var ringPlayer: AsyncPlayer? = null
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (Utils.ACTION_VOIP_RECEIVER == action) {
            val room = intent.getStringExtra("room")
            val audioOnly = intent.getBooleanExtra("audioOnly", true)
            val inviteId = intent.getStringExtra("inviteId")
            var inviteUserName = intent.getStringExtra("inviteUserName")
            val userList = intent.getStringExtra("userList")
            val list = userList!!.split(",").toTypedArray()
            SkyEngineKit.init(VoipEvent)

            if (inviteUserName == null) {
                inviteUserName = "p2pChat"
            }

            App.instance?.otherUserId = inviteId!!
            App.instance?.roomId = room!!

            if (list.size == 1) {
//                CallActivity.startCallActivity(context = context)
                MainActivity.startActivity(context = context)

            } else {
                // 群聊
            }

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                if (com.iffly.webrtc_compose.util.Utils.isAppRunningForeground()) {
//                    onForegroundOrBeforeVersionO(
//                        room,
//                        userList,
//                        inviteId,
//                        audioOnly,
//                        inviteUserName,
//                        true
//                    )
//                } else {
//                    onBackgroundAfterVersionO(room, userList, inviteId, audioOnly, inviteUserName)
//                }
//            } else {
//                onForegroundOrBeforeVersionO(
//                    room,
//                    userList,
//                    inviteId,
//                    audioOnly,
//                    inviteUserName,
//                    com.iffly.webrtc_compose.util.Utils.isAppRunningForeground()
//                )
//            }
        }
    }

//    private fun onBackgroundAfterVersionO(
//        room: String?, userList: String?,
//        inviteId: String?, audioOnly: Boolean, inviteUserName: String
//    ) {
//        val strArr = userList!!.split(",").toTypedArray()
//        val list = ArrayList<String>()
//        for (str in strArr) list.add(str)
//        SkyEngineKit.init(VoipEvent())
//        val activity: Activity =
//            ActivityStackManager.instance!!.getTopActivity() as BaseActivity
//        // 权限检测
//        val per: Array<String>
//        per = if (audioOnly) {
//            arrayOf(Manifest.permission.RECORD_AUDIO)
//        } else {
//            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
//        }
//        val hasPermission: Boolean = Permissions.has(activity, per)
//        if (hasPermission) {
//            onBackgroundHasPermission(activity, room, list, inviteId, audioOnly, inviteUserName)
//        } else {
//            val notification = CallForegroundNotification(App.instance!!)
//            notification.sendRequestIncomingPermissionsNotification(
//                activity,
//                room,
//                userList,
//                inviteId,
//                inviteUserName,
//                audioOnly
//            )
//        }
//    }
//
//    private fun onBackgroundHasPermission(
//        context: Context, room: String?, list: ArrayList<String>,
//        inviteId: String?, audioOnly: Boolean, inviteUserName: String
//    ) {
//        val b =
//            SkyEngineKit.Instance().startInCall(App.instance!!, room!!, inviteId!!, audioOnly)
//        LogUtils.dTag(TAG, "onBackgroundHasPermission b = $b")
//        if (b) {
//            App.instance!!.setOtherUserId(inviteId)
//            if (list.size == 1) {
//                val notification = CallForegroundNotification(App.instance!!)
//                notification.sendIncomingCallNotification(
//                    App.instance!!,
//                    inviteId,
//                    false,
//                    inviteUserName,
//                    audioOnly,
//                    true
//                )
//            }
//        }
//    }
//
//    private fun onForegroundOrBeforeVersionO(
//        room: String?, userList: String?,
//        inviteId: String?, audioOnly: Boolean, inviteUserName: String, isForeGround: Boolean
//    ) {
//        val strArr = userList!!.split(",").toTypedArray()
//        val list = ArrayList<String>()
//        for (str in strArr) list.add(str)
//        SkyEngineKit.init(VoipEvent())
//        val activity: BaseActivity =
//            ActivityStackManager.instance!!.getTopActivity() as BaseActivity
//        // 权限检测
//        val per: Array<String>
//        per = if (audioOnly) {
//            arrayOf(Manifest.permission.RECORD_AUDIO)
//        } else {
//            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
//        }
//        val hasPermission: Boolean = Permissions.has(activity, per)
//        LogUtils.dTag(
//            TAG,
//            "onForegroundOrBeforeVersionO hasPermission = $hasPermission, isForeGround = $isForeGround"
//        )
//        if (hasPermission) {
//            onHasPermission(activity, room, list, inviteId, audioOnly, inviteUserName)
//        } else {
//            ringPlayer = AsyncPlayer(null)
//            shouldStartRing(true) //来电先响铃
//            if (isForeGround) {
//                Alerter.create(activity).setTitle("来电通知")
//                    .setText(
//                        "您收到" + inviteUserName + "的来电邀请，请允许"
//                                + (if (audioOnly) "录音" else "录音和相机") + "权限来通话"
//                    )
//                    .enableSwipeToDismiss()
//                    .setBackgroundColorRes(R.color.colorAccent) // or setBackgroundColorInt(Color.CYAN)
//                    .setDuration(60 * 1000)
//                    .addButton("确定", R.style.AlertButtonBgWhite) { v ->
//                        Permissions.request(activity, per) { integer ->
//                            shouldStopRing()
//                            Log.d(TAG, "Permissions.request integer = $integer")
//                            if (integer === 0) { //权限同意
//                                onHasPermission(
//                                    activity,
//                                    room,
//                                    list,
//                                    inviteId,
//                                    audioOnly,
//                                    inviteUserName
//                                )
//                            } else {
//                                onPermissionDenied(room, inviteId)
//                            }
//                            Alerter.hide()
//                        }
//                    }
//                    .addButton("取消", R.style.AlertButtonBgWhite) { v ->
//                        shouldStopRing()
//                        onPermissionDenied(room, inviteId)
//                        Alerter.hide()
//                    }.show()
//            } else {
//                val notification = CallForegroundNotification(App.instance!!)
//                notification.sendRequestIncomingPermissionsNotification(
//                    activity,
//                    room,
//                    userList,
//                    inviteId,
//                    inviteUserName,
//                    audioOnly
//                )
//            }
//        }
//    }
//
//    private fun onHasPermission(
//        context: Context, room: String?, list: ArrayList<String>,
//        inviteId: String?, audioOnly: Boolean, inviteUserName: String
//    ) {
//        val b =
//            SkyEngineKit.Instance().startInCall(App.instance!!, room!!, inviteId!!, audioOnly)
//        LogUtils.dTag(TAG, "onHasPermission b = $b")
//        if (b) {
//            App.instance!!.setOtherUserId(inviteId)
//            LogUtils.dTag(TAG, "onHasPermission list.size() = " + list.size)
//            if (list.size == 1) {
//                //以视频电话拨打，切换到音频或重走这里，结束掉上一个，防止对方挂断后，下边还有一个通话界面
//                if (context is CallSingleActivity) {
//                    (context as CallSingleActivity).finish()
//                }
//                CallSingleActivity.openActivity(
//                    context,
//                    inviteId,
//                    false,
//                    inviteUserName,
//                    audioOnly,
//                    true
//                )
//            } else {
//                // 群聊
//            }
//        } else {
//            val activity: Activity = ActivityStackManager.instance!!.getTopActivity()
//            activity.finish() //销毁掉刚才拉起的
//        }
//    }
//
//    // 权限拒绝
//    private fun onPermissionDenied(room: String?, inviteId: String?) {
//        SkyEngineKit.Instance().sendRefuseOnPermissionDenied(room!!, inviteId!!) //通知对方结束
//        Toast.makeText(App.instance!!, "权限被拒绝，无法通话", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun shouldStartRing(isComing: Boolean) {
//        if (isComing) {
//            val uri = Uri.parse(
//                "android.resource://" + App.instance!!.getPackageName()
//                    .toString() + "/" + R.raw.incoming_call_ring
//            )
//            ringPlayer!!.play(App.instance!!, uri, true, AudioManager.STREAM_RING)
//        } else {
//            val uri = Uri.parse(
//                "android.resource://" + App.instance!!.getPackageName()
//                    .toString() + "/" + R.raw.wr_ringback
//            )
//            ringPlayer!!.play(App.instance!!, uri, true, AudioManager.STREAM_RING)
//        }
//    }
//
//    private fun shouldStopRing() {
//        Log.d(TAG, "shouldStopRing begin")
//        ringPlayer!!.stop()
//    }

    companion object {
        private const val TAG = "VoipReceiver"
    }
}