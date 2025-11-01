package com.baothanhbin.feature.chatbot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.layout.ContentScale
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.baothanhbin.agridoctorai.resources.R
import com.baothanhbin.core.theme.GreenSurface
import com.baothanhbin.core.theme.Subtitle
import com.baothanhbin.core.theme.White
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatbotRoute(
    navController: NavHostController? = null,
    viewModel: ChatbotViewModel = hiltViewModel()
) {
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

    // Log state changes
    LaunchedEffect(uiState.inputText) {
        android.util.Log.d("ChatbotScreen", "uiState.inputText changed to: '${uiState.inputText}'")
    }
    
    LaunchedEffect(uiState.messages.size) {
        android.util.Log.d("ChatbotScreen", "uiState.messages.size changed to: ${uiState.messages.size}")
        android.util.Log.d("ChatbotScreen", "Messages: ${uiState.messages.map { "${it.id}:${it.text.take(20)}" }}")
        if (uiState.messages.isNotEmpty()) {
            android.util.Log.d("ChatbotScreen", "Scrolling to item ${uiState.messages.size - 1}")
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
                    android.util.Log.d("ChatbotScreen", "Rendering ChatMessagesList with ${uiState.messages.size} messages")
                    ChatMessagesList(
                        messages = uiState.messages,
                        isLoading = uiState.isLoading,
                        listState = listState
                    )
                }

                // Input Bar
                ChatInputBar(
                    text = uiState.inputText,
                    onTextChange = viewModel::onInputTextChanged,
                    onSendClick = {
                        android.util.Log.d("ChatbotScreen", "ChatInputBar onSendClick called. Current text: '${uiState.inputText}'")
                        if (uiState.inputText.isNotBlank()) {
                            android.util.Log.d("ChatbotScreen", "Sending message: '${uiState.inputText}'")
                            viewModel.onMessageSent(uiState.inputText)
                        } else {
                            android.util.Log.d("ChatbotScreen", "Input text is blank, not sending")
                        }
                    }
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
                    contentDescription = stringResource(R.string.settings),
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
                contentDescription = stringResource(R.string.edit),
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
    listState: androidx.compose.foundation.lazy.LazyListState
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
                ChatMessageBubble(message = message)
            }
            
            if (isLoading) {
                item(key = "loading") {
                    ChatMessageBubble(
                        message = ChatMessage(
                            id = "loading",
                            text = "Typing...",
                            isUser = false
                        ),
                        isLoading = true
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
    isLoading: Boolean = false
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
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
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
                        contentDescription = "Copy",
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
                        contentDescription = "Speaker",
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
                        contentDescription = "Thumbs up",
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
                        contentDescription = "Thumbs down",
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
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Camera Icon
        IconButton(
            onClick = { },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_camera),
                contentDescription = "Camera",
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
                .height(55 .dp),
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
                contentDescription = "Send",
                tint = Color.Unspecified,
                modifier = Modifier.size(50.dp)
            )
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
                        contentDescription = "Close",
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
                    contentDescription = "Delete",
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
                listState = rememberLazyListState()
            )
        }
        
        ChatInputBar(
            text = "",
            onTextChange = { },
            onSendClick = { }
        )
    }
}

