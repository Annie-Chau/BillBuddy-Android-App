package com.learning.billbuddy.models.compose.google_api

data class SignInResult (
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)