package com.consultantvendor.ui.drawermenu.profile

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
import com.consultantvendor.data.models.responses.UserData
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentProfileBinding
import com.consultantvendor.ui.dashboard.home.AppointmentViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.SignUpActivity
import com.consultantvendor.ui.loginSignUp.document.DocumentsFragment
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment.Companion.CATEGORY_PARENT_ID
import com.consultantvendor.utils.*
import com.consultantvendor.utils.PermissionUtils
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import okhttp3.MediaType
import okhttp3.RequestBody
import permissions.dispatcher.*
import java.io.File
import javax.inject.Inject
import kotlin.collections.set

@RuntimePermissions
class ProfileFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentProfileBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: AppointmentViewModel

    private lateinit var viewModelDoctor: DoctorViewModel

    private lateinit var viewModelLogin: LoginViewModel

    private var userData: UserData? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
            rootView = binding.root

            initialise()
            setUserProfile()
            hiApiDoctorDetail()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]
        viewModelDoctor = ViewModelProvider(this, viewModelFactory)[DoctorViewModel::class.java]
        viewModelLogin = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
    }

    private fun hiApiDoctorDetail() {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["doctor_id"] = userRepository.getUser()?.id ?: ""
            viewModelDoctor.doctorDetails(hashMap)
        }
    }

    private fun setUserProfile() {
        userData = userRepository.getUser()

        binding.tvName.text = getDoctorName(userData)
        binding.tvEmailV.text = userData?.email ?: getString(R.string.na)
        binding.tvPhoneV.text = "${userData?.country_code ?: ""} ${userData?.phone ?: ""}"
        binding.tvDOBV.text = userData?.profile?.dob ?: getString(R.string.na)
        binding.tvDesc.text = userData?.categoryData?.name ?: getString(R.string.na)

        //binding.tvRating.text = userData?.speciaity ?: getString(R.string.na)
        binding.tvPatientV.text = userData?.patientCount ?: getString(R.string.na)
        binding.tvExperienceV.text =
                "${getAge(userData?.profile?.working_since)} ${getString(R.string.years)}"
        binding.tvReviewsV.text = userData?.reviews ?: getString(R.string.na)

        if (!userData?.profile?.dob.isNullOrEmpty())
            binding.tvDOBV.text = DateUtils.dateFormatChange(
                    DateFormat.DATE_FORMAT,
                    DateFormat.MON_DAY_YEAR, userData?.profile?.dob ?: ""
            )

        loadImage(binding.ivPic, userData?.profile_image,
                R.drawable.ic_profile_placeholder)

        binding.tvSetPrefrences.gone()
        binding.tvDocuments.gone()
        binding.tvSetAvailability.gone()
        binding.tvUpdateCategory.gone()

    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.tvEdit.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(UPDATE_PROFILE, true), AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvSetAvailability.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(CATEGORY_PARENT_ID, userData?.categoryData)
                    .putExtra(UPDATE_AVAILABILITY, true), AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvSetPrefrences.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(CATEGORY_PARENT_ID, userData?.categoryData)
                    .putExtra(UPDATE_PREFRENCES, true), AppRequestCode.PROFILE_UPDATE)

        }

        binding.tvDocuments.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(CATEGORY_PARENT_ID, userData?.categoryData)
                    .putExtra(DocumentsFragment.UPDATE_DOCUMENTS, true), AppRequestCode.PROFILE_UPDATE)

        }

        binding.tvUpdateCategory.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(UPDATE_CATEGORY, true), AppRequestCode.PROFILE_UPDATE)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == AppRequestCode.IMAGE_PICKER) {
                val docPaths = ArrayList<Uri>()
                docPaths.addAll(data?.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
                        ?: emptyList())

                val fileToUpload = File(getPathUri(requireContext(), docPaths[0]))
                Glide.with(requireContext()).load(fileToUpload).into(binding.ivPic)

                uploadFileOnServer(compressImage(requireActivity(), fileToUpload))
            } else if (requestCode == AppRequestCode.PROFILE_UPDATE) {
                setUserProfile()
                requireActivity().setResult(Activity.RESULT_OK)
            }
        }
    }

    private fun uploadFileOnServer(fileToUpload: File?) {

        val hashMap = HashMap<String, RequestBody>()

        hashMap["name"] = getRequestBody( userData?.name)

        if (fileToUpload != null && fileToUpload.exists()) {
            hashMap["type"] = getRequestBody("img")
            val body: RequestBody =
                    RequestBody.create(MediaType.parse("image/jpeg"), fileToUpload)

            hashMap["profile_image\"; fileName=\"" + fileToUpload.name] = body
        }
        viewModelLogin.updateProfile(hashMap)
    }


    private fun bindObservers() {
        viewModelLogin.updateProfile.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)
                    setUserProfile()
                    requireActivity().setResult(Activity.RESULT_OK)
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

        viewModelDoctor.doctorDetails.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    val doctorData = it.data?.dcotor_detail

                    binding.tvRating.text = getString(
                            R.string.s_s_reviews,
                            getUserRating(doctorData?.totalRating),
                            doctorData?.reviewCount
                    )
                    binding.tvPatientV.text = doctorData?.patientCount ?: getString(R.string.na)
                    binding.tvExperienceV.text =
                            "${getAge(doctorData?.profile?.working_since)} ${getString(R.string.years)}"
                    binding.tvReviewsV.text = doctorData?.reviewCount ?: getString(R.string.na)

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
