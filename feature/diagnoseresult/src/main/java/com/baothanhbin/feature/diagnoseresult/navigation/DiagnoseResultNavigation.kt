package com.baothanhbin.feature.diagnoseresult.navigation

import android.net.Uri
import android.util.Base64
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.baothanhbin.core.model.ApiType
import com.baothanhbin.core.model.ClassifyData
import com.baothanhbin.core.ui.util.LocationStateHolder
import com.baothanhbin.feature.diagnoseresult.DiagnoseResultRoute
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val DIAGNOSE_RESULT_ROUTE = "DIAGNOSE_RESULT_ROUTE"
private const val ARG_IMAGE_URI = "imageUri"
private const val ARG_API_TYPE = "apiType"
private const val ARG_CLASSIFY_DATA = "classifyData"
private const val DIAGNOSE_RESULT_ROUTE_WITH_ARG = "$DIAGNOSE_RESULT_ROUTE/{$ARG_IMAGE_URI}?$ARG_API_TYPE={$ARG_API_TYPE}&$ARG_CLASSIFY_DATA={$ARG_CLASSIFY_DATA}"

fun NavController.navigateToDiagnoseResult(
    imageUri: Uri? = null,
    apiType: ApiType = ApiType.DETECT,
    classifyData: ClassifyData? = null,
    navOptions: NavOptions? = null
) {
    val apiTypeString = if (apiType == ApiType.CLASSIFY) "CLASSIFY" else "DETECT"
    // Serialize ClassifyData to JSON and encode as Base64 for safe URL transmission
    val classifyDataJson = classifyData?.let {
        try {
            val json = Json { ignoreUnknownKeys = true }
            val jsonString = json.encodeToString(ClassifyData.serializer(), it)
            Base64.encodeToString(jsonString.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    } ?: ""
    
    val route = if (imageUri != null) {
        val encoded = Uri.encode(imageUri.toString())
        "$DIAGNOSE_RESULT_ROUTE/$encoded?$ARG_API_TYPE=$apiTypeString&$ARG_CLASSIFY_DATA=$classifyDataJson"
    } else {
        "$DIAGNOSE_RESULT_ROUTE?$ARG_API_TYPE=$apiTypeString&$ARG_CLASSIFY_DATA=$classifyDataJson"
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.diagnoseResultScreen(
    navController: NavHostController? = null,
    locationStateHolder: LocationStateHolder
) {
    // Route with imageUri argument
    composable(
        route = DIAGNOSE_RESULT_ROUTE_WITH_ARG,
        arguments = listOf(
            navArgument(ARG_IMAGE_URI) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(ARG_API_TYPE) {
                type = NavType.StringType
                defaultValue = "DETECT"
            },
            navArgument(ARG_CLASSIFY_DATA) {
                type = NavType.StringType
                nullable = true
            }
        )
    ) {
        val uriString = it.arguments?.getString(ARG_IMAGE_URI)
        val imageUri = uriString?.let { s -> Uri.parse(Uri.decode(s)) }
        val apiTypeString = it.arguments?.getString(ARG_API_TYPE) ?: "DETECT"
        val apiType = when (apiTypeString) {
            "CLASSIFY" -> ApiType.CLASSIFY
            else -> ApiType.DETECT
        }
        // Deserialize ClassifyData from Base64 JSON
        val classifyDataJson = it.arguments?.getString(ARG_CLASSIFY_DATA)
        val classifyData = classifyDataJson?.let { jsonBase64 ->
            try {
                val jsonString = String(Base64.decode(jsonBase64, Base64.URL_SAFE or Base64.NO_WRAP))
                val json = Json { ignoreUnknownKeys = true }
                json.decodeFromString(ClassifyData.serializer(), jsonString)
            } catch (e: Exception) {
                null
            }
        }
        DiagnoseResultRoute(
            navController = navController,
            imageUri = imageUri,
            locationStateHolder = locationStateHolder,
            apiType = apiType,
            classifyData = classifyData
        )
    }
    // Route without argument (for backward compatibility)
    composable(
        route = "$DIAGNOSE_RESULT_ROUTE?$ARG_API_TYPE={$ARG_API_TYPE}&$ARG_CLASSIFY_DATA={$ARG_CLASSIFY_DATA}",
        arguments = listOf(
            navArgument(ARG_API_TYPE) {
                type = NavType.StringType
                defaultValue = "DETECT"
            },
            navArgument(ARG_CLASSIFY_DATA) {
                type = NavType.StringType
                nullable = true
            }
        )
    ) {
        val apiTypeString = it.arguments?.getString(ARG_API_TYPE) ?: "DETECT"
        val apiType = when (apiTypeString) {
            "CLASSIFY" -> ApiType.CLASSIFY
            else -> ApiType.DETECT
        }
        // Deserialize ClassifyData from Base64 JSON
        val classifyDataJson = it.arguments?.getString(ARG_CLASSIFY_DATA)
        val classifyData = classifyDataJson?.let { jsonBase64 ->
            try {
                val jsonString = String(Base64.decode(jsonBase64, Base64.URL_SAFE or Base64.NO_WRAP))
                val json = Json { ignoreUnknownKeys = true }
                json.decodeFromString(ClassifyData.serializer(), jsonString)
            } catch (e: Exception) {
                null
            }
        }
        DiagnoseResultRoute(
            navController = navController,
            imageUri = null,
            locationStateHolder = locationStateHolder,
            apiType = apiType,
            classifyData = classifyData
        )
    }
}
