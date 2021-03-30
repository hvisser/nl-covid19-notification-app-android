/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en.status

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import nl.rijksoverheid.en.ExposureNotificationsRepository
import nl.rijksoverheid.en.config.AppConfigManager
import nl.rijksoverheid.en.enapi.StatusResult
import nl.rijksoverheid.en.notifier.NotificationsRepository
import nl.rijksoverheid.en.onboarding.OnboardingRepository
import nl.rijksoverheid.en.settings.Settings
import nl.rijksoverheid.en.settings.SettingsRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class StatusViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val notificationsRepository: NotificationsRepository,
    settingsRepository: SettingsRepository,
    private val appConfigManager: AppConfigManager,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

    fun isPlayServicesUpToDate() = onboardingRepository.isGooglePlayServicesUpToDate()

    val headerState = combine(
        exposureNotificationsRepository.getStatus(),
        settingsRepository.exposureNotificationsPausedState(),
        exposureNotificationsRepository.lastKeyProcessed(),
        exposureNotificationsRepository.notificationsEnabledTimestamp()
    ) { status, pausedState, _, _ ->
        status to pausedState
    }.flatMapLatest { (status, pausedState) ->
        exposureNotificationsRepository.getLastExposureDate()
            .map { date -> Triple(status, pausedState, date) }
    }.map { (status, pausedState, exposedDate) ->
        createHeaderState(
            status,
            exposedDate,
            exposureNotificationsRepository.getLastNotificationReceivedDate(),
            exposureNotificationsRepository.keyProcessingOverdue(),
            pausedState
        )
    }.onEach {
        notificationsRepository.cancelExposureNotification()
    }.asLiveData(viewModelScope.coroutineContext)

    val exposureDetected: Boolean
        get() = headerState.value is HeaderState.Exposed

    val errorState = combine(
        exposureNotificationsRepository.notificationsEnabledTimestamp()
            .flatMapLatest { exposureNotificationsRepository.getStatus() },
        settingsRepository.exposureNotificationsPausedState(),
        exposureNotificationsRepository.getLastExposureDate(),
        notificationsRepository.exposureNotificationsEnabled(),
    ) { statusResult, pausedState, lastExposureDate, exposureNotificationsEnabled ->
        createErrorState(
            statusResult,
            lastExposureDate,
            exposureNotificationsEnabled,
            exposureNotificationsRepository.keyProcessingOverdue(),
            pausedState
        )
    }.asLiveData(viewModelScope.coroutineContext)

    val pausedState: LiveData<Settings.PausedState> = settingsRepository.exposureNotificationsPausedState()
        .asLiveData(viewModelScope.coroutineContext)

    val lastKeysProcessed = exposureNotificationsRepository.lastKeyProcessed()
        .map {
            if (it != null && it > 0)
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            else
                null
        }.asLiveData(viewModelScope.coroutineContext)

    suspend fun getAppointmentPhoneNumber() =
        appConfigManager.getCachedConfigOrDefault().appointmentPhoneNumber

    fun hasCompletedOnboarding(): Boolean {
        return onboardingRepository.hasCompletedOnboarding()
    }

    private fun createHeaderState(
        status: StatusResult,
        lastExposureDate: LocalDate?,
        notificationReceivedDate: LocalDate?,
        keyProcessingOverdue: Boolean,
        pausedState: Settings.PausedState
    ): HeaderState {
        return when {
            lastExposureDate != null -> HeaderState.Exposed(lastExposureDate, notificationReceivedDate, clock, pausedState)
            pausedState is Settings.PausedState.Paused -> HeaderState.Paused(pausedState)
            status is StatusResult.BluetoothDisabled -> HeaderState.BluetoothDisabled
            status is StatusResult.LocationPreconditionNotSatisfied -> HeaderState.LocationDisabled
            status !is StatusResult.Enabled -> HeaderState.Disabled
            keyProcessingOverdue -> HeaderState.SyncIssues
            else -> HeaderState.Active
        }
    }

    private fun createErrorState(
        status: StatusResult,
        lastExposureDate: LocalDate?,
        exposureNotificationsEnabled: Boolean,
        keyProcessingOverdue: Boolean,
        pausedState: Settings.PausedState
    ): ErrorState {
        val isPaused = pausedState is Settings.PausedState.Paused
        return when {
            status == StatusResult.BluetoothDisabled && !isPaused && lastExposureDate != null -> {
                ErrorState.BluetoothDisabled
            }
            status == StatusResult.LocationPreconditionNotSatisfied && !isPaused && lastExposureDate != null -> {
                ErrorState.LocationDisabled
            }
            status != StatusResult.Enabled && !isPaused && lastExposureDate != null -> {
                ErrorState.ConsentRequired
            }
            !exposureNotificationsEnabled -> {
                ErrorState.NotificationsDisabled
            }
            lastExposureDate != null && keyProcessingOverdue && !isPaused -> {
                ErrorState.SyncIssues
            }
            else -> ErrorState.None
        }
    }

    fun removeExposure() {
        viewModelScope.launch {
            exposureNotificationsRepository.resetExposures()
        }
    }

    fun resetErrorState() {
        viewModelScope.launch {
            exposureNotificationsRepository.resetNotificationsEnabledTimestamp()
            exposureNotificationsRepository.rescheduleBackgroundJobs()
        }
    }

    sealed class HeaderState {
        object Active : HeaderState()
        object BluetoothDisabled : HeaderState()
        object LocationDisabled: HeaderState()
        object Disabled : HeaderState()
        object SyncIssues : HeaderState()
        data class Paused(val pauseState: Settings.PausedState.Paused, val durationHours: Long? = null, val durationMinutes: Long? = null) : HeaderState()
        data class Exposed(val date: LocalDate, val notificationReceivedDate: LocalDate?, val clock: Clock, val pauseState: Settings.PausedState) : HeaderState()
    }

    sealed class ErrorState {
        object None : ErrorState()
        object BluetoothDisabled : ErrorState()
        object LocationDisabled : ErrorState()
        object ConsentRequired : ErrorState()
        object NotificationsDisabled : ErrorState()
        object SyncIssues : ErrorState()
    }
}
