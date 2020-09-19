package com.consultantvendor.ui.calling

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantvendor.R
import com.consultantvendor.data.models.PushData
import com.consultantvendor.data.models.responses.JitsiClass
import com.consultantvendor.data.models.responses.Request
import com.consultantvendor.data.network.PushType
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityCallingBinding
import com.consultantvendor.ui.calling.Constants.CALL_NOTIFICATION_ID
import com.consultantvendor.ui.jitsimeet.JitsiActivity
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class CallingActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    lateinit var binding: ActivityCallingBinding

    private lateinit var callInvite: PushData

    private var requestItem: Request? = null

    private var callId: String? = null

    private lateinit var progressDialog: ProgressDialog

    private var audioManager: AudioManager? = null

    private var savedAudioMode = AudioManager.MODE_INVALID

    private var isReceiverRegistered = false

    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialise()
        listeners()
        disconnectCall()

        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.action == Constants.ACTION_ACCEPT) {

            callInvite = intent.getSerializableExtra(Constants.INCOMING_CALL_INVITE) as PushData
            callId = callInvite.call_id

            /*Data for jitsi class*/
            val jitsiClass = JitsiClass()
            jitsiClass.id = callInvite.request_id
            jitsiClass.call_id = callInvite.call_id
            jitsiClass.callType = callInvite.service_type
            jitsiClass.name = ""

            val intent = Intent(this, JitsiActivity::class.java)
            intent.putExtra(EXTRA_CALL_NAME, jitsiClass)
            startActivity(intent)
            clearNotification()
            finish()
            mHandler.removeCallbacksAndMessages(null)
        } else if (intent?.action == Constants.ACTION_REJECT) {
            finish()
            mHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun initialise() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_calling)

        // These flags ensure that the activity can be launched when the screen is locked.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        progressDialog = ProgressDialog(this)

        if (intent.hasExtra(Constants.INCOMING_CALL_INVITE)) {
            callInvite = intent.getSerializableExtra(Constants.INCOMING_CALL_INVITE) as PushData
            callId = callInvite.call_id
        }

        if (intent.hasExtra(EXTRA_REQUEST_ID)) {
            requestItem = intent.getSerializableExtra(EXTRA_REQUEST_ID) as Request
            callId = requestItem?.call_id

            binding.tvName.text = requestItem?.from_user?.name ?: ""
            binding.tvDesc.text = requestItem?.from_user?.categoryData?.name ?: ""
            loadImage(binding.ivPic, requestItem?.from_user?.profile_image,
                    R.drawable.ic_profile_placeholder)

            binding.tvTime.text = "${DateUtils.dateTimeFormatFromUTC(
                    DateFormat.MON_YEAR_FORMAT, requestItem?.bookingDateUTC)} · " +
                    "${DateUtils.dateTimeFormatFromUTC(DateFormat.TIME_FORMAT, requestItem?.bookingDateUTC)}"

            binding.tvCallType.text = requestItem?.service_type
        }


        /*
        * Needed for setting/abandoning audio focus during a call
        */
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager?.isSpeakerphoneOn = true


        /*
         * Enable changing the volume using the up/down keys during a conversation
         */
        volumeControlStream = AudioManager.STREAM_VOICE_CALL

        setAudioFocus(true)

        SoundPoolManager.getInstance(this).playRinging()


        if (intent?.action == Constants.ACTION_ACCEPT) {
            callInvite = intent.getSerializableExtra(Constants.INCOMING_CALL_INVITE) as PushData
            callId = callInvite.call_id

            /*Data for jitsi class*/
            val jitsiClass = JitsiClass()
            jitsiClass.id = callInvite.request_id
            jitsiClass.callType = callInvite.service_type
            jitsiClass.name = ""

            val intent = Intent(this, JitsiActivity::class.java)
            intent.putExtra(EXTRA_CALL_NAME, jitsiClass)
            startActivity(intent)
            clearNotification()
            finish()
            mHandler.removeCallbacksAndMessages(null)
        } else if (intent?.action == Constants.ACTION_REJECT) {
            finish()
            mHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun listeners() {
        binding.ivSpeaker.setOnClickListener {
            disableButton(binding.ivSpeaker)
            if (audioManager?.isSpeakerphoneOn == true) {
                audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager?.isSpeakerphoneOn = false
                binding.ivSpeaker.setImageResource(R.drawable.ic_speaker_off)
            } else {
                audioManager?.mode = AudioManager.MODE_NORMAL
                audioManager?.isSpeakerphoneOn = true
                binding.ivSpeaker.setImageResource(R.drawable.ic_speaker)
            }
        }

        binding.ivRejectCall.setOnClickListener {
            if (isConnectedToInternet(this, true)) {
                userRepository.callStatus(requestItem?.id ?: "", requestItem?.call_id ?: "",
                        PushType.CALL_CANCELED)
                finish()
            }
        }
    }

    override fun onBackPressed() {
    }

    private fun setAudioFocus(setFocus: Boolean) {
        if (audioManager != null) {
            if (setFocus) {
                savedAudioMode = audioManager?.mode ?: AudioManager.MODE_INVALID
                // Request audio focus before making any device switch.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val playbackAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener { i: Int -> }
                            .build()
                    audioManager?.requestAudioFocus(focusRequest)
                } else {
                    audioManager?.requestAudioFocus(
                            { focusChange: Int -> },
                            AudioManager.STREAM_VOICE_CALL,
                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                }
                /*
                 * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
                 * required to be in this mode when playout and/or recording starts for
                 * best possible VoIP performance. Some devices have difficulties with speaker mode
                 * if this is not set.
                 */audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            } else {
                audioManager?.mode = savedAudioMode
                audioManager?.abandonAudioFocus(null)
            }
        }
    }

    private fun clearNotification() {
        val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }

    override fun onDestroy() {
        SoundPoolManager.getInstance(this).release()
        super.onDestroy()
    }


    override fun onResume() {
        super.onResume()
        registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(Constants.ACTION_INCOMING_CALL)
            intentFilter.addAction(Constants.ACTION_CANCEL_CALL)
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    callCancelledReceiver, intentFilter
            )
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(callCancelledReceiver)
            isReceiverRegistered = false
        }
    }

    private val callCancelledReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Constants.ACTION_CANCEL_CALL) {
                if (intent.hasExtra(EXTRA_REQUEST_ID) && intent.getStringExtra(EXTRA_REQUEST_ID) == callId) {
                    finish()
                }
            }
        }
    }

    private fun disconnectCall() {
        mHandler.postDelayed({
            binding.ivRejectCall.performClick()
        }, 45000)

    }
}
