/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en.test

import com.google.android.gms.nearby.exposurenotification.DailySummariesConfig
import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import nl.rijksoverheid.en.enapi.DailyRiskScoresResult
import nl.rijksoverheid.en.enapi.DiagnosisKeysResult
import nl.rijksoverheid.en.enapi.DisableNotificationsResult
import nl.rijksoverheid.en.enapi.EnableNotificationsResult
import nl.rijksoverheid.en.enapi.ExposureNotificationApi
import nl.rijksoverheid.en.enapi.StatusResult
import nl.rijksoverheid.en.enapi.TemporaryExposureKeysResult
import nl.rijksoverheid.en.enapi.UpdateToDateResult
import java.io.File

open class FakeExposureNotificationApi :
    ExposureNotificationApi {
    override suspend fun getStatus(): StatusResult = StatusResult.Disabled

    override suspend fun requestEnableNotifications(): EnableNotificationsResult =
        EnableNotificationsResult.Enabled

    override suspend fun disableNotifications(): DisableNotificationsResult =
        DisableNotificationsResult.Disabled

    override suspend fun requestTemporaryExposureKeyHistory(): TemporaryExposureKeysResult =
        TemporaryExposureKeysResult.Success(
            emptyList()
        )

    override suspend fun provideDiagnosisKeys(
        files: List<File>,
        diagnosisKeysDataMapping: DiagnosisKeysDataMapping
    ): DiagnosisKeysResult = DiagnosisKeysResult.Success

    override suspend fun getDailyRiskScores(config: DailySummariesConfig): DailyRiskScoresResult =
        DailyRiskScoresResult.Success(emptyList())

    override fun deviceSupportsLocationlessScanning(): Boolean = false

    override suspend fun isExposureNotificationApiUpToDate(): UpdateToDateResult =
        UpdateToDateResult.UpToDate
}
