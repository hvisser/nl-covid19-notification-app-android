/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en.status.items

import androidx.annotation.StringRes
import com.xwray.groupie.Item
import nl.rijksoverheid.en.R
import nl.rijksoverheid.en.databinding.ItemStatusDashboardErrorBinding
import nl.rijksoverheid.en.items.BaseBindableItem

class StatusDashboardErrorItem(@StringRes val errorMessage: Int) :
    BaseBindableItem<ItemStatusDashboardErrorBinding>() {
    override fun getLayout() = R.layout.item_status_dashboard_error

    override fun bind(viewBinding: ItemStatusDashboardErrorBinding, position: Int) {
        viewBinding.errorText.text = viewBinding.root.context.getString(errorMessage)
    }

    override fun isSameAs(other: Item<*>): Boolean =
        other is StatusDashboardErrorItem && other.errorMessage == errorMessage

    override fun hasSameContentAs(other: Item<*>) =
        other is StatusDashboardErrorItem && other.errorMessage == errorMessage
}