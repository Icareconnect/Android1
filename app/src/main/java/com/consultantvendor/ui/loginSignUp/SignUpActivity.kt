package com.consultantvendor.ui.loginSignUp

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.consultantvendor.R
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityContainerBinding
import com.consultantvendor.ui.loginSignUp.category.CategoryFragment
import com.consultantvendor.ui.loginSignUp.document.DocumentsFragment
import com.consultantvendor.ui.loginSignUp.document.DocumentsFragment.Companion.UPDATE_DOCUMENTS
import com.consultantvendor.ui.loginSignUp.login.LoginFragment
import com.consultantvendor.ui.loginSignUp.prefrence.PrefrenceFragment
import com.consultantvendor.ui.loginSignUp.register.RegisterFragment
import com.consultantvendor.ui.loginSignUp.service.ServiceFragment
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment.Companion.CATEGORY_PARENT_ID
import com.consultantvendor.ui.loginSignUp.welcome.WelcomeFragment
import com.consultantvendor.utils.*
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class SignUpActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    lateinit var binding: ActivityContainerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialise()
        //makeFullScreen(this)
    }

    private fun initialise() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_container)

        LocaleHelper.setLocale(this, userRepository.getUserLanguage(), prefsManager)

        val fragment: Fragment
        val bundle = Bundle()
        when {
            intent.hasExtra(UPDATE_PROFILE) -> {
                fragment = RegisterFragment()
                bundle.putBoolean(UPDATE_PROFILE, true)
            }
            intent.hasExtra(UPDATE_NUMBER) -> {
                fragment = LoginFragment()
                bundle.putBoolean(UPDATE_NUMBER, true)
            }
            intent.hasExtra(UPDATE_DOCUMENTS) -> {
                fragment = DocumentsFragment()
                bundle.putBoolean(UPDATE_DOCUMENTS, true)
                bundle.putSerializable(CATEGORY_PARENT_ID, intent.getSerializableExtra(CATEGORY_PARENT_ID))
            }
            intent.hasExtra(UPDATE_AVAILABILITY) -> {
                fragment = ServiceFragment()
                bundle.putSerializable(CATEGORY_PARENT_ID, intent.getSerializableExtra(CATEGORY_PARENT_ID))
            }
            intent.hasExtra(UPDATE_CATEGORY) -> {
                fragment = CategoryFragment()
            }
            intent.hasExtra(UPDATE_PREFRENCES) -> {
                fragment = PrefrenceFragment()
                bundle.putSerializable(CATEGORY_PARENT_ID, intent.getSerializableExtra(CATEGORY_PARENT_ID))
            }
            intent.hasExtra(WelcomeFragment.EXTRA_LOGIN) -> {
                fragment = LoginFragment()
            }
            else -> {
                fragment = WelcomeFragment()
            }
        }

        fragment.arguments = bundle
        addFragment(supportFragmentManager, fragment, R.id.container)

    }


}
