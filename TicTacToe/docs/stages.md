# Tic-Tac-Toe Android App — Stage Implementation Plan

> **Rule:** Do not begin a new stage until the current stage compiles, runs, and its
> goals are fully verified. Each stage builds on the last.

---

## STAGE 1 — Foundation
**Goal:** App launches without crashing. Theme applied. Navigation compiles. No UI yet.

### Deliverables

#### 1.1 — Theme
- `ui/theme/Color.kt`
  - Define all named colors:
    - `NavyPrimary = Color(0xFF0D1B2A)`
    - `AmberSecondary = Color(0xFFFFB703)`
    - `OffWhiteSurface = Color(0xFFE0E1DD)`
    - `TealTertiary = Color(0xFF2A9D8F)`
    - `XColor = Color(0xFFF4A261)`
    - `OColor = Color(0xFF2A9D8F)`
    - Background, OnPrimary, OnSurface, etc.
- `ui/theme/Type.kt`
  - Define typography scale (headlineMedium, titleLarge, bodyMedium, labelSmall)
- `ui/theme/Theme.kt`
  - `TicTacToeTheme` wrapping `MaterialTheme` with the above colors and typography
  - Light color scheme only via `lightColorScheme(...)`

#### 1.2 — String Resources
- `res/values/strings.xml` — define ALL user-facing strings up front:
  - Screen titles: `app_name`, `title_welcome`, `title_game`, `title_game_over`
  - Button labels: `btn_start_game`, `btn_play_again`
  - Player setup: `label_player_one`, `label_player_two`, `hint_player_name`
  - Player types: `type_human`, `type_easy_ai`, `type_medium_ai`, `type_hard_ai`
  - Snackbar messages: `error_name_empty`, `error_cell_occupied`, `error_ai_thinking`
  - Result messages: `result_wins`, `result_tie`
  - Score labels: `label_wins`, `label_ties`
  - Content descriptions: `cd_app_logo`, `cd_back_button`, `cd_board_cell`

#### 1.3 — Data Models
- `model/PlayerType.kt`
  - `enum class PlayerType { HUMAN, EASY_AI, MEDIUM_AI, HARD_AI }`
- `model/PlayerConfig.kt`
  - `data class PlayerConfig(val name: String, val type: PlayerType)`
- `model/PlayerPiece.kt`
  - `enum class PlayerPiece { X, O }`
- `model/Move.kt`
  - `data class Move(val row: Int, val col: Int)`
- Verify or create `model/Board.kt`:
  - Immutable board representation
  - `fun getCell(row: Int, col: Int): PlayerPiece?`
  - `fun applyMove(move: Move, piece: PlayerPiece): Board`
  - `fun checkWinner(): PlayerPiece?`
  - `fun isFull(): Boolean`
  - `fun serialize(): String` — 9-char string, `X`, `O`, `-` per cell, left-to-right top-to-bottom
  - `companion object { fun deserialize(s: String): Board }`
- Verify or create AI player classes with `fun chooseMove(board: Board): Move`

#### 1.4 — Navigation
- `navigation/AppNavigation.kt`
  - Route constants:
    ```kotlin
    const val ROUTE_WELCOME = "welcome"
    const val ROUTE_GAME = "game"
    const val ROUTE_GAME_OVER = "game_over"
    ```
  - Nav argument key constants:
    ```kotlin
    const val ARG_P1_NAME = "p1Name"
    const val ARG_P2_NAME = "p2Name"
    const val ARG_P1_TYPE = "p1Type"
    const val ARG_P2_TYPE = "p2Type"
    const val ARG_WINNER_NAME = "winnerName"
    const val ARG_P1_WINS = "p1Wins"
    const val ARG_P2_WINS = "p2Wins"
    const val ARG_TIES = "ties"
    const val ARG_FINAL_BOARD = "finalBoard"
    ```
  - `@Composable fun AppNavigation(navController, snackbarHostState)`
    - `NavHost` with 3 stub destinations (empty `Box` placeholders for now)
    - Route patterns with argument definitions using `navArgument`

