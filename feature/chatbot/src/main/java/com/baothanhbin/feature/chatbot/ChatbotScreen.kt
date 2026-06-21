package com.baothanhbin.feature.chatbot

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
import com.baothanhbin.feature.chatbot.component.*
import com.baothanhbin.feature.chatbot.dialog.*

@Composable
fun ChatbotRoute(
    navController: NavHostController? = null,
    viewModel: ChatbotViewModel = hiltViewModel(),
    initialMessage: String? = null
) {
    // Process initialMessage only once using ViewModel's tracking mechanism
    LaunchedEffect(initialMessage) {
        if (!initialMessage.isNullOrBlank()) {
            viewModel.processInitialMessageIfNeeded(initialMessage)
        }
    }

    ChatbotScreen(
        navController = navController,
        viewModel = viewModel,
    )
}

@Composable
fun ChatbotScreen(
    navController: NavHostController? = null,
    viewModel: ChatbotViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var fullScreenImageUri by remember { mutableStateOf<String?>(null) }

    // Sync drawer state with ViewModel state
    LaunchedEffect(uiState.isDrawerOpen) {
        if (uiState.isDrawerOpen) {
            if (drawerState.isClosed) {
                drawerState.open()
            }
        } else {
            if (drawerState.isOpen) {
                drawerState.close()
            }
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Sync drawer state changes back to ViewModel
    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen && uiState.isDrawerOpen) {
            viewModel.closeDrawer()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatHistoryDrawer(
                chatHistory = uiState.chatHistory,
                currentChatId = uiState.currentChatId,
                onChatSelected = { chatId ->
                    scope.launch {
                        drawerState.close()
                    }
                    viewModel.loadChat(chatId)
                },
                onDeleteChat = { chatId ->
                    viewModel.deleteChat(chatId)
                },
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar
                ChatbotTopBar(
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) {
                                drawerState.open()
                                viewModel.toggleDrawer()
                            } else {
                                drawerState.close()
                                viewModel.closeDrawer()
                            }
                        }
                    },
                    onNewChatClick = {
                        viewModel.createNewChat()
                    }
                )

                // Content - Always show chat messages list
                Box(modifier = Modifier.weight(1f)) {
                    ChatMessagesList(
                        messages = uiState.messages,
                        isLoading = uiState.isLoading,
                        listState = listState,
                        onImageClick = { fullScreenImageUri = it }
                    )
                }

                // Input Bar
                ChatInputBar(
                    text = uiState.inputText,
                    selectedImageUri = uiState.selectedImageUri,
                    onTextChange = viewModel::onInputTextChanged,
                    onImageSelected = viewModel::onImageSelected,
                    onClearImage = viewModel::clearSelectedImage,
                    onPreviewImage = { fullScreenImageUri = it.toString() },
                    onSendClick = {
                        if (uiState.inputText.isNotBlank() || uiState.selectedImageUri != null) {
                            viewModel.onMessageSent(uiState.inputText)
                        }
                    }
                )
            }

            fullScreenImageUri?.let { imageUri ->
                FullScreenImageDialog(
                    imageUri = imageUri,
                    onDismiss = { fullScreenImageUri = null }
                )
            }
        }
    }
}
