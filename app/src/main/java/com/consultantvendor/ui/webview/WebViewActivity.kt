package com.consultantvendor.ui.webview

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantvendor.R
import com.consultantvendor.data.network.Config
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityWebViewBinding
import com.consultantvendor.utils.EXTRA_REQUEST_ID
import com.consultantvendor.utils.PrefsManager
import com.consultantvendor.utils.gone
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class WebViewActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityWebViewBinding

    private var isReceiverRegistered = false

    private var transactionId = ""

    private var loadUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view)

        initialise()
        bindViews()
        setListeners()
    }

    private fun initialise() {
        binding.toolbar.title = intent.getStringExtra(LINK_TITLE)
        if (intent.hasExtra(PAYMENT_URL)) {
            transactionId = intent.getStringExtra(EXTRA_REQUEST_ID) ?: ""
            loadUrl = intent.getStringExtra(PAYMENT_URL) ?: ""
        } else {
            loadUrl = "${Config.baseURL}${intent.getStringExtra(LINK_URL)}"
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun bindViews() {

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }

        binding.webView.setBackgroundColor(Color.TRANSPARENT)
        binding.webView.settings.setSupportZoom(true)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if (url?.contains("PAYMENT_SUCCESS") == true) {
                }
            }
        }


        binding.webView.settings.setAppCacheEnabled(true)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.useWideViewPort = true
        binding.webView.webChromeClient = WebChromeClient()


        // Return the app name after finish loading

        binding.webView.loadUrl(loadUrl)

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {

                // Return the app name after finish loading
                if (progress == 100)
                    binding.clLoader.gone()
            }
        }
    }

    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

    }

    companion object {
        const val LINK_TITLE = "LINK_TITLE"
        const val LINK_URL = "LINK_URL"
        const val PAYMENT_URL = "PAYMENT_URL"
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            /*intentFilter.addAction(PushType.BALANCE_ADDED)
            intentFilter.addAction(PushType.BALANCE_FAILED)*/
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    callCancelledReceiver, intentFilter
            )
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(callCancelledReceiver)
            isReceiverRegistered = false
        }
    }

    private val callCancelledReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            /* if (intent.getStringExtra(EXTRA_REQUEST_ID) == transactionId) {
                 if (intent.action == PushType.BALANCE_ADDED) {
                     longToast(getString(R.string.transaction_success))
                     setResult(Activity.RESULT_OK)
                     finish()
                 } else if (intent.action == PushType.BALANCE_FAILED) {
                     longToast(getString(R.string.transaction_failed))
                     finish()
                 }
             }*/
        }
    }
}
