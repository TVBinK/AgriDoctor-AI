# AI Skill Context for AgriDoctorAI

## Purpose
Tài liệu này dùng để đưa một AI agent mới vào đúng quỹ đạo coding của dự án `AgriDoctorAI` càng nhanh càng tốt. Mục tiêu không phải mô tả Android nói chung, mà là mô tả cách dự án này đang được tổ chức và cách phải code để hòa vào codebase hiện tại.

## Architecture Summary

### High-level architecture
Project này là một **Android multi-module monolith theo feature modules**, kết hợp:

- `app` làm application shell và navigation root
- `feature/*` chứa UI, screen logic, navigation cho từng tính năng
- `core/*` chứa shared model, data access, persistence, networking, theme, UI utility và alarm reminder

Module boundary mang tính **pragmatic** hơn là strict:

- phần lớn dependency đi qua `core/*`
- nhưng có một số dependency `feature -> feature` để reuse screen/navigation theo flow thực tế, ví dụ `home`, `camera`, `diagnose`, `processimage`

Nó **không phải Clean Architecture thuần** vì:

- không có domain layer/use case riêng
- `ViewModel` thường gọi repository trực tiếp
- một số `ViewModel` còn gọi `NetworkDataSource` trực tiếp
- `AndroidViewModel` được dùng ở những nơi cần `Application` context
- một số feature modules phụ thuộc trực tiếp vào feature khác thay vì chỉ đi qua `core/*`

Mô hình thực tế gần nhất là:

- **Feature-first modular MVVM**
- **Repository pattern nhẹ**
- **Single app shell + shared core modules**

### Operational layer map
- UI layer: Jetpack Compose screens trong `feature/*`
- State layer: `ViewModel` + `MutableStateFlow` / `StateFlow` / `SharedFlow`
- Navigation layer: `navigation/*Navigation.kt` trong từng feature
- Data layer: phần lớn repository interfaces ở `core:data/repository`, implementations ở `core:data/impl`
- Local persistence: Room trong `core:database`
- Key/token persistence: Proto DataStore trong `core:datastore`
- Remote access: Ktor clients + `NetworkDataSource` trong `core:network`
- Care reminders: AlarmManager + Hilt BroadcastReceiver trong `core:alarm`

## Coding Standards

### Mandatory rules
- Giữ nguyên **feature-first modular structure**. Tính năng mới đi vào `feature:<name>`, không nhét thẳng vào `app`.
- Ưu tiên phụ thuộc qua `core/*`; repo hiện có một số dependency `feature -> feature` để ghép flow, nhưng không nên mở rộng kiểu phụ thuộc này nếu chưa thực sự cần.
- Mọi dependency app-wide phải đi qua **Hilt**. Phần lớn repository ở `core:data` dùng interface + binding; các concrete sync repository như `ChatSyncRepository` và `HistorySyncRepository` dùng constructor injection trực tiếp.
- UI phải viết bằng **Jetpack Compose**.
- State màn hình phải đi qua **`ViewModel` + Flow/StateFlow**, không giữ business state phân tán trong Composable nếu state đó ảnh hưởng luồng xử lý.
- Navigation phải khai báo trong file riêng `navigation/<Feature>Navigation.kt`.
- Model API phải ở `core:model`.
- Cache/local DB phải qua `core:database`.
- JWT token và user ID phải qua `core:datastore`, không hardcode trong feature.
- Async phải chạy bằng coroutine trong `viewModelScope` hoặc `withContext(Dispatchers.IO)`.
- Nếu một feature cần `Application` context, theo pattern hiện tại có thể dùng `AndroidViewModel`; nếu không cần thì dùng `ViewModel`.
- Nếu đang sửa code cũ, ưu tiên giữ pattern cục bộ của module hơn là ép toàn repo về một style mới.

### Naming conventions

#### Modules
- App shell: `:app`
- Shared modules: `:core:<name>`
- Feature modules: `:feature:<name>`
- Android namespace theo pattern:
  - `com.baothanhbin.agridoctorai` cho `app`
  - `com.baothanhbin.core.<name>` cho core
  - `com.baothanhbin.feature.<name>` cho feature

#### Files and classes
- Screen file: `<Feature>Screen.kt`
- ViewModel: `<Feature>ViewModel.kt`
- Navigation file: `<Feature>Navigation.kt`
- Reusable UI component: `component/<ComponentName>.kt`
- Dialog composable: `dialog/<DialogName>.kt`
- Bottom sheet composable: `sheet/<SheetName>.kt`
- Dùng `component` số ít, không tạo package `components`.
- Mỗi component/dialog/sheet nằm trong một file riêng và tên file trùng tên composable chính.
- UI state: `<Feature>UiState`
- Navigation event sealed class: `<Feature>NavigationEvent`
- Repository interface: `<Thing>Repository`
- Repository implementation: `<Thing>RepositoryImpl`
- Room entity: `<Thing>Entity`
- Có một vài tên legacy chưa hoàn toàn chuẩn như `MyplantRoute`, `MyplantNavigation`, `VerifycationOTPScreen`; không rename hàng loạt nếu task không yêu cầu.
- Mapper extension:
  - remote/domain to entity: `toEntity()`, `toPlantEntity()`
  - entity to model: `toClassifyData()`

