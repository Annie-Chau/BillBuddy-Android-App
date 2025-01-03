package com.example.blooddonation.composables.register.login_signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.blooddonation.ui.theme.AppTheme
import com.learning.billbuddy.R
import com.learning.billbuddy.composables.base.Input
import com.learning.billbuddy.composables.base.PrimaryButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Signup(
    modifier: Modifier = Modifier,
    onSignup: (email: String, password: String) -> Unit,
    onNavigateSignInScreen: () -> Unit = {}
) {
//    val snackbarHostState = LocalSnackbarHostState.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var email by remember { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
//    var role by rememberSaveable { mutableStateOf(Role.DEFAULT) }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
//    val roles = listOf(Role.DEFAULT.name, Role.DONOR.name, Role.ADMIN.name)


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
                topString = "Welcome.",
                bottomString = "Create an account",
                modifier = Modifier
            )

            Spacer(modifier = Modifier.height(AppTheme.size.normal))

            Input(
                label = "Email",
                value = email,
                placeHolder = "Enter email",
                onValueChange = { email = it },
                leadingIcon = R.drawable.mail
            )

            Input(
                label = "Username",
                value = email,
                placeHolder = "Enter username",
                onValueChange = { email = it },
                leadingIcon = R.drawable.username
            )

            Input(
                label = "Password",
                value = password,
                placeHolder = "Enter password",
                onValueChange = { password = it },
                leadingIcon = R.drawable.lock,
                isPasswordInput = true
            )

            Input(
                label = "Confirm Password",
                value = confirmPassword,
                placeHolder = "Confirm password",
                onValueChange = { confirmPassword = it },
                leadingIcon = R.drawable.lock,
                isPasswordInput = true
            )

            Spacer(modifier = Modifier.height(AppTheme.size.large))

            PrimaryButton(
                label = "Signup",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    keyboardController?.hide()
                    CoroutineScope(Dispatchers.IO).launch {
                        onSignup(email, password)
                    }
                })

            Spacer(modifier = Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(
                    "Already have an account?", color = AppTheme.colorScheme.onBackground,
                    style = AppTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = AppTheme.size.small)
                )
                Text(
                    "Sign in",
                    color = AppTheme.colorScheme.primary,
                    style = AppTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(start = AppTheme.size.small)
                        .clickable(onClick = onNavigateSignInScreen)
                )
            }

        }
    }
}

@Preview
@Composable
fun SignupPreview() {
    AppTheme {
        Signup(onSignup = { email, password -> })
    }
}