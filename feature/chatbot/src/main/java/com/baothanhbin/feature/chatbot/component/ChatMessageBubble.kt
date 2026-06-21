package com.baothanhbin.feature.chatbot.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.White
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.baothanhbin.feature.chatbot.*
import com.baothanhbin.feature.chatbot.*
import com.baothanhbin.feature.chatbot.component.*
import com.baothanhbin.feature.chatbot.dialog.*

@Composable
internal fun ChatMessageBubble(
    message: ChatMessage,
    isLoading: Boolean = false,
    onImageClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            if (message.isUser) {
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isUser) 16.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (message.isUser) {
                            Color(0xFFE8F5E9) // Light green for user messages
                        } else {
                            Color(0xFFF5F5F5) // Light gray for bot messages
                        }
                    )
                    .padding(12.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    message.imageUri?.let { imageUri ->
                        ChatImageThumbnail(
                            imageUri = imageUri,
                            onClick = { onImageClick(imageUri) }
                        )
                    }

                    if (message.imageUri == null && message.hasImage) {
                        Text(
                            text = "Da gui hinh anh.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Subtitle
                        )
                    }

                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
            }

            if (!message.isUser) {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // Action buttons for bot messages
        if (!message.isUser && !isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(32.dp)
                ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.copy),
                            tint = Subtitle,
                            modifier = Modifier.size(18.dp)
                        )
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(32.dp)
                ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = stringResource(R.string.speaker),
                            tint = Subtitle,
                            modifier = Modifier.size(18.dp)
                        )
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(32.dp)
                ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = stringResource(R.string.thumbs_up),
                            tint = Subtitle,
                            modifier = Modifier.size(18.dp)
                        )
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(32.dp)
                ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = stringResource(R.string.thumbs_down),
                            tint = Subtitle,
                            modifier = Modifier.size(18.dp)
                        )
                }
            }
        }
    }
}