#### 1.5 — Scaffold + MainActivity
- `scaffold/AppScaffold.kt`
  - `@Composable fun AppScaffold(navController, snackbarHostState, content)`
  - Reads `navController.currentBackStackEntryAsState()` to get current route
  - Conditionally renders `TopAppBar`:
    - Hidden on `ROUTE_WELCOME`
    - Shown with `Icons.AutoMirrored.Filled.ArrowBack` on `ROUTE_GAME` and `ROUTE_GAME_OVER`
  - `SnackbarHost(snackbarHostState)` always present
  - Accepts `onBackClick: (() -> Unit)?` as parameter for back arrow action
- `MainActivity.kt`
  - `enableEdgeToEdge()` called before `setContent`
  - `WindowCompat.setDecorFitsSystemWindows(window, false)`
  - Creates `rememberNavController()`
  - Creates `remember { SnackbarHostState() }`
  - Wraps everything in `TicTacToeTheme`
  - `AppScaffold` is created HERE, OUTSIDE `NavHost`
  - `AppNavigation` nested inside `AppScaffold` content lambda

### ✅ Stage 1 Complete When:
- App launches to a blank/stub screen without crashing
- Theme colors are visibly applied
- Navigation compiles with 3 routes defined
- Scaffold renders with correct conditional TopAppBar logic
- No linter errors

---

## STAGE 2 — Welcome Screen
**Goal:** Welcome screen is fully functional. Validates input. Navigates to a stub game screen.

### Deliverables

#### 2.1 — Drawable Asset
- Add `res/drawable/ic_tictactoe_logo.xml` (vector drawable)
  - Simple tic-tac-toe grid illustration or X/O icon
  - Used at top of WelcomeScreen

#### 2.2 — WelcomeScreen Composables
- `screens/WelcomeScreen.kt`

  - **`WelcomeScreen(snackbarHostState, onStartGame, modifier)`**
    - Local state:
      - `player1Name: String` — initialized to a random name from a hardcoded list
      - `player2Name: String` — different random name
      - `player1Type: PlayerType` — default `HUMAN`
      - `player2Type: PlayerType` — default `HUMAN`
    - Handles `BoxWithConstraints` for portrait vs landscape:
      - Portrait: `Column` stacking title → P1 card → P2 card → button
      - Landscape: title on top, `Row` with P1 card | P2 card side by side, button below
    - Wraps content in `Modifier.verticalScroll(rememberScrollState())` for small screens
    - On Start Game click:
      - If either name is blank: `snackbarHostState.showSnackbar(errorNameEmpty)`
      - Else: call `onStartGame(PlayerConfig(p1Name, p1Type), PlayerConfig(p2Name, p2Type))`

  - **`PlayerSetupCard(playerNumber, name, onNameChange, playerType, onTypeChange, modifier)`**
    - Renders a `Card` containing:
      - Label: "Player 1" or "Player 2" from strings
      - `OutlinedTextField` for name input
      - `PlayerTypeDropdown` for type selection

  - **`PlayerTypeDropdown(selectedType, onTypeChange, modifier)`**
    - `ExposedDropdownMenuBox` with `ExposedDropdownMenu`
    - Four items: Human, Easy AI, Medium AI, Hard AI (all from `strings.xml`)
    - Maps selection back to `PlayerType` enum

  - **`StartButton(onClick, modifier)`**
    - `Button` filling max width
    - Label from `strings.xml`

#### 2.3 — Navigation Wiring
- In `AppNavigation.kt`, replace Welcome stub with real `WelcomeScreen`
- `onStartGame` lambda:
  - Encodes player configs into route string
  - Calls `navController.navigate("game?p1Name=...&p2Name=...&p1Type=...&p2Type=...")`
- Game destination remains a stub `Box` with a `Text("Game Screen Coming")`

### ✅ Stage 2 Complete When:
- Welcome screen renders correctly in portrait and landscape
- Random names populate on launch
- Dropdowns work for all 4 player types
- Empty name shows snackbar, does not navigate
- Valid input navigates to stub game screen
- App logo image displays on welcome screen
- No linter errors

---

## STAGE 3 — Game Screen UI (Static)
**Goal:** Board renders beautifully in portrait and landscape. No game logic yet.

### Deliverables

