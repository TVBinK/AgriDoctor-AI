# AI Skill Context for AgriDoctorAI

## Purpose
Tài liệu này dùng để đưa một AI agent mới vào đúng quỹ đạo coding của dự án `AgriDoctorAI` càng nhanh càng tốt. Mục tiêu không phải mô tả Android nói chung, mà là mô tả cách dự án này đang được tổ chức và cách phải code để hòa vào codebase hiện tại.

## Architecture Summary

### High-level architecture
Project này là một **Android multi-module monolith theo feature modules**, kết hợp:

- `app` làm application shell và navigation root
- `feature/*` chứa UI, screen logic, navigation cho từng tính năng
- `core/*` chứa shared model, data access, persistence, networking, theme, UI utility, background worker

Nó **không phải Clean Architecture thuần** vì:

- không có domain layer/use case riêng
- `ViewModel` thường gọi repository trực tiếp
- một số `ViewModel` còn gọi `NetworkDataSource` trực tiếp
- `AndroidViewModel` được dùng ở những nơi cần `Application` context

Mô hình thực tế gần nhất là:

- **Feature-first modular MVVM**
- **Repository pattern nhẹ**
- **Single app shell + shared core modules**

### Operational layer map
- UI layer: Jetpack Compose screens trong `feature/*`
- State layer: `ViewModel` + `MutableStateFlow` / `StateFlow` / `SharedFlow`
- Navigation layer: `navigation/*Navigation.kt` trong từng feature
- Data layer: repository interfaces ở `core:data/repository`, implementations ở `core:data/impl`
- Local persistence: Room trong `core:database`
- Key/token persistence: Proto DataStore trong `core:datastore`
- Remote access: Ktor clients + `NetworkDataSource` trong `core:network`
- Background tasks: WorkManager + Hilt worker trong `core:worker`

## Coding Standards

### Mandatory rules
- Giữ nguyên **feature-first modular structure**. Tính năng mới đi vào `feature:<name>`, không nhét thẳng vào `app`.
- Mọi dependency app-wide phải đi qua **Hilt**. Repository dùng interface + binding trong Hilt module.
- UI phải viết bằng **Jetpack Compose**.
- State màn hình phải đi qua **`ViewModel` + Flow/StateFlow`**, không giữ business state phân tán trong Composable nếu state đó ảnh hưởng luồng xử lý.
- Navigation phải khai báo trong file riêng `navigation/<Feature>Navigation.kt`.
- Model API phải ở `core:model`.
- Cache/local DB phải qua `core:database`.
- Token/API key phải qua `core:datastore`, không hardcode trong feature.
- Async phải chạy bằng coroutine trong `viewModelScope` hoặc `withContext(Dispatchers.IO)`.
- Nếu một feature cần `Application` context, theo pattern hiện tại có thể dùng `AndroidViewModel`; nếu không cần thì dùng `ViewModel`.

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
- UI state: `<Feature>UiState`
- Navigation event sealed class: `<Feature>NavigationEvent`
- Repository interface: `<Thing>Repository`
- Repository implementation: `<Thing>RepositoryImpl`
- Room entity: `<Thing>Entity`
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
- Route constants viết `UPPER_SNAKE_CASE`
- Navigation args viết `ARG_*`

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
core/datastore/       Proto DataStore cho auth token và API key
core/theme/           Colors, fonts, typography extensions, app theme
core/ui/              Shared UI helpers/dialog/location utilities
core/worker/          WorkManager workers + notification helper
feature/*/            Mỗi feature độc lập: screen, viewmodel, navigation, đôi khi components/
resources/            Shared drawables, strings, raw assets
```

### `app` module responsibilities
- Khởi tạo Hilt app (`App.kt`)
- Cung cấp WorkManager config với `HiltWorkerFactory`
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

#### `core:data`
- Chứa contracts truy cập dữ liệu
- `repository/`: interface
- `impl/`: implementation
- `di/`: Hilt bindings/provides
- Đây là cầu nối giữa feature với DB/DataStore/network

#### `core:database`
- `AgriDoctorDatabase`
- `dao/`
- `model/` cho Room entities
- `converter/` cho list/object serialization
- `di/DatabaseModule.kt` cung cấp singleton DB

#### `core:datastore`
- Proto DataStore cho auth/api key
- Serializer custom + wrapper class
- Không expose trực tiếp cho feature; đi qua repository hoặc module provider

#### `core:ui`
- Shared utility có tính cross-feature
- Hiện tại tập trung vào location: `LocationHelper`, `LocationStateHolder`, `LocationDialog`

#### `core:worker`
- Background care reminder bằng WorkManager
- Worker dùng `@HiltWorker`

