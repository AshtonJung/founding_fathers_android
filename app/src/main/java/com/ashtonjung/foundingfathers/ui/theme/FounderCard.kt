package com.ashtonjung.foundingfathers.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ashtonjung.foundingfathers.data.FoundingFather
import com.ashtonjung.foundingfathers.data.Tier

@Composable
fun FounderCard(
    father: FoundingFather,
    progress: Double,
    tier: Tier,
    hasWrong: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .clickable(onClick = onClick)
                .padding(12.dp)
        ) {
            Image(
                painter = painterResource(father.portraitRes),
                contentDescription = father.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
            )
            Text(
                father.name,
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 8.dp)
            )
            Box(modifier = Modifier.padding(top = 8.dp).size(40.dp), contentAlignment = Alignment.Center) {
                ProgressRing(progress = progress.toFloat(), modifier = Modifier.size(40.dp))
                Text("${(progress * 100).toInt()}%", color = AppColors.TextSecondary, fontSize = 9.sp)
            }
        }

        if (tier != Tier.NONE) {
            TierRibbon(tier = tier, modifier = Modifier.align(Alignment.TopStart).padding(4.dp))
        }
        if (hasWrong) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(AppColors.AccentRed)
            )
        }
    }
}
