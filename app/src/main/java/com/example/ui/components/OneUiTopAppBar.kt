package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun OneUiTopAppBar(
    title: String,
    lazyScrollState: LazyGridState? = null,
    lazyListState: LazyListState? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val firstVisibleItemIndex = lazyScrollState?.firstVisibleItemIndex ?: lazyListState?.firstVisibleItemIndex ?: 0
    val firstVisibleItemScrollOffset = lazyScrollState?.firstVisibleItemScrollOffset ?: lazyListState?.firstVisibleItemScrollOffset ?: 0

    // Compute scroll progress to transition between large header and small top app bar
    val maxOffset = 300f
    val currentOffset = if (firstVisibleItemIndex == 0) firstVisibleItemScrollOffset.toFloat() else maxOffset
    val progress = min(1f, currentOffset / maxOffset)

    val headerHeight by animateDpAsState(targetValue = 140.dp - (80.dp * progress), label = "headerHeight")
    val largeTitleAlpha by animateFloatAsState(targetValue = 1f - (progress * 1.5f).coerceIn(0f, 1f), label = "largeTitleAlpha")
    val smallTitleAlpha by animateFloatAsState(targetValue = if (progress > 0.8f) (progress - 0.8f) * 5f else 0f, label = "smallTitleAlpha")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Small Top App Bar Items (Actions & Small Title)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(smallTitleAlpha)
            )
            Row(content = actions)
        }

        // Large Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 16.dp)
                .alpha(largeTitleAlpha)
        )
    }
}
