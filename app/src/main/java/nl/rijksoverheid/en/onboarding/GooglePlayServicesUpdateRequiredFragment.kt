/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en.onboarding

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.GoogleApiAvailability
import nl.rijksoverheid.en.BaseFragment
import nl.rijksoverheid.en.R
import nl.rijksoverheid.en.databinding.FragmentGooglePlayServicesUpgradeRequiredBinding
import nl.rijksoverheid.en.util.setSlideTransition

class GooglePlayServicesUpdateRequiredFragment :
    BaseFragment(R.layout.fragment_google_play_services_upgrade_required) {

    val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSlideTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentGooglePlayServicesUpgradeRequiredBinding.bind(view)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }

        binding.next.setOnClickListener {
            openPlayStore()
        }

        viewModel.isExposureNotificationApiUpToDate.observe(viewLifecycleOwner) { upToDate ->
            if (upToDate)
                findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshExposureNotificationApiUpToDate()
    }

    private fun openPlayStore() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE}")
        ).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            requireActivity().startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // let it crash if there's no browser to handle this
            requireActivity().startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=${GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
