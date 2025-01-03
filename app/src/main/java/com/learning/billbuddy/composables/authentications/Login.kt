package com.example.blooddonation.composables.register.login_signup

import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.blooddonation.ui.theme.AppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.learning.billbuddy.R
import com.learning.billbuddy.composables.base.Input
import com.learning.billbuddy.composables.base.PrimaryButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun Login(
    modifier: Modifier = Modifier,
    onNavigateToSignUpScreen: () -> Unit = {},
    onLogin: (email: String, password: String) -> Unit,
    onSignInByGoogle: () -> Unit = {}
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.size.normal),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(AppTheme.colorScheme.background)
                .fillMaxSize()
                .padding(AppTheme.size.medium),
        ) {


            WelcomeHeading(
                topString = "Hello.",
                bottomString = "Welcome back!",
                modifier = Modifier
            )

            Spacer(modifier = Modifier.height(AppTheme.size.medium))

            Input(
                label = "Email",
                value = email,
                placeHolder = "Enter your email",
                onValueChange = { email = it },
                leadingIcon = R.drawable.mail
            )

            Input(
                label = "Password",
                value = password,
                placeHolder = "Enter your password",
                onValueChange = { password = it },
                leadingIcon = R.drawable.lock,
                isPasswordInput = true
            )

            Spacer(modifier = Modifier.height(50.dp))

            PrimaryButton(
                label = "Login",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    keyboardController?.hide()
                    onLogin(email, password)
                })

            Text(
                "OR SIGN IN WITH",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colorScheme.border
            )

            Button(
                onClick = onSignInByGoogle,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp), // Remove default padding
                modifier = Modifier.size(20.dp) // Ensure the button size matches the icon size
            ) {
                Image(
                    painter = painterResource(R.drawable.google_icon),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(20.dp) // Match the size of the button
                )
            }


            Spacer(modifier = Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(
                    "Don't have an account?", color = AppTheme.colorScheme.onBackground,
                    style = AppTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = AppTheme.size.small)
                )
                Text(
                    "Sign up",
                    color = AppTheme.colorScheme.primary,
                    style = AppTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(start = AppTheme.size.small)
                        .clickable(onClick = onNavigateToSignUpScreen)
                )
            }
        }
    }
}

@Composable
fun WelcomeHeading(topString: String, bottomString: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.size.medium)) {
        Text(
            "Bill Buddy",
            style = AppTheme.typography.heading,
            color = AppTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(AppTheme.size.medium))

        Text(
            text = topString,
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = bottomString,
            style = AppTheme.typography.bodyExtraLarge,
            color = AppTheme.colorScheme.border,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Preview
@Composable
fun LoginPreview() {
    AppTheme {
        Login(onLogin = { email, password -> })
    }
}