#### 3.1 — Game Screen Composables (UI only)
- `screens/GameScreen.kt`

  - **`GameScreen(player1Config, player2Config, snackbarHostState, onGameOver, onNavigateBack, modifier)`**
    - Placeholder state: hardcoded empty board, Player 1 as current player
    - Uses `BoxWithConstraints` to switch layout:
      - Portrait: `Column` → `CurrentPlayerBanner` → `BoardGrid`
      - Landscape: `Row` → `CurrentPlayerBanner` on side → `BoardGrid` centered
    - `onNavigateBack` wired to TopAppBar back arrow via `AppScaffold`

  - **`BoardGrid(board, onCellClick, enabled, modifier)`**
    - 3x3 grid using nested `Column` + `Row` or `LazyVerticalGrid(columns = Fixed(3))`
    - `Modifier.aspectRatio(1f)` to keep board square
    - Renders a `BoardCell` for each of the 9 positions

  - **`BoardCell(piece, onClick, enabled, modifier)`**
    - `Button` or `Surface` with:
      - Background = `MaterialTheme.colorScheme.surface` when empty (blends in)
      - Visible border/outline for cell separation
      - When `piece == X`: show styled "X" text or icon in `XColor`
      - When `piece == O`: show styled "O" text or icon in `OColor`
      - `Modifier.aspectRatio(1f)` to keep cells square
      - Disabled appearance when `enabled = false`

  - **`CurrentPlayerBanner(playerName, piece, modifier)`**
    - `Text` composable: e.g. "Claude's turn — playing X"
    - Distinct styling (card or highlighted row)
    - In landscape mode, this sits to the left or right of the board

#### 3.2 — TopAppBar Wiring for Game Screen
- In `AppScaffold`, when route is `ROUTE_GAME`:
  - TopAppBar title = `stringResource(R.string.title_game)`
  - Back arrow click triggers `onBackClick` lambda
- In `AppNavigation`, wire Game screen's `onNavigateBack` to:
  - `navController.popBackStack(ROUTE_WELCOME, inclusive = false)`

### ✅ Stage 3 Complete When:
- Board renders as a clean 3x3 grid, cells square, in both orientations
- Empty cells blend into background, pieces display in correct colors
- `CurrentPlayerBanner` shows correct player info
- Back arrow returns to Welcome screen
- Landscape layout correctly places banner beside the board
- No game logic runs yet (clicking does nothing or logs)
- No linter errors

---

## STAGE 4 — Game Logic
**Goal:** Full game is playable. Human vs Human works. AI turns work with delay.

### Deliverables

#### 4.1 — State Setup in GameScreen
Replace placeholder state with real game state using `remember`:
```kotlin
var board by remember { mutableStateOf(Board.empty()) }
var currentPlayerIsP1 by remember { mutableStateOf(true) }
var isAiTurn by remember { mutableStateOf(false) }
var player1Wins by remember { mutableStateOf(0) }
var player2Wins by remember { mutableStateOf(0) }
var ties by remember { mutableStateOf(0) }
var gameOver by remember { mutableStateOf(false) }
```
- Derive current player config: `if (currentPlayerIsP1) player1Config else player2Config`
- Derive current piece: `if (currentPlayerIsP1) PlayerPiece.X else PlayerPiece.O`

#### 4.2 — Human Move Handler
`fun onCellClick(row: Int, col: Int)`:
1. If `isAiTurn || gameOver` → `showSnackbar(errorAiThinking)`, return
2. If `board.getCell(row, col) != null` → `showSnackbar(errorCellOccupied)`, return
3. `val newBoard = board.applyMove(Move(row, col), currentPiece)`
4. `board = newBoard`
5. Check `newBoard.checkWinner()`:
   - Not null → increment winner's wins, set `gameOver = true`, call `onGameOver(..., newBoard)`
6. Check `newBoard.isFull()`:
   - True → increment `ties`, set `gameOver = true`, call `onGameOver(null, ..., newBoard)`
7. Else:
   - Toggle `currentPlayerIsP1`
   - If new current player type is AI → set `isAiTurn = true`

#### 4.3 — AI Move Trigger
```kotlin
LaunchedEffect(isAiTurn) {
    if (isAiTurn && !gameOver) {
        delay(750L)
        val aiMove = getAiPlayer(currentConfig.type).chooseMove(board)
        // Apply move using same logic as onCellClick but skip isAiTurn check
        isAiTurn = false
    }
}
```
- Create AI player instances based on `PlayerConfig.type`
- Apply AI move using shared move-application logic (extract to a private helper function to avoid duplication between human and AI paths)

