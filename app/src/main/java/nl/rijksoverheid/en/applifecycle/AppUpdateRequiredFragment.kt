/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en.applifecycle

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.en.BaseFragment
import nl.rijksoverheid.en.BuildConfig
import nl.rijksoverheid.en.R
import nl.rijksoverheid.en.databinding.FragmentAppUpdateRequiredBinding
import nl.rijksoverheid.en.util.IntentHelper
import timber.log.Timber

private const val APP_GALLERY_PACKAGE = "com.huawei.appmarket"

class AppUpdateRequiredFragment : BaseFragment(R.layout.fragment_app_update_required) {
    private val args: AppUpdateRequiredFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAppUpdateRequiredBinding.bind(view)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }

        binding.next.setOnClickListener {
            openAppStore()
        }
    }

    private fun openAppStore() {
        when (args.appStorePackage) {
            APP_GALLERY_PACKAGE -> openAppGallery()
            else -> IntentHelper.openPlayStore(requireActivity(), BuildConfig.APPLICATION_ID)
        }
    }

    private fun openAppGallery() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("appmarket://details?id=${BuildConfig.APPLICATION_ID}")
        ).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            requireActivity().startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Timber.w("Could not open app gallery!")
        }
    }
}