#### Functions
- Composable route wrapper: `<Feature>Route(...)`
- Main reusable screen: `<Feature>Screen(...)`
- NavController extension: `navigateTo<Feature>(...)`
- NavGraphBuilder registration: camelCase feature + `Screen`, ví dụ `loginScreen`, `homeScreen`, `processImageScreen`
- State mutators dùng động từ rõ nghĩa: `login`, `signup`, `verifyOtp`, `processImage`, `toggleReminderStatus`, `loadChatHistory`

#### Variables
- Backing state flow: `_uiState`
- Public state flow: `uiState`
- One-off event flow: `_navigationEvent`, `navigationEvent`
- Boolean UI flags thường dùng prefix `is`, `show`, `has`, `should`
- Route constants hiện đang có cả `UPPER_SNAKE_CASE` (`HOME_ROUTE`) lẫn lowercase string (`login`, `settings`, `verification_otp_route`); follow pattern của module đang sửa.
- Navigation args hiện có cả `ARG_*` và tên cụ thể như `EMAIL_ARG`; ưu tiên nhất quán trong chính feature đó.

### Style conventions observed
- `data class` cho UI state, API payload, response, entity
- Ưu tiên immutable public state, mutate qua `.copy(...)`
- Dùng extension functions để chuyển model
- Dùng `collectAsStateWithLifecycle()` ở app/route level khi có lifecycle dependency; vài chỗ cũ vẫn dùng `collectAsState()`
- Logging dùng `Log.d/e(...)` với tag hardcoded hoặc tên class
- Một số comment tiếng Việt; có thể tiếp tục dùng nếu nhất quán và hữu ích

## Project Blueprint

### Root module structure
```text
app/                  Application shell, root NavHost, bottom nav, app-level ViewModel
build-logic/          Convention plugins cho module/app/feature/Hilt/Room/Compose
core/model/           Shared API models, enums, DTOs
core/network/         Ktor clients và network datasource
core/data/            Repository interfaces, implementations, DI bindings
core/database/        Room database, entities, dao, converters, DI
core/datastore/       Proto DataStore cho JWT token, timestamp và user ID
core/theme/           Colors, fonts, typography extensions, app theme
core/ui/              Shared UI helpers/dialog/location utilities
core/alarm/           AlarmManager scheduler, receivers + notification helper
feature/*/            Feature UI modules: screen, viewmodel, navigation, component, dialog, sheet
resources/            Shared drawables, strings, raw assets
```

### `app` module responsibilities
- Khởi tạo Hilt app (`App.kt`)
- Xác định start destination dựa trên auth state (`MainActivity`, `MainViewModel`)
- Nắm `NavHostController`, bottom navigation, shared `LocationStateHolder`

### `core` module responsibilities

#### `core:model`
- Chứa mọi API request/response model, enum, value object shared
- Ví dụ: `LoginRequest`, `AuthResponse`, `DiagnoseApiResponse`, `ClassifyData`, `ApiType`

#### `core:network`
- Chứa Ktor `HttpClient` singleton theo từng endpoint group trong `NetworkClients`
- `NetworkDataSource` là facade gọi HTTP thực tế
- Pattern hiện tại: object singleton, không DI bằng interface
- `API_BASE_URL` được tạo qua `BuildConfig`: ưu tiên Gradle property `api.baseUrl`, sau đó environment variable `API_BASE_URL`, cuối cùng fallback `https://api.example.com`.
- App chặn cleartext traffic bằng manifest và `network_security_config`; endpoint runtime phải dùng HTTPS.

#### `core:data`
- Chứa contracts truy cập dữ liệu
- `repository/`: interface
- `impl/`: implementation
- `di/`: Hilt bindings/provides
- Đây là cầu nối giữa feature với DB/DataStore/network
- `ChatSyncRepository` và `HistorySyncRepository` là concrete service-style repository trong `impl/`, được inject trực tiếp thay vì có interface riêng.

#### `core:database`
- `AgriDoctorDatabase`
- `dao/`
- `model/` cho Room entities
- `converter/` cho list/object serialization
- `di/DatabaseModule.kt` cung cấp singleton DB
- Runtime hiện tại dùng `fallbackToDestructiveMigration()`, nên không được giả định migration dữ liệu luôn an toàn

#### `core:datastore`
- Proto DataStore cho JWT token, timestamp và user ID
- Serializer custom + wrapper class
- Không expose trực tiếp cho feature; đi qua repository hoặc module provider

#### `core:ui`
- Shared utility có tính cross-feature
- Hiện tại tập trung vào location: `LocationHelper`, `LocationStateHolder`, `LocationDialog`

#### `core:alarm`
- Care reminder bằng AlarmManager: exact khi có quyền, fallback inexact khi chưa có quyền
- Receiver dùng `@AndroidEntryPoint`

### `feature` module structure
Feature đơn giản thường có:

```text
feature/<name>/
  build.gradle.kts
  src/main/java/com/baothanhbin/feature/<name>/
    <Feature>Screen.kt
    <Feature>ViewModel.kt
    component/
      <ComponentName>.kt
    navigation/
      <Feature>Navigation.kt
```

