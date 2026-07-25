package com.woojoo.foundingfathers.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woojoo.foundingfathers.state.AppViewModel
import com.woojoo.foundingfathers.ui.theme.AppColors
import com.woojoo.foundingfathers.ui.theme.ShinyButton
import com.woojoo.foundingfathers.ui.theme.gradientBackground
import com.woojoo.foundingfathers.ui.theme.themedCard

@Composable
fun HistoryScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val items = state.wrongHistory

    Column(modifier = Modifier.fillMaxSize().gradientBackground()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Review Mistakes", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        if (items.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.height(44.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("No mistakes to review", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Great job! Come back after a quiz to review missed questions.",
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { w ->
                    Column(modifier = Modifier.fillMaxWidth().themedCard()) {
                        Text(w.question, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = AppColors.AccentRed)
                            Text(w.chosen, color = AppColors.TextSecondary, fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF4CAF50))
                            Text(w.correct, color = AppColors.TextSecondary, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ShinyButton(text = "Delete", onClick = { viewModel.deleteWrongAnswer(w) })
                    }
                }
            }
        }
    }
}
