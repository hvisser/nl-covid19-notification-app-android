/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en.applifecycle

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.en.api.model.AppConfig
import nl.rijksoverheid.en.config.AppConfigManager
import nl.rijksoverheid.en.util.observeForTesting
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Test {

    @Mock
    private lateinit var appLifecycleManager: AppLifecycleManager

    @Mock
    private lateinit var appConfigManager: AppConfigManager

    private lateinit var closeable: AutoCloseable

    @Before
    fun openMocks() {
        closeable = MockitoAnnotations.openMocks(this)
    }

    @After
    fun releaseMocks() {
        closeable.close()
    }

    @Test
    fun `EndOfLife event triggered when coronaMelderDeactivated`() = runBlocking {
        val appLifecycleViewModel = AppLifecycleViewModel(
            appLifecycleManager,
            appConfigManager
        )

        Mockito.`when`(appConfigManager.getConfigOrDefault())
            .thenReturn(AppConfig().copy(coronaMelderDeactivated = "deactivated"))

        appLifecycleViewModel.checkForForcedAppUpdate()

        appLifecycleViewModel.updateEvent.observeForTesting {
            Assert.assertEquals(
                AppLifecycleViewModel.AppLifecycleStatus.EndOfLife,
                it.values.first().getContentIfNotHandled()
            )
        }
    }

    @Test
    fun `Update event triggered when updateRequired`() = runBlocking {
        val appLifecycleViewModel = AppLifecycleViewModel(
            appLifecycleManager,
            appConfigManager
        )
        val mockUpdateState = mock(AppLifecycleManager.UpdateState.UpdateRequired::class.java)

        Mockito.`when`(appConfigManager.getConfigOrDefault())
            .thenReturn(AppConfig())
        Mockito.`when`(appLifecycleManager.getUpdateState())
            .thenReturn(mockUpdateState)

        appLifecycleViewModel.checkForForcedAppUpdate()

        appLifecycleViewModel.updateEvent.observeForTesting {
            Assert.assertTrue(
                it.values.first().getContentIfNotHandled() is AppLifecycleViewModel.AppLifecycleStatus.Update
            )
        }
    }

    @Test
    fun `No unnecessary events triggered when checkForForcedAppUpdate`() = runBlocking {
        val appLifecycleViewModel = AppLifecycleViewModel(
            appLifecycleManager,
            appConfigManager
        )

        val mockUpdateToDateState = mock(AppLifecycleManager.UpdateState.UpToDate::class.java)
        Mockito.`when`(appConfigManager.getConfigOrDefault())
            .thenReturn(AppConfig())
        Mockito.`when`(appLifecycleManager.getUpdateState())
            .thenReturn(mockUpdateToDateState)

        appLifecycleViewModel.checkForForcedAppUpdate()

        appLifecycleViewModel.updateEvent.observeForTesting {
            Assert.assertTrue(
                it.values.isEmpty()
            )
        }
    }
}
