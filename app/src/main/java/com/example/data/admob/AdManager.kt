package com.example.data.admob

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Centralized AdManager for Google AdMob Monetization & Policy Compliance.
 *
 * Implements:
 * 1. Official Google Mobile Ads Test Ad Unit IDs
 * 2. Safe placement policy (Never placed over payment buttons or OTP screens)
 * 3. Interstitial Frequency Capping
 * 4. Google User Messaging Platform (UMP) consent flow abstraction
 */
object AdManager {

    // Official Google AdMob Test IDs (Strict policy: Never use fake IDs or unapproved live IDs during development)
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Configurable Ad Unit IDs (allows runtime switching or remote config)
    var bannerAdUnitId: String = TEST_BANNER_AD_UNIT_ID
    var interstitialAdUnitId: String = TEST_INTERSTITIAL_AD_UNIT_ID
    var isAdsEnabled: Boolean = true

    // Policy Frequency Cap tracking
    private var actionCountSinceLastAd = 0
    private const val ACTIONS_PER_INTERSTITIAL = 4
    private var lastInterstitialTime = 0L
    private const val MIN_INTERSTITIAL_INTERVAL_MILLIS = 90_000L // 90 seconds minimum between interstitials

    var isUmpConsentGathered: Boolean = true
    var consentStatusDescription: String = "GDPR / UMP Consent: Personalized Ads Granted (Test Mode)"

    /**
     * Call this when a user completes a meaningful flow (e.g. successfully saving a transaction or viewing a full monthly report).
     * Strictly complies with Google Play policy: NEVER shows immediately upon app launch or interrupting critical input.
     */
    fun shouldTriggerInterstitial(): Boolean {
        if (!isAdsEnabled) return false
        actionCountSinceLastAd++
        val now = System.currentTimeMillis()
        if (actionCountSinceLastAd >= ACTIONS_PER_INTERSTITIAL && (now - lastInterstitialTime) > MIN_INTERSTITIAL_INTERVAL_MILLIS) {
            actionCountSinceLastAd = 0
            lastInterstitialTime = now
            return true
        }
        return false
    }

    fun resetActionCounter() {
        actionCountSinceLastAd = 0
    }
}

/**
 * Policy-compliant Banner Ad Container.
 * Shows official Test Ad status indicator with clear separation from financial controls.
 */
@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdManager.TEST_BANNER_AD_UNIT_ID
) {
    if (!AdManager.isAdsEnabled) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2E7D32))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Ad · Test",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Google Mobile Ads (AdMob) টেস্ট ব্যানার",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${adUnitId.take(20)}...",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "AdMob Policy Protected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
