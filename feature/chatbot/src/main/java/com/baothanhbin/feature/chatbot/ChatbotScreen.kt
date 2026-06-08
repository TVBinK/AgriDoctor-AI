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

@Composable
private fun ChatbotTopBar(
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.menu),
                    tint = GreenSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        IconButton(
            onClick = onNewChatClick,
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_new_chat),
                contentDescription = stringResource(R.string.new_chat),
                tint = GreenSurface,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Composable
private fun ChatMessagesList(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onImageClick: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (messages.isEmpty() && !isLoading) {
            // Show empty state UI with illustration and text
            EmptyChatState()
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = messages,
                key = { message -> message.id }
            ) { message ->
                ChatMessageBubble(
                    message = message,
                    onImageClick = onImageClick
                )
            }
            
            if (isLoading) {
                item(key = "loading") {
                    ChatMessageBubble(
                        message = ChatMessage(
                            id = "loading",
                            text = stringResource(R.string.typing),
                            isUser = false
                        ),
                        isLoading = true,
                        onImageClick = onImageClick
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration
        Image(
            painter = painterResource(id = R.drawable.ic_not_chatbot),
            contentDescription = null,
            modifier = Modifier.size(280.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = stringResource(R.string.ask_botanist),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = stringResource(R.string.get_expert_plant_advice),
            style = MaterialTheme.typography.bodyMedium,
            color = Subtitle,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun ChatMessageBubble(
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

@Composable
private fun ChatInputBar(
    text: String,
    selectedImageUri: android.net.Uri?,
    onTextChange: (String) -> Unit,
    onImageSelected: (android.net.Uri?) -> Unit,
    onClearImage: () -> Unit,
    onPreviewImage: (android.net.Uri) -> Unit,
    onSendClick: () -> Unit
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: android.net.Uri? ->
        onImageSelected(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 20.dp)
    ) {
        if (selectedImageUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = stringResource(R.string.selected_image),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = GreenSurface.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onPreviewImage(selectedImageUri) },
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = onClearImage,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-3).dp)
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.clear_image),
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera Icon
            IconButton(
                onClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = stringResource(R.string.camera),
                    tint = GreenSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text Input - Using local state to prevent reset issues
            var localText by remember(text) { mutableStateOf(text) }

            LaunchedEffect(text) {
                if (text.isEmpty()) {
                    // Only sync when text is cleared (after send)
                    localText = text
                }
            }

            OutlinedTextField(
                value = localText,
                onValueChange = { newValue ->
                    localText = newValue
                    onTextChange(newValue)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Black
                ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.ask_anything),
                        color = GreenSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenSurface,
                    unfocusedBorderColor = GreenSurface.copy(alpha = 0.5f),
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = GreenSurface
                ),
                singleLine = true,
                enabled = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onSendClick()
                    }
                )
            )

            // Send Button
            IconButton(
                onClick = onSendClick,
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_send),
                    contentDescription = stringResource(R.string.send),
                    tint = Color.Unspecified,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatHistoryDrawer(
    chatHistory: List<ChatHistoryItem>,
    currentChatId: Long?,
    onChatSelected: (Long) -> Unit,
    onDeleteChat: (Long) -> Unit,
    onCloseDrawer: () -> Unit
) {
    
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = Color(0xFFE8F5E9)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9))
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.chat_history),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.close),
                        tint = GreenSurface
                    )
                }
            }
            
            // Chat list
            if (chatHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_chat_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtitle,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = chatHistory,
                        key = { it.id }
                    ) { chatItem ->
                        ChatHistoryItem(
                            chatItem = chatItem,
                            isSelected = chatItem.id == currentChatId,
                            onChatClick = { onChatSelected(chatItem.id) },
                            onDeleteClick = { onDeleteChat(chatItem.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatHistoryItem(
    chatItem: ChatHistoryItem,
    isSelected: Boolean,
    onChatClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = if (isSelected) GreenSurface else Color(0xFFE0E0E0),
                shape = shape
            )
            .clip(shape)
            .background(
                if (isSelected) GreenSurface.copy(alpha = 0.05f) else White
            )
            .clickable { onChatClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chatItem.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = chatItem.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = Subtitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(32.dp)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = Subtitle,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(name = "Chat Messages", showBackground = true)
@Composable
fun ChatbotChatScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        ChatbotTopBar(
            onMenuClick = { },
            onNewChatClick = { },
        )
        
        Box(modifier = Modifier.weight(1f)) {
            ChatMessagesList(
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        text = "The question here",
                        isUser = true
                    ),
                    ChatMessage(
                        id = "2",
                        text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum is simply dummy text of the printing and typesetting industry.",
                        isUser = false
                    )
                ),
                isLoading = false,
                listState = rememberLazyListState(),
                onImageClick = { }
            )
        }
        
        ChatInputBar(
            text = "",
            selectedImageUri = null,
            onTextChange = { },
            onImageSelected = { },
            onClearImage = { },
            onPreviewImage = { },
            onSendClick = { }
        )
    }
}

@Composable
private fun ChatImageThumbnail(
    imageUri: String,
    onClick: () -> Unit
) {
    AsyncImage(
        model = imageUri,
        contentDescription = stringResource(R.string.chat_image),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp, max = 220.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun FullScreenImageDialog(
    imageUri: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.48f))
                .clickable(onClick = onDismiss)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 28.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(enabled = false) { }
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(R.string.full_screen_image),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 560.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        }
                        .pointerInput(imageUri) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val nextScale = (scale * zoom).coerceIn(1f, 4f)
                                if (nextScale == 1f) {
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                }
                                scale = nextScale
                            }
                        },
                    contentScale = ContentScale.Fit
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(30.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(15.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close_image_preview),
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

