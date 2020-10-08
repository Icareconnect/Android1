package com.consultantvendor.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.ui.AppVersionViewModel
import com.consultantvendor.ui.chat.ChatViewModel
import com.consultantvendor.ui.chat.UploadFileViewModel
import com.consultantvendor.ui.dashboard.feeds.FeedViewModel
import com.consultantvendor.ui.dashboard.home.AppointmentViewModel
import com.consultantvendor.ui.dashboard.home.appointmentStatus.DirectionViewModel
import com.consultantvendor.ui.dashboard.revenue.RevenueViewModel
import com.consultantvendor.ui.dashboard.wallet.BankViewModel
import com.consultantvendor.ui.dashboard.wallet.WalletViewModel
import com.consultantvendor.ui.drawermenu.classes.ClassesViewModel
import com.consultantvendor.ui.drawermenu.profile.DoctorViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.availability.GetSlotsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
abstract class ViewModelsModule {

    @Module
    companion object {

        @Provides
        @Singleton
        @JvmStatic
        fun viewModelProviderFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory =
            factory
    }

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun loginViewModel(viewModel: LoginViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(AppointmentViewModel::class)
    abstract fun appointmentViewModel(viewModel: AppointmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WalletViewModel::class)
    abstract fun walletViewModel(viewModel: WalletViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    abstract fun chatViewModel(viewModel: ChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UploadFileViewModel::class)
    abstract fun uploadFileViewModel(viewModel: UploadFileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BankViewModel::class)
    abstract fun bankViewModel(viewModel: BankViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RevenueViewModel::class)
    abstract fun revenueViewModel(viewModel: RevenueViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeedViewModel::class)
    abstract fun feedViewModel(viewModel: FeedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ClassesViewModel::class)
    abstract fun classesViewModel(viewModel: ClassesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DoctorViewModel::class)
    abstract fun doctorViewModel(viewModel: DoctorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetSlotsViewModel::class)
    abstract fun getSlotsViewModel(viewModel: GetSlotsViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(AppVersionViewModel::class)
    abstract fun appVersionViewModel(viewModel: AppVersionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DirectionViewModel::class)
    abstract fun directionViewModel(viewModel: DirectionViewModel): ViewModel

}