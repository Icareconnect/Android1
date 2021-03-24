package com.consultantvendor.ui.loginSignUp.register

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.consultantvendor.R
import com.consultantvendor.appClientDetails
import com.consultantvendor.data.models.requests.SaveAddress
import com.consultantvendor.data.models.requests.SetFilter
import com.consultantvendor.data.models.requests.SetService
import com.consultantvendor.data.models.requests.UpdateServices
import com.consultantvendor.data.models.responses.Filter
import com.consultantvendor.data.models.responses.FilterOption
import com.consultantvendor.data.models.responses.UserData
import com.consultantvendor.data.models.responses.appdetails.Insurance
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentRegisterBinding
import com.consultantvendor.ui.chat.UploadFileViewModel
import com.consultantvendor.ui.dashboard.HomeActivity
import com.consultantvendor.ui.drawermenu.classes.ClassesViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.loginemail.LoginEmailFragment.Companion.DUMMY_NAME
import com.consultantvendor.ui.loginSignUp.masterprefrence.MasterPrefrenceFragment
import com.consultantvendor.ui.loginSignUp.masterprefrence.MasterPrefrenceFragment.Companion.MASTER_PREFRENCE_TYPE
import com.consultantvendor.ui.loginSignUp.service.ServiceFragment.Companion.FILTER_DATA
import com.consultantvendor.utils.*
import com.consultantvendor.utils.PermissionUtils
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.consultantvendor.utils.dialogs.ProgressDialogImage
import com.google.android.libraries.places.widget.Autocomplete
import com.google.gson.Gson
import dagger.android.support.DaggerFragment
import droidninja.filepicker.FilePickerConst
import okhttp3.MediaType
import okhttp3.RequestBody
import permissions.dispatcher.*
import java.io.File
import javax.inject.Inject


@RuntimePermissions
class RegisterFragment : DaggerFragment(), OnDateSelected {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentRegisterBinding

    private var rootView: View? = null

    private lateinit var viewModel: LoginViewModel

    private lateinit var viewModelFilter: ClassesViewModel

    private lateinit var viewModelUpload: UploadFileViewModel

    private lateinit var progressDialog: ProgressDialog

    private lateinit var progressDialogImage: ProgressDialogImage

    private lateinit var adapterQualification: CheckItemAdapter

    private var itemFilter = ArrayList<Filter>()

    private var itemsQualification = ArrayList<FilterOption>()

    private lateinit var adapterShift: CheckItemAdapter

    private var itemsShift = ArrayList<FilterOption>()

    private lateinit var adapterExperience: CheckItemAdapter

    private var itemsExperience = ArrayList<FilterOption>()

    private var address: SaveAddress? = null

    private var userData: UserData? = null

    private var fileToUpload: File? = null

    private var hashMap = HashMap<String, Any>()

    private var qualification = ""
    private var shift = ""
    private var experience = ""


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
            rootView = binding.root

            initialise()
            listeners()
            setAdapter()
            bindObservers()
            setEditInformation()

            if (isConnectedToInternet(requireContext(), true)) {
                val hashMap = HashMap<String, String>()
                hashMap["category_id"] = CATEGORY_ID
                viewModelFilter.getFilters(hashMap)
            }
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        viewModelUpload = ViewModelProvider(this, viewModelFactory)[UploadFileViewModel::class.java]
        viewModelFilter = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
        progressDialogImage = ProgressDialogImage(requireActivity())

