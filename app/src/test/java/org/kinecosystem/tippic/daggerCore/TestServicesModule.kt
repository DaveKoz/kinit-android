package org.kinecosystem.tippic.daggerCore

import android.content.Context
import org.kinecosystem.tippic.analytics.Analytics
import org.kinecosystem.tippic.blockchain.Wallet
import org.kinecosystem.tippic.dagger.ServicesModule
import org.kinecosystem.tippic.repository.*
import org.kinecosystem.tippic.server.*
import org.kinecosystem.tippic.util.Scheduler
import org.mockito.Mockito.mock

class TestServicesModule : ServicesModule() {

    override fun servicesProvider(context: Context, dataStoreProvider: DataStoreProvider, userRepository: UserRepository, offersRepository: OffersRepository, categoryRepository: CategoriesRepository, ecoApplicationsRepository: EcoApplicationsRepository, analytics: Analytics, scheduler: Scheduler): NetworkServices {
        return mock(NetworkServices::class.java)
    }

    override fun wallet(): Wallet {
        return mock(Wallet::class.java)
    }

    override fun onboardingService(): OnboardingService {
        return mock(OnboardingService::class.java)
    }

    override fun categoriesService(): CategoriesService {
        return mock(CategoriesService::class.java)
    }

    override fun taskService(): TaskService {
        return mock(TaskService::class.java)
    }

    override fun offerService(): OfferService {
        return mock(OfferService::class.java)
    }
}