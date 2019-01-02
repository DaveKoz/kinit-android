package org.kinecosystem.tippic.navigation

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import org.kinecosystem.tippic.TippicApplication
import org.kinecosystem.tippic.R
import org.kinecosystem.tippic.model.earn.isTaskWebView
import org.kinecosystem.tippic.model.spend.EcoApplication
import org.kinecosystem.tippic.model.spend.Offer
import org.kinecosystem.tippic.repository.CategoriesRepository
import org.kinecosystem.tippic.util.GeneralUtils
import org.kinecosystem.tippic.view.MainActivity
import org.kinecosystem.tippic.view.RegisterErrorActivity
import org.kinecosystem.tippic.view.backup.BackupWalletActivity
import org.kinecosystem.tippic.view.comingSoon.EcoAppsComingSoonActivity
import org.kinecosystem.tippic.view.createWallet.CreateWalletActivity
import org.kinecosystem.tippic.view.earn.CategoryTaskActivity
import org.kinecosystem.tippic.view.earn.QuestionnaireActivity
import org.kinecosystem.tippic.view.earn.WebTaskActivity
import org.kinecosystem.tippic.view.earn.WebTaskCompleteActivity
import org.kinecosystem.tippic.view.faq.FAQActivity
import org.kinecosystem.tippic.view.phoneVerify.PhoneVerifyActivity
import org.kinecosystem.tippic.view.restore.RestoreWalletActivity
import org.kinecosystem.tippic.view.spend.AppDetailsActivity
import org.kinecosystem.tippic.view.spend.Peer2PeerActivity
import org.kinecosystem.tippic.view.spend.PurchaseOfferActivity
import org.kinecosystem.tippic.view.tutorial.TutorialActivity
import javax.inject.Inject

class Navigator(private val context: Context) {

    enum class Destination {
        MAIN_SCREEN, PEER2PEER, COMPLETE_WEB_TASK, WALLET_BACKUP, WALLET_CREATE, TUTORIAL, PHONE_VERIFY, WALLET_RESTORE, FAQ, ERROR_REGISTER, ECO_APPS_COMING_SOON
    }

    @Inject
    lateinit var categoriesRepository: CategoriesRepository

    init {
        TippicApplication.coreComponent.inject(this)
    }

    fun navigateTo(dest: Destination) {
        navigateTo(dest, false, false)
    }

    fun navigateToTask(categoryId: String) {
        categoriesRepository.getTask(categoryId)?.let {
            if (it.isTaskWebView()) {
                context.startActivity(WebTaskActivity.getIntent(context))
            } else {
                context.startActivity(QuestionnaireActivity.getIntent(context))
            }
        }
        if (context is AppCompatActivity) {
            context.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
        }
    }

    fun navigateTo(offer: Offer) {
        context.startActivity(PurchaseOfferActivity.getIntent(context, offer))
        if (context is AppCompatActivity) {
            context.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
        }
    }

    fun navigateTo(app: EcoApplication) {
        context.startActivity(AppDetailsActivity.getIntent(context, app))
        if (context is AppCompatActivity) {
            context.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
        }
    }

    fun navigateTo(dest: Destination, withSlideAnim: Boolean, reverseAnim: Boolean) {
        when (dest) {
            Destination.PEER2PEER -> navigateToActivity(Peer2PeerActivity.getIntent(context), withSlideAnim, reverseAnim)
            Destination.ERROR_REGISTER -> navigateToActivity(RegisterErrorActivity.getIntent(context), withSlideAnim, reverseAnim)
            Destination.COMPLETE_WEB_TASK -> navigateToActivity(WebTaskCompleteActivity.getIntent(context), withSlideAnim, reverseAnim)
            Destination.WALLET_BACKUP -> navigateToActivity(BackupWalletActivity.getIntent(context), withSlideAnim, reverseAnim)
            Destination.TUTORIAL -> navigateToActivity(TutorialActivity.getIntent(context), withSlideAnim, reverseAnim)
            Destination.PHONE_VERIFY -> navigateToActivity(PhoneVerifyActivity.getIntent(context), withSlideAnim, reverseAnim)
            Destination.WALLET_CREATE -> navigateToActivity(CreateWalletActivity.getIntent(context), withSlideAnim, reverseAnim)
            Destination.MAIN_SCREEN -> navigateToActivity(MainActivity.getIntent(context), withSlideAnim, reverseAnim)
            Destination.WALLET_RESTORE -> navigateToActivity(RestoreWalletActivity.getIntent(context), withSlideAnim, reverseAnim)
            Destination.FAQ -> navigateToActivity(FAQActivity.getIntent(context), withSlideAnim, reverseAnim)
            Destination.ECO_APPS_COMING_SOON -> navigateToActivity(EcoAppsComingSoonActivity.getIntent(context), withSlideAnim, reverseAnim)
        }
    }

    private fun navigateToActivity(intent: Intent, withSlideAnimation: Boolean = true, reverse: Boolean = false) {
        context.startActivity(intent)
        if (withSlideAnimation && context is AppCompatActivity) {
            if (reverse) {
                context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
            } else {
                context.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
            }
        }
    }

    fun navigateToCategory(categoryId: String, reverseAnimation: Boolean = false) {
        context.startActivity(CategoryTaskActivity.getIntent(context, categoryId))
        if (context is AppCompatActivity) {
            if (reverseAnimation) {
                context.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
            } else {
                context.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out)
            }
        }
    }

    fun navigateToUrl(url: String) {
        GeneralUtils.navigateToUrl(context, url)
    }
}