package com.woojoo.foundingfathers.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woojoo.foundingfathers.data.FoundingFather
import com.woojoo.foundingfathers.data.SampleData
import com.woojoo.foundingfathers.ui.theme.AppColors
import com.woojoo.foundingfathers.ui.theme.gradientBackground
import com.woojoo.foundingfathers.ui.theme.themedCard

@Composable
fun FounderDetailScreen(father: FoundingFather, onBack: () -> Unit) {
    val context = LocalContext.current
    val sources = SampleData.sources[father.name].orEmpty()

    Column(modifier = Modifier.fillMaxSize().gradientBackground()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Image(
                        painter = painterResource(father.portraitRes),
                        contentDescription = father.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp).clip(CircleShape)
                    )
                    Column {
                        Text(father.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(father.shortBio, color = AppColors.TextSecondary, fontSize = 13.sp)
                    }
                }
            }
            item {
                Text("Key Timeline", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            items(father.timeline) { event ->
                Column(modifier = Modifier.fillMaxWidth().themedCard()) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("${event.year}", color = AppColors.AccentGold, fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.Bookmark, contentDescription = null, tint = AppColors.PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(event.detail, color = AppColors.TextSecondary, fontSize = 13.sp)
                }
            }
            if (father.quotes.isNotEmpty()) {
                item { Text("Representative Quotes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                items(father.quotes) { quote ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().themedCard()
                    ) {
                        Icon(Icons.Filled.Book, contentDescription = null, tint = AppColors.AccentGold)
                        Text("“$quote”", color = Color.White, fontStyle = FontStyle.Italic)
                    }
                }
            }
            if (sources.isNotEmpty()) {
                item { Text("Sources", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                items(sources) { s ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .themedCard()
                            .clickable {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(s.url)))
                            }
                    ) {
                        Icon(Icons.Filled.Book, contentDescription = null, tint = AppColors.AccentGold)
                        Text(s.title, color = Color.White, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}
