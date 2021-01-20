package com.consultantvendor.ui.loginSignUp.document.add

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.AdditionalFieldDocument
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.ItemAddDocumentBinding
import com.consultantvendor.ui.chat.UploadFileViewModel
import com.consultantvendor.ui.loginSignUp.document.DocumentsFragment
import com.consultantvendor.utils.*
import com.consultantvendor.utils.PermissionUtils
import com.consultantvendor.utils.dialogs.ProgressDialogImage
import dagger.android.support.DaggerDialogFragment
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import okhttp3.MediaType
import okhttp3.RequestBody
import permissions.dispatcher.*
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.emptyList
import kotlin.collections.set


@RuntimePermissions
class DialogAddDocumentFragment(private val fragment: DocumentsFragment,
                                private var documentMain: AdditionalFieldDocument?) : DaggerDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: ItemAddDocumentBinding

    private var fileToUpload: File? = null

    private lateinit var viewModelUpload: UploadFileViewModel

    private lateinit var progressDialogImage: ProgressDialogImage


    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.item_add_document, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)

        initialise()
        listeners()
        binObservers()
    }

    private fun initialise() {
        viewModelUpload = ViewModelProvider(this, viewModelFactory)[UploadFileViewModel::class.java]
        progressDialogImage = ProgressDialogImage(requireActivity())

        if (documentMain != null) {
            binding.etName.setText(documentMain?.title)
            binding.etDesc.setText(documentMain?.description)
            loadImage(binding.ivImage, documentMain?.file_name, R.drawable.image_placeholder)
        }
    }

    private fun listeners() {
        binding.ivBack.setOnClickListener {
            dialog?.dismiss()
        }

        binding.ivImage.setOnClickListener {
            getStorageWithPermissionCheck()
        }

        binding.tvAdd.setOnClickListener {
            binding.tvAdd.hideKeyboard()
            when {
                fileToUpload == null && documentMain == null -> {
                    binding.etName.showSnackBar(getString(R.string.select_image))
                }
                binding.etName.text.toString().trim().isEmpty() -> {
                    binding.etName.showSnackBar(getString(R.string.enter_name))
                }
                binding.etDesc.text.toString().trim().isEmpty() -> {
                    binding.etDesc.showSnackBar(getString(R.string.description))
                }
                isConnectedToInternet(requireContext(), true) -> {
                    if (fileToUpload != null)
                        uploadFileOnServer()
                    else if (documentMain != null) {
                        val document = AdditionalFieldDocument()
                        document.id = documentMain?.id
                        document.title = binding.etName.text.toString().trim()
                        document.description = binding.etDesc.text.toString().trim()
                        document.file_name = documentMain?.file_name ?: ""
                        document.is_edit = true

                        fragment.addedDocument(document)
                        dialog?.dismiss()
                    }
                }
            }
        }
    }

    private fun uploadFileOnServer() {
        val hashMap = HashMap<String, RequestBody>()
        hashMap["type"] = getRequestBody(DocType.IMAGE)

        val body: RequestBody =
                RequestBody.create(MediaType.parse("text/plain"), fileToUpload)
        hashMap["image\"; fileName=\"" + fileToUpload?.name] = body

        viewModelUpload.uploadFile(hashMap)
    }

    private fun binObservers() {
        viewModelUpload.uploadFile.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialogImage.setLoading(false)

                    val document = AdditionalFieldDocument()
                    document.id = documentMain?.id
                    document.title = binding.etName.text.toString().trim()
                    document.description = binding.etDesc.text.toString().trim()
                    document.file_name = it.data?.image_name ?: ""
                    document.is_edit = true

                    fragment.addedDocument(document)
                    dialog?.dismiss()

                }
                Status.ERROR -> {
                    progressDialogImage.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialogImage.setLoading(true)

                }
            }
        })
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
                docPaths.addAll(data?.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
                        ?: emptyList())

                fileToUpload = File(getPathUri(requireContext(), docPaths[0]))
                Glide.with(requireContext()).load(fileToUpload).into(binding.ivImage)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getStorage() {
        selectImages()
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(requireContext(), R.string.media_permission, request)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(
                requireContext(), R.string.media_permission
        )
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showDeniedForStorage() {
        PermissionUtils.showAppSettingsDialog(
                requireContext(), R.string.media_permission
        )
    }
}
