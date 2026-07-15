package com.dollyplastic.invoiceapp.ui.screens.masters.party

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dollyplastic.invoiceapp.ui.common.dialogs.ConfirmDetailDialog
import com.dollyplastic.invoiceapp.ui.common.dialogs.DetailSection
import com.dollyplastic.invoiceapp.ui.common.dialogs.DetailItem
import com.dollyplastic.invoiceapp.ui.screens.masters.party.PartyViewModel.PartyUiEvent
import com.dollyplastic.invoiceapp.ui.common.TextFields.AuthLabel
import com.dollyplastic.invoiceapp.ui.common.TextFields.AuthTextField
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyFormScreen(
    navController: NavController,
    partyId: String?,
    viewModel: PartyViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(partyId) {
        partyId?.let { viewModel.loadPartyForEdit(it) }
    }
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PartyUiEvent.ShowErrorDialog -> {
                    showErrorDialog = event.message
                }
                PartyUiEvent.ShowConfirmDialog -> {
                    showConfirmDialog = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PartyUiEvent.ShowErrorDialog -> {
                    showErrorDialog = event.message
                }
                PartyUiEvent.ShowConfirmDialog -> {
                    showConfirmDialog = true
                }
            }
        }
    }



    Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally // Center the profile icon
    ) {
        Spacer(modifier=Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier
                    .padding(8.dp)
                    .size(18.dp)
                    .clickable {
                        navController.popBackStack()
                    }
            )
            Text(
                if (partyId == null) "Add New Client" else "Edit Client",
                style = AppText.H2.copy(fontSize =20.sp)
            )
            Icon(
                Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp),
                tint=Color.Transparent

            )
        }

        Spacer(modifier=Modifier.height(45.dp))
            
            // Profile Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AppColors.PrimaryBlue,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

        Spacer(modifier=Modifier.height(16.dp))

            // Trade Name
            PartyFormField(
                label = "Client Name *",
                value = state.tradeName,
                onValueChange = { viewModel.onFieldChange("tradeName", it) },
                onBlur = { viewModel.onFieldBlur("tradeName") },
                placeholder = "Enter Trade Name",
                icon = Icons.Default.Person,
                error = state.errors["tradeName"]
            )

            // GSTIN
            PartyFormField(
                label = "GSTIN *",
                value = state.gstin,
                onValueChange = { viewModel.onGstinChange(it) },
                onBlur = { viewModel.onFieldBlur("gstin") },
                placeholder = "GST Identification Number",
                icon = Icons.Default.Receipt,
                error = state.errors["gstin"]
            )

            // Nick Name
            PartyFormField(
                label = "Nick Name (Optional)",
                value = state.nickName,
                onValueChange = { viewModel.onFieldChange("nickName", it) },
                onBlur = { viewModel.onFieldBlur("nickName") },
                placeholder = "Short internal name",
                icon = Icons.Default.Face
            )
            


            // Address Line 1
            PartyFormField(
                label = "Address Line 1 *",
                value = state.addressLine1,
                onValueChange = { viewModel.onFieldChange("addressLine1", it) },
                onBlur = { viewModel.onFieldBlur("addressLine1") },
                placeholder = "Street address",
                icon = Icons.Default.Home,
                error = state.errors["addressLine1"]
            )

            // Address Line 2
            PartyFormField(
                label = "Address Line 2",
                value = state.addressLine2 ?: "",
                onValueChange = { viewModel.onFieldChange("addressLine2", it) },
                onBlur = { viewModel.onFieldBlur("addressLine2") },
                placeholder = "Apartment, suite, etc.",
                icon = Icons.Default.Business
            )

            // City
            PartyFormField(
                label = "City *",
                value = state.city,
                onValueChange = { viewModel.onFieldChange("city", it) },
                onBlur = { viewModel.onFieldBlur("city") },
                placeholder = "City",
                icon = Icons.Default.LocationCity,
                error = state.errors["city"]
            )

            // State
            PartyFormField(
                label = "State *",
                value = state.state,
                onValueChange = { }, // State logic
                onBlur = { viewModel.onFieldBlur("state") },
                placeholder = "State",
                icon = Icons.Default.Map,
                error = state.errors["state"]
            )
            
            // State Code
            PartyFormField(
                label = "State Code",
                value = state.stateCode,
                onValueChange = {},
                placeholder = "Code",
                icon = Icons.Default.Numbers
            )

            // Pincode
            PartyFormField(
                label = "Pincode *",
                value = state.pincode,
                onValueChange = { viewModel.onFieldChange("pincode", it) },
                onBlur = { viewModel.onFieldBlur("pincode") },
                placeholder = "ZIP/Postal Code",
                icon = Icons.Default.PinDrop,
                error = state.errors["pincode"],
                trailingContent = {
                    if (state.isLoadingDistances) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )

            
                state.unknownPincodes.forEach { group ->
                    PartyFormField(
                        label = "Distance to ${group.pincode} (Km)",
                        value = group.distance,
                        onValueChange = { viewModel.onDistanceChange(group.pincode, it) },
                        placeholder = "Distance in Km",
                        icon = Icons.Default.DirectionsCar
                    )
                }

            Spacer(modifier = Modifier.height(24.dp))
            
            val isDistanceValid = !state.isLoadingDistances && state.unknownPincodes.all { 
                it.distance.isNotBlank() && (it.distance.toIntOrNull() ?: 0) > 0 
            }

            val isModified by viewModel.isModified.collectAsState()
            
            Button(
                onClick = { viewModel.requestSaveConfirmation() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = isFormValid && isDistanceValid && !state.isLoadingDistances && isModified,
                colors = ButtonColors(
                    containerColor = AppColors.PrimaryBlue,
                    contentColor = Color.White,
                    disabledContainerColor = AppColors.PrimaryBlue.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text(if (partyId == null) "Add Client" else "Update Client")
            }
            Spacer(modifier = Modifier.height(24.dp))

    }
    
    val confirmSections = listOf(
        DetailSection(
            title = "General Info",
            items = listOf(
                DetailItem("Trade Name", state.tradeName),
                DetailItem("Nick Name", state.nickName.ifBlank { "-" }),
                DetailItem("GSTIN", state.gstin)
            )
        ),
        DetailSection(
            title = "Address",
            items = listOf(
                DetailItem("Address", "${state.addressLine1}${if (!state.addressLine2.isNullOrBlank()) "\n${state.addressLine2}" else ""}"),
                DetailItem("City/State", "${state.city}, ${state.state} - ${state.pincode}")
            )
        )
    )

    if (showConfirmDialog) {
        ConfirmDetailDialog(
            title = "Confirm Client Details",
            description = "Please review the details below before saving.",
            sections = confirmSections,
            icon = Icons.Default.Person,
            onConfirm = {
                showConfirmDialog = false
                viewModel.saveParty {
                    navController.popBackStack()
                }
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }

    showErrorDialog?.let { message ->
        com.dollyplastic.invoiceapp.ui.common.dialogs.ValidationErrorDialog(
            errors = listOf(message),
            onDismiss = { showErrorDialog = null }
        )
    }
}

@Composable
fun PartyFormField(
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit, 
    onBlur: (() -> Unit)? = null,
    placeholder: String, 
    icon: ImageVector, 
    error: String? = null, 
    trailingContent: @Composable (() -> Unit)? = null
) {
    var wasFocused by remember { mutableStateOf(false) }
    
    Column(Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        AuthLabel(label)
        AuthTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            leadingIcon = icon,
            trailingContent = trailingContent,
            modifier = Modifier.onFocusChanged { focusState ->
                if (wasFocused && !focusState.isFocused) {
                    onBlur?.invoke()
                }
                wasFocused = focusState.isFocused
            }
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}
