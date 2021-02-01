/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nl.rijksoverheid.en.applifecycle.AppLifecycleViewModel
import nl.rijksoverheid.en.factory.createAppConfigManager
import nl.rijksoverheid.en.factory.createAppLifecycleManager
import nl.rijksoverheid.en.factory.createExposureNotificationsRepository
import nl.rijksoverheid.en.factory.createLabTestRepository
import nl.rijksoverheid.en.factory.createOnboardingRepository
import nl.rijksoverheid.en.factory.createResourceBundleManager
import nl.rijksoverheid.en.factory.createSettingsRepository
import nl.rijksoverheid.en.labtest.LabTestViewModel
import nl.rijksoverheid.en.notification.PostNotificationViewModel
import nl.rijksoverheid.en.notifier.NotificationsRepository
import nl.rijksoverheid.en.onboarding.OnboardingViewModel
import nl.rijksoverheid.en.settings.PauseConfirmationViewModel
import nl.rijksoverheid.en.settings.SettingsViewModel
import nl.rijksoverheid.en.status.StatusViewModel

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val context = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            ExposureNotificationsViewModel::class.java -> ExposureNotificationsViewModel(
                createExposureNotificationsRepository(context),
                createSettingsRepository(context)
            ) as T
            AppLifecycleViewModel::class.java -> AppLifecycleViewModel(
                createAppLifecycleManager(context),
                createAppConfigManager(context)
            ) as T
            OnboardingViewModel::class.java -> OnboardingViewModel(
                createOnboardingRepository(context)
            ) as T
            StatusViewModel::class.java -> StatusViewModel(
                createOnboardingRepository(context),
                createExposureNotificationsRepository(context),
                NotificationsRepository(context),
                createSettingsRepository(context),
                createAppConfigManager(context)
            ) as T
            LabTestViewModel::class.java -> LabTestViewModel(
                createLabTestRepository(context)
            ) as T
            PostNotificationViewModel::class.java -> PostNotificationViewModel(
                createResourceBundleManager(context),
                createAppConfigManager(context)
            ) as T
            SettingsViewModel::class.java -> SettingsViewModel(createSettingsRepository(context)) as T
            PauseConfirmationViewModel::class.java -> PauseConfirmationViewModel(createSettingsRepository(context)) as T
            else -> throw IllegalStateException("Unknown view model class $modelClass")
        }
    }
}
