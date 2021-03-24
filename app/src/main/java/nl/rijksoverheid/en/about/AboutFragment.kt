/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en.about

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import nl.rijksoverheid.en.BaseFragment
import nl.rijksoverheid.en.R
import nl.rijksoverheid.en.databinding.FragmentListBinding
import nl.rijksoverheid.en.navigation.navigateCatchingErrors

class AboutFragment : BaseFragment(R.layout.fragment_list) {
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition =
            TransitionInflater.from(context).inflateTransition(R.transition.slide_start)

        adapter.add(AboutSection())
    }

    @SuppressLint("StringFormatInvalid") // for overridden urls in fy language
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentListBinding.bind(view)

        binding.toolbar.setTitle(R.string.about_toolbar_title)

        binding.content.adapter = adapter
        binding.content.addItemDecoration(
            FAQItemDecoration(
                requireContext(),
                resources.getDimensionPixelOffset(R.dimen.activity_horizontal_margin)
            )
        )

        adapter.setOnItemClickListener { item, _ ->
            when (item) {
                is FAQOnboardingItem -> findNavController().navigateCatchingErrors(
                    AboutFragmentDirections.actionAboutDetail(FAQItemId.ONBOARDING),
                    FragmentNavigatorExtras(binding.appbar to binding.appbar.transitionName)
                )
                is FAQTechnicalExplanationItem -> findNavController().navigateCatchingErrors(
                    AboutFragmentDirections.actionAboutDetail(FAQItemId.TECHNICAL),
                    FragmentNavigatorExtras(binding.appbar to binding.appbar.transitionName)
                )
                is FAQItem -> findNavController().navigateCatchingErrors(
                    AboutFragmentDirections.actionAboutDetail(item.id),
                    FragmentNavigatorExtras(binding.appbar to binding.appbar.transitionName)
                )
                is WebsiteLinkItem -> {
                    val url = Uri.parse(
                        getString(
                            R.string.coronamelder_url,
                            getString(R.string.app_language)
                        )
                    )
                    CustomTabsIntent.Builder().build().launchUrl(requireContext(), url)
                }
                is HelpdeskItem -> {
                    val phoneNumber = getString(R.string.helpdesk_phone_number)
                    try {
                        startActivity(
                            Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phoneNumber")
                            }
                        )
                    } catch (e: ActivityNotFoundException) {
                        findNavController().navigateCatchingErrors(
                            AboutFragmentDirections.actionPhoneCallNotSupportedDialog(phoneNumber)
                        )
                    }
                }
                is ReviewItem -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(getString(R.string.play_store_url))
                        setPackage("com.android.vending")
                    }
                    startActivity(intent)
                }
                is PrivacyStatementItem -> {
                    val url = Uri.parse(
                        getString(
                            R.string.privacy_policy_url,
                            getString(R.string.app_language)
                        )
                    )
                    CustomTabsIntent.Builder().build().launchUrl(requireContext(), url)
                }
                is AccessibilityItem -> {
                    val url = Uri.parse(
                        getString(
                            R.string.accessibility_url,
                            getString(R.string.app_language)
                        )
                    )
                    CustomTabsIntent.Builder().build().launchUrl(requireContext(), url)
                }
                is ColofonItem -> {
                    val url =
                        Uri.parse(getString(R.string.colofon_url, getString(R.string.app_language)))
                    CustomTabsIntent.Builder().build().launchUrl(requireContext(), url)
                }
            }
        }
    }
}
