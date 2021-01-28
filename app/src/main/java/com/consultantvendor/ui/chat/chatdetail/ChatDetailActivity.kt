package com.consultantvendor.ui.chat.chatdetail

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.CommonDataModel
import com.consultantvendor.data.models.responses.chat.ChatMessage
import com.consultantvendor.data.models.responses.chat.MessageSend
import com.consultantvendor.data.network.ApiKeys
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PER_PAGE_LOAD
import com.consultantvendor.data.network.PushType
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityChatDetailBinding
import com.consultantvendor.ui.chat.ChatViewModel
import com.consultantvendor.ui.chat.UploadFileViewModel
import com.consultantvendor.ui.dashboard.home.AppointmentViewModel
import com.consultantvendor.utils.*
import com.consultantvendor.utils.AppSocket.Events.*
import com.consultantvendor.utils.PermissionUtils
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.google.gson.Gson
import dagger.android.support.DaggerAppCompatActivity
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import io.socket.client.Ack
import io.socket.emitter.Emitter
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import permissions.dispatcher.*
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@RuntimePermissions
class ChatDetailActivity : DaggerAppCompatActivity(), AppSocket.OnMessageReceiver {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var appSocket: AppSocket

    @Inject
    lateinit var userRepository: UserRepository


    companion object {
        const val DELAY: Long = 2000
        var otherUserID = "-1"
        var requestId = "-1"
        var isActive = false
    }

    lateinit var binding: ActivityChatDetailBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var adapter: ChatDetailAdapter

    private lateinit var viewModel: ChatViewModel

    private lateinit var viewModelCall: AppointmentViewModel

    private var userID = ""

    private var userName = ""

    private var newItem = true

    private var pageNo = 1

    private lateinit var llm: LinearLayoutManager

    private var items = ArrayList<ChatMessage>()

    private var fileToUpload: File? = null

    private lateinit var viewModelUpload: UploadFileViewModel

    private var isTyping = false

    private var timer = Timer()

    private var countDownTimer: CountDownTimer? = null

    private val TOTAL_TIME = 60000L

