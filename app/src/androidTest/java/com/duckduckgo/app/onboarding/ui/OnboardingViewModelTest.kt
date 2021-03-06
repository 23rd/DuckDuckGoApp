/*
 * Copyright (c) 2018 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.onboarding.ui

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.duckduckgo.app.browser.defaultBrowsing.DefaultBrowserDetector
import com.duckduckgo.app.onboarding.store.OnboardingStore
import com.duckduckgo.app.statistics.Variant
import com.duckduckgo.app.statistics.VariantManager
import com.duckduckgo.app.statistics.VariantManager.VariantFeature.DefaultBrowserFeature.ShowInOnboarding
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test


class OnboardingViewModelTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private var onboardingStore: OnboardingStore = mock()
    private var mockDefaultBrowserDetector: DefaultBrowserDetector = mock()
    private var variantManager: VariantManager = mock()

    private val testee: OnboardingViewModel by lazy {
        OnboardingViewModel(onboardingStore, mockDefaultBrowserDetector, variantManager)
    }

    @Test
    fun whenOnboardingDoneThenStoreNotifiedThatOnboardingShown() {
        verify(onboardingStore, never()).onboardingShown()
        testee.onOnboardingDone()
        verify(onboardingStore).onboardingShown()
    }

    @Test
    fun whenFirstPageRequestedThenProtectDataReturned() {
        val page = testee.getItem(0)
        assertTrue(page is OnboardingPageFragment.ProtectDataPage)
    }

    @Test
    fun whenSecondPageRequestedThenNoTracePageReturned() {
        val page = testee.getItem(1)
        assertTrue(page is OnboardingPageFragment.NoTracePage)
    }

    @Test
    fun whenThirdPageRequestedWithFeatureEnabledAndDefaultBrowserCapableThenDefaultBrowserPageReturned() {
        whenever(variantManager.getVariant()).thenReturn(variantWithOnboardingEnabled())
        whenever(mockDefaultBrowserDetector.deviceSupportsDefaultBrowserConfiguration()).thenReturn(true)
        val page = testee.getItem(2)
        assertTrue(page is OnboardingPageFragment.DefaultBrowserPage)
    }

    @Test
    fun whenThirdPageRequestedWithFeatureDisabledAndDefaultBrowserCapableThenNoPageReturned() {
        whenever(variantManager.getVariant()).thenReturn(variantWithOnboardingDisabled())
        whenever(mockDefaultBrowserDetector.deviceSupportsDefaultBrowserConfiguration()).thenReturn(true)
        val page = testee.getItem(2)
        assertNull(page)
    }

    @Test
    fun whenThirdPageRequestedButDefaultBrowserNotCapableThenNoPageReturned() {
        whenever(variantManager.getVariant()).thenReturn(variantWithOnboardingEnabled())
        whenever(mockDefaultBrowserDetector.deviceSupportsDefaultBrowserConfiguration()).thenReturn(false)
        val page = testee.getItem(2)
        assertNull(page)
    }

    private fun variantWithOnboardingEnabled(): Variant =
        Variant("", 0.0, listOf(ShowInOnboarding))

    private fun variantWithOnboardingDisabled(): Variant =
        Variant("", 0.0, listOf())

}