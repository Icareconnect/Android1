package com.consultantvendor.di

import com.consultantvendor.ui.SplashActivity
import com.consultantvendor.ui.calling.CallingActivity
import com.consultantvendor.ui.chat.ChatFragment
import com.consultantvendor.ui.chat.chatdetail.ChatDetailActivity
import com.consultantvendor.ui.dashboard.HomeActivity
import com.consultantvendor.ui.dashboard.feeds.AddFeedFragment
import com.consultantvendor.ui.dashboard.feeds.FeedDetailsFragment
import com.consultantvendor.ui.dashboard.feeds.FeedsFragment
import com.consultantvendor.ui.dashboard.home.AppointmentDetailsFragment
import com.consultantvendor.ui.dashboard.home.AppointmentFragment
import com.consultantvendor.ui.dashboard.home.HomeFragment
import com.consultantvendor.ui.dashboard.home.appointmentStatus.AppointmentStatusActivity
import com.consultantvendor.ui.dashboard.home.appointmentStatus.DialogStatusFragment
import com.consultantvendor.ui.dashboard.home.appointmentStatus.StatusUpdateFragment
import com.consultantvendor.ui.dashboard.home.verification.UserVerificationFragment
import com.consultantvendor.ui.dashboard.revenue.RevenueFragment
import com.consultantvendor.ui.dashboard.settings.SettingsFragment
import com.consultantvendor.ui.dashboard.wallet.PayoutFragment
import com.consultantvendor.ui.dashboard.wallet.WalletFragment
import com.consultantvendor.ui.dashboard.wallet.addmoney.AddCardFragment
import com.consultantvendor.ui.dashboard.wallet.addmoney.AddMoneyActivity
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.ui.drawermenu.classes.ClassesFragment
import com.consultantvendor.ui.drawermenu.classes.addclass.AddClassFragment
import com.consultantvendor.ui.drawermenu.notification.NotificationFragment
import com.consultantvendor.ui.drawermenu.profile.ProfileFragment
import com.consultantvendor.ui.jitsimeet.JitsiActivity
import com.consultantvendor.ui.loginSignUp.SignUpActivity
import com.consultantvendor.ui.loginSignUp.availability.SetAvailabilityFragment
import com.consultantvendor.ui.loginSignUp.category.CategoryFragment
import com.consultantvendor.ui.loginSignUp.changepassword.ChangePasswordFragment
import com.consultantvendor.ui.loginSignUp.document.DocumentsFragment
import com.consultantvendor.ui.loginSignUp.document.add.DialogAddDocumentFragment
import com.consultantvendor.ui.loginSignUp.forgotpassword.ForgotPasswordFragment
import com.consultantvendor.ui.loginSignUp.insurance.InsuranceFragment
import com.consultantvendor.ui.loginSignUp.login.LoginFragment
import com.consultantvendor.ui.loginSignUp.loginemail.LoginEmailFragment
import com.consultantvendor.ui.loginSignUp.masterprefrence.MasterPrefrenceFragment
import com.consultantvendor.ui.loginSignUp.prefrence.PrefrenceFragment
import com.consultantvendor.ui.loginSignUp.register.RegisterFragment
import com.consultantvendor.ui.loginSignUp.service.ServiceFragment
import com.consultantvendor.ui.loginSignUp.signup.SignUpFragment
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment
import com.consultantvendor.ui.loginSignUp.verifyotp.VerifyOTPFragment
import com.consultantvendor.ui.loginSignUp.welcome.BannerFragment
import com.consultantvendor.ui.loginSignUp.welcome.WelcomeFragment
import com.consultantvendor.ui.walkthrough.WalkThroughFragment
import com.consultantvendor.ui.walkthrough.WalkthroughDetailFragment
import com.consultantvendor.ui.webview.WebViewActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BindingsModule {

    @ContributesAndroidInjector
    abstract fun splashActivity(): SplashActivity


    @ContributesAndroidInjector
    abstract fun homeActivity(): HomeActivity

    @ContributesAndroidInjector
    abstract fun signUpActivity(): SignUpActivity

    @ContributesAndroidInjector
    abstract fun welcomeFragment(): WelcomeFragment

    @ContributesAndroidInjector
    abstract fun loginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun verifyOTPFragment(): VerifyOTPFragment

    @ContributesAndroidInjector
    abstract fun drawerActivity(): DrawerActivity

    @ContributesAndroidInjector
    abstract fun signUpFragment(): SignUpFragment

    @ContributesAndroidInjector
    abstract fun loginEmailFragment(): LoginEmailFragment

    @ContributesAndroidInjector
    abstract fun homeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun appointmentFragment(): AppointmentFragment

    @ContributesAndroidInjector
    abstract fun profileFragment(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun walletFragment(): WalletFragment

    @ContributesAndroidInjector
    abstract fun notificationFragment(): NotificationFragment

    @ContributesAndroidInjector
    abstract fun forgotPasswordFragment(): ForgotPasswordFragment

    @ContributesAndroidInjector
    abstract fun changePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector
    abstract fun chatFragment(): ChatFragment

    @ContributesAndroidInjector
    abstract fun chatDetailActivity(): ChatDetailActivity

    @ContributesAndroidInjector
    abstract fun payoutFragment(): PayoutFragment

    @ContributesAndroidInjector
    abstract fun revenueFragment(): RevenueFragment

    @ContributesAndroidInjector
    abstract fun classesFragment(): ClassesFragment

    @ContributesAndroidInjector
    abstract fun addClassFragment(): AddClassFragment

    @ContributesAndroidInjector
    abstract fun jitsiActivity(): JitsiActivity

    @ContributesAndroidInjector
    abstract fun categoryFragment(): CategoryFragment

    @ContributesAndroidInjector
    abstract fun subCategoryFragment(): SubCategoryFragment

    @ContributesAndroidInjector
    abstract fun serviceFragment(): ServiceFragment

    @ContributesAndroidInjector
    abstract fun prefrenceFragment(): PrefrenceFragment

    @ContributesAndroidInjector
    abstract fun setAvailabilityFragment(): SetAvailabilityFragment

    @ContributesAndroidInjector
    abstract fun settingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun bannerFragment(): BannerFragment

    @ContributesAndroidInjector
    abstract fun callingActivity(): CallingActivity

    @ContributesAndroidInjector
    abstract fun webViewActivity(): WebViewActivity

    @ContributesAndroidInjector
    abstract fun addMoneyActivity(): AddMoneyActivity

    @ContributesAndroidInjector
    abstract fun addCardFragment(): AddCardFragment

    @ContributesAndroidInjector
    abstract fun insuranceFragment(): InsuranceFragment

    @ContributesAndroidInjector
    abstract fun documentsFragment(): DocumentsFragment

    @ContributesAndroidInjector
    abstract fun dialogAddDocumentFragment(): DialogAddDocumentFragment

    @ContributesAndroidInjector
    abstract fun feedsFragment(): FeedsFragment

    @ContributesAndroidInjector
    abstract fun feedDetailsFragment(): FeedDetailsFragment

    @ContributesAndroidInjector
    abstract fun addFeedFragment(): AddFeedFragment

    @ContributesAndroidInjector
    abstract fun userVerificationFragment(): UserVerificationFragment

    @ContributesAndroidInjector
    abstract fun registerFragment(): RegisterFragment

    @ContributesAndroidInjector
    abstract fun walkThroughFragment(): WalkThroughFragment

    @ContributesAndroidInjector
    abstract fun walkthroughDetailFragment(): WalkthroughDetailFragment

    @ContributesAndroidInjector
    abstract fun appointmentDetailsFragment(): AppointmentDetailsFragment

    @ContributesAndroidInjector
    abstract fun appointmentStatusActivity(): AppointmentStatusActivity

    @ContributesAndroidInjector
    abstract fun statusUpdateFragment(): StatusUpdateFragment

    @ContributesAndroidInjector
    abstract fun dialogStatusFragment(): DialogStatusFragment

    @ContributesAndroidInjector
    abstract fun masterPrefrenceFragment(): MasterPrefrenceFragment

}