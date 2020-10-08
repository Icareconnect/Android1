package com.consultantvendor.ui.dashboard.home.appointmentStatus

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.databinding.DialogStatusBinding
import com.consultantvendor.ui.chat.UploadFileViewModel
import com.consultantvendor.utils.PrefsManager
import com.consultantvendor.utils.dialogs.ProgressDialogImage
import com.consultantvendor.utils.longToast
import com.consultantvendor.utils.showSnackBar
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject


class DialogStatusFragment(private val fragment: StatusUpdateFragment) : DaggerDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: DialogStatusBinding


    private lateinit var viewModelUpload: UploadFileViewModel

    private lateinit var progressDialogImage: ProgressDialogImage


    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_status, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)

        initialise()
        listeners()
    }

    private fun initialise() {
        viewModelUpload = ViewModelProvider(this, viewModelFactory)[UploadFileViewModel::class.java]
        progressDialogImage = ProgressDialogImage(requireActivity())
    }

    private fun listeners() {
        binding.tvUpdate.setOnClickListener {
            if (!binding.tvCompleted.isChecked) {
                binding.tvCompleted.showSnackBar(getString(R.string.update_status))
            } else {
                fragment.hitApiStartRequest()
                dialog?.dismiss()
            }
        }

        binding.tvClear.setOnClickListener {
            dialog?.dismiss()
        }
    }
}