#### 4.4 — isAiTurn UI blocking
- Pass `enabled = !isAiTurn && !gameOver` to `BoardGrid`
- Guard inside `onCellClick` for safety (belt and suspenders)

#### 4.5 — Navigation to Game Over
- When `onGameOver` is called:
  - Serialize `finalBoard` via `board.serialize()`
  - Navigate to:
    ```
    game_over?winnerName={name}&p1Name={...}&p2Name={...}&p1Wins={n}&p2Wins={n}&ties={n}&finalBoard={serialized}
    ```
  - `winnerName` = winner's display name, or `""` for tie
  - Use `navController.navigate(...)` — do NOT pop the game screen

### ✅ Stage 4 Complete When:
- Human vs Human full game works end-to-end
- Win and tie detection work correctly
- AI takes its turn after ~750ms delay
- Tapping during AI delay shows snackbar
- Tapping occupied cell shows snackbar
- Game navigates to stub GameOver screen on completion
- Scores increment correctly across turns
- No linter errors

---

## STAGE 5 — Game Over Screen
**Goal:** Complete game loop works end-to-end. Play Again returns to game.

### Deliverables

#### 5.1 — Game Over Screen Composables
- `screens/GameOverScreen.kt`

  - **`GameOverScreen(winnerName, player1Name, player2Name, player1Wins, player2Wins, ties, finalBoard, onPlayAgain, onNavigateBack, modifier)`**
    - `Column` layout centered vertically and horizontally
    - Renders: `ResultHeadline` → `ScoreBoard` → `FinalBoardDisplay` → Play Again button

  - **`ResultHeadline(winnerName: String?, modifier)`**
    - If `winnerName` non-null and non-blank:
      - `"${winnerName} wins!"` from `strings.xml` template
    - Else:
      - `stringResource(R.string.result_tie)`
    - Large, prominent typography (`headlineLarge`)

  - **`ScoreBoard(player1Name, player1Wins, player2Name, player2Wins, ties, modifier)`**
    - `Card` with a `Column`:
      - `"${player1Name}: ${player1Wins} wins"`
      - `"${player2Name}: ${player2Wins} wins"`
      - `"Ties: ${ties}"`

  - **`FinalBoardDisplay(board: Board, modifier)`**
    - Calls `BoardGrid(board = board, onCellClick = {}, enabled = false, modifier)`
    - Read-only — cells are non-interactive, pieces fully visible

  - Play Again `Button`:
    - Label from `strings.xml`
    - Calls `onPlayAgain()`

#### 5.2 — Navigation Wiring for Game Over
- In `AppNavigation.kt`:
  - Game Over destination:
    - Decode all nav arguments
    - Deserialize `finalBoard` via `Board.deserialize(serialized)`
    - Pass all to `GameOverScreen`
  - `onPlayAgain` lambda:
    - `navController.popBackStack()` — pops Game Over, returns to existing Game screen
  - `onNavigateBack` lambda (TopAppBar back):
    - Same as `onPlayAgain`

#### 5.3 — New Round Handling in GameScreen
- When GameScreen returns from Game Over (popped back):
  - Reset `board`, `currentPlayerIsP1`, `isAiTurn`, `gameOver` to initial values
  - Scores (`player1Wins`, `player2Wins`, `ties`) are preserved in GameScreen state
  - Use `LaunchedEffect(Unit)` with a `newRoundKey` or check for a `savedStateHandle` 
    result from GameOver to trigger reset cleanly

#### 5.4 — TopAppBar for Game Over
- In `AppScaffold`, when route is `ROUTE_GAME_OVER`:
  - Title = `stringResource(R.string.title_game_over)`
  - Back arrow click = `onBackClick` → mirrors `onPlayAgain`

### ✅ Stage 5 Complete When:
- Game Over screen shows correct winner or tie message
- Scores display correctly
- Final board renders read-only with all pieces visible
- Play Again returns to GameScreen and starts a fresh round
- TopAppBar back arrow on Game Over behaves identically to Play Again
- Scores persist correctly across multiple rounds
- Full end-to-end loop works: Welcome → Game → Game Over → Game → Game Over
- No linter errors

---

## STAGE 6 — Polish + 300-Level Requirements
**Goal:** Submission-ready. All rubric points covered. Zero linter warnings.

### Deliverables

