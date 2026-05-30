package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.model.Domain
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleDomain = Domain(
        id = 1,
        name = "SaudiAI.com",
        price = 8500.0,
        category = "ذكاء اصطناعي",
        description = "اسم نطاق متميز يربط مشاريع الذكاء الاصطناعي في المملكة العربية السعودية بملكية استثنائية.",
        isPremium = true,
        isFavorite = true
    )

    composeTestRule.setContent {
        MyApplicationTheme {
            Box(modifier = Modifier.padding(24.dp)) {
                DomainGridItem(
                    domain = sampleDomain,
                    onFavoriteToggle = {},
                    onClick = {}
                )
            }
        }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
