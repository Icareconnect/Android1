package com.consultantvendor.ui.loginSignUp.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.consultantvendor.R
import com.consultantvendor.databinding.FragmentBannerBinding
import dagger.android.support.DaggerFragment


class BannerFragment : DaggerFragment() {

    private lateinit var binding: FragmentBannerBinding

    private var rootView: View? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_banner, container, false)
            rootView = binding.root


        }
        return rootView
    }
}
