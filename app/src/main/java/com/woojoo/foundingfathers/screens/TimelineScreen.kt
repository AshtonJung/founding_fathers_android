package com.woojoo.foundingfathers.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woojoo.foundingfathers.data.FoundingFather
import com.woojoo.foundingfathers.state.AppViewModel
import com.woojoo.foundingfathers.ui.theme.AppColors
import com.woojoo.foundingfathers.ui.theme.gradientBackground
import com.woojoo.foundingfathers.ui.theme.themedCard

@Composable
fun TimelineListScreen(viewModel: AppViewModel, onOpenFounder: (String) -> Unit) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().gradientBackground().padding(16.dp)) {
        Text("Timelines", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.fathers) { father ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .themedCard()
                        .clickable { onOpenFounder(father.id) }
                ) {
                    Image(
                        painter = painterResource(father.portraitRes),
                        contentDescription = father.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    )
                    Column {
                        Text(father.name, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(father.shortBio, color = AppColors.TextSecondary, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineDetailScreen(father: FoundingFather, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().gradientBackground()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(father.name, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(father.timeline.sortedBy { it.year }) { event ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().themedCard()
                ) {
                    Text(
                        "${event.year}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Column {
                        Text(event.title, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(event.detail, color = AppColors.TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
