package com.baothanhbin.agridoctorai.navigation

import com.baothanhbin.agridoctorai.resources.R
import androidx.compose.ui.res.stringResource

enum class TopLevelDestination(
    val label: Int,
    val selectedIcon: Int,
    val unselectedIcon: Int,
)
{
    HOME(
        label = R.string.home,
        selectedIcon = R.drawable.ic_selected_home,
        unselectedIcon = R.drawable.ic_unselected_home
    ),
    DIAGNOSE(
        label = R.string.diagnose,
        selectedIcon = R.drawable.ic_selected_diagnose,
        unselectedIcon = R.drawable.ic_unselected_diagnose
    ),
    MY_PLANTS(
        label = R.string.my_plants,
        selectedIcon = R.drawable.ic_selected_plant,
        unselectedIcon = R.drawable.ic_unselected_plant
    ),
    CHATBOT(
        label = R.string.chatbot,
        selectedIcon = R.drawable.ic_selected_chatbot,
        unselectedIcon = R.drawable.ic_unselected_chatbot
    ),
}