Feature chỉ tạo thêm package modal khi thực sự có loại UI tương ứng:

```text
dialog/
  <DialogName>.kt
sheet/
  <SheetName>.kt
```

Quy tắc tổ chức UI:
- Dùng `component/`, không dùng `components/`.
- Một file chỉ chứa một component/dialog/sheet cấp cao nhất.
- `Screen.kt` giữ `Route`, screen-level composition, state và callback wiring; không nhét các card, row, top bar hoặc modal dài vào đây.
- Card, row, top bar, empty state, list item và section độc lập đặt trong `component/`.
- Dialog đặt trong `dialog/`; modal bottom sheet đặt trong `sheet/`.
- `navigation/` chỉ chứa route, navigation arguments và các extension điều hướng.
- Không tạo package `dialog/` hoặc `sheet/` rỗng.
- Một số feature vẫn import trực tiếp feature khác để reuse navigation/screen trong cùng flow; không tạo dependency vòng.

## Data and Logic Flow

### Standard screen flow
Luồng điển hình trong project:

1. `NavHost` ở `app/navigation/MainNavHost.kt` đăng ký destination từ từng feature.
2. `navigation/<Feature>Navigation.kt` định nghĩa:
   - route constant
   - `NavController.navigateToFeature(...)`
   - `NavGraphBuilder.featureScreen(...)`
3. Destination gọi `<Feature>Route(...)`.
4. Thường `<Feature>Route(...)` lấy `ViewModel` bằng `hiltViewModel()`, collect state, xử lý `LaunchedEffect`, rồi truyền data xuống `<Feature>Screen(...)`.
5. Ở một số module cũ, `Route(...)` chỉ forward tham số còn `Screen(...)` tự `hiltViewModel()` và collect state.
6. `<Feature>Screen(...)` ghép các composable trong `component/`, `dialog/`, `sheet/` và truyền UI-ready state + callback xuống.
7. Callback gọi `ViewModel`.
8. `ViewModel` dùng repository hoặc data source để load/save/process dữ liệu.
9. Repository hoặc object datasource gọi Room/DataStore/network.
10. Kết quả được đẩy ngược lên `StateFlow` hoặc `SharedFlow`, hoặc một số auth/onboarding flow dùng boolean flag trong `UiState` kết hợp `LaunchedEffect`.

### Concrete data flow examples

#### Auth flow
- `MainViewModel` quan sát `AuthRepository.isLoggedIn`
- `LoginViewModel` gọi `authRepository.login(LoginRequest)`
- `AuthRepositoryImpl` gọi `NetworkClients.authClient.post(...)`
- Nếu response có token thì lưu bằng `AuthDataStore.saveToken(...)`
- `AuthDataStore.isLoggedInFlow()` phát lại trạng thái
- `MainActivity` đổi `startDestination` / redirect sang login hoặc home

#### Diagnose image flow
- `CameraScreen` chụp ảnh và điều hướng qua `navigateToProcessImage(...)`
- `ProcessImageRoute` gọi `viewModel.processImage(...)` trong `LaunchedEffect`
- `ProcessImageViewModel`:
  - đọc bytes từ `Uri`
  - lấy token từ `AuthRepository`
  - gọi `NetworkDataSource.detectImageTyped(...)`
  - copy ảnh sang app storage
  - map response bằng `DiagnoseData.toEntity(...)`
  - lưu qua `DiagnoseResultRepository.insertDiagnoseResult(...)`
  - phát `ProcessImageNavigationEvent.NavigateToResult`
- `DiagnoseResultViewModel` ưu tiên đọc `LatestDiagnoseResultCache`, fallback về Room nếu cache không còn

#### Plant classify flow
- Flow giống diagnose nhưng gọi `NetworkDataSource.classifyImageTyped(...)`
- Response `ClassifyData` được map bằng `toPlantEntity()`
- Lưu vào `PlantRepository`
- `MyPlantsViewModel` và `HomeViewModel` quan sát `getAllPlants()`

#### Chatbot flow
- `ChatbotViewModel` tự init:
  - load và đồng bộ chat history qua `ChatSyncRepository`
- Khi user gửi message:
  - cập nhật `messages`
  - lấy JWT qua `AuthRepository`
  - map tối đa 10 message gần nhất sang `ChatbotHistoryMessage`
  - đọc optional image bytes từ `Uri`
  - gọi backend qua `NetworkDataSource.sendChatMessage(...)`
  - append bot message
  - lưu và đồng bộ conversation qua `ChatSyncRepository`
- `ChatSyncRepository` quản lý Room, create/update/delete chat history trên server và đồng bộ pending chats theo user.

### State management
- Chuẩn chính: `MutableStateFlow` + public `StateFlow`
- Event một lần: `MutableSharedFlow`
- Một số auth/reset flow hiện dùng cờ trong `UiState` như `isOtpSent`, `isSignupSuccess`, `isSuccess`, `isResetSuccessful` + `LaunchedEffect` + hàm reset/consume thay vì `SharedFlow`
- DB Flow thường được convert bằng `stateIn(viewModelScope, SharingStarted.WhileSubscribed(...), initial)`
- Shared non-ViewModel UI state nhỏ dùng `remember...StateHolder()`, ví dụ `LocationStateHolder`