    private var isReceiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //LocaleHelper.setLocale(this, getUserLanguage())
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_detail)

        setAdapter()
        initialise()
        liveData()

        listeners()
        getChatData()
    }

    /*override fun attachBaseContext(base: Context?) {
        val locale = Locale(getUserLanguage())
        val contxt = ContextWrapper.wrap(base,locale)
        super.attachBaseContext(contxt)
    }*/

    private fun initialise() {
        progressDialog = ProgressDialog(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]
        viewModelCall = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]
        viewModelUpload = ViewModelProvider(this, viewModelFactory)[UploadFileViewModel::class.java]
        registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        appSocket.connect()

        otherUserID = intent.getStringExtra(USER_ID)?:""
        requestId = intent.getStringExtra(EXTRA_REQUEST_ID)?:""

        userID = userRepository.getUser()?.id ?: ""
        userName = intent.getStringExtra(USER_NAME)?:""
        binding.tvUserName.text = userName

        if (intent.hasExtra(EXTRA_IS_FIRST))
            generateNewMessage(getString(R.string.chat_first_message, userName))
    }


    override fun onStart() {
        super.onStart()
        appSocket.on(TYPING, listener)
        appSocket.on(READ_MESSAGE, listenerRead)
        appSocket.on(DELIVERED_MESSAGE, listenerDelivered)
        appSocket.on(BROADCAST, listenerStatus)
    }

    override fun onDestroy() {
        super.onDestroy()
        appSocket.off(TYPING, listener)
        appSocket.off(READ_MESSAGE, listenerRead)
        appSocket.off(DELIVERED_MESSAGE, listenerDelivered)
        appSocket.off(BROADCAST, listenerStatus)
        appSocket.removeOnMessageReceiver(this)
        unregisterReceiver(broadcastReceiver)
        unregisterReceiver()
    }

    override fun onBackPressed() {
        if (items.isNotEmpty()) {
            val intent = Intent()
            intent.putExtra(LAST_MESSAGE, Gson().toJson(items[0]))
            intent.putExtra(OTHER_USER_ID, otherUserID)
            setResult(Activity.RESULT_OK, intent)
        }
        otherUserID = "-1"
        super.onBackPressed()
    }

    private fun getChatData() {
        if (isConnectedToInternet(this, true)) {
            val hashMap = HashMap<String, String>()

            if (items.isNotEmpty())
                hashMap[ApiKeys.AFTER] = items[items.size - 1].id.toString()

            hashMap[ApiKeys.PER_PAGE] = PER_PAGE_LOAD.toString()
            hashMap["request_id"] = requestId
            viewModel.getChatMessage(hashMap)
        }
    }

    private fun liveData() {
        viewModel.chatMessages.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.pbLoaderBottom.gone()

                    val data = it.data

                    setStatus(data?.isOnline)
                    if (data?.messages?.size ?: 0 < PER_PAGE_LOAD)
                        newItem = false

                    if (pageNo == 1) {
                        items.clear()
                        items.addAll(data?.messages ?: ArrayList())
                        adapter.notifyDataSetChanged()

                        /*If message is not read*/
                        if (items.isNotEmpty())
                            sendMessageRead(items[0].messageId)

                    } else {
                        val size = items.size
                        items.addAll(data?.messages ?: ArrayList())
                        adapter.notifyItemRangeInserted(size, items.size)
                        adapter.notifyItemRangeChanged(size - 1, items.size)
                    }

                    /*Check request request_status*/
                    showTimer(data?.request_status == CallAction.START, data)
                }

                Status.ERROR -> {
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    if (pageNo == 1) {
                        binding.clLoader.visible()
                        if (items.isNotEmpty()) {
                            binding.clLoader.setBackgroundColor(
                                ContextCompat.getColor(this, android.R.color.transparent)
                            )
                        }
                    } else
                        binding.pbLoaderBottom.visible()
                }
            }
        })

        viewModelUpload.uploadFile.observe(this, Observer {
            resources ?: return@Observer

            when (it.status) {

                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    /*val image = ImageModel()
                    image.original = resources.data?.original
                    image.thumbnail = resources.data?.thumbnail*/
                    if (isConnectedToInternet(this, false)) {
                        sendImage(it.data?.image_name ?: "")
                    } else {
                        binding.etMessage.showSnackBar(getString(R.string.check_internet))
                    }
                }

                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
            }
        })

        viewModelCall.completeChat.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    showTimer(false, null)

                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })
    }

    /*Show time and complete button*/
    private fun showTimer(show: Boolean, data: CommonDataModel?) {
        if (show) {
            binding.ivCompleteChat.visible()
            binding.rlChatInput.visible()

            binding.tvTimer.visible()
            startTimer(((data?.currentTimer ?: 0) * 1000))
        } else {
            binding.ivCompleteChat.gone()
            binding.rlChatInput.gone()

            binding.tvTimer.gone()
            countDownTimer?.cancel()
            binding.ivCompleteChat.hideKeyboard()
        }

    }

    private fun startTimer(currentTimer: Long) {

        val totalTimerToRun = (TOTAL_TIME * 1000) + currentTimer
        countDownTimer = object : CountDownTimer(totalTimerToRun, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val newTimer = ((totalTimerToRun - millisUntilFinished) + currentTimer) / 1000
                binding.tvTimer.text = convertMiliSecondsToMinute(newTimer)
            }

            override fun onFinish() {}
        }
        countDownTimer?.start()
    }


    private fun convertMiliSecondsToMinute(seconds: Long): String {
        val s = seconds % 60
        val m = seconds / 60 % 60
        return String.format("%02d:%02d", m, s)
    }

    private fun setStatus(isOnline: Boolean?) {
        runOnUiThread {
            if (isOnline == true)
                binding.tvUserStatus.text = getString(R.string.active_now)
            else
                binding.tvUserStatus.text = getString(R.string.offline)
        }
    }

    private fun sendImage(image: String) {

        val msg = MessageSend(
            image, "",
            userID,
            userRepository.getUser()?.name,
            otherUserID,
            DocType.IMAGE,
            requestId,
            Calendar.getInstance().timeInMillis
        )
        sendMessage(msg)
    }


    private fun listeners() {
        appSocket.addOnMessageReceiver(this)

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnCamera.setOnClickListener {
            binding.btnCamera.hideKeyboard()
            getStorageWithPermissionCheck()

        }

        binding.ivSend.setOnClickListener {
            if (binding.etMessage.text.trim().toString().isEmpty()) {
                binding.etMessage.error = getString(R.string.enter_message)
                return@setOnClickListener
            }

            generateNewMessage(binding.etMessage.text.toString().trim())
        }


        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                timer = Timer()
                if (!isTyping) {
                    isTyping = true
                    startTyping()
                }
                timer.cancel()
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        isTyping = false
                        stopTypingSocket()
                    }
                }, DELAY)
            }
        })

        binding.ivCompleteChat.setOnClickListener {
            showCompleteRequestDialog()
        }

        binding.rvChatData.addOnScrollListener(onScrollListener)
    }

    private fun generateNewMessage(message: String) {
        if (isConnectedToInternet(this, false)) {

            val msg = MessageSend(
                String(),
                message,
                userID,
                userRepository.getUser()?.name,
                otherUserID,
                DocType.TEXT,
                requestId,
                Calendar.getInstance().timeInMillis
            )

            sendMessage(msg)
        } else {
            binding.etMessage.showSnackBar(getString(R.string.check_internet))
        }

    }

    private var onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (llm.findLastVisibleItemPosition() == adapter.itemCount - 1 && newItem) {
                ++pageNo
                getChatData()
            }
        }
    }

    private fun sendMessageRead(id: String?) {
        val obj = JSONObject()
        obj.put("messageId", id)
        obj.put("receiverId", otherUserID)
        obj.put("senderId", userID)
        appSocket.emit(READ_MESSAGE, obj, Ack {

        })
    }

    /*--------   TYPING ----------*/
    private fun startTyping() {
        val obj = JSONObject()
        obj.put("isTyping", true)
        obj.put("receiverId", otherUserID)
        obj.put("senderId", userID)
        appSocket.emit(TYPING, obj)
    }

    private fun stopTypingSocket() {
        val obj = JSONObject()
        obj.put("isTyping", false)
        obj.put("receiverId", otherUserID)
        obj.put("senderId", userID)
        appSocket.emit(TYPING, obj)
    }

    private val listener = Emitter.Listener {
        Timber.e("Typing $it")
        val senderId = (it[0] as JSONObject).getString("senderId")
        val isTyping = (it[0] as JSONObject).getBoolean("isTyping")
        if (senderId == otherUserID) {
            if (isTyping) {
                runOnUiThread { binding.tvUserStatus.text = getString(R.string.typing) }
                /* Timer("typing", false).schedule(5000) {
                     runOnUiThread { tvUserStatus.text = getString(R.string.active_now) }
                 }*/
            } else {
                runOnUiThread { binding.tvUserStatus.text = getString(R.string.active_now) }
            }
        }
    }

    private val listenerRead = Emitter.Listener {
        Timber.e("Typing $it")

        val data = it[0] as JSONObject
        val messageId = data.getString("messageId")

        runOnUiThread {
            items.forEachIndexed { index, chatMessage ->
                if (items[index].status != AppSocket.MessageStatus.SEEN) {
                    items[index].status = AppSocket.MessageStatus.SEEN

                    adapter.notifyItemChanged(items.indexOf(chatMessage))
                }
            }
        }
    }

    private val listenerDelivered = Emitter.Listener {
        Timber.e("Typing $it")

        val data = it[0] as JSONObject
        val messageId = data.getString("messageId")

        runOnUiThread {
            items.forEachIndexed { index, chatMessage ->
                if (items[index].status != AppSocket.MessageStatus.SEEN &&
                    items[index].status != AppSocket.MessageStatus.DELIVERED
                ) {
                    items[index].status = AppSocket.MessageStatus.DELIVERED

                    adapter.notifyItemChanged(items.indexOf(chatMessage))
                }
            }
        }
    }

    private val listenerStatus = Emitter.Listener {
        Timber.e("BroadCast $it")
        val senderId = (it[0] as JSONObject).getString("userId")
        val isOnline = (it[0] as JSONObject).getBoolean("isOnline")
        if (senderId == otherUserID)
            setStatus(isOnline)
    }


    private fun sendMessage(messageSend: MessageSend) {
        if (appSocket.isConnected) {
            //appSocket.sendMessage(message) {}

            /*Add Message to list*/
            binding.etMessage.setText("")

            val message = ChatMessage(
                messageSend.imageUrl, "", messageSend.message,
                messageSend.sentAt, messageSend.messageType,
                AppSocket.MessageStatus.NOT_SENT, true,
                messageSend.senderId, messageSend.senderName,
                messageSend.receiverId, "", 0
            )

            items.add(0, message)
            adapter.notifyItemInserted(0)
            binding.rvChatData.scrollToPosition(0)

            /*Send event*/
            try {
                val jsonObject = JSONObject(Gson().toJson(messageSend))

                appSocket.emit(SEND_MESSAGE, jsonObject, Ack {
                    val data = it[0] as JSONObject
                    Log.e("ack**", data.toString())

                    /*If request completed end chat*/
                    if (data.optString("status") == PushType.REQUEST_COMPLETED) {
                        runOnUiThread {
                            showTimer(false, null)
                            longToast(getString(R.string.request_completed))
                        }
                    } else if (!data.optString("messageId").isNullOrEmpty()) {
                        runOnUiThread {
                            Log.e("ack========", data.optString("messageId"))

                            items.forEachIndexed { index, chatMessage ->
                                if (chatMessage.status == AppSocket.MessageStatus.NOT_SENT) {
                                    /*If message is not read*/
                                    items[index].status = AppSocket.MessageStatus.SENT
                                    adapter.notifyItemChanged(items.indexOf(chatMessage))
                                }
                            }
                        }
                    }

                })
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

    }

    private fun setAdapter() {
        llm = LinearLayoutManager(this)
        llm.reverseLayout = true
        llm.orientation = LinearLayoutManager.VERTICAL
        binding.rvChatData.layoutManager = llm

        adapter = ChatDetailAdapter(this, items)
        binding.rvChatData.adapter = adapter
    }


    override fun onMessageReceive(message: ChatMessage?) {
        if (message?.senderId == otherUserID && message.request_id == requestId) {
            sendMessageRead(message.messageId)

            message.let { items.add(0, it) }
            adapter.notifyItemInserted(0)
            binding.rvChatData.scrollToPosition(0)
        }
    }


    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {

            }
        }
    }

    private fun selectImages() {
        FilePickerBuilder.instance
            .setMaxCount(1)
            .setActivityTheme(R.style.AppTheme)
            .setActivityTitle(getString(R.string.select_image))
            .enableVideoPicker(false)
            .enableCameraSupport(true)
            .showGifs(false)
            .showFolderView(true)
            .enableSelectAll(false)
            .enableImagePicker(true)
            .setCameraPlaceholder(R.drawable.ic_camera)
            .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .pickPhoto(this, AppRequestCode.IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == AppRequestCode.IMAGE_PICKER) {

                val docPaths = ArrayList<Uri>()
                docPaths.addAll(data?.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA) ?: emptyList())

                val fileToUpload = File(getPathUri(this,docPaths[0]))

                uploadFileOnServer(compressImage(this, fileToUpload))
            }
        }
    }


    private fun uploadFileOnServer(fileToUpload: File?) {
        val hashMap = HashMap<String, RequestBody>()
        hashMap["type"] = getRequestBody(DocType.IMAGE)

        val body: RequestBody =
                RequestBody.create(MediaType.parse("text/plain"), fileToUpload)
        hashMap["image\"; fileName=\"" + fileToUpload?.name] = body

        viewModelUpload.uploadFile(hashMap)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getStorage() {
        selectImages()
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(this, R.string.media_permission, request)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(
            this, R.string.media_permission
        )
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showDeniedForStorage() {
        PermissionUtils.showAppSettingsDialog(
            this, R.string.media_permission
        )
    }


    private fun showCompleteRequestDialog() {
        AlertDialogUtil.instance.createOkCancelDialog(this, R.string.end_chat,
            R.string.end_chat_desc, R.string.end_chat, R.string.cancel, false,
            object : AlertDialogUtil.OnOkCancelDialogListener {
                override fun onOkButtonClicked() {
                    hitApiAcceptRequest()
                }

                override fun onCancelButtonClicked() {
                }
            }).show()
    }

    private fun hitApiAcceptRequest() {
        if (isConnectedToInternet(this, true)) {
            val hashMap = HashMap<String, Any>()
            hashMap["request_id"] = requestId

            viewModelCall.completeChat(hashMap)
        }
    }


    override fun onPause() {
        super.onPause()
        isActive = false
    }

    override fun onResume() {
        super.onResume()
        isActive = true

        registerReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(PushType.REQUEST_COMPLETED)
            LocalBroadcastManager.getInstance(this).registerReceiver(refreshRequests, intentFilter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshRequests)
            isReceiverRegistered = false
        }
    }

    private val refreshRequests = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == PushType.REQUEST_COMPLETED ) {
                if (intent.getStringExtra(EXTRA_REQUEST_ID) == requestId) {
                    showTimer(false, null)
                    longToast(getString(R.string.request_completed))
                }
            }
        }
    }
}
