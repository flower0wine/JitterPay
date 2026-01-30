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
    const val RECURRING = "recurring"
    const val ADD_RECURRING = "add_recurring"
    const val RECURRING_DETAIL = "recurring_detail/{recurringId}"
    const val EDIT_TRANSACTION = "edit_transaction/{transactionId}"
    const val BUDGET = "budget"
    const val ADD_BUDGET = "add_budget"
    const val EDIT_BUDGET = "edit_budget/{budgetId}"
    
    fun goalDetail(goalId: Long): String = "goal_detail/$goalId"
    fun addFunds(goalId: Long): String = "add_funds/$goalId"
    fun withdrawFunds(goalId: Long): String = "withdraw_funds/$goalId"
    fun editGoal(goalId: Long): String = "edit_goal/$goalId"
    fun recurringDetail(recurringId: Long): String = "recurring_detail/$recurringId"
    fun editTransaction(transactionId: Long): String = "edit_transaction/$transactionId"
    fun editBudget(budgetId: Long): String = "edit_budget/$budgetId"
}

/**
 * Content descriptions for accessibility
 */
object ContentDescriptions {
    const val ADD_BUTTON = "Add"
}

/**
 * Splash screen constants
 */
object SplashConstants {
    const val ANIMATION_FILE_NAME = "splash_animation"
    const val TIMEOUT_MILLIS: Long = 2000L // 2 seconds as requested
}
