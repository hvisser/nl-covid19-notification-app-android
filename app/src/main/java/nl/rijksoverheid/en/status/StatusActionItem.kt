/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en.status

import android.view.View
import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.xwray.groupie.Item
import nl.rijksoverheid.en.R
import nl.rijksoverheid.en.databinding.ItemStatusActionBinding
import nl.rijksoverheid.en.items.BaseBindableItem

sealed class StatusActionItem(
    @DrawableRes private val icon: Int,
    @StringRes private val title: Int,
    @StringRes private val subtitle: Int
) : BaseBindableItem<ItemStatusActionBinding>() {
    override fun getLayout() = R.layout.item_status_action
    override fun isClickable() = true

    override fun bind(viewBinding: ItemStatusActionBinding, position: Int) {
        ViewCompat.setAccessibilityDelegate(
            viewBinding.container,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityNodeInfo(
                    host: View,
                    info: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.contentDescription =
                        "${viewBinding.statusTitle.text}\n${viewBinding.statusSubtitle.text}"
                    info.className = Button::class.java.name
                }
            }
        )
        viewBinding.infoIcon = icon
        viewBinding.infoTitle = title
        viewBinding.infoSubtitle = subtitle
    }

    object About : StatusActionItem(
        R.drawable.ic_info,
        R.string.status_info_about_title,
        R.string.status_info_about_subtitle
    ) {
        override fun isSameAs(other: Item<*>) = other is About
    }

    object Settings : StatusActionItem(
        R.drawable.ic_settings,
        R.string.status_info_settings_title,
        R.string.status_info_settings_subtitle
    ) {
        override fun isSameAs(other: Item<*>): Boolean = other is Settings
    }

    object Share : StatusActionItem(
        R.drawable.ic_share,
        R.string.status_info_share_title,
        R.string.status_info_share_subtitle
    ) {
        override fun isSameAs(other: Item<*>) = other is Share
    }

    object GenericNotification : StatusActionItem(
        R.drawable.ic_warning,
        R.string.status_info_notification_title,
        R.string.status_info_notification_subtitle
    ) {
        override fun isSameAs(other: Item<*>) = other is GenericNotification
    }

    object RequestTest : StatusActionItem(
        R.drawable.ic_test,
        R.string.status_info_test_title,
        R.string.status_info_test_subtitle
    ) {
        override fun isSameAs(other: Item<*>) = other is RequestTest
    }

    object LabTest : StatusActionItem(
        R.drawable.ic_virus,
        R.string.status_info_lab_title,
        R.string.status_info_lab_subtitle
    ) {
        override fun isSameAs(other: Item<*>) = other is LabTest
    }
}