### `feature` module structure
Feature đơn giản thường có:

```text
feature/<name>/
  build.gradle.kts
  src/main/java/com/baothanhbin/feature/<name>/
    <Feature>Screen.kt
    <Feature>ViewModel.kt
    navigation/
      <Feature>Navigation.kt
```

Feature lớn hơn có thể thêm:

```text
components/
```

Ví dụ:
- `feature:myplants` có `components/`
- `feature:chatbot` vẫn giữ file screen/viewmodel ở root package feature

## Data and Logic Flow

### Standard screen flow
Luồng điển hình trong project:

1. `NavHost` ở `app/navigation/MainNavHost.kt` đăng ký destination từ từng feature.
2. `navigation/<Feature>Navigation.kt` định nghĩa:
   - route constant
   - `NavController.navigateToFeature(...)`
   - `NavGraphBuilder.featureScreen(...)`
3. Destination gọi `<Feature>Route(...)`.
4. `<Feature>Route(...)` lấy `ViewModel` bằng `hiltViewModel()`, collect state, xử lý `LaunchedEffect`, rồi truyền data xuống `<Feature>Screen(...)`.
5. `<Feature>Screen(...)` chủ yếu render UI + callback.
6. Callback gọi `ViewModel`.
7. `ViewModel` dùng repository hoặc data source để load/save/process dữ liệu.
8. Repository gọi Room/DataStore/network.
9. Kết quả được đẩy ngược lên `StateFlow` hoặc `SharedFlow`.
10. UI recompose theo state mới hoặc điều hướng theo event.

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
- `DiagnoseResultViewModel` load lại kết quả gần nhất từ Room

#### Plant classify flow
- Flow giống diagnose nhưng gọi `NetworkDataSource.classifyImageTyped(...)`
- Response `ClassifyData` được map bằng `toPlantEntity()`
- Lưu vào `PlantRepository`
- `MyPlantsViewModel` và `HomeViewModel` quan sát `getAllPlants()`

#### Chatbot flow
- `ChatbotViewModel` tự init:
  - load chat history từ Room
  - lấy API key qua `ApiKeyRepository`
  - warm up disease cache từ `/api/diseases`
- Khi user gửi message:
  - cập nhật `messages`
  - build prompt + optional disease summary
  - gọi Gemini SDK
  - append bot message
  - lưu toàn bộ conversation vào `ChatEntity`

### State management
- Chuẩn chính: `MutableStateFlow` + public `StateFlow`
- Event một lần: `MutableSharedFlow`
- DB Flow thường được convert bằng `stateIn(viewModelScope, SharingStarted.WhileSubscribed(...), initial)`
- Shared non-ViewModel UI state nhỏ dùng `remember...StateHolder()`, ví dụ `LocationStateHolder`

### Dependency injection
- Dùng Hilt toàn project
- `@HiltAndroidApp` tại `App`
- `@AndroidEntryPoint` cho `MainActivity`
- `@HiltViewModel` cho ViewModel
- Repository bind qua `@Binds` trong `core:data:di/DataModule.kt`
- Singleton services khác dùng `@Provides`
- Worker dùng `@HiltWorker` + `HiltWorkerFactory`

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
- **Hilt**: DI cho app, ViewModel, repository, worker
- **Room**: local caching cho diagnose results, plants, reminders, chats, diseases cache
- **Proto DataStore**: lưu JWT token và Gemini API key
- **Ktor Client + OkHttp engine**: HTTP cho backend APIs
- **kotlinx.serialization**: serialize/deserialize JSON request/response
- **WorkManager**: care reminder scheduling
- **Google Generative AI SDK**: chatbot Gemini integration
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

## Implementation Guide

### Boilerplate for a new simple feature screen
Khi thêm một feature chuẩn dạng screen + viewmodel + navigation:

#### 1. Tạo module
- Thêm `include(":feature:newfeature")` vào `settings.gradle.kts`
- Tạo `feature/newfeature/build.gradle.kts`
- Dùng plugin `agridoctor.feature`
- Add thêm dependencies thực sự cần như `projects.core.data`, `projects.core.theme`, `projects.core.ui`

