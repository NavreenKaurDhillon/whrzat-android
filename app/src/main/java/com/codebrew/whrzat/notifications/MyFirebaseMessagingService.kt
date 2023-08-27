package com.codebrew.whrzat.notifications


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.codebrew.whrzat.R
import com.codebrew.whrzat.activity.HomeActivity
import com.codebrew.whrzat.event.ChatApi
import com.codebrew.whrzat.event.ChatHistoryRefershApi
import com.codebrew.whrzat.event.ChatSocketReferesh
import com.codebrew.whrzat.event.RefershNotificationApi
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs
import org.greenrobot.eventbus.EventBus

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var count = 0
    val CHANNEL_ID = "whrzat"


    override fun onNewToken(token: String) {
        super.onNewToken(token)

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //Displaying data in log
        //It is optional
        Log.e(TAG, "From: " + remoteMessage.from)
        Log.e(TAG, "Notification Message Body: " + remoteMessage.notification)

        //Calling method to generate notification
        //  String click_action = remoteMessage.getNotification().getClickAction();
        /*  if(remoteMessage.data[Constants.POSTD_NOTIF]!=Prefs.with(this).getString(Constants.OTHER_USER_ID,"")){
          }*/

        if (remoteMessage.data[Constants.NOTIFICATION_TYPE] == "0") {
            if (remoteMessage.data[Constants.ID] != Prefs.with(this).getString(Constants.OTHER_USER_ID, "")) {
                if (Prefs.with(this).getBoolean(Constants.NOTIFICATION_0, false)) {
                    sendNotification(remoteMessage.data)
                }
                EventBus.getDefault().postSticky(ChatSocketReferesh(true))

            }
        } else {
            sendNotification(remoteMessage.data)
        }

    }

    private fun sendNotification(data: Map<String, String>) {

        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(Constants.NOTIFICATION_TYPE, data[Constants.NOTIFICATION_TYPE])
        intent.putExtra(Constants.ID, data[Constants.ID])
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent,
              if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){  PendingIntent.FLAG_ONE_SHOT} else {  PendingIntent.FLAG_IMMUTABLE})


        if (data[Constants.NOTIFICATION_TYPE] == "0") {
            //EventBus.getDefault().postSticky(ChatApi(true))
            // EventBus.getDefault().postSticky(ReceiveChatApi(true,data[Constants.POSTD_NOTIF]))
        }

        val notificationBuilder = NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources,
                        R.drawable.ic_logo))
                //.setContentTitle(getString(R.string.app_name))
                .setContentTitle(data["title"])
                .setContentText(data["body"])
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //notificationManager.notify(count++, notificationBuilder.build())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)

//            val mChannel = NotificationChannel(
//                    CHANNEL_ID.toString(), name,
//                    NotificationManager.IMPORTANCE_DEFAULT.apply {
//
//                        setShowBadge(false)
//                    }
//
//            )

            val mChannel1 = NotificationChannel(CHANNEL_ID.toString(), name,
                    NotificationManager.IMPORTANCE_DEFAULT).apply {

                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(mChannel1)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
//        notification.flags |= Notification.FLAG_AUTO_CANCEL
    }

    companion object {
        private val TAG = "MyFirebaseMsgService"
    }
}