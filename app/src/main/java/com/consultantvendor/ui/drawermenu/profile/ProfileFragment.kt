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
import com.consultantvendor.data.models.responses.Filter
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

    private lateinit var viewModelLogin: LoginViewModel

    private var userData: UserData? = null

    private var apiForAvailability = ""


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
        viewModelLogin = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
    }

    private fun hiApiDoctorDetail() {
        if (isConnectedToInternet(requireContext(), true)) {
            viewModelLogin.profile()
        }
    }

    private fun setUserProfile() {
        userData = userRepository.getUser()

        binding.tvName.text = getDoctorName(userData)
        binding.tvBioV.text = userData?.profile?.bio ?: getString(R.string.na)
        binding.tvEmailV.text = userData?.email ?: getString(R.string.na)
        binding.tvPhoneV.text = "${userData?.country_code ?: getString(R.string.na)} ${userData?.phone ?: ""}"
        binding.tvDOBV.text = userData?.profile?.dob ?: getString(R.string.na)

        /*Buttons*/
        binding.tbAvailability.tag = null
        binding.tbAvailability.isChecked = userData?.manual_available ?: false
        binding.tbNotification.tag = null
        binding.tbNotification.isChecked = userData?.notification_enable ?: false

        binding.tvDesc.text = userData?.categoryData?.name ?: getString(R.string.na)

        //binding.tvRating.text = userData?.speciaity ?: getString(R.string.na)
        binding.tvPatientV.text = userData?.patientCount ?: getString(R.string.na)
        binding.tvReviewsV.text = userData?.reviews ?: getString(R.string.na)

        if (userData?.profile?.dob.isNullOrEmpty()) {
            binding.tvDOB.gone()
            binding.tvDOBV.gone()
        } else {
            binding.tvDOBV.text = DateUtils.dateFormatChange(
                    DateFormat.DATE_FORMAT,
                    DateFormat.MON_DAY_YEAR, userData?.profile?.dob ?: ""
            )
        }

        loadImage(binding.ivPic, userData?.profile_image,
                R.drawable.ic_profile_placeholder)

        binding.tvSetPrefrences.gone()
        binding.tvDocuments.gone()
        binding.tvSetAvailability.gone()
        binding.tvUpdateCategory.gone()


        var specialisation = ""
        userData?.filters?.forEach {
            it.options?.forEach {
                if (it.isSelected) {
                    specialisation += "${it.option_name}, "
                }
            }
        }
        binding.tvSpecialisation.text = specialisation.removeSuffix(", ")
        binding.tvSpecialisation.hideShowView(specialisation.isNotEmpty())

        userData?.custom_fields?.forEach {
            when (it.field_name) {
                CustomFields.WORK_EXPERIENCE -> {
                    binding.tvExperienceV.text = it.field_value
                }
            }
        }


        val covid = ArrayList<Filter>()
        val personalInterest = ArrayList<Filter>()
        val providableServices = ArrayList<Filter>()
        val workExperience = ArrayList<Filter>()
        userData?.master_preferences?.forEach {
            when (it.preference_type) {
                PreferencesType.COVID ->
                    covid.add(it)
                PreferencesType.PERSONAL_INTEREST ->
                    personalInterest.add(it)
                PreferencesType.WORK_ENVIRONMENT ->
                    workExperience.add(it)
                PreferencesType.PROVIDABLE_SERVICES ->
                    providableServices.add(it)
            }
        }

        if (covid.isNotEmpty()) {
            var covidText = ""
            covid.forEach {
                covidText += it.preference_name + "\n"

                it.options?.forEach {
                    if (it.isSelected) {
                        covidText += it.option_name + "\n\n"
                    }
                }
            }
            binding.tvCovidV.text = covidText

            binding.tvCovid.hideShowView(covidText.isNotEmpty())
            binding.tvCovidV.hideShowView(covidText.isNotEmpty())
        } else {
            binding.tvCovid.gone()
            binding.tvCovidV.gone()
        }

        if (providableServices.isNotEmpty()) {
            var servicesText = ""
            providableServices.forEach {
                it.options?.forEach {
                    if (it.isSelected) {
                        servicesText += it.option_name + ", "
                    }
                }
            }
            binding.tvServicesV.text = servicesText.removeSuffix(", ")

            binding.tvServices.hideShowView(servicesText.isNotEmpty())
            binding.tvServicesV.hideShowView(servicesText.isNotEmpty())
        } else {
            binding.tvServices.gone()
            binding.tvServicesV.gone()
        }

        if (personalInterest.isNotEmpty()) {
            var personalText = ""
            personalInterest.forEach {
                it.options?.forEach {
                    if (it.isSelected) {
                        personalText += it.option_name + ", "
                    }
                }
            }
            binding.tvPersonalV.text = personalText.removeSuffix(", ")

            binding.tvPersonal.hideShowView(personalText.isNotEmpty())
            binding.tvPersonalV.hideShowView(personalText.isNotEmpty())
        } else {
            binding.tvPersonal.gone()
            binding.tvPersonalV.gone()
        }

        if (workExperience.isNotEmpty()) {
            var workText = ""
            workExperience.forEach {
                it.options?.forEach {
                    if (it.isSelected) {
                        workText += it.option_name + ", "
                    }
                }
            }
            binding.tvWorkV.text = workText.removeSuffix(", ")

            binding.tvWork.hideShowView(workText.isNotEmpty())
            binding.tvWorkV.hideShowView(workText.isNotEmpty())
        } else {
            binding.tvWork.gone()
            binding.tvWorkV.gone()
        }

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

        binding.tbAvailability.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.tag != null)
                return@setOnCheckedChangeListener

            if (isConnectedToInternet(requireContext(), true)) {
                apiForAvailability = ManualUpdate.AVAILABILITY
                val hashMap = HashMap<String, Any>()
                hashMap["manual_available"] = if (isChecked) 1 else 0
                viewModelLogin.manualAvailable(hashMap)
            } else {
                binding.tbAvailability.tag = null
                binding.tbAvailability.isChecked = !isChecked
                //binding.tbAvailability.setOnCheckedChangeListener(mListener)
            }
        }

        binding.tbNotification.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.tag != null)
                return@setOnCheckedChangeListener

            if (isConnectedToInternet(requireContext(), true)) {
                apiForAvailability = ManualUpdate.NOTIFICATION
                val hashMap = HashMap<String, Any>()
                hashMap["notification_enable"] = if (isChecked) 1 else 0
                viewModelLogin.manualAvailable(hashMap)
            } else {
                binding.tbNotification.tag = null
                binding.tbNotification.isChecked = !isChecked
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

        hashMap["name"] = getRequestBody(userData?.name)

        if (fileToUpload != null && fileToUpload.exists()) {
            hashMap["type"] = getRequestBody("img")
            val body: RequestBody =
                    RequestBody.create(MediaType.parse("image/jpeg"), fileToUpload)

            hashMap["profile_image\"; fileName=\"" + fileToUpload.name] = body
        }
        viewModelLogin.updateProfile(hashMap)
    }


    private fun bindObservers() {
        viewModelLogin.updateProfile.observe(requireActivity(), Observer {
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

        viewModelLogin.manualAvailable.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    val userData = userRepository.getUser()
                    if (apiForAvailability==ManualUpdate.AVAILABILITY)
                        userData?.manual_available = binding.tbAvailability.isChecked
                    else
                        userData?.notification_enable = binding.tbNotification.isChecked

                    prefsManager.save(USER_DATA, userData)

                }
                Status.ERROR -> {
                    if (apiForAvailability==ManualUpdate.AVAILABILITY) {
                        binding.tbAvailability.tag = null
                        binding.tbAvailability.isChecked = !binding.tbAvailability.isChecked
                    } else {
                        binding.tbNotification.tag = null
                        binding.tbNotification.isChecked = !binding.tbNotification.isChecked
                    }

                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })

        viewModelLogin.profile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    setUserProfile()
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(false)
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

    companion object {

        object ManualUpdate {
            const val AVAILABILITY = ""
            const val NOTIFICATION = ""
            const val PREMIUM = ""
        }
    }
}
