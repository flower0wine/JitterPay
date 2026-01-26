package com.example.jitterpay.constants

/**
 * Navigation constants for bottom navigation bar tabs
 */
object NavigationTabs {
    const val HOME = "Home"
    const val STATS = "Stats"
    const val DATA = "DATA"
    const val GOALS = "Goals"
    const val PROFILE = "Profile"
}

/**
 * Navigation routes for Navigation Compose
 */
object NavigationRoutes {
    const val HOME = "home"
    const val STATS = "stats"
    const val GOALS = "goals"
    const val PROFILE = "profile"
    const val ADD_TRANSACTION = "add_transaction"
    const val SEARCH = "search"
    const val AVATAR_SELECTION = "avatar_selection"
    const val ADD_GOAL = "add_goal"
    const val GOAL_DETAIL = "goal_detail/{goalId}"
    const val ADD_FUNDS = "add_funds/{goalId}"
    const val WITHDRAW_FUNDS = "withdraw_funds/{goalId}"
    const val EDIT_GOAL = "edit_goal/{goalId}"
    
    fun goalDetail(goalId: Long): String = "goal_detail/$goalId"
    fun addFunds(goalId: Long): String = "add_funds/$goalId"
    fun withdrawFunds(goalId: Long): String = "withdraw_funds/$goalId"
    fun editGoal(goalId: Long): String = "edit_goal/$goalId"
}

/**
 * Content descriptions for accessibility
 */
object ContentDescriptions {
    const val ADD_BUTTON = "Add"
}