### Dependency injection
- Dùng Hilt toàn project
- `@HiltAndroidApp` tại `App`
- `@AndroidEntryPoint` cho `MainActivity`
- `@HiltViewModel` cho ViewModel
- Phần lớn repository bind qua `@Binds` trong `core:data:di/DataModule.kt`
- Một số concrete service/repository và DataStore wrapper được cung cấp bằng `@Provides`
- `NetworkDataSource` và `NetworkClients` hiện là object singleton, không có abstraction DI riêng
- Alarm receiver dùng `@AndroidEntryPoint`

### Async handling
- Main pattern:
  - `viewModelScope.launch { ... }`
  - `withContext(Dispatchers.IO) { ... }`
  - repository suspend functions
- Không dùng RxJava
- Không có use case executor riêng

### Important base contracts and shared abstractions
Không có abstract base class kiểu framework nội bộ. Các contract quan trọng nhất là:

- `AuthRepository`
- `PlantRepository`
- `DiagnoseResultRepository`
- `LocationStateHolder`
- `AgriDoctorDatabase`
- DAO interfaces (`PlantDao`, `DiagnoseResultDao`, `ChatDao`, ...)
- Navigation contract per feature:
  - route constant
  - `navigateTo...`
  - `...Screen(...)` registration

## Key Libraries and How They Are Used

- **Jetpack Compose**: toàn bộ UI screen
- **Navigation Compose**: feature navigation registration và route args
- **Hilt**: DI cho app, ViewModel, repository và alarm receiver
- **Room**: local caching cho diagnose results, plants, reminders, chats, diseases cache
- **Proto DataStore**: lưu JWT token, timestamp và user ID
- **Ktor Client + OkHttp engine**: HTTP cho backend APIs
- **kotlinx.serialization**: serialize/deserialize JSON request/response
- **AlarmManager**: exact reminder khi được cấp quyền; fallback inexact reminder khi chưa có quyền
- **Backend chatbot API**: xử lý hội thoại qua `NetworkDataSource.sendChatMessage(...)`
- **Coil**: ảnh/GIF trong Compose
- **CameraX**: camera preview và image capture
- **Play Services Location**: lấy vị trí hiện tại

## Build Conventions

### Convention plugins
Project dùng `build-logic` để tránh lặp lại config:

- `agridoctor.compose.application`
- `agridoctor.compose.module`
- `agridoctor.feature`
- `agridoctor.module`
- `agridoctor.hilt`
- `agridoctor.room`

### Feature module baseline
Feature mới gần như luôn bắt đầu bằng:

```kotlin
plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.<feature>"
}
```

`agridoctor.feature` tự kéo theo:
- Compose module convention
- Hilt
- Kotlin serialization
- `:resources`
- `:core:model`
- `androidx.navigation.compose`

Repo hiện vẫn chấp nhận một số dependency `feature -> feature` nếu flow reuse trực tiếp screen/navigation; tuy nhiên tránh tạo vòng phụ thuộc mới.

## Implementation Guide

### Boilerplate for a new simple feature screen
Khi thêm một feature chuẩn dạng screen + viewmodel + navigation:

#### 1. Tạo module
- Thêm `include(":feature:newfeature")` vào `settings.gradle.kts`
- Tạo `feature/newfeature/build.gradle.kts`
- Dùng plugin `agridoctor.feature`
- Add thêm dependencies thực sự cần như `projects.core.data`, `projects.core.theme`, `projects.core.ui`
- Chỉ thêm dependency sang feature khác nếu đang reuse trực tiếp flow/navigation đã có và không có cách tách qua `core/*` hợp lý hơn

#### 2. Tạo package structure
```text
feature/newfeature/src/main/java/com/baothanhbin/feature/newfeature/
  NewFeatureScreen.kt
  NewFeatureViewModel.kt
  component/
    NewFeatureContent.kt
    NewFeatureTopBar.kt
  navigation/NewFeatureNavigation.kt
```

Nếu feature có modal, thêm từng package khi cần:

```text
  dialog/ConfirmActionDialog.kt
  sheet/EditItemSheet.kt
```

Không tạo `components/`. Mỗi composable cấp component phải có file riêng.

#### 3. ViewModel template
```kotlin
package com.baothanhbin.feature.newfeature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewFeatureUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NewFeatureViewModel @Inject constructor(
    private val repository: SomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewFeatureUiState())
    val uiState: StateFlow<NewFeatureUiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching {
                repository.loadSomething()
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }
}
```

#### 4. Screen template
```kotlin
package com.baothanhbin.feature.newfeature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.baothanhbin.feature.newfeature.component.NewFeatureContent

@Composable
fun NewFeatureRoute(
    onBackClick: () -> Unit,
    viewModel: NewFeatureViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    NewFeatureScreen(
        isLoading = uiState.isLoading,
        error = uiState.error,
        onBackClick = onBackClick
    )
}

@Composable
fun NewFeatureScreen(
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit
) {
    NewFeatureContent(
        isLoading = isLoading,
        error = error,
        onBackClick = onBackClick
    )
}
```