        editTextScroll(binding.etBio)
        //binding.cvQualification.gone()
    }

    private fun setEditInformation() {
        userData = userRepository.getUser()

        if (!userData?.name.equals(DUMMY_NAME))
            binding.etName.setText(userData?.name ?: "")

        binding.etEmail.setText(userData?.email ?: "")
        if (arguments?.containsKey(UPDATE_PROFILE) == true) {
            binding.tvBioDesc.gone()
            binding.ccpCountryCode.gone()
            binding.ivLine.gone()
            binding.etMobileNumber.gone()
            binding.ivLine1.gone()

            binding.tvName.text = getString(R.string.update)
            binding.tvDesc.gone()
            //binding.cvQualification.visible()

            loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)
            binding.etBio.setText(userData?.profile?.bio ?: "")
            if (!userData?.profile?.location_name.isNullOrEmpty()) {
                binding.etLocation.setText(userData?.profile?.location_name)

                address = SaveAddress()
                address?.locationName = userData?.profile?.location_name
                address?.location = ArrayList()
                address?.location?.add(userData?.profile?.long?.toDouble() ?: 0.0)
                address?.location?.add(userData?.profile?.lat?.toDouble() ?: 0.0)
            }

            userData?.custom_fields?.forEach {
                when (it.field_name) {
                    CustomFields.WORK_EXPERIENCE -> {
                        when (it.field_value) {
                            getString(R.string.exp_1) -> {
                                itemsExperience[0].isSelected = true
                            }
                            getString(R.string.exp_2) -> {
                                itemsExperience[1].isSelected = true
                            }
                            getString(R.string.exp_3) -> {
                                itemsExperience[2].isSelected = true
                            }
                            getString(R.string.exp_4) -> {
                                itemsExperience[3].isSelected = true
                            }
                        }
                        adapterExperience.notifyDataSetChanged()

                        return@forEach
                    }
                    CustomFields.WORKING_SHIFTS -> {
                        if (it.field_value?.contains(getString(R.string.shift_0)) == true) {
                            itemsShift[0].isSelected = true
                        }
                        if (it.field_value?.contains(getString(R.string.shift_01)) == true) {
                            itemsShift[1].isSelected = true
                        }
                        if (it.field_value?.contains(getString(R.string.shift_1)) == true) {
                            itemsShift[2].isSelected = true
                        }
                        if (it.field_value?.contains(getString(R.string.shift_2)) == true) {
                            itemsShift[3].isSelected = true
                        }
                        if (it.field_value?.contains(getString(R.string.shift_3)) == true) {
                            itemsShift[4].isSelected = true
                        }

                        adapterShift.notifyDataSetChanged()

                        return@forEach
                    }
                    CustomFields.PROFESSIONAL_LISCENCE -> {
                        binding.etLiscence.setText(it.field_value ?: "")
                        return@forEach
                    }
                    CustomFields.CERTIFICATION -> {
                        binding.etCertification.setText(it.field_value ?: "")
                        return@forEach
                    }
                    CustomFields.START_DATE -> {
                        binding.etStartDate.setText(DateUtils.dateFormatChange(DateFormat.DATE_FORMAT,
                                DateFormat.DATE_FORMAT_SLASH, it.field_value ?: ""))
                        return@forEach
                    }
                }
            }
        } else if (arguments?.containsKey(UPDATE_NUMBER) == true) {
            if (!userData?.phone.isNullOrEmpty()) {
                binding.ccpCountryCode.gone()
                binding.ivLine.gone()
                binding.etMobileNumber.gone()
                binding.ivLine1.gone()
            }

            if (!userData?.email.isNullOrEmpty()) {
                binding.ilEmail.gone()
            }
        }
    }

    private fun setAdapter() {

        val listShift = resources.getStringArray(R.array.shift)
        itemsShift.clear()
        listShift.forEach {
            val item = FilterOption()
            item.option_name = it
            itemsShift.add(item)
        }

        adapterShift = CheckItemAdapter(true, itemsShift)
        binding.rvShift.adapter = adapterShift

        val listExperience = resources.getStringArray(R.array.experience)
        itemsExperience.clear()
        listExperience.forEach {
            val item = FilterOption()
            item.option_name = it
            itemsExperience.add(item)
        }

        adapterExperience = CheckItemAdapter(false, itemsExperience)
        binding.rvExperience.adapter = adapterExperience
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.ivPic.setOnClickListener {
            getStorageWithPermissionCheck()
        }

        binding.etStartDate.setOnClickListener {
            binding.etStartDate.hideKeyboard()
            DateUtils.openDatePicker(requireActivity(), this, false, true)
        }

        binding.etLocation.setOnClickListener {
            placePicker(this, requireActivity())
        }

      /*  binding.etBio.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) binding.etBio.hint = getString(R.string.bio)
            else binding.etBio.hint = getString(R.string.bio_des)
        }*/

        binding.tvContinue.setOnClickListener {
            qualification = ""
            itemsQualification.forEachIndexed { index, filterOption ->
                if (filterOption.isSelected) {
                    qualification += "${filterOption.id},"
                }
            }

            shift = ""
            itemsShift.forEachIndexed { index, filterOption ->
                if (filterOption.isSelected) {
                    shift += "${filterOption.option_name}, "
                }
            }

            experience = ""
            itemsExperience.forEachIndexed { index, filterOption ->
                if (filterOption.isSelected) {
                    experience = filterOption.option_name ?: ""
                    return@forEachIndexed
                }
            }

            when {
                binding.etName.text.toString().trim().isEmpty() -> {
                    binding.etName.showSnackBar(getString(R.string.enter_name))
                }
                (binding.etMobileNumber.visibility == View.VISIBLE &&
                        (binding.etMobileNumber.text.toString().isEmpty() || binding.etMobileNumber.text.toString().length < 6)) -> {
                    binding.etEmail.showSnackBar(getString(R.string.enter_phone_number))
                }
                (binding.ilEmail.visibility == View.VISIBLE && binding.etEmail.text.toString().trim().isEmpty()) -> {
                    binding.etEmail.showSnackBar(getString(R.string.enter_email))
                }
                (binding.etEmail.text.toString().trim().isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(
                        binding.etEmail.text.toString().trim()).matches()) -> {
                    binding.etEmail.showSnackBar(getString(R.string.enter_correct_email))
                }
                binding.etLocation.text.toString().isEmpty() -> {
                    binding.etLocation.showSnackBar(getString(R.string.select_address))
                }
                binding.etBio.text.toString().trim().isEmpty() -> {
                    binding.etBio.showSnackBar(getString(R.string.enter_bio))
                }
                qualification.isEmpty() -> {
                    binding.tvQualification.showSnackBar(getString(R.string.select_your_qualification_type))
                }
                shift.isEmpty() -> {
                    binding.tvShift.showSnackBar(getString(R.string.what_type_of_shifts))
                }
                experience.isEmpty() -> {
                    binding.tvExperience.showSnackBar(getString(R.string.please_select_your_experience))
                }
                /* binding.etLiscence.text.toString().trim().isEmpty() -> {
                     binding.etLiscence.showSnackBar(getString(R.string.professional_liscence))
                 }*/
                /* binding.etCertification.text.toString().trim().isEmpty() -> {
                     binding.etCertification.showSnackBar(getString(R.string.certification))
                 }*/
                binding.etStartDate.text.toString().trim().isEmpty() -> {
                    binding.etStartDate.showSnackBar(getString(R.string.start_date))
                }
                isConnectedToInternet(requireContext(), true) -> {
                    requireActivity().intent.putExtra(FILTER_DATA, qualification.removeSuffix(","))

                    if (fileToUpload != null && fileToUpload?.exists() == true) {
                        uploadFileOnServer(fileToUpload)
                    } else {
                        hitApi(null)
                    }
                }
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

    private fun hitApi(image: String?) {
        val hashMap = HashMap<String, Any>()

        hashMap["name"] = binding.etName.text.toString().trim()
        hashMap["location_name"] = address?.locationName ?: ""
        hashMap["lat"] = address?.location?.get(1).toString()
        hashMap["long"] = address?.location?.get(0).toString()
        hashMap["bio"] = binding.etBio.text.toString().trim()
        if (binding.etMobileNumber.text.toString().trim().isNotEmpty()) {
            hashMap["country_code"] = binding.ccpCountryCode.selectedCountryCodeWithPlus
            hashMap["phone"] = binding.etMobileNumber.text.toString()
        }
        if (binding.etEmail.text.toString().trim().isNotEmpty())
            hashMap["email"] = binding.etEmail.text.toString()


        val custom_fields = ArrayList<Insurance>()

        appClientDetails.custom_fields?.service_provider?.forEach {
            val item = it
            when (it.field_name) {
                CustomFields.WORKING_SHIFTS -> {
                    item.field_value = shift.removeSuffix(",")
                    custom_fields.add(item)
                }
                CustomFields.WORK_EXPERIENCE -> {
                    item.field_value = experience
                    custom_fields.add(item)
                }
                CustomFields.PROFESSIONAL_LISCENCE -> {
                    if (binding.etLiscence.text.toString().trim().isNotEmpty()) {
                        item.field_value = binding.etLiscence.text.toString().trim()
                        custom_fields.add(item)
                    }
                }
                CustomFields.CERTIFICATION -> {
                    if (binding.etCertification.text.toString().trim().isNotEmpty()) {
                        item.field_value = binding.etCertification.text.toString().trim()
                        custom_fields.add(item)
                    }
                }
                CustomFields.START_DATE -> {
                    item.field_value = DateUtils.dateFormatChange(DateFormat.DATE_FORMAT_SLASH,
                            DateFormat.DATE_FORMAT, binding.etStartDate.text.toString())
                    custom_fields.add(item)
                }
            }
        }

        hashMap["custom_fields"] = Gson().toJson(custom_fields)

        if (image != null)
            hashMap["profile_image"] = image

        /*Update profile or register*/
        when {
            arguments?.containsKey(UPDATE_NUMBER) == true -> {
                viewModel.updateProfile(hashMap)
            }
            arguments?.containsKey(UPDATE_PROFILE) == true -> {
                viewModel.updateProfile(hashMap)
            }
            else -> {
                hashMap["user_type"] = APP_TYPE
                viewModel.register(hashMap)
            }
        }
    }

    private fun bindObservers() {
        viewModel.register.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)
                    /*If need to move to phone number*/

                    updateCategory()
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

        viewModel.updateProfile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    requireActivity().setResult(Activity.RESULT_OK)
                    updateCategory()
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

        viewModelUpload.uploadFile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialogImage.setLoading(false)

                    hitApi(it.data?.image_name ?: "")
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

        viewModelFilter.getFilters.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    itemFilter.clear()
                    itemFilter.addAll(it.data?.filters ?: emptyList())

                    itemsQualification.clear()
                    if (itemFilter.isNotEmpty())
                        itemsQualification.addAll(itemFilter[0].options ?: emptyList())

                    if (!userData?.filters.isNullOrEmpty()) {
                        itemsQualification.forEachIndexed { index, filterOption ->
                            userData?.filters?.get(0)?.options?.forEach {
                                if (filterOption.id == it.id && it.isSelected) {
                                    itemsQualification[index].isSelected = true
                                    return@forEach
                                }
                            }
                        }
                    }

                    adapterQualification = CheckItemAdapter(false, itemsQualification)
                    binding.rvQualification.adapter = adapterQualification
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

        viewModel.updateServices.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    requireActivity().setResult(Activity.RESULT_OK)

                    when {
                        arguments?.containsKey(UPDATE_PROFILE) == true -> {
                            requireActivity().finish()
                        }
                        userRepository.isUserLoggedIn() -> {
                            startActivity(Intent(requireContext(), HomeActivity::class.java)
                                    .putExtra(EXTRA_IS_FIRST, true))
                            requireActivity().finish()
                        }
                        else -> {
                            val fragment = MasterPrefrenceFragment()
                            val bundle = Bundle()
                            bundle.putString(MASTER_PREFRENCE_TYPE, PreferencesType.PERSONAL_INTEREST)
                            fragment.arguments = bundle

                            replaceFragment(requireActivity().supportFragmentManager,
                                    fragment, R.id.container)
                        }
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

    private fun updateCategory() {
        if (isConnectedToInternet(requireContext(), true)) {
            val updateServices = UpdateServices()
            updateServices.category_id = CATEGORY_ID

            /*Add service type*/
            updateServices.category_services_type = ArrayList()

            val setService = SetService()
            setService.id = SERVICE_ID
            setService.available = "1"
            setService.price = 10
            updateServices.category_services_type?.add(setService)

            val filterArray = ArrayList<SetFilter>()
            var setFilter: SetFilter

            itemFilter.forEach {
                setFilter = SetFilter()

                /*Set filter Id*/
                setFilter.filter_id = it.id
                setFilter.filter_option_ids = ArrayList()

                itemsQualification.forEach {
                    if (it.isSelected) {
                        setFilter.filter_option_ids?.add(it.id ?: "")
                    }
                }
                filterArray.add(setFilter)
            }

            updateServices.filters = filterArray
            viewModel.updateServices(updateServices)
        }
    }

    override fun onDateSelected(date: String) {
        binding.etStartDate.setText(date)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppRequestCode.AUTOCOMPLETE_REQUEST_CODE -> {
                    binding.etLocation.hideKeyboard()
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(it)

                        binding.etLocation.setText(getAddress(place))

                        Log.i("Place===", "Place: " + place.name + ", " + place.id)

                        address = SaveAddress()
                        address?.locationName = getAddress(place)
                        address?.location = ArrayList()
                        address?.location?.add(place.latLng?.longitude ?: 0.0)
                        address?.location?.add(place.latLng?.latitude ?: 0.0)

                        binding.etLiscence.hideKeyboard()
                        //performAddressSelectAction(false, address)
                    }
                }
                AppRequestCode.IMAGE_PICKER -> {
                    val docPaths = ArrayList<Uri>()
                    docPaths.addAll(data?.getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
                            ?: emptyList())

                    fileToUpload = File(getPathUri(requireContext(), docPaths[0]))
                    Glide.with(requireContext()).load(fileToUpload).into(binding.ivPic)
                }
            }
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun getStorage() {
        selectImages(this,requireActivity())
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
