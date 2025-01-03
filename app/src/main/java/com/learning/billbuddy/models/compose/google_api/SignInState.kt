package com.learning.billbuddy.models.compose.google_api

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)