/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.play.core.install.model.AppUpdateType
import dev.chrisbanes.insetter.applyInsetter
import nl.rijksoverheid.en.applifecycle.AppLifecycleManager
import nl.rijksoverheid.en.applifecycle.AppLifecycleViewModel
import nl.rijksoverheid.en.applifecycle.AppUpdateRequiredFragmentDirections
import nl.rijksoverheid.en.applifecycle.EndOfLifeFragmentDirections
import nl.rijksoverheid.en.databinding.ActivityMainBinding
import nl.rijksoverheid.en.databinding.ActivityMainBinding.inflate
import nl.rijksoverheid.en.debug.DebugNotification
import nl.rijksoverheid.en.job.RemindExposureNotificationWorker
import nl.rijksoverheid.en.lifecyle.EventObserver
import nl.rijksoverheid.en.notifier.NotificationsRepository

private const val RC_REQUEST_CONSENT = 1
private const val RC_UPDATE_APP = 2
private const val TAG_GENERIC_ERROR = "generic_error"

class MainActivity : AppCompatActivity() {
    private val viewModel: ExposureNotificationsViewModel by viewModels()
    private val appLifecycleViewModel: AppLifecycleViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.root.applyInsetter {
            type(navigationBars = true) {
                padding()
            }
        }

        viewModel.notificationsResult.observe(
            this,
            EventObserver {
                when (it) {
                    is ExposureNotificationsViewModel.NotificationsStatusResult.ConsentRequired -> {
                        startIntentSenderForResult(
                            it.intent.intentSender,
                            RC_REQUEST_CONSENT,
                            null,
                            0,
                            0,
                            0
                        )
                    }
                    is ExposureNotificationsViewModel.NotificationsStatusResult.Unavailable,
                    is ExposureNotificationsViewModel.NotificationsStatusResult.UnknownError -> {
                        if (supportFragmentManager.findFragmentByTag(TAG_GENERIC_ERROR) == null) {
                            ExposureNotificationApiNotAvailableDialogFragment().show(
                                supportFragmentManager,
                                TAG_GENERIC_ERROR
                            )
                        }
                    }
                }
            }
        )

        appLifecycleViewModel.updateEvent.observe(
            this,
            EventObserver {
                when (it) {
                    is AppLifecycleViewModel.AppLifecycleStatus.Update -> {
                        if (it.update is AppLifecycleManager.UpdateState.InAppUpdate) {
                            it.update.appUpdateManager.startUpdateFlowForResult(
                                it.update.appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                RC_UPDATE_APP
                            )
                        } else {
                            val installerPackageName =
                                (it.update as AppLifecycleManager.UpdateState.UpdateRequired).installerPackageName
                            findNavController(R.id.nav_host_fragment).navigate(
                                AppUpdateRequiredFragmentDirections.actionAppUpdateRequired(
                                    installerPackageName
                                )
                            )
                        }
                    }
                    AppLifecycleViewModel.AppLifecycleStatus.EndOfLife -> {
                        viewModel.disableExposureNotifications()
                        findNavController(R.id.nav_host_fragment).navigate(
                            EndOfLifeFragmentDirections.actionEndOfLife()
                        )
                    }
                }
            }
        )

        if (BuildConfig.FEATURE_DEBUG_NOTIFICATION) {
            DebugNotification(this).show()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (BuildConfig.FEATURE_SECURE_SCREEN) {
            findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener(
                SecureScreenNavigationListener(
                    window,
                    R.id.nav_status,
                    R.id.nav_post_notification,
                    R.id.nav_remove_exposed_message_dialog
                )
            )
        }
    }

    override fun onStart() {
        super.onStart()
        NotificationsRepository(this).clearAppInactiveNotification()
        RemindExposureNotificationWorker.cancel(this)
    }

    override fun onResume() {
        super.onResume()
        appLifecycleViewModel.checkForForcedAppUpdate()
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return ViewModelFactory(applicationContext)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_REQUEST_CONSENT && resultCode == Activity.RESULT_OK) {
            viewModel.requestEnableNotifications()
        }
        // If user canceled the forced update, do not allow them to use the app
        if (requestCode == RC_UPDATE_APP && resultCode != Activity.RESULT_OK) {
            finish()
        }
    }
}
