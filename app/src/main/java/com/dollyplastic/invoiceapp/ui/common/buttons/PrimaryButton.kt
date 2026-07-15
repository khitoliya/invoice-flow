package com.dollyplastic.invoiceapp.ui.common.buttons

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppRadius
import com.dollyplastic.invoiceapp.ui.theme.AppText


@Composable
fun PrimaryButton(
    text: String,
    loading: Boolean,
    enabled: Boolean = true,
    textStyle: TextStyle=AppText.Button,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.PrimaryBlue
        ),
        shape = RoundedCornerShape(AppRadius.Lg),
        elevation = ButtonDefaults.buttonElevation(2.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = Color.White,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text,
                style = textStyle,
                color = AppColors.PrimaryForeground
            )
        }
    }
}

