package com.learning.billbuddy.composables.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.blooddonation.ui.theme.AppTheme
import com.learning.billbuddy.R


@Composable
fun Input(
    label: String,
    value: String,
    placeHolder: String,
    onValueChange: (newVal: String) -> Unit,
    leadingIcon: Int? = null,
    modifier: Modifier = Modifier,
    isPasswordInput: Boolean = false
) {

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {

        Text(
            text = label,
            style = AppTheme.typography.titleSmall,
            color = AppTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = {
                if (leadingIcon != null) {
                    Image(
                        painter = painterResource(leadingIcon),
                        contentDescription = "TextField",
                        colorFilter = ColorFilter.tint(AppTheme.colorScheme.onBackground),
                        modifier = Modifier
                            .padding(horizontal = AppTheme.size.medium)
                            .size(AppTheme.size.large)
                    )
                }
            },
            trailingIcon = {
                if (label == "Password" || label == "Confirm Password") {
                    Image(
                        painter = painterResource(if (passwordVisible) R.drawable.visbility else R.drawable.visibility_off),
                        contentDescription = "Visibility Icon",
                        colorFilter = ColorFilter.tint(AppTheme.colorScheme.onBackground),
                        modifier = Modifier
                            .padding(horizontal = AppTheme.size.medium)
                            .size(AppTheme.size.large)
                            .background(Color.Transparent)
                            .clickable { passwordVisible = !passwordVisible }
                    )
                }
            },
            placeholder = {
                Text(
                    text = placeHolder,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.border
                )
            },
            shape = AppTheme.shape.container,
            textStyle = AppTheme.typography.bodyMedium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                unfocusedTextColor = Color.Gray,
                focusedTextColor = AppTheme.colorScheme.onBackground,
            ),
            visualTransformation = if (isPasswordInput && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .shadow(3.dp, AppTheme.shape.container)
                .clip(AppTheme.shape.container)
                .padding(vertical = 0.dp)
                .background(AppTheme.colorScheme.background)


        )
    }
}

@Preview(showBackground = true)
@Composable
fun InputPreview() {
    AppTheme {
        Input(
            label = "Email",
            value = "",
            placeHolder = "Enter your email",
            onValueChange = { },
            leadingIcon = R.drawable.mail,
            modifier = Modifier.padding(16.dp)
        )
    }
}