#### 2. Tạo package structure
```text
feature/newfeature/src/main/java/com/baothanhbin/feature/newfeature/
  NewFeatureScreen.kt
  NewFeatureViewModel.kt
  navigation/NewFeatureNavigation.kt
```

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
    // Compose UI only
}
```

#### 5. Navigation template
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

#### 6. Register in app shell
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
- Repository contract: `core:data/repository`
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

### Pattern notes for Compose routes
- Giữ `Route` làm lớp nối giữa `ViewModel` và `Screen`
- `Screen` nên nhận primitive/UI-ready state + callbacks, tránh tự fetch data
- Điều hướng một lần nên đi qua `SharedFlow` + `LaunchedEffect`
- Nếu cần xử lý khi vào màn hình, dùng `LaunchedEffect(Unit)` hoặc key theo arg

### Pattern notes for Room integration
- Entity trong `core:database:model`
- DAO trong `core:database:dao`
- DB access qua repository, trừ một số legacy/current exceptions như `ChatbotViewModel` dùng DB trực tiếp
- Nếu thêm object/list phức tạp, tạo `TypeConverter`

### Pattern notes for DataStore integration
- Dùng Proto DataStore, không phải Preferences DataStore
- Tạo:
  - `.proto`
  - `Serializer`
  - wrapper class
- Expose API dạng `saveX()`, `getX()`, `clearX()`, và `Flow` nếu state cần observe

### Pattern notes for workers
- Worker ở `core:worker`
- Dùng `@HiltWorker` + `@AssistedInject`
- Schedule từ feature qua `WorkManager`
- Nếu feature cần lưu lịch, lưu cả metadata vào Room như `ReminderEntity`

## Important Existing Patterns and Exceptions

### Strong existing patterns to preserve
- Feature module = `Screen + ViewModel + navigation`
- Repository interface + implementation cho data access
- `MutableStateFlow` state holder
- Hilt constructor injection
- `NavController` extension for navigation
- Mapping via extension functions

### Existing inconsistencies you should be aware of
- Không phải mọi remote call đều đi qua repository; `ProcessImageViewModel` gọi `NetworkDataSource` trực tiếp
- Không phải mọi DB access đều đi qua repository; `ChatbotViewModel` dùng `AgriDoctorDatabase` trực tiếp
- Có cả `ViewModel` và `AndroidViewModel`
- Có chỗ dùng `collectAsStateWithLifecycle`, có chỗ dùng `collectAsState`

Khi viết code mới, **ưu tiên pattern tốt hơn nhưng vẫn tương thích codebase hiện tại**:
- Ưu tiên repository thay vì gọi DB/network trực tiếp
- Ưu tiên `collectAsStateWithLifecycle`
- Chỉ dùng `AndroidViewModel` khi thực sự cần `Application`

## Fast Mental Model for New Agents
- Nếu sửa app shell, vào `app/`
- Nếu thêm màn hình mới, vào `feature/<name>/`
- Nếu cần model cho API, vào `core:model`
- Nếu cần HTTP/backend, vào `core:network`
- Nếu cần contract dữ liệu, vào `core:data`
- Nếu cần cache/local DB, vào `core:database`
- Nếu cần token/key persistence, vào `core:datastore`
- Nếu cần shared helper UI/location/dialog, vào `core:ui`
- Nếu cần background reminder/notification, vào `core:worker`

## Recommended Workflow for Any New Change
1. Xác định feature module chịu trách nhiệm UI.
2. Kiểm tra đã có repository contract phù hợp chưa.
3. Nếu chưa có, thêm model + network + repository + DI binding.
4. Tạo hoặc cập nhật `ViewModel` với `UiState`.
5. Giữ `Route` lo collect state và side effects.
6. Giữ `Screen` tập trung vào Compose UI.
7. Đăng ký navigation trong file `navigation/*`.
8. Nếu dữ liệu cần tồn tại sau process death hoặc dùng lại, lưu vào Room/DataStore.

## Reference Files Worth Reading First
- `app/src/main/java/com/baothanhbin/agridoctorai/navigation/MainNavHost.kt`
- `app/src/main/java/com/baothanhbin/agridoctorai/navigation/AppState.kt`
- `core/data/src/main/java/com/baothanhbin/core/data/di/DataModule.kt`
- `core/network/src/main/java/com/baothanhbin/core/network/NetworkDataSource.kt`
- `feature/login/src/main/java/com/baothanhbin/feature/login/LoginScreen.kt`
- `feature/processimage/src/main/java/com/baothanhbin/feature/processimage/ProcessImageViewModel.kt`
- `feature/chatbot/src/main/java/com/baothanhbin/feature/chatbot/ChatbotViewModel.kt`

## Final Guidance to Future Agents
Nếu phải chọn giữa “sạch hơn theo lý thuyết” và “giống codebase hiện tại”, hãy ưu tiên:

- giống module boundaries hiện tại
- giống naming hiện tại
- giống navigation pattern hiện tại
- giống state/update flow hiện tại

Sau đó mới tối ưu dần, nhưng không phá consistency của repo.
