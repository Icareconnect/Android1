package com.consultantvendor.ui.loginSignUp.document

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
import com.consultantvendor.data.models.requests.UpdateDocument
import com.consultantvendor.data.models.responses.AdditionalField
import com.consultantvendor.data.models.responses.AdditionalFieldDocument
import com.consultantvendor.data.models.responses.Categories
import com.consultantvendor.data.models.responses.UserData
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentServiceBinding
import com.consultantvendor.ui.dashboard.HomeActivity
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.document.add.DialogAddDocumentFragment
import com.consultantvendor.ui.loginSignUp.prefrence.PrefrenceFragment
import com.consultantvendor.ui.loginSignUp.service.ServiceFragment
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class DocumentsFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentServiceBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: LoginViewModel

    private var items = ArrayList<AdditionalField>()

    private lateinit var adapter: DocumentsAdapter

    private var categoryData: Categories? = null

    private var userData: UserData? = null

    private var selectedPos = -1

    private var editSelectedPos: Int? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
            setAdapter()
        }
        return rootView
    }

    private fun initialise() {
        binding.swipeRefresh.isEnabled = false

        binding.tvTitle.text = getString(R.string.upload_documents)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        categoryData = arguments?.getSerializable(SubCategoryFragment.CATEGORY_PARENT_ID) as Categories
        userData = userRepository.getUser()
    }

    private fun setAdapter() {
        adapter = DocumentsAdapter(this, items)
        binding.rvListing.adapter = adapter

        /*If document already there*/
        if (arguments?.containsKey(UPDATE_DOCUMENTS) == true) {
            items.addAll(userData?.additionals ?: emptyList())
            adapter.notifyDataSetChanged()

            adapter.setAddOption(false)
            binding.tvNext.text = getString(R.string.update)
            //binding.tvNext.gone()
        } else if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["category_id"] = categoryData?.id ?: ""

            viewModel.additionalDetails(hashMap)
        }
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.tvNext.setOnClickListener {
            binding.tvNext.hideKeyboard()

            if (items.isNotEmpty()) {
                items.forEach {
                    if (it.documents.isEmpty()) {
                        binding.tvNext.showSnackBar("${getString(R.string.please_add)} ${it.name}")
                        return@setOnClickListener
                    }
                }

                if (isConnectedToInternet(requireContext(), true)) {
                    val updateDocument = UpdateDocument()
                    if (arguments?.containsKey(UPDATE_DOCUMENTS) == true)
                        updateDocument.sp_id = userData?.id

                    updateDocument.fields = ArrayList()
                    updateDocument.fields?.addAll(items)

                    viewModel.additionalDetailsUpdate(updateDocument)
                }
            }else{
                binding.tvNext.showSnackBar(getString(R.string.upload_documents))
            }
        }
    }


    private fun bindObservers() {
        viewModel.additionalDetails.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    items.clear()
                    items.addAll(it.data?.additional_details ?: emptyList())

                    adapter.notifyDataSetChanged()

                    adapter.setAllItemsLoaded(true)
                    binding.tvNoData.hideShowView(items.isEmpty())
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

        viewModel.additionalDetailsUpdate.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    userData?.additionals = ArrayList()
                    userData?.additionals?.addAll(it.data?.additionals ?: emptyList())

                    prefsManager.save(USER_DATA, userData)

                    when {
                        arguments?.containsKey(UPDATE_DOCUMENTS) == true -> {
                            requireActivity().setResult(Activity.RESULT_OK)
                            requireActivity().finish()
                        }
                        userRepository.isUserLoggedIn() -> {
                            startActivity(Intent(requireContext(), HomeActivity::class.java))
                            requireActivity().finish()
                        }
                        else -> {
                            val fragment = when {
                                categoryData?.is_filters == true -> PrefrenceFragment()
                                else -> ServiceFragment()
                            }

                            val bundle = Bundle()
                            bundle.putSerializable(SubCategoryFragment.CATEGORY_PARENT_ID, categoryData)
                            fragment.arguments = bundle

                            replaceFragment(requireActivity().supportFragmentManager, fragment, R.id.container)
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

    fun addDocument(pos: Int, editPos: Int?) {
        selectedPos = pos
        if (editPos != null) {
            editSelectedPos = editPos

            val documentMain: AdditionalFieldDocument? = if (editSelectedPos != -1)
                items[selectedPos].documents[editSelectedPos ?: 0]
            else null

            val fragment = DialogAddDocumentFragment(this, documentMain)
            fragment.show(requireActivity().supportFragmentManager, fragment.tag)
        } else if (items[pos].documents.size < 2) {
            val fragment = DialogAddDocumentFragment(this, null)
            fragment.show(requireActivity().supportFragmentManager, fragment.tag)
        }
    }

    fun addedDocument(document: AdditionalFieldDocument) {
        if (editSelectedPos != null)
            items[selectedPos].documents[editSelectedPos ?: 0] = document
        else
            items[selectedPos].documents.add(document)

        adapter.notifyDataSetChanged()
    }

    companion object {
        const val UPDATE_DOCUMENTS = "UPDATE_DOCUMENTS"
    }
}