#### 5. Component template
```kotlin
package com.baothanhbin.feature.newfeature.component

import androidx.compose.runtime.Composable

@Composable
internal fun NewFeatureContent(
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit
) {
    // Render one cohesive UI section only.
}
```

#### 6. Navigation template
```kotlin
package com.baothanhbin.feature.newfeature.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.baothanhbin.feature.newfeature.NewFeatureRoute

const val NEW_FEATURE_ROUTE = "NEW_FEATURE_ROUTE"

fun NavController.navigateToNewFeature(navOptions: NavOptions? = null) {
    navigate(NEW_FEATURE_ROUTE, navOptions)
}

fun NavGraphBuilder.newFeatureScreen(
    onBackClick: () -> Unit
) {
    composable(route = NEW_FEATURE_ROUTE) {
        NewFeatureRoute(onBackClick = onBackClick)
    }
}
```

#### 7. Register in app shell
- Import `newFeatureScreen(...)` vào `app/navigation/MainNavHost.kt`
- Gọi registration trong `NavHost`
- Nếu là top-level tab thì cập nhật thêm:
  - `TopLevelDestination`
  - `AppState.currentTopLevelDestination`
  - `AppState.navigateToTopLevelDestination`
  - bottom bar icons/resources

### Boilerplate for a new repository-backed API

#### When to place code where
- API request/response DTO: `core:model`
- HTTP call: `core:network`
- Repository contract hoặc service-style repository: `core:data/repository`
- Repository implementation: `core:data/impl`
- Hilt binding/provision: `core:data/di`
- Local cache entity/dao nếu cần: `core:database`

#### Example pattern

##### API model
```kotlin
@Serializable
data class NewApiRequest(
    val value: String
)

@Serializable
data class NewApiResponse(
    val success: Boolean,
    val data: NewApiData
)
```

##### Network datasource
```kotlin
suspend fun fetchNewThing(request: NewApiRequest, token: String? = null): NewApiResponse? =
    withContext(Dispatchers.IO) {
        try {
            val response = NetworkClients.someClient.post("endpoint") {
                contentType(ContentType.Application.Json)
                setBody(request)
                if (token != null) {
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            if (response.status == HttpStatusCode.OK) response.body() else null
        } catch (e: Exception) {
            Log.e("fetchNewThing", "Error: ${e.message}", e)
            null
        }
    }
```

##### Repository contract
```kotlin
interface NewThingRepository {
    suspend fun fetchNewThing(request: NewApiRequest): Result<NewApiResponse>
}
```

##### Repository implementation
```kotlin
class NewThingRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository
) : NewThingRepository {

    override suspend fun fetchNewThing(request: NewApiRequest): Result<NewApiResponse> {
        return try {
            val token = authRepository.getToken()
            val response = NetworkDataSource.fetchNewThing(request, token)
            if (response != null) Result.success(response)
            else Result.failure(IllegalStateException("Null response"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

##### Hilt binding
```kotlin
@Binds
@Singleton
abstract fun bindNewThingRepository(
    impl: NewThingRepositoryImpl
): NewThingRepository
```

Nếu repository đó chưa cần abstraction ngay, repo hiện tại cũng có precedent dùng concrete class với constructor injection như `ChatSyncRepository` và `HistorySyncRepository`.

### Additional base templates

#### Boilerplate for a new Room-backed local feature
Khi data là local-first hoặc cần cache/query lại nhiều lần, pattern thực tế của repo là:

##### Entity + DAO
```kotlin
@Entity(tableName = "care_tasks")
data class CareTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantId: Long,
    val title: String,
    val targetTimestamp: Long,
    val isCompleted: Boolean = false
)

@Dao
interface CareTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CareTaskEntity): Long

    @Query("SELECT * FROM care_tasks ORDER BY targetTimestamp ASC")
    fun getAllTasks(): Flow<List<CareTaskEntity>>

    @Query("UPDATE care_tasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, isCompleted: Boolean)
}
```

##### Repository contract + implementation
```kotlin
interface CareTaskRepository {
    suspend fun insertTask(task: CareTaskEntity): Long
    fun getAllTasks(): Flow<List<CareTaskEntity>>
    suspend fun updateTaskStatus(id: Long, isCompleted: Boolean)
}

class CareTaskRepositoryImpl @Inject constructor(
    private val database: AgriDoctorDatabase
) : CareTaskRepository {
    private val taskDao get() = database.careTaskDao()

    override suspend fun insertTask(task: CareTaskEntity): Long =
        taskDao.insertTask(task)

    override fun getAllTasks(): Flow<List<CareTaskEntity>> =
        taskDao.getAllTasks()

    override suspend fun updateTaskStatus(id: Long, isCompleted: Boolean) {
        taskDao.updateTaskStatus(id, isCompleted)
    }
}
```

##### ViewModel consume pattern
```kotlin
@HiltViewModel
class CareTaskViewModel @Inject constructor(
    private val repository: CareTaskRepository
) : ViewModel() {

    val tasks: StateFlow<List<CareTaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleTask(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTaskStatus(id, isCompleted)
        }
    }
}
```

#### Boilerplate for a new Proto DataStore wrapper
Repo đang dùng Proto DataStore wrapper class, không dùng Preferences DataStore.

##### `.proto`
```proto
syntax = "proto3";

