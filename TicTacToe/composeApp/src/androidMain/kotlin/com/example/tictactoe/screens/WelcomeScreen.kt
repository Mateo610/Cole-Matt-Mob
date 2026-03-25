package com.example.tictactoe.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import com.example.tictactoe.ui.theme.TicTacToeColors
import kotlinx.coroutines.launch

/**
 * First screen: player setup (names and types) and navigation into a new match.
 *
 * There is no top app bar on this route; the scaffold omits it by design.
 *
 * @param snackbarHostState Host used to show validation errors (empty names).
 * @param onStartGame Invoked with both player configs when input is valid.
 * @param modifier Optional modifier for the root of this screen.
 */
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
            .background(TicTacToeColors.WelcomeBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(TicTacToeColors.WelcomeLogoCircle),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tictactoe_logo),
                    contentDescription = stringResource(id = R.string.cd_app_logo),
                    modifier = Modifier
                        .size(72.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.welcome_title),
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 46.sp
            )

            Spacer(modifier = Modifier.height(52.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PlayerColumn(
                    label = stringResource(id = R.string.label_player_one),
                    name = player1Name,
                    onNameChange = { player1Name = it },
                    playerType = player1Type,
                    onTypeChange = { player1Type = it },
                    namePlaceholder = stringResource(id = R.string.hint_player_one_name),
                    modifier = Modifier.weight(1f)
                )
                PlayerColumn(
                    label = stringResource(id = R.string.label_player_two),
                    name = player2Name,
                    onNameChange = { player2Name = it },
                    playerType = player2Type,
                    onTypeChange = { player2Type = it },
                    namePlaceholder = stringResource(id = R.string.hint_player_two_name),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(44.dp))

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
                    containerColor = Color.White,
                    contentColor = TicTacToeColors.WelcomeStartButtonLabel
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.btn_start_game),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * One player column: label, type dropdown, and name field styled for the welcome screen.
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
        Text(
            text = label,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        PlayerTypeDropdown(
            selectedType = playerType,
            onTypeChange = onTypeChange,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = namePlaceholder,
                    fontSize = 13.sp,
                    color = TicTacToeColors.WelcomeFieldLabel
                )
            },
            textStyle = TextStyle(
                color = TicTacToeColors.WelcomeFieldText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TicTacToeColors.WelcomeFieldBackground,
                unfocusedContainerColor = TicTacToeColors.WelcomeFieldBackground,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = TicTacToeColors.WelcomeFieldText
            )
        )
    }
}

/**
 * Pill-shaped dropdown listing all [PlayerType] options.
 *
 * @param selectedType Currently selected type for this player.
 * @param onTypeChange Called when the user picks a new type.
 * @param modifier Optional modifier for the menu anchor.
 */
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
            shape = RoundedCornerShape(50.dp),
            textStyle = TextStyle(
                color = TicTacToeColors.WelcomeFieldText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = TicTacToeColors.WelcomeFieldBackground,
                unfocusedContainerColor = TicTacToeColors.WelcomeFieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = TicTacToeColors.WelcomeFieldText,
                unfocusedTextColor = TicTacToeColors.WelcomeFieldText,
                focusedTrailingIconColor = TicTacToeColors.WelcomeFieldText,
                unfocusedTrailingIconColor = TicTacToeColors.WelcomeFieldText
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
    PlayerType.HUMAN -> stringResource(id = R.string.type_human)
    PlayerType.EASY_AI -> stringResource(id = R.string.type_easy_ai)
    PlayerType.MEDIUM_AI -> stringResource(id = R.string.type_medium_ai)
    PlayerType.HARD_AI -> stringResource(id = R.string.type_hard_ai)
}

private fun pickTwoDifferent(options: List<String>): Pair<String, String> {
    if (options.isEmpty()) return "" to ""
    if (options.size == 1) return options[0] to options[0]
    val first = options.random()
    var second = options.random()
    while (second == first) second = options.random()
    return first to second
}
