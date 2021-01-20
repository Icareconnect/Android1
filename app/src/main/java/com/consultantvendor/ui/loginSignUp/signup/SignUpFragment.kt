package com.consultantvendor.ui.loginSignUp.signup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.consultantvendor.R
import com.consultantvendor.appClientDetails
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentSignupBinding
import com.consultantvendor.ui.drawermenu.classes.ClassesViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.category.CategoryFragment
import com.consultantvendor.ui.loginSignUp.insurance.InsuranceFragment
import com.consultantvendor.ui.loginSignUp.login.LoginFragment
import com.consultantvendor.utils.*
import com.consultantvendor.utils.PermissionUtils
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import permissions.dispatcher.*
import java.io.File
import javax.inject.Inject

@RuntimePermissions
class SignUpFragment : DaggerFragment(), OnDateSelected {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentSignupBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: LoginViewModel

    private lateinit var viewModelClass: ClassesViewModel

    private var isUpdate = false

    private var fileToUpload: File? = null

    private var selectedDob = true


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)
            rootView = binding.root

            initialise()
            listeners()
            setEditInformation()
            bindObservers()
        }
        return rootView
    }


    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        viewModelClass = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

    }

    private fun setEditInformation() {
        editTextScroll(binding.etBio)
        val userData = userRepository.getUser()

        if (arguments?.containsKey(UPDATE_PROFILE) == true) {
            binding.tvNext.text = getString(R.string.update)

            val title = userData?.profile?.title ?: getString(R.string.title)
            val list = resources.getStringArray(R.array.dr_title)
            binding.spnTitle.setSelection(list.indexOf(title))

            binding.etName.setText(userData?.name ?: "")
            binding.etBio.setText(userData?.profile?.bio ?: "")
            binding.etEmail.setText(userData?.email ?: "")

            if (!userData?.profile?.dob.isNullOrEmpty())
                binding.etDob.setText(DateUtils.dateFormatChange(DateFormat.DATE_FORMAT,
                        DateFormat.DATE_FORMAT_SLASH, userData?.profile?.dob ?: ""))

            if (!userData?.profile?.working_since.isNullOrEmpty())
                binding.etYears.setText(DateUtils.dateFormatChange(DateFormat.DATE_FORMAT,
                        DateFormat.DATE_FORMAT_SLASH, userData?.profile?.working_since ?: ""))

            loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)

            binding.ilPassword.gone()
            binding.ilConfirmPassword.gone()

            isUpdate = true
        } else if (arguments?.containsKey(UPDATE_NUMBER) == true) {
            binding.ilPassword.gone()
            binding.ilConfirmPassword.gone()

            binding.etName.setText(userData?.name ?: "")
            binding.etEmail.setText(userData?.email ?: "")

            if (!userData?.name.isNullOrEmpty())
                binding.etName.isFocusable = false

            if (!userData?.email.isNullOrEmpty())
                binding.etEmail.isFocusable = false

            loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)

            isUpdate = true
        }
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (arguments?.containsKey(UPDATE_PROFILE) == true)
                requireActivity().finish()
            else
                requireActivity().supportFragmentManager.popBackStack()
        }

        binding.etDob.setOnClickListener {
            selectedDob = true
            binding.etDob.hideKeyboard()
            DateUtils.openDatePicker(requireActivity(), this, true, false)
        }
        binding.etYears.setOnClickListener {
            selectedDob = false
            binding.etDob.hideKeyboard()
            DateUtils.openDatePicker(requireActivity(), this, true, false)
        }

        binding.tvNext.setOnClickListener {
            checkValidation()
        }

        binding.etTitle.setOnClickListener {
            binding.spnTitle.performClick()
        }

        binding.ivPic.setOnClickListener {
            getStorageWithPermissionCheck()
        }

        binding.spnTitle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>,
                                        selectedItemView: View?, position: Int, id: Long) {
                binding.etTitle.setText(binding.spnTitle.selectedItem.toString())
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {

            }
        }
    }

    private fun checkValidation() {
        when {
            binding.spnTitle.selectedItemPosition == 0 -> {
                binding.etTitle.showSnackBar(getString(R.string.enter_title))
            }
            binding.etName.text.toString().trim().isEmpty() -> {
                binding.etName.showSnackBar(getString(R.string.enter_name))
            }
            (!isUpdate && binding.etEmail.text.toString().trim().isEmpty()) -> {
                binding.etEmail.showSnackBar(getString(R.string.enter_email))
            }
            (binding.etEmail.text.toString().trim().isNotEmpty() &&
                    !Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString().trim()).matches()) -> {
                binding.etEmail.showSnackBar(getString(R.string.enter_correct_email))
            }
            /*(!isUpdate && binding.etPassword.text.toString().length < 8) -> {
                binding.etPassword.showSnackBar(getString(R.string.enter_password))
            }
            binding.etPassword.text.toString() != binding.etConfirmPassword.text.toString() -> {
                binding.etPassword.showSnackBar(getString(R.string.enter_confirm_password))
            }*/
            binding.etDob.text.toString().isEmpty() -> {
                binding.etDob.showSnackBar(getString(R.string.select_dob))
            }
            binding.etYears.text.toString().isEmpty() -> {
                binding.etYears.showSnackBar(getString(R.string.since_working))
            }

            binding.etBio.text.toString().trim().isEmpty() -> {
                binding.etBio.showSnackBar(getString(R.string.enter_bio))
            }
            isConnectedToInternet(requireContext(), true) -> {

                val hashMap = HashMap<String, Any>()
                hashMap["title"] = binding.etTitle.text.toString()
                hashMap["name"] = binding.etName.text.toString().trim()
                hashMap["dob"] = DateUtils.dateFormatChange(DateFormat.DATE_FORMAT_SLASH,
                        DateFormat.DATE_FORMAT, binding.etDob.text.toString())
                hashMap["working_since"] = DateUtils.dateFormatChange(DateFormat.DATE_FORMAT_SLASH,
                        DateFormat.DATE_FORMAT, binding.etYears.text.toString())
                hashMap["bio"] = binding.etBio.text.toString().trim()


                hashMap["email"] = binding.etEmail.text.toString().trim()
                /*Update profile or register*/
                when {
                    arguments?.containsKey(UPDATE_NUMBER) == true -> {
                        viewModel.updateProfile(hashMap)
                    }
                    arguments?.containsKey(UPDATE_PROFILE) == true -> {
                        viewModel.updateProfile(hashMap)
                    }
                    else -> {
                        hashMap["password"] = binding.etPassword.text.toString().trim()
                        hashMap["user_type"] = APP_TYPE
                        viewModel.register(hashMap)
                    }
                }
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

                    val fragment = LoginFragment()
                    val bundle = Bundle()
                    bundle.putBoolean(UPDATE_NUMBER, true)
                    fragment.arguments = bundle

                    replaceFragment(requireActivity().supportFragmentManager,
                            fragment, R.id.container)

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

                    if (appClientDetails.insurance == true || appClientDetails.clientFeaturesKeys.isAddress == true) {
                        val fragment = InsuranceFragment()
                        val bundle = Bundle()
                        if (arguments?.containsKey(UPDATE_PROFILE) == true)
                            bundle.putBoolean(UPDATE_PROFILE, true)
                        fragment.arguments = bundle

                        replaceFragment(requireActivity().supportFragmentManager,
                                fragment, R.id.container)
                    } else if (arguments?.containsKey(UPDATE_PROFILE) == true) {
                        requireActivity().finish()
                    } else
                        replaceFragment(requireActivity().supportFragmentManager,
                                CategoryFragment(), R.id.container)

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


    override fun onDateSelected(date: String) {
        if (selectedDob)
            binding.etDob.setText(date)
        else
            binding.etYears.setText(date)
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
                Glide.with(requireContext()).load(fileToUpload).into(binding.ivPic)

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
