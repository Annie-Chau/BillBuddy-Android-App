package com.learning.billbuddy.models.compose

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth


data class SignInResult (
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val id: String,
    val email: String?,
    val photoUrl: String?,
    val name: String?
)

class UserViewModel : ViewModel() {
    var currentUser by mutableStateOf<UserData?>(null)

    fun setCurrentUser(user: FirebaseUser) {
        Log.d("UserViewModel", "setCurrentUser: ${user.displayName}")
        Log.d("UserViewModel", "setCurrentUser: ${user.uid}")
        Log.d("UserViewModel", "setCurrentUser: ${user.email}")
        Log.d("UserViewModel", "setCurrentUser: ${user.photoUrl}")

        currentUser = UserData(
            id = user.uid,
            email = user.email,
            photoUrl = user.photoUrl.toString() ?: "",
            name = user.displayName ?: ""
        )
    }
}