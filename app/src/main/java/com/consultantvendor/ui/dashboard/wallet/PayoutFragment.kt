package com.consultantvendor.ui.dashboard.wallet

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Bank
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentPayoutBinding
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class PayoutFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var binding: FragmentPayoutBinding

    private var rootView: View? = null

    private lateinit var viewModel: BankViewModel

    private lateinit var viewModelWallet: WalletViewModel

    private lateinit var progressDialog: ProgressDialog

    private var bank: Bank? = null

    private var moneyToTransfer = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payout, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        progressDialog = ProgressDialog(requireActivity())
        binding.clLoader.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))

        viewModel = ViewModelProvider(this, viewModelFactory)[BankViewModel::class.java]
        viewModelWallet = ViewModelProvider(this, viewModelFactory)[WalletViewModel::class.java]

        viewModelWallet.wallet(HashMap())
        viewModel.bankAccounts(HashMap())
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.btnPayout.setOnClickListener {
            if (binding.nsvBank.visibility == View.VISIBLE) {
                binding.btnPayout.showSnackBar(getString(R.string.add_bank))
            } else {
                val hashMap = HashMap<String, String>()

                hashMap["bank_id"] = bank?.id ?: ""
                hashMap["amount"] = moneyToTransfer

                viewModel.payouts(hashMap)
            }
        }

        binding.tvEditBank.setOnClickListener {
            binding.nsvBank.visible()
            binding.btnAddBank.text = getString(R.string.edit_bank)

            if (bank?.customer_type == getString(R.string.temporary_agent))
                binding.rbAgent.isChecked = true

            binding.etAccountNumber.setText(bank?.account_number ?: "")
            binding.etAccountName.setText(bank?.name ?: "")
            binding.etBankName.setText(bank?.bank_name ?: "")
            binding.etInstitutionNumber.setText(bank?.institution_number ?: "")
            binding.etTransitNumber.setText(bank?.ifc_code ?: "")
            binding.etAddress.setText(bank?.address ?: "")
            binding.etCity.setText(bank?.city ?: "")
            binding.etProvince.setText(bank?.province ?: "")
            binding.etPostalCode.setText(bank?.postal_code ?: "")

            val listCountry = resources.getStringArray(R.array.country)
            binding.spnCountry.setSelection(listCountry.indexOf(bank?.country ?: ""))

            val listCurrency = resources.getStringArray(R.array.currency)
            binding.spnCurrency.setSelection(listCurrency.indexOf(bank?.currency ?: ""))

        }

        binding.btnAddBank.setOnClickListener {

            when {
                binding.etAccountNumber.text.toString().trim().isEmpty() -> {
                    binding.etAccountNumber.showSnackBar(getString(R.string.account_number))
                }
                binding.etAccountName.text.toString().trim().isEmpty() -> {
                    binding.etAccountName.showSnackBar(getString(R.string.account_name))
                }
                binding.etBankName.text.toString().trim().isEmpty() -> {
                    binding.etBankName.showSnackBar(getString(R.string.bank_name))
                }
                binding.etInstitutionNumber.text.toString().trim().isEmpty() -> {
                    binding.etInstitutionNumber.showSnackBar(getString(R.string.ifsc_code))
                }
                binding.etAddress.text.toString().trim().isEmpty() -> {
                    binding.etAddress.showSnackBar(getString(R.string.address))
                }
                binding.etCity.text.toString().trim().isEmpty() -> {
                    binding.etCity.showSnackBar(getString(R.string.city))
                }
                binding.etProvince.text.toString().trim().isEmpty() -> {
                    binding.etProvince.showSnackBar(getString(R.string.province))
                }
                binding.etPostalCode.text.toString().trim().isEmpty() -> {
                    binding.etPostalCode.showSnackBar(getString(R.string.postal_code))
                }
                binding.spnCountry.selectedItemPosition == 0 -> {
                    binding.etAccountNumber.showSnackBar(getString(R.string.select_country))
                }
                binding.spnCurrency.selectedItemPosition == 0 -> {
                    binding.etAccountNumber.showSnackBar(getString(R.string.select_currency))
                }
                else -> {

                    val hashMap = HashMap<String, String>()

                    if (bank != null)
                        hashMap["bank_id"] = bank?.id ?: ""

                    hashMap["customer_type"] = if (binding.rbIndividuval.isChecked)
                        binding.rbIndividuval.text.toString()
                    else binding.rbAgent.text.toString()
                    hashMap["account_number"] = binding.etAccountNumber.text.toString()
                    hashMap["account_holder_name"] = binding.etAccountName.text.toString()
                    hashMap["bank_name"] = binding.etBankName.text.toString()
                    hashMap["institution_number"] = binding.etInstitutionNumber.text.toString()
                    hashMap["ifc_code"] = binding.etTransitNumber.text.toString()
                    hashMap["account_holder_type"] = "individual"
                    hashMap["country"] = binding.spnCountry.selectedItem.toString()
                    hashMap["currency"] = binding.spnCurrency.selectedItem.toString()
                    hashMap["address"] = binding.etAddress.text.toString()
                    hashMap["city"] = binding.etCity.text.toString()
                    hashMap["province"] = binding.etProvince.text.toString()
                    hashMap["postal_code"] = binding.etPostalCode.text.toString()

                    viewModel.addBank(hashMap)

                }
            }

        }
    }

    private fun bindObservers() {
        viewModelWallet.wallet.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    moneyToTransfer = it.data?.balance ?: ""
                    binding.tvAmount.text = getCurrency(it.data?.balance)

                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {

                }
            }
        })

        viewModel.bankAccounts.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()

                    if (it.data?.bank_accounts.isNullOrEmpty()) {
                        binding.tvBankAccount.text = getString(R.string.add_bank)
                        binding.tvBankAccountV.invisible()
                        binding.nsvBank.visible()
                    } else {
                        binding.tvBankAccount.visible()
                        binding.tvBankAccountV.visible()
                        binding.tvBankAccount.text = getString(R.string.bank_account)
                        binding.tvEditBank.visible()
                        binding.nsvBank.gone()

                        bank = it.data?.bank_accounts?.get(0)
                        binding.tvBankAccountV.text = "${bank?.name}\n${bank?.bank_name} (${bank?.account_number})"
                    }

                }
                Status.ERROR -> {
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.visible()
                }
            }
        })

        viewModel.addBank.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    viewModel.bankAccounts(HashMap())

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

        viewModel.payout.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()

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
}