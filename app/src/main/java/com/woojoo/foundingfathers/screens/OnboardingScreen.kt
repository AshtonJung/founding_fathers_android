package com.woojoo.foundingfathers.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woojoo.foundingfathers.ui.theme.AppColors
import com.woojoo.foundingfathers.ui.theme.MedalView
import com.woojoo.foundingfathers.ui.theme.ShinyButton
import com.woojoo.foundingfathers.ui.theme.gradientBackground
import com.woojoo.foundingfathers.data.Tier

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .gradientBackground()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MedalView(tier = Tier.GOLD, size = 96.dp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Voices of the Founders",
            color = AppColors.TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Explore the lives of five Founding Fathers through interactive timelines, quizzes, and quote challenges.",
            color = AppColors.TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        ShinyButton(text = "Get Started", onClick = onDone)
    }
}
