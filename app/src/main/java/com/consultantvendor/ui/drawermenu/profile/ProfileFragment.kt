package com.consultantvendor.ui.drawermenu.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import com.consultantvendor.ui.loginSignUp.masterprefrence.MasterPrefrenceFragment
import com.consultantvendor.ui.loginSignUp.service.ServiceFragment
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment.Companion.CATEGORY_PARENT_ID
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import permissions.dispatcher.*
import javax.inject.Inject
import kotlin.collections.set

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
        binding.tvApproved.text = "${getString(R.string.approved)} : " +
                "${DateUtils.dateTimeFormatFromUTC(DateFormat.MONTH_DAY_YEAR, userData?.account_verified_at)}"

        binding.tvBioV.text = userData?.profile?.bio ?: getString(R.string.na)
        binding.tvEmailV.text = userData?.email ?: getString(R.string.na)
        binding.tvPhoneV.text = "${userData?.country_code ?: getString(R.string.na)} ${userData?.phone ?: ""}"
        binding.tvDOBV.text = userData?.profile?.dob ?: getString(R.string.na)

        binding.tvRating.text = getString(R.string.s_s_reviews,
                getUserRating(userData?.totalRating), userData?.reviewCount)

        /*Buttons*/
        binding.tbAvailability.tag = null
        binding.tbAvailability.isChecked = userData?.manual_available ?: false
        binding.tbNotification.tag = null
        binding.tbNotification.isChecked = userData?.notification_enable ?: false
        binding.tbPremium.tag = null
        binding.tbPremium.isChecked = userData?.premium_enable ?: false

        binding.tvDesc.text = userData?.categoryData?.name ?: getString(R.string.na)

        //binding.tvRating.text = userData?.speciaity ?: getString(R.string.na)
        binding.tvPatient.gone()
        binding.tvPatientV.gone()
        binding.tvPatientV.text = userData?.patientCount ?: getString(R.string.na)
        binding.tvReviewsV.text = userData?.reviewCount ?: getString(R.string.na)

        if (userData?.profile?.dob.isNullOrEmpty()) {
            binding.tvDOB.gone()
            binding.tvDOBV.gone()
        } else {
            binding.tvDOBV.text = DateUtils.dateFormatChange(DateFormat.DATE_FORMAT,
                    DateFormat.MON_DAY_YEAR, userData?.profile?.dob ?: "")
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

            binding.tvCovidV.hideShowView(covidText.isNotEmpty())
        } else {
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

            binding.tvServicesV.hideShowView(servicesText.isNotEmpty())
        } else {
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

            binding.tvPersonalV.hideShowView(personalText.isNotEmpty())
        } else {
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

            binding.tvWorkV.hideShowView(workText.isNotEmpty())
        } else {
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

        binding.tvPhoneUpdate.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(UPDATE_NUMBER, true), AppRequestCode.PROFILE_UPDATE)
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
            val itemImages = java.util.ArrayList<String>()
            itemImages.add(getImageBaseUrl(ImageFolder.UPLOADS,userRepository.getUser()?.profile_image))
            viewImageFull(requireActivity(), itemImages, 0)
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

        binding.tbPremium.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.tag != null)
                return@setOnCheckedChangeListener

            if (isConnectedToInternet(requireContext(), true)) {
                apiForAvailability = ManualUpdate.PREMIUM
                val hashMap = HashMap<String, Any>()
                hashMap["premium_enable"] = if (isChecked) 1 else 0
                viewModelLogin.manualAvailable(hashMap)
            } else {
                binding.tbPremium.tag = null
                binding.tbPremium.isChecked = !isChecked
            }
        }

        binding.tvWorkUpdate.setOnClickListener {

            val fragment = MasterPrefrenceFragment()
            val bundle = Bundle()
            bundle.putString(MasterPrefrenceFragment.MASTER_PREFRENCE_TYPE, PreferencesType.WORK_ENVIRONMENT)
            bundle.putBoolean(UPDATE_PROFILE, true)
            fragment.arguments = bundle

            replaceResultFragment(this, fragment, R.id.container, AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvPersonalUpdate.setOnClickListener {

            val fragment = MasterPrefrenceFragment()
            val bundle = Bundle()
            bundle.putString(MasterPrefrenceFragment.MASTER_PREFRENCE_TYPE, PreferencesType.PERSONAL_INTEREST)
            bundle.putBoolean(UPDATE_PROFILE, true)
            fragment.arguments = bundle

            replaceResultFragment(this, fragment, R.id.container, AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvCovidUpdate.setOnClickListener {

            val fragment = MasterPrefrenceFragment()
            val bundle = Bundle()
            bundle.putString(MasterPrefrenceFragment.MASTER_PREFRENCE_TYPE, PreferencesType.COVID)
            bundle.putBoolean(UPDATE_PROFILE, true)
            fragment.arguments = bundle

            replaceResultFragment(this, fragment, R.id.container, AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvServiceUpdate.setOnClickListener {
            var qualification = ""
            userData?.filters?.forEach { it1 ->
                it1.options?.forEach {
                    if (it.isSelected)
                        qualification += "${it.id},"
                }
            }
            requireActivity().intent.putExtra(ServiceFragment.FILTER_DATA, qualification.removeSuffix(","))

            val fragment = MasterPrefrenceFragment()
            val bundle = Bundle()
            bundle.putString(MasterPrefrenceFragment.MASTER_PREFRENCE_TYPE, PreferencesType.PROVIDABLE_SERVICES)
            bundle.putBoolean(UPDATE_PROFILE, true)
            fragment.arguments = bundle

            replaceResultFragment(this, fragment, R.id.container, AppRequestCode.PROFILE_UPDATE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.PROFILE_UPDATE) {
                setUserProfile()
                requireActivity().setResult(Activity.RESULT_OK)
            }
        }
    }


    private fun bindObservers() {
        viewModelLogin.manualAvailable.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    val userData = userRepository.getUser()
                    when (apiForAvailability) {
                        ManualUpdate.AVAILABILITY -> userData?.manual_available = binding.tbAvailability.isChecked
                        ManualUpdate.PREMIUM -> userData?.premium_enable = binding.tbPremium.isChecked
                        ManualUpdate.NOTIFICATION -> userData?.notification_enable = binding.tbNotification.isChecked
                    }

                    prefsManager.save(USER_DATA, userData)

                }
                Status.ERROR -> {
                    when (apiForAvailability) {
                        ManualUpdate.AVAILABILITY -> {
                            binding.tbAvailability.tag = null
                            binding.tbAvailability.isChecked = !binding.tbAvailability.isChecked
                        }
                        ManualUpdate.PREMIUM -> {
                            binding.tbPremium.tag = null
                            binding.tbPremium.isChecked = !binding.tbPremium.isChecked
                        }
                        ManualUpdate.NOTIFICATION -> {
                            binding.tbNotification.tag = null
                            binding.tbNotification.isChecked = !binding.tbNotification.isChecked
                        }
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

    companion object {

        object ManualUpdate {
            const val AVAILABILITY = "AVAILABILITY"
            const val NOTIFICATION = "NOTIFICATION"
            const val PREMIUM = "PREMIUM"
        }
    }
}
