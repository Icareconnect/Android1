package com.consultantvendor.pushNotifications

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantvendor.R
import com.consultantvendor.data.models.PushData
import com.consultantvendor.data.network.PushType
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.ui.calling.Constants
import com.consultantvendor.ui.calling.IncomingCallNotificationService
import com.consultantvendor.ui.chat.chatdetail.ChatDetailActivity
import com.consultantvendor.ui.dashboard.HomeActivity
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.ui.drawermenu.DrawerActivity.Companion.CLASSES
import com.consultantvendor.ui.loginSignUp.SignUpActivity
import com.consultantvendor.ui.loginSignUp.document.DocumentsFragment
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment
import com.consultantvendor.utils.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import org.json.JSONObject
import java.util.*
import javax.inject.Inject


class MessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var appSocket: AppSocket

    private val channelId = "Consultant user"


    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("fcmToken", token)

        userRepository.pushTokenUpdate()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("remoteMessage", remoteMessage.data.toString())

        val notificationData = JSONObject(remoteMessage.data as MutableMap<Any?, Any?>)

        if (userRepository.isUserLoggedIn() && userRepository.getUser()?.notification_enable == true) {
            sendNotification(notificationData)
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    private fun sendNotification(notificationData: JSONObject) {

        userRepository.isNewNotification.postValue(true)

        val pushData = PushData(
                msg = notificationData.optString("msg"),
                title = notificationData.optString("title"),
                sound = notificationData.optString("sound"),
                pushType = notificationData.optString("pushType"),
                imageUrl = notificationData.optString("imageUrl"),
                message = notificationData.optString("message"),
                senderId = notificationData.optString("senderId"),
                senderName = notificationData.optString("senderName"),
                receiverId = notificationData.optString("receiverId"),
                messageType = notificationData.optString("messageType"),
                request_id = notificationData.optString("request_id"),
                call_id = notificationData.optString("call_id"),
                service_type = notificationData.optString("service_type"),
                sentAt = notificationData.optLong("sentAt"),
                request_time = notificationData.optString("request_time"),
                sender_name = notificationData.optString("sender_name"),
                sender_image = notificationData.optString("sender_image"),
                vendor_category_name = notificationData.optString("vendor_category_name")
        )


        val requestID = Calendar.getInstance().timeInMillis.toInt()

        /*Stack builder home activity*/
        val stackBuilder = TaskStackBuilder.create(this)

        stackBuilder.addParentStack(HomeActivity::class.java)
        val homeIntent = Intent(this, HomeActivity::class.java)
        //stackBuilder.addNextIntent(homeIntent)

        Log.e("Notification", "Parent added")
        /*Final activity to open*/
        var intent: Intent? = null

        val titleString = pushData.pushType.replace("_", " ").toLowerCase()
        var title = ""

        val lineScan = Scanner(titleString)
        while (lineScan.hasNext()) {
            val word: String = lineScan.next()
            title += Character.toUpperCase(word[0]).toString() + word.substring(1) + " "
        }

        val msg = pushData.message

        when (pushData.pushType) {
            PushType.CHAT -> {
                title = pushData.senderName
                intent = Intent(this, ChatDetailActivity::class.java)
                        .putExtra(USER_ID, pushData.senderId)
                        .putExtra(USER_NAME, pushData.senderName)
                        .putExtra(EXTRA_REQUEST_ID, pushData.request_id)
            }
            PushType.DOCUMENT_STATUS -> {
                intent = Intent(this, SignUpActivity::class.java)
                        .putExtra(SubCategoryFragment.CATEGORY_PARENT_ID, userRepository.getUser()?.categoryData)
                        .putExtra(DocumentsFragment.UPDATE_DOCUMENTS, true)
            }
            PushType.NEW_REQUEST, PushType.REQUEST_FAILED, PushType.REQUEST_COMPLETED,PushType.COMPLETED,
            PushType.CANCELED_REQUEST, PushType.RESCHEDULED_REQUEST -> {
                intent = Intent(this, DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.APPOINTMENT_DETAILS)
                        .putExtra(EXTRA_REQUEST_ID, pushData.request_id)

                val intentBroadcast = Intent()
                intentBroadcast.action = pushData.pushType
                intentBroadcast.putExtra(EXTRA_REQUEST_ID, pushData.request_id)

                LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)
            }
            PushType.PROFILE_APPROVED -> {
                val userData = userRepository.getUser()
                if (userData?.isApproved == false) {
                    userData.isApproved = true
                    prefsManager.save(USER_DATA, userData)
                }

                val intentBroadcast = Intent()
                intentBroadcast.action = pushData.pushType
                intentBroadcast.putExtra(EXTRA_REQUEST_ID, pushData.request_id)

                LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)
            }
            PushType.AMOUNT_RECEIVED, PushType.PAYOUT_PROCESSED -> {
                //homeIntent.putExtra(EXTRA_TAB, "1")
            }
            PushType.ASSINGED_USER -> {
                intent = Intent(this, DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, CLASSES)
            }
            PushType.CALL_RINGING -> {
                wakeDevice()
                return
            }
            PushType.CALL_ACCEPTED -> {
                wakeDevice()
                val intent = Intent(this, IncomingCallNotificationService::class.java)
                intent.action = Constants.ACTION_ACCEPT
                intent.putExtra(Constants.INCOMING_CALL_INVITE, pushData)

                startService(intent)
                return
            }
            PushType.CALL_CANCELED -> {
                wakeDevice()
                handleCanceledCallInvite(pushData)
                return
            }
        }

        stackBuilder.addNextIntent(homeIntent)
        if (intent != null)
            stackBuilder.addNextIntent(intent)
        /*val pendingIntent = PendingIntent.getActivity(this, requestID,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)*/

        /*Flags*/
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        homeIntent.action = System.currentTimeMillis().toString()

        val pendingIntent =
                stackBuilder.getPendingIntent(requestID, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setContentTitle(title) //Header
                .setContentText(msg) //Content
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
            notificationBuilder.color = ContextCompat.getColor(this, R.color.colorAccent)
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
        }

        val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                    channelId, getText(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(mChannel)
        }


        if (pushData.pushType == PushType.CHAT && pushData.senderId == ChatDetailActivity.otherUserID &&
                pushData.request_id == ChatDetailActivity.requestId
        ) {
            /*Don't generate push*/
            Log.e("", "")
        } else
            notificationManager.notify(requestID, notificationBuilder.build())
    }

    private fun wakeDevice() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "Consultant:"
        )
        wl.acquire(25000)
    }

    private fun handleInvite(pushData: PushData, notificationId: Int) {
        val intent = Intent(this, IncomingCallNotificationService::class.java)
        intent.action = Constants.ACTION_INCOMING_CALL
        intent.putExtra(Constants.INCOMING_CALL_NOTIFICATION_ID, notificationId)
        intent.putExtra(Constants.INCOMING_CALL_INVITE, pushData)
        intent.putExtra(EXTRA_REQUEST_ID, pushData.call_id)

        startService(intent)
    }

    private fun handleCanceledCallInvite(pushData: PushData) {
        val intent = Intent(this, IncomingCallNotificationService::class.java)
        intent.action = Constants.ACTION_CANCEL_CALL
        intent.putExtra(Constants.INCOMING_CALL_INVITE, pushData)
        intent.putExtra(EXTRA_REQUEST_ID, pushData.call_id)

        startService(intent)
    }

}