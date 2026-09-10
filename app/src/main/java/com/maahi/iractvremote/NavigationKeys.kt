package com.maahi.iractvremote

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object DashboardNavKey : NavKey

@Serializable
data object DeviceTypePickerNavKey : NavKey

// AC Navigation Keys
@Serializable
data object BrandPickerNavKey : NavKey

@Serializable
data class TestingWizardNavKey(val brandName: String) : NavKey

@Serializable
data class RemoteNavKey(val remoteId: String) : NavKey

// TV Navigation Keys
@Serializable
data object TvBrandPickerNavKey : NavKey

@Serializable
data class TvTestingWizardNavKey(val brandName: String) : NavKey

@Serializable
data class TvRemoteNavKey(val remoteId: String) : NavKey
