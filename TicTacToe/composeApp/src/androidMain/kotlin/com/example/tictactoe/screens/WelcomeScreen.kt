package com.example.tictactoe.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tictactoe.R
import com.example.tictactoe.model.PlayerConfig
import com.example.tictactoe.model.PlayerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Welcome screen where both players choose a name and player type before starting a game.
 *
 * @param snackbarHostState Shared snackbar host state for validation messages.
 * @param onStartGame Callback invoked when the user presses Start Game with valid inputs.
 * @param modifier Modifier applied to the root layout.
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
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val nameEmptyError: String = stringResource(id = R.string.error_name_empty)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape: Boolean = maxWidth > maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tictactoe_logo),
                    contentDescription = stringResource(id = R.string.cd_app_logo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(id = R.string.title_welcome))
            }

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PlayerSetupCard(
                        playerNumber = 1,
                        name = player1Name,
                        onNameChange = { player1Name = it },
                        playerType = player1Type,
                        onTypeChange = { player1Type = it },
                        modifier = Modifier.weight(1f)
                    )
                    PlayerSetupCard(
                        playerNumber = 2,
                        name = player2Name,
                        onNameChange = { player2Name = it },
                        playerType = player2Type,
                        onTypeChange = { player2Type = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                PlayerSetupCard(
                    playerNumber = 1,
                    name = player1Name,
                    onNameChange = { player1Name = it },
                    playerType = player1Type,
                    onTypeChange = { player1Type = it },
                    modifier = Modifier.fillMaxWidth()
                )
                PlayerSetupCard(
                    playerNumber = 2,
                    name = player2Name,
                    onNameChange = { player2Name = it },
                    playerType = player2Type,
                    onTypeChange = { player2Type = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            StartButton(
                onClick = {
                    if (player1Name.isBlank() || player2Name.isBlank()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message = nameEmptyError)
                        }
                        return@StartButton
                    }
                    onStartGame(
                        PlayerConfig(name = player1Name.trim(), type = player1Type),
                        PlayerConfig(name = player2Name.trim(), type = player2Type)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Card that captures a player's name and player type.
 *
 * @param playerNumber Player number displayed in the card header.
 * @param name Current player name text.
 * @param onNameChange Callback invoked when the user edits the name.
 * @param playerType Current selected player type.
 * @param onTypeChange Callback invoked when the user selects a different type.
 * @param modifier Modifier applied to the card.
 */
@Composable
fun PlayerSetupCard(
    playerNumber: Int,
    name: String,
    onNameChange: (String) -> Unit,
    playerType: PlayerType,
    onTypeChange: (PlayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val titleRes = if (playerNumber == 1) R.string.label_player_one else R.string.label_player_two

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = stringResource(id = titleRes))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = stringResource(id = R.string.hint_player_name)) }
            )
            PlayerTypeDropdown(
                selectedType = playerType,
                onTypeChange = onTypeChange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Dropdown menu used to select a player type.
 *
 * @param selectedType Currently selected type.
 * @param onTypeChange Callback invoked when a new type is selected.
 * @param modifier Modifier applied to the dropdown container.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTypeDropdown(
    selectedType: PlayerType,
    onTypeChange: (PlayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options: List<PlayerType> = remember {
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
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
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

/**
 * Start game button shown at the bottom of the welcome screen.
 *
 * @param onClick Callback invoked when the user taps the button.
 * @param modifier Modifier applied to the button.
 */
@Composable
fun StartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(onClick = onClick, modifier = modifier) {
        Text(text = stringResource(id = R.string.btn_start_game))
    }
}

@Composable
private fun playerTypeLabel(type: PlayerType): String {
    return when (type) {
        PlayerType.HUMAN -> stringResource(id = R.string.type_human)
        PlayerType.EASY_AI -> stringResource(id = R.string.type_easy_ai)
        PlayerType.MEDIUM_AI -> stringResource(id = R.string.type_medium_ai)
        PlayerType.HARD_AI -> stringResource(id = R.string.type_hard_ai)
    }
}

private fun pickTwoDifferent(options: List<String>): Pair<String, String> {
    if (options.isEmpty()) return "" to ""
    if (options.size == 1) return options[0] to options[0]

    val first = options.random()
    var second = options.random()
    while (second == first) {
        second = options.random()
    }
    return first to second
}

