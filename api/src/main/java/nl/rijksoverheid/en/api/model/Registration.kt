/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Registration(
    @Json(name = "ggdKey") val ggdKey: String,
    @Json(name = "bucketId") val bucketId: String,
    @Json(name = "confirmationKey") val confirmationKey: ByteArray,
    @Json(name = "validity") val validitySeconds: Long
)