#### 6.1 — OS Status Bar Coloring (300-level +4 pts)
- In `MainActivity.onCreate()`, BEFORE `setContent`:
  ```kotlin
  enableEdgeToEdge()
  WindowCompat.setDecorFitsSystemWindows(window, false)
  ```
- Inside `TicTacToeTheme` or `MainActivity` content:
  ```kotlin
  val primary = MaterialTheme.colorScheme.primary
  SideEffect {
      val controller = WindowCompat.getInsetsController(window, view)
      controller.isAppearanceLightStatusBars = false
      window.statusBarColor = primary.toArgb()
      window.navigationBarColor = primary.toArgb()
  }
  ```
- Verify status bar and nav bar are colored with `NavyPrimary` on all screens

#### 6.2 — AutoMirrored Back Arrow (300-level +4 pts)
- Verify every `TopAppBar` uses:
  ```kotlin
  Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
      contentDescription = stringResource(R.string.cd_back_button)
  )
  ```
- Check this is correct on both `ROUTE_GAME` and `ROUTE_GAME_OVER`

#### 6.3 — KDoc Pass (code quality 15 pts)
Add KDoc to every public function and every class:
- All screen composables (`WelcomeScreen`, `GameScreen`, `GameOverScreen`)
- All sub-composables (`PlayerSetupCard`, `BoardGrid`, `BoardCell`, etc.)
- All model classes (`Board`, `PlayerConfig`, `PlayerType`, `Move`, `PlayerPiece`)
- `AppNavigation`, `AppScaffold`
- All AI player classes
- Format:
  ```kotlin
  /**
   * Brief one-line description.
   *
   * @param paramName Description of the parameter.
   * @return Description of the return value.
   */
  ```

#### 6.4 — Strings Audit
- Run a search for all hardcoded strings in `.kt` files
- Move every user-visible string to `strings.xml`
- Check: snackbar messages, button labels, dropdown items, result text, score labels

#### 6.5 — Inline Comments Pass
Add comments for anything a CS2/3 student would need explained:
- AI trigger logic via `LaunchedEffect`
- Board serialization/deserialization for nav args
- `isAiTurn` flag management
- Edge-to-edge system UI handling
- Why `Scaffold` is outside `NavHost`
- Why `remember` (not `rememberSaveable`) is used for board state

#### 6.6 — Linter + Formatting Pass
- Run `Code > Reformat Code` on every file
- Run `Code > Optimize Imports` on every file
- Run Android Studio's `Analyze > Inspect Code`
- Fix every warning:
  - No unused imports
  - No unused variables or parameters
  - No magic numbers (extract to named constants)
  - No TODOs anywhere in code or KDoc
- Pass lint: `./gradlew lint` — zero errors

#### 6.7 — Final Visual Polish (extra +5 pts)
- Ensure consistent padding/spacing across all screens
- Board cells have satisfying size and touch targets
- Transitions between screens feel smooth
- AI "thinking" state has a visual indicator (e.g. subtle text "AI is thinking...")
- Verify all screens look good on:
  - Small phone portrait
  - Small phone landscape
  - Large phone portrait

### ✅ Stage 6 Complete When:
- Status bar and nav bar are app primary color on all screens
- AutoMirrored back arrow used everywhere
- Every public function and class has KDoc
- Zero hardcoded user-visible strings
- Inline comments present for non-obvious logic
- `./gradlew lint` passes with zero errors
- `Inspect Code` shows zero meaningful warnings
- No TODOs anywhere
- App looks polished and professional on all tested screen sizes

---

## Summary Checklist

| Stage | Description | Key Milestone |
|-------|-------------|---------------|
| 1 | Foundation | App launches, theme + nav compile |
| 2 | Welcome Screen | Input, validation, navigation work |
| 3 | Game Screen UI | Board renders in both orientations |
| 4 | Game Logic | Full game playable, AI works |
| 5 | Game Over Screen | Complete loop end-to-end |
| 6 | Polish + 300-level | Submission ready, zero lint errors |

---

## Key Rules (Never Violate)

- No global or static mutable variables — only `const val` constants
- No ViewModel
- `Scaffold` created once in `MainActivity`, outside `NavHost`
- Each screen in its own file
- All screens work in portrait AND landscape
- No hardcoded user-visible strings
- No TODOs in final submission
- All public functions and classes have KDoc
- `./gradlew lint` passes clean