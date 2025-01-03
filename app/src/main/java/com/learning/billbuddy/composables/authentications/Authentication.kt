package com.learning.billbuddy.composables.authentications

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.blooddonation.composables.register.login_signup.Login
import com.example.blooddonation.composables.register.login_signup.Signup
import com.learning.billbuddy.composables.layout.SlideInLayout


@Composable
fun Authentication(modifier: Modifier = Modifier) {
    var isDisplaySignUpScreen by remember { mutableStateOf(false) }


    Box(modifier = modifier) {
        Login(onNavigateToSignUpScreen = {
            isDisplaySignUpScreen = true
        }) { email, password ->

        }

        SlideInLayout(flag = isDisplaySignUpScreen) {
            Signup(onSignup = { email, password ->
                // Handle signup
            }, onNavigateSignInScreen = {
                isDisplaySignUpScreen = false
            })
        }
    }
}

