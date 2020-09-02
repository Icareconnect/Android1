package com.consultantvendor.ui.dashboard.feeds

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
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
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentAddFeedBinding
import com.consultantvendor.ui.chat.UploadFileViewModel
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.utils.*
import com.consultantvendor.utils.PermissionUtils
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.consultantvendor.utils.dialogs.ProgressDialogImage
import dagger.android.support.DaggerFragment
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import permissions.dispatcher.*
import java.io.File
import javax.inject.Inject

@RuntimePermissions
class AddFeedFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentAddFeedBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var progressDialogImage: ProgressDialogImage

    private lateinit var viewModel: FeedViewModel

    private lateinit var viewModelUpload: UploadFileViewModel

    private var fileToUpload: File? = null

    private var typeOfBlog = BlogType.BLOG


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding =
                    DataBindingUtil.inflate(inflater, R.layout.fragment_add_feed, container, false)
            rootView = binding.root

            initialise()
            listeners()
            setEditInformation()
            bindObservers()
        }
        return rootView
    }


    private fun initialise() {
        editTextScroll(binding.etDesc)

        when (requireActivity().intent.getStringExtra(PAGE_TO_OPEN)) {
            DrawerActivity.BLOGS, DrawerActivity.ADD_BLOG -> {
                typeOfBlog = BlogType.BLOG
                binding.tvTitle.text = getString(R.string.post_blog)
            }
            DrawerActivity.ARTICLE, DrawerActivity.ADD_ARTICLE -> {
                typeOfBlog = BlogType.ARTICLE

                binding.tvTitle.text = getString(R.string.post_article)
            }
            else -> {
                typeOfBlog = BlogType.ARTICLE
                binding.tvTitle.text = getString(R.string.latest_articles)
            }
        }

        viewModel = ViewModelProvider(this, viewModelFactory)[FeedViewModel::class.java]
        viewModelUpload = ViewModelProvider(this, viewModelFactory)[UploadFileViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
        progressDialogImage = ProgressDialogImage(requireActivity())
    }

    private fun setEditInformation() {

    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.ivNext.setOnClickListener {
            checkValidation()
        }

        binding.ivPic.setOnClickListener {
            getStorageWithPermissionCheck()
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

    private fun checkValidation() {
        when {
            fileToUpload == null -> {
                binding.etTitle.showSnackBar(getString(R.string.select_image))
            }
            binding.etTitle.text.toString().isEmpty() -> {
                binding.etTitle.showSnackBar(getString(R.string.title))
            }
            binding.etDesc.text.toString().isEmpty() -> {
                binding.etDesc.showSnackBar(getString(R.string.description))
            }
            isConnectedToInternet(requireContext(), true) -> {
                fileToUpload = compressImage(requireActivity(), fileToUpload)
                uploadFileOnServer()
            }
        }
    }


    private fun uploadFileOnServer() {
        val typeRequest = RequestBody.create(MediaType.parse("text/plain"), "img")

        val filesPartArray = arrayOfNulls<MultipartBody.Part>(1)
        val mimeType = "img"

        fileToUpload = compressImage(requireActivity(), fileToUpload)

        val fileImage = RequestBody.create(MediaType.parse(mimeType), fileToUpload)
        filesPartArray[0] = MultipartBody.Part.createFormData("image", fileToUpload?.name, fileImage)

        viewModelUpload.uploadFile(typeRequest, filesPartArray)
    }

    private fun bindObservers() {
        viewModelUpload.uploadFile.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialogImage.setLoading(false)

                    /*Create Article*/
                    val hashMap = HashMap<String, String>()
                    hashMap["title"] = binding.etTitle.text.toString()
                    hashMap["description"] = binding.etDesc.text.toString()
                    hashMap["type"] = typeOfBlog
                    hashMap["image"] = it.data?.image_name ?: ""

                    viewModel.feeds(hashMap)

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

        viewModel.feeds.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                        resultFragmentIntent(this, targetFragment ?: this,
                                AppRequestCode.ARTICLE_CHANGES, Intent())
                    else {
                        requireActivity().setResult(Activity.RESULT_OK)
                        requireActivity().finish()
                    }
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == AppRequestCode.IMAGE_PICKER) {
                val docPaths = ArrayList<Uri>()
                docPaths.addAll(data?.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
                        ?: emptyList())

                fileToUpload = File(getPathUri(requireContext(), docPaths[0]))
                Glide.with(requireContext()).load(fileToUpload).centerCrop().into(binding.ivPic)
            }
        }
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
