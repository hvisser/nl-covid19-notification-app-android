/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en.onboarding

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import nl.rijksoverheid.en.BaseFragment
import nl.rijksoverheid.en.ExposureNotificationsViewModel
import nl.rijksoverheid.en.R
import nl.rijksoverheid.en.about.FAQDetailSections
import nl.rijksoverheid.en.about.FAQHeaderItem
import nl.rijksoverheid.en.about.FAQItem
import nl.rijksoverheid.en.about.FAQItemDecoration
import nl.rijksoverheid.en.about.FAQItemId
import nl.rijksoverheid.en.databinding.FragmentListWithButtonBinding
import nl.rijksoverheid.en.ignoreInitiallyEnabled
import nl.rijksoverheid.en.lifecyle.EventObserver
import nl.rijksoverheid.en.navigation.navigateCatchingErrors
import nl.rijksoverheid.en.util.requestDisableBatteryOptimizations
import timber.log.Timber

private const val RC_DISABLE_BATTERY_OPTIMIZATIONS = 1

private val crossLinks = mapOf(
    FAQItemId.REASON to listOf(FAQItemId.LOCATION, FAQItemId.NOTIFICATION_MESSAGE),
    FAQItemId.ANONYMOUS to listOf(
        FAQItemId.NOTIFICATION_MESSAGE,
        FAQItemId.LOCATION,
        FAQItemId.LOCATION_PERMISSION
    ),
    FAQItemId.LOCATION to listOf(FAQItemId.BLUETOOTH, FAQItemId.LOCATION_PERMISSION),
    FAQItemId.NOTIFICATION to listOf(FAQItemId.NOTIFICATION_MESSAGE, FAQItemId.BLUETOOTH),
    FAQItemId.NOTIFICATION_MESSAGE to listOf(
        FAQItemId.NOTIFICATION,
        FAQItemId.REASON,
        FAQItemId.BLUETOOTH
    ),
    FAQItemId.LOCATION_PERMISSION to listOf(
        FAQItemId.LOCATION,
        FAQItemId.REASON,
        FAQItemId.ANONYMOUS
    ),
    FAQItemId.BLUETOOTH to listOf(FAQItemId.NOTIFICATION, FAQItemId.ANONYMOUS),
    FAQItemId.POWER_USAGE to listOf(FAQItemId.LOCATION_PERMISSION, FAQItemId.REASON)
)

class HowItWorksDetailFragment : BaseFragment(R.layout.fragment_list_with_button) {
    private val viewModel: ExposureNotificationsViewModel by activityViewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()

    private val args: HowItWorksDetailFragmentArgs by navArgs()

    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.add(
            FAQDetailSections(
                openAndroidSettings = {
                    startActivity(Intent(ExposureNotificationClient.ACTION_EXPOSURE_NOTIFICATION_SETTINGS))
                }
            ).getSection(args.faqItemId)
        )

        crossLinks[args.faqItemId]?.let { crossLinks ->
            adapter.add(FAQHeaderItem(R.string.cross_links_header))
            adapter.addAll(crossLinks.map(::FAQItem))
        }

        enterTransition = TransitionInflater.from(context).inflateTransition(R.transition.slide_end)
        exitTransition =
            TransitionInflater.from(context).inflateTransition(R.transition.slide_start)

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(R.transition.move_fade)
        sharedElementReturnTransition = sharedElementEnterTransition
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentListWithButtonBinding.bind(view)

        binding.toolbar.setTitle(R.string.onboarding_how_it_works_detail_toolbar_title)
        binding.content.adapter = adapter

        binding.content.addItemDecoration(
            FAQItemDecoration(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.activity_horizontal_margin)
            )
        )

        adapter.setOnItemClickListener { item, _ ->
            when (item) {
                is FAQItem -> {
                    enterTransition = exitTransition
                    findNavController().navigateCatchingErrors(
                        HowItWorksDetailFragmentDirections.actionHowItWorksDetail(item.id),
                        FragmentNavigatorExtras(binding.appbar to binding.appbar.transitionName)
                    )
                }
            }
        }

        binding.button.apply {
            setText(R.string.onboarding_how_it_works_request_consent)
            setOnClickListener { viewModel.requestEnableNotificationsForcingConsent() }
        }
        viewModel.notificationState.ignoreInitiallyEnabled().observe(viewLifecycleOwner) {
            if (it is ExposureNotificationsViewModel.NotificationsState.Enabled) {
                requestDisableBatteryOptimizationsAndContinue()
            }
        }

        onboardingViewModel.continueOnboarding.observe(
            viewLifecycleOwner,
            EventObserver {
                findNavController().navigateCatchingErrors(
                    HowItWorksDetailFragmentDirections.actionNext(),
                    FragmentNavigatorExtras(
                        binding.appbar to binding.appbar.transitionName
                    )
                )
            }
        )
    }

    private fun requestDisableBatteryOptimizationsAndContinue() {
        try {
            requestDisableBatteryOptimizations(RC_DISABLE_BATTERY_OPTIMIZATIONS)
        } catch (ex: ActivityNotFoundException) {
            // ignore
            Timber.e(ex)
            onboardingViewModel.continueOnboarding()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_DISABLE_BATTERY_OPTIMIZATIONS) {
            onboardingViewModel.continueOnboarding()
        }
    }
}
