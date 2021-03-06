package org.kinecosystem.kinit.view.transfer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import org.kinecosystem.kinit.KinitApplication
import org.kinecosystem.kinit.R
import org.kinecosystem.kinit.model.spend.EcosystemApp
import org.kinecosystem.kinit.navigation.Navigator
import org.kinecosystem.kinit.view.BaseActivity
import org.kinecosystem.kinit.view.customView.AlertManager
import org.kinecosystem.kinit.viewmodel.spend.TransferActivityModel

private const val APP_PARAM = "TransferActivity_APP_PARAM"
private const val FROM_APP_DETAIL_PARAM = "FROM_APP_DETAIL_PARAM"

class TransferActivity : BaseActivity(), TransferActions {
    lateinit var navigator: Navigator
    private lateinit var model: TransferActivityModel
    lateinit var app: EcosystemApp
    private var returnToAppDetail: Boolean = false
    private var currentFragmentTag: String = ""


    private val SAVE_FRAGMENT_TAG_KEY = "CURRENT_FRAGMENT_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        KinitApplication.coreComponent.inject(this)
        super.onCreate(savedInstanceState)
        navigator = Navigator(this)
        setContentView(R.layout.single_fragment_layout)
        app = intent.getParcelableExtra(APP_PARAM)
        returnToAppDetail = intent.getBooleanExtra(FROM_APP_DETAIL_PARAM, false)
        model = TransferActivityModel(app, this)
        savedInstanceState?.let {
            if (it.containsKey(SAVE_FRAGMENT_TAG_KEY)) {
                val tag = it.getString(SAVE_FRAGMENT_TAG_KEY)
                replaceFragment(supportFragmentManager.findFragmentByTag(tag), tag)
            }
        } ?: run {
            val fragment = AppsConnectionFragment.newInstance(app)
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment, AppsConnectionFragment.TAG).commitNowAllowingStateLoss()
            currentFragmentTag = AppsConnectionFragment.TAG
            model.startConnectWithDelay()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(SAVE_FRAGMENT_TAG_KEY, currentFragmentTag)
    }


    override fun onPause() {
        super.onPause()
        model.onPause()
    }

    override fun onResume() {
        super.onResume()
        model.onResume()
    }

    override fun onDestroy() {
        model.onDestroy()
        super.onDestroy()
    }


    override fun onStartConnect() {
        model.startTransferRequestActivity(this)
    }

    override fun onAnimationConnectionEnd() {
        replaceFragment(SendAmountFragment.newInstance(app), SendAmountFragment.TAG)
    }

    override fun onConnectionError() {
        showConnectionErrorAlert()
    }

    override fun onStartConnectingError() {
        showStartConnectingErrorAlert()
    }

    override fun onClose() {
        if (returnToAppDetail) {
            navigator.navigateTo(app)
        } else {
            navigator.navigateTo(Navigator.Destination.MAIN_SCREEN)
        }
        finish()
    }

    override fun onStartTransferring(amount: Int) {
        replaceFragment(TransferringFragment.newInstance(app, amount), TransferringFragment.TAG)
    }

    override fun onTransferFailed() {
        replaceFragment(TransferFailedFragment.newInstance(), TransferFailedFragment.TAG)
    }

    override fun onTransferTimeout() {
        replaceFragment(TransferTimeoutFragment.newInstance(), TransferTimeoutFragment.TAG)
    }

    override fun onConnected() {
        val fragment = supportFragmentManager.findFragmentByTag(AppsConnectionFragment.TAG)
        if (fragment is AppsConnectionFragment && fragment.isVisible) {
            fragment.animateConnected()
        } else {
            onAnimationConnectionEnd()
        }
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out)
                .replace(R.id.fragment_container, fragment, tag).commit()
        currentFragmentTag = tag

    }

    override fun onTransferComplete() {
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent != null) {
            model.parseResult(this, requestCode, resultCode, intent)
        }
    }

    private fun showConnectionErrorAlert() {
        AlertManager.showAlert(this, R.string.transfer_connection_error_title, R.string.transfer_connection_error_message, R.string.dialog_ok, { onClose() })
    }

    private fun showStartConnectingErrorAlert() {
        val message = resources.getString(R.string.transfer_start_connecting_error_message, app.name)
        AlertManager.showAlert(this, R.string.transfer_start_connecting_error_title, message ,R.string.dialog_ok, { onNeedUpdate() })
    }

    private fun onNeedUpdate(){
        onClose()
        navigator.navigateToUrl(app.data.appUrl)
    }

    companion object {
        fun getIntent(context: Context, app: EcosystemApp, fromAppDetail: Boolean): Intent {
            val intent = Intent(context, TransferActivity::class.java)
            intent.putExtra(APP_PARAM, app)
            intent.putExtra(FROM_APP_DETAIL_PARAM, fromAppDetail)
            return intent
        }
    }
}

interface TransferActions {
    fun onClose()
    fun onStartConnect()
    fun onConnected()
    fun onAnimationConnectionEnd()
    fun onConnectionError()
    fun onStartConnectingError()
    fun onStartTransferring(amount: Int)
    fun onTransferFailed()
    fun onTransferTimeout()
    fun onTransferComplete()
}
