package com.learning.billbuddy.composables.base

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.blooddonation.ui.theme.AppTheme


@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    label: String,
    containerColor: Color? = null,
    contentColor: Color? = null,
    onClick: () -> Unit,
) {
    val localContainerColor = containerColor ?: AppTheme.colorScheme.primary
    val localContentColor = contentColor ?: AppTheme.colorScheme.onPrimary

    Column(modifier = modifier) {
        Button(
            shape = AppTheme.shape.container,
            modifier = Modifier
                .fillMaxWidth(),

            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = localContainerColor,
                contentColor = localContentColor
            ),
        ) {
            Text(text = label, style = AppTheme.typography.bodySmall)
        }
    }

}

@Composable()
fun ContainerButton(
    label: String,
    onSelectedColor: Color,
    defaultColor: Color,
    onClick: () -> Unit,
    height: Dp = 50.dp,
    with: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    var isSelected by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(
                color = if (isSelected) onSelectedColor else defaultColor,
                shape = AppTheme.shape.container
            )
            .height(height)
            .size(with)
            .clickable(onClick = {
                isSelected = !isSelected
                onClick()
            })
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyMedium,
            color = if (isSelected) defaultColor else onSelectedColor,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}

@Composable()
fun HoistedContainerButton(
    label: String,
    onSelectedColor: Color,
    defaultColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    height: Dp = 50.dp,
    with: Dp = 100.dp,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .background(
                color = if (isSelected) onSelectedColor else defaultColor,
                shape = AppTheme.shape.container
            )
            .height(height)
            .size(with)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyMedium,
            color = if (isSelected) defaultColor else onSelectedColor,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}

@Composable()
fun ContainerButtonToggle(
    label: String,
    onSelectedColor: Color,
    defaultColor: Color,
    onClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) onSelectedColor else defaultColor,
                shape = AppTheme.shape.container
            )
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyMedium,
            color = if (isSelected) defaultColor else onSelectedColor,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}

@Composable()
fun CircleButtonToggle(
    label: String? = null,
    icon: Int?,
    size: Dp?,
    onSelectedColor: Color,
    defaultColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSelected by remember { mutableStateOf(false) }

    if (label == null && icon == null) {
        return
    }

    Box(
        modifier = modifier
            .background(
                color = if (isSelected) onSelectedColor else defaultColor,
                shape = AppTheme.shape.button
            )
            .clickable(onClick = {
                isSelected = !isSelected
                onClick()
            })
    ) {
        if (label !== null) {
            Text(
                text = label,
                style = AppTheme.typography.bodySmall,
                color = if (isSelected) defaultColor else onSelectedColor,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        } else {
            if (size == null || icon == null) {
                return
            }
            Image(
                painter = painterResource(icon),
                contentDescription = "Circle Button",
                colorFilter = ColorFilter.tint(if (isSelected) defaultColor else onSelectedColor),
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .padding(AppTheme.size.small)
                    .clickable(onClick = {
                        isSelected = !isSelected
                        onClick()
                    })
            )
        }

    }
}

@Composable()
fun CircleButton(
    label: String? = null,
    icon: Int?,
    size: Dp?,
    isSelected: Boolean = false,
    onSelectedColor: Color,
    defaultColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    if (label == null && icon == null) {
        return
    }

    Box(
        modifier = modifier
            .clip(AppTheme.shape.button)
            .background(AppTheme.colorScheme.third)
            .padding(AppTheme.size.small)
            .clickable(onClick = onClick),
    ) {
        if (label !== null) {
            Text(
                text = label,
                style = AppTheme.typography.bodySmall,
                color = if (isSelected) defaultColor else onSelectedColor,
                modifier = Modifier
            )
        } else {
            if (size == null || icon == null) {
                return@Box
            }
            Image(
                painter = painterResource(icon),
                contentDescription = "Circle Button",
                colorFilter = ColorFilter.tint(AppTheme.colorScheme.primary),
                modifier = Modifier
                    .size(size)
            )
        }
    }

//    Button(
//        modifier = modifier.size(size ?: 50.dp),
//        shape = CircleShape,
//        onClick = onClick,
//        colors = ButtonDefaults.buttonColors(
//            contentColor = defaultColor,
//            containerColor = Color.Transparent
//        )
//
//    ) {
//        if (label !== null) {
//            Text(
//                text = label,
//                style = AppTheme.typography.bodySmall,
//                color = if (isSelected) defaultColor else onSelectedColor,
//                modifier = Modifier
//            )
//        } else {
//            if (size == null || icon == null) {
//                return@Button
//            }
//            Image(
//                painter = painterResource(icon),
//                contentDescription = "Circle Button",
//                colorFilter = ColorFilter.tint(AppTheme.colorScheme.primary),
//                modifier = Modifier
//                    .size(size)
//                    .clip(CircleShape)
//                    .padding(AppTheme.size.small)
//                    .clickable(onClick = {
//                        onClick()
//                    })
//            )
//        }

//    }
}


@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
fun PrimaryButtonPreview() {
    AppTheme {
        PrimaryButton(label = "Primary Button", onClick = {})
    }
}