option java_package = "com.baothanhbin.core.datastore";
option java_multiple_files = true;

message FeatureSettingProto {
  string value = 1;
  int64 timestamp = 2;
}
```

##### Serializer + wrapper class
```kotlin
object FeatureSettingSerializer : Serializer<FeatureSettingProto> {
    override val defaultValue: FeatureSettingProto = FeatureSettingProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): FeatureSettingProto {
        return FeatureSettingProto.parseFrom(input)
    }

    override suspend fun writeTo(t: FeatureSettingProto, output: OutputStream) {
        t.writeTo(output)
    }
}

class FeatureSettingDataStore(private val context: Context) {
    private val Context.featureSettingDataStore: DataStore<FeatureSettingProto> by dataStore(
        fileName = "feature_setting.pb",
        serializer = FeatureSettingSerializer
    )

    suspend fun saveValue(value: String) {
        context.featureSettingDataStore.updateData { current ->
            current.toBuilder()
                .setValue(value)
                .setTimestamp(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun getValue(): String? {
        return context.featureSettingDataStore.data
            .map { proto -> proto.value.ifEmpty { null } }
            .first()
    }

    suspend fun clearValue() {
        context.featureSettingDataStore.updateData {
            FeatureSettingProto.getDefaultInstance()
        }
    }
}
```

##### Hilt provide
```kotlin
@Provides
@Singleton
fun provideFeatureSettingDataStore(
    @ApplicationContext context: Context
): FeatureSettingDataStore {
    return FeatureSettingDataStore(context)
}
```

#### Boilerplate for a new AlarmManager-backed reminder
Pattern hiện tại là: lưu metadata vào Room trước, sau đó schedule `ReminderAlarmScheduler`.

- Android trước API 31 hoặc khi đã có exact-alarm access: dùng `setExactAndAllowWhileIdle(...)`.
- Khi chưa có quyền exact alarm: fallback `setAndAllowWhileIdle(...)`, sau đó mở trang `ACTION_REQUEST_SCHEDULE_EXACT_ALARM`.
- Khi quyền exact alarm được cấp hoặc thiết bị reboot, `ReminderBootReceiver` đặt lại các reminder chưa hoàn thành còn trong tương lai.

##### Schedule from ViewModel or feature layer
```kotlin
fun scheduleReminder(
    context: Context,
    plantId: Long,
    title: String,
    targetTimestamp: Long
) {
    if (targetTimestamp <= System.currentTimeMillis()) return

    viewModelScope.launch {
        val entity = CareTaskEntity(
            plantId = plantId,
            title = title,
            targetTimestamp = targetTimestamp
        )
        val taskId = repository.insertTask(entity)

        ReminderAlarmScheduler.schedule(
            context = context.applicationContext,
            reminderId = taskId,
            plantName = "Cây của bạn",
            actionName = title,
            targetTimestamp = targetTimestamp
        )
    }
}
```

##### Cancel when user marks complete manually
```kotlin
fun toggleTask(id: Long, isCompleted: Boolean) {
    viewModelScope.launch {
        repository.updateTaskStatus(id, isCompleted)
        if (isCompleted) {
            ReminderAlarmScheduler.cancel(getApplication(), id)
        }
    }
}
```

#### Boilerplate for app shell top-level destination
Khi thêm top-level tab mới, phải sửa đủ `TopLevelDestination`, `AppState`, và `MainNavHost`.

##### Top-level destination enum
```kotlin
enum class TopLevelDestination(
    val label: Int,
    val selectedIcon: Int,
    val unselectedIcon: Int,
) {
    NEW_FEATURE(
        label = R.string.new_feature,
        selectedIcon = R.drawable.ic_selected_new_feature,
        unselectedIcon = R.drawable.ic_unselected_new_feature
    )
}
```

##### `AppState` route mapping + navigate
```kotlin
val currentTopLevelDestination: TopLevelDestination?
    @Composable get() {
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination
        return when (currentDestination?.route) {
            NEW_FEATURE_ROUTE -> TopLevelDestination.NEW_FEATURE
            else -> null
        }
    }

fun navigateToTopLevelDestination(destination: TopLevelDestination) {
    val topLevelNavOptions = navOptions {
        popUpTo(HOME_ROUTE) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    when (destination) {
        TopLevelDestination.NEW_FEATURE -> navController.navigateToNewFeature(topLevelNavOptions)
        else -> Unit
    }
}
```

##### Register in `MainNavHost`
```kotlin
NavHost(
    navController = navController,
    startDestination = startDestination
) {
    newFeatureScreen(
        onBackClick = { navController.navigateUp() }
    )
}
```

#### Boilerplate for one-off navigation via boolean flag in `UiState`
Đây là pattern đang tồn tại khá nhiều ở auth/onboarding. Với code mới, ưu tiên `SharedFlow`; nhưng nếu đang follow flow cũ thì dùng mẫu này.

##### ViewModel
```kotlin
data class VerifyUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VerifyViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState: StateFlow<VerifyUiState> = _uiState.asStateFlow()

    fun verify(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.verifySomething(code)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun consumeSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}
```

##### Route
```kotlin
@Composable
fun VerifyRoute(
    onVerifySuccess: () -> Unit,
    viewModel: VerifyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onVerifySuccess()
            viewModel.consumeSuccess()
        }
    }

    VerifyScreen(
        isLoading = uiState.isLoading,
        error = uiState.error,
        onVerify = viewModel::verify
    )
}
```

#### Boilerplate for `Screen`-owned `ViewModel`
Một vài module cũ để `Route(...)` chỉ forward arg, còn `Screen(...)` tự inject `ViewModel`.

```kotlin
@Composable
fun NewFeatureRoute(
    navController: NavController,
    sharedStateHolder: LocationStateHolder
) {
    NewFeatureScreen(
        navController = navController,
        sharedStateHolder = sharedStateHolder
    )
}

@Composable
fun NewFeatureScreen(
    navController: NavController,
    sharedStateHolder: LocationStateHolder,
    viewModel: NewFeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData(sharedStateHolder)
    }

    // render UI
}
```

Chỉ dùng biến thể này khi đang sửa một module vốn đã đi theo style đó.

#### Boilerplate for feature-to-feature reuse
Repo hiện có precedent `feature -> feature` khi reuse trực tiếp screen/navigation theo flow người dùng.

##### `build.gradle.kts`
```kotlin
dependencies {
    implementation(projects.feature.camera)
    implementation(projects.feature.settings)
}
```

##### Call navigation extension từ feature khác
```kotlin
fun onDiagnoseClick(navController: NavController) {
    navController.navigateToCamera(modeIndex = 0)
}

fun onSettingsClick(navController: NavController) {
    navController.navigateToSettings()
}
```

Điều kiện chấp nhận:
- reuse cả flow hoặc screen/navigation đã ổn định
- không tạo vòng phụ thuộc
- không lôi business logic shared sang `feature/*` nếu đáng ra nên đặt ở `core/*`

### Pattern notes for Compose routes
- Ưu tiên giữ `Route` làm lớp nối giữa `ViewModel` và `Screen`
- Nhưng nếu module hiện hữu đang để `Screen` tự `hiltViewModel()` thì đừng refactor chỉ để đồng nhất hình thức
- `Screen` nên nhận primitive/UI-ready state + callbacks, tránh tự fetch data
- `Screen` chỉ ghép UI cấp màn hình; tách top bar, section, card, row, item và empty state vào `component/`.
- Mỗi composable được tách có một file riêng; không tạo file gom kiểu `HomeComponents.kt`.
- Tách dialog sang `dialog/` và bottom sheet sang `sheet/`.
- Ưu tiên điều hướng một lần qua `SharedFlow` + `LaunchedEffect`
- Tuy nhiên auth/onboarding hiện có nhiều flow dùng boolean flag trong `UiState` + `LaunchedEffect` + reset/consume
- Nếu cần xử lý khi vào màn hình, dùng `LaunchedEffect(Unit)` hoặc key theo arg

### Pattern notes for Room integration
- Entity trong `core:database:model`
- DAO trong `core:database:dao`
- DB access qua repository, trừ một số legacy/current exceptions như `ChatbotViewModel` dùng DB trực tiếp
- Nếu thêm object/list phức tạp, tạo `TypeConverter`
- Đừng quên repo hiện tại đang `fallbackToDestructiveMigration()`, nên thay đổi schema cần cân nhắc dữ liệu cũ

### Pattern notes for DataStore integration
- Dùng Proto DataStore, không phải Preferences DataStore
- Tạo:
  - `.proto`
  - `Serializer`
  - wrapper class
- Expose API dạng `saveX()`, `getX()`, `clearX()`, và `Flow` nếu state cần observe

### Pattern notes for alarms
- Alarm scheduler và receiver ở `core:alarm`
- Receiver dùng `@AndroidEntryPoint`
- Schedule từ feature qua `ReminderAlarmScheduler`
- Exact timing chỉ được đảm bảo khi hệ thống cho phép schedule exact alarm; nếu không, scheduler fallback sang alarm không chính xác tuyệt đối.
- `ReminderBootReceiver` khôi phục reminder tương lai sau reboot và sau khi quyền exact alarm thay đổi.
- Nếu feature cần lưu lịch, lưu cả metadata vào Room như `ReminderEntity`

## Important Existing Patterns and Exceptions

### Strong existing patterns to preserve
- Feature module có `Screen + ViewModel + navigation + component`; thêm `dialog`/`sheet` khi cần
- Phần lớn data access đi theo `Repository interface + implementation`
- `MutableStateFlow` state holder
- Hilt constructor injection
- `NavController` extension cho navigation
- Mapping via extension functions
- Cross-feature dependency hiện có nhưng chủ yếu theo flow một chiều; tránh tạo vòng phụ thuộc mới

### Existing inconsistencies you should be aware of
- Không phải mọi remote call đều đi qua repository; `ProcessImageViewModel` và `ChatbotViewModel` vẫn gọi `NetworkDataSource` trực tiếp cho tác vụ chính.
- Không phải mọi data service đều có `interface + impl`; `ChatSyncRepository` và `HistorySyncRepository` là concrete class dùng constructor injection.
- Không phải mọi feature đều độc lập; có các dependency `feature -> feature` ở `home`, `camera`, `diagnose`, `processimage`
- Ownership giữa `Route` và `Screen` không thống nhất hoàn toàn giữa các module
- One-off event pattern đang bị trộn giữa `SharedFlow` và boolean flag trong `UiState`
- Route naming đang bị trộn giữa uppercase constant style và lowercase route string style
- Có cả `ViewModel` và `AndroidViewModel`
- Có chỗ dùng `collectAsStateWithLifecycle`, có chỗ dùng `collectAsState`

Khi viết code mới, **ưu tiên pattern tốt hơn nhưng vẫn tương thích codebase hiện tại**:
- Ưu tiên repository thay vì gọi DB/network trực tiếp
- Ưu tiên `collectAsStateWithLifecycle`
- Chỉ dùng `AndroidViewModel` khi thực sự cần `Application`

## Fast Mental Model for New Agents
- Nếu sửa app shell, vào `app/`
- Nếu thêm màn hình mới, vào `feature/<name>/`
- Nếu thêm thành phần UI của một feature, tạo file riêng trong `feature/<name>/component/`
- Nếu thêm dialog hoặc bottom sheet, dùng lần lượt `dialog/` hoặc `sheet/`
- Nếu cần model cho API, vào `core:model`
- Nếu cần HTTP/backend, vào `core:network`
- Nếu cần contract dữ liệu, vào `core:data`
- Nếu cần cache/local DB, vào `core:database`
- Nếu cần token/key persistence, vào `core:datastore`
- Nếu cần shared helper UI/location/dialog, vào `core:ui`
- Nếu cần alarm reminder/notification, vào `core:alarm`

## Recommended Workflow for Any New Change
1. Xác định feature module chịu trách nhiệm UI.
2. Kiểm tra đã có repository contract hoặc concrete service phù hợp chưa.
3. Nếu chưa có, thêm model + network + repository/service + DI binding/provide.
4. Tạo hoặc cập nhật `ViewModel` với `UiState`.
5. Ưu tiên để `Route` lo collect state và side effects; nếu module hiện hữu đang để `Screen` tự lấy `ViewModel` thì follow local pattern.
6. Giữ `Screen` tập trung vào screen-level composition; tách từng khối UI sang một file trong `component/`, `dialog/` hoặc `sheet/`.
7. Đăng ký navigation trong file `navigation/*`; nếu flow đã reuse feature khác thì dùng lại extension/navigation có sẵn thay vì copy.
8. Nếu dữ liệu cần tồn tại sau process death hoặc dùng lại, lưu vào Room/DataStore.

## Reference Files Worth Reading First
- `app/src/main/java/com/baothanhbin/agridoctorai/navigation/MainNavHost.kt`
- `app/src/main/java/com/baothanhbin/agridoctorai/navigation/AppState.kt`
- `app/src/main/java/com/baothanhbin/agridoctorai/navigation/TopLevelDestination.kt`
- `core/data/src/main/java/com/baothanhbin/core/data/di/DataModule.kt`
- `core/network/src/main/java/com/baothanhbin/core/network/NetworkClients.kt`
- `core/network/src/main/java/com/baothanhbin/core/network/NetworkDataSource.kt`
- `core/database/src/main/java/com/baothanhbin/core/database/di/DatabaseModule.kt`
- `core/database/src/main/java/com/baothanhbin/core/database/model/ReminderEntity.kt`
- `core/database/src/main/java/com/baothanhbin/core/database/dao/ReminderDao.kt`
- `core/datastore/src/main/java/com/baothanhbin/core/datastore/AuthDataStore.kt`
- `core/datastore/src/main/proto/auth.proto`
- `core/alarm/src/main/java/com/baothanhbin/core/alarm/ReminderAlarmScheduler.kt`
- `feature/login/src/main/java/com/baothanhbin/feature/login/LoginScreen.kt`
- `feature/home/src/main/java/com/baothanhbin/feature/home/HomeScreen.kt`
- `feature/home/src/main/java/com/baothanhbin/feature/home/component/HomeTopBar.kt`
- `feature/myplants/src/main/java/com/baothanhbin/feature/myplants/MyPlantsScreen.kt`
- `feature/myplants/src/main/java/com/baothanhbin/feature/myplants/sheet/AddPlantBottomSheet.kt`
- `feature/myplants/src/main/java/com/baothanhbin/feature/myplants/MyPlantsViewModel.kt`
- `feature/verificationotp/src/main/java/com/baothanhbin/feature/verificationotp/VerificationOTPViewModel.kt`
- `feature/processimage/src/main/java/com/baothanhbin/feature/processimage/ProcessImageViewModel.kt`
- `feature/chatbot/src/main/java/com/baothanhbin/feature/chatbot/ChatbotViewModel.kt`

## Final Guidance to Future Agents
Nếu phải chọn giữa “sạch hơn theo lý thuyết” và “giống codebase hiện tại”, hãy ưu tiên:

- giống module boundaries hiện tại
- giống naming hiện tại
- giống navigation pattern hiện tại
- giống state/update flow hiện tại
- không tự chuẩn hóa route name, typo legacy, hay ownership `Route/Screen` nếu task không yêu cầu

Sau đó mới tối ưu dần, nhưng không phá consistency của repo.
