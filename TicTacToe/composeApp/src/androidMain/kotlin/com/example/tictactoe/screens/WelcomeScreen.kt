package com.example.tictactoe.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictactoe.R
import com.example.tictactoe.model.PlayerConfig
import com.example.tictactoe.model.PlayerType
import kotlinx.coroutines.launch

// ── Exact colors extracted from the screenshot ─────────────────────────────
private val BgPurple     = Color(0xFF7B6EC8)   // medium purple background
private val LogoCircle   = Color(0xFF5A4BA0)   // darker circle behind XO text
private val FieldBg      = Color(0xFFEAE6F8)   // light lavender for all fields
private val FieldText    = Color(0xFF4A4070)   // dark purple text inside fields
private val FieldLabel   = Color(0xFF9B93C4)   // muted purple floating label
private val StartBtnBg   = Color(0xFFFFFFFF)   // white pill button
private val StartBtnText = Color(0xFF7B6EC8)   // purple text on Start button

@Composable
fun WelcomeScreen(
    snackbarHostState: SnackbarHostState,
    onStartGame: (player1Config: PlayerConfig, player2Config: PlayerConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val funNames: List<String> = stringArrayResource(id = R.array.fun_player_names).toList()
    val (defaultP1, defaultP2) = remember(funNames) { pickTwoDifferent(funNames) }

    var player1Name by remember { mutableStateOf(defaultP1) }
    var player2Name by remember { mutableStateOf(defaultP2) }
    var player1Type by remember { mutableStateOf(PlayerType.HUMAN) }
    var player2Type by remember { mutableStateOf(PlayerType.HUMAN) }
    val coroutineScope = rememberCoroutineScope()
    val nameEmptyError = stringResource(id = R.string.error_name_empty)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgPurple)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // ── XO Logo: dark purple circle, white text only ──────────────
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(LogoCircle),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "XO",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "OX",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── "Welcome to Tic-Tac-Toe!" ─────────────────────────────────
            Text(
                text = "Welcome to\nTic-Tac-Toe!",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 46.sp
            )

            Spacer(modifier = Modifier.height(52.dp))

            // ── Two player columns side-by-side ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PlayerColumn(
                    label = "Player 1",
                    name = player1Name,
                    onNameChange = { player1Name = it },
                    playerType = player1Type,
                    onTypeChange = { player1Type = it },
                    namePlaceholder = "Player 1 Name",
                    modifier = Modifier.weight(1f)
                )
                PlayerColumn(
                    label = "Player 2",
                    name = player2Name,
                    onNameChange = { player2Name = it },
                    playerType = player2Type,
                    onTypeChange = { player2Type = it },
                    namePlaceholder = "Player 2 Name",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(44.dp))

            // ── Start! button ─────────────────────────────────────────────
            Button(
                onClick = {
                    if (player1Name.isBlank() || player2Name.isBlank()) {
                        coroutineScope.launch { snackbarHostState.showSnackbar(nameEmptyError) }
                        return@Button
                    }
                    onStartGame(
                        PlayerConfig(name = player1Name.trim(), type = player1Type),
                        PlayerConfig(name = player2Name.trim(), type = player2Type)
                    )
                },
                modifier = Modifier
                    .width(160.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StartBtnBg,
                    contentColor = StartBtnText
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Start!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * One player column: white label, pill dropdown, rounded name field.
 * No card background — fields float directly on the purple screen.
 */
@Composable
private fun PlayerColumn(
    label: String,
    name: String,
    onNameChange: (String) -> Unit,
    playerType: PlayerType,
    onTypeChange: (PlayerType) -> Unit,
    namePlaceholder: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // White heading label
        Text(
            text = label,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Pill-shaped dropdown
        PlayerTypeDropdown(
            selectedType = playerType,
            onTypeChange = onTypeChange,
            modifier = Modifier.fillMaxWidth()
        )

        // Name text field — placeholder avoids the floating-label background artifact
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = namePlaceholder,
                    fontSize = 13.sp,
                    color = FieldLabel
                )
            },
            textStyle = TextStyle(
                color = FieldText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FieldBg,
                unfocusedContainerColor = FieldBg,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = FieldText
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTypeDropdown(
    selectedType: PlayerType,
    onTypeChange: (PlayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = remember {
        listOf(PlayerType.HUMAN, PlayerType.EASY_AI, PlayerType.MEDIUM_AI, PlayerType.HARD_AI)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = playerTypeLabel(selectedType),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(50.dp),   // pill shape
            textStyle = TextStyle(
                color = FieldText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldBg,
                unfocusedContainerColor = FieldBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = FieldText,
                unfocusedTextColor = FieldText,
                focusedTrailingIconColor = FieldText,
                unfocusedTrailingIconColor = FieldText
            ),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = playerTypeLabel(option)) },
                    onClick = {
                        expanded = false
                        onTypeChange(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun playerTypeLabel(type: PlayerType): String = when (type) {
    PlayerType.HUMAN     -> stringResource(id = R.string.type_human)
    PlayerType.EASY_AI   -> stringResource(id = R.string.type_easy_ai)
    PlayerType.MEDIUM_AI -> stringResource(id = R.string.type_medium_ai)
    PlayerType.HARD_AI   -> stringResource(id = R.string.type_hard_ai)
}

private fun pickTwoDifferent(options: List<String>): Pair<String, String> {
    if (options.isEmpty()) return "" to ""
    if (options.size == 1) return options[0] to options[0]
    val first = options.random()
    var second = options.random()
    while (second == first) second = options.random()
    return first to second
}