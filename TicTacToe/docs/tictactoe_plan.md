## Tic-Tac-Toe Android App — Implementation Plan

This document describes the full architecture and implementation plan for the Tic-Tac-Toe Android app using Kotlin and Jetpack Compose. It is structured around the required sections from the assignment.

---

### 1. PROJECT STRUCTURE

**High-level module layout**

- **Module**: `app`
  - Standard Android application module using Kotlin, Jetpack Compose, and Material 3.

**Package and file structure**

- `app/src/main/java/com/example/tictactoe/`
  - `MainActivity.kt`
    - Single-activity entry point.
    - Sets the app content with `TicTacToeTheme`.
    - Configures system UI status/navigation bar colors.
    - Creates and owns a single `SnackbarHostState`.
    - Hosts `AppScaffold` (Scaffold is created here, outside of NavHost).
    - Provides a `NavHostController` to `AppNavigation` via composition/local parameters.
  - `navigation/`
    - `AppNavigation.kt`
      - Defines route constants for navigation:
        - `const val ROUTE_WELCOME = "welcome"`
        - `const val ROUTE_GAME = "game"`
        - `const val ROUTE_GAME_OVER = "game_over"`
      - Defines argument keys for routes (e.g., player names, types, scores, board state).
      - Provides a `@Composable fun AppNavigation(...)` that sets up a `NavHost`.
      - Encodes/decodes complex nav arguments (player configs, board) into primitive-safe formats (Strings/Ints/Booleans).
  - `scaffold/`
    - `AppScaffold.kt`
      - Contains a single `@Composable fun AppScaffold(...)` that:
        - Owns `Scaffold` (Material 3).
        - Accepts:
          - `navController: NavHostController`
          - `snackbarHostState: SnackbarHostState`
          - `content: @Composable (PaddingValues) -> Unit`
          - Optional `currentRoute: String` and `onBackClick: (() -> Unit)?`
        - Shows/hides `TopAppBar` based on `currentRoute`:
          - Hidden on `ROUTE_WELCOME`
          - Shown with back arrow on `ROUTE_GAME` and `ROUTE_GAME_OVER`
        - Uses `Icons.AutoMirrored.Filled.ArrowBack` for navigation icon.
        - Provides a `SnackbarHost` using the shared `SnackbarHostState`.
        - Applies system bar paddings if needed.
  - `screens/`
    - `WelcomeScreen.kt`
      - Top-level composable: `WelcomeScreen(...)`.
      - Composable breakdown:
        - `WelcomeScreen(...)`
        - `PlayerSetupCard(...)`
        - `PlayerTypeDropdown(...)`
        - `StartButton(...)`
      - No `TopAppBar` (Scaffold will be configured accordingly).
      - Uses `SnackbarHostState` from `AppScaffold` via parameter.
    - `GameScreen.kt`
      - Top-level composable: `GameScreen(...)`.
      - Composable breakdown:
        - `GameScreen(...)`
        - `BoardGrid(...)`
        - `BoardCell(...)`
        - `CurrentPlayerBanner(...)`
      - Contains local state for board, current player, and scores using `remember`.
      - Integrates existing `Board`, `Player`, and AI classes.
      - Uses `SnackbarHostState` for errors.
    - `GameOverScreen.kt`
      - Top-level composable: `GameOverScreen(...)`.
      - Composable breakdown:
        - `GameOverScreen(...)`
        - `ResultHeadline(...)`
        - `ScoreBoard(...)`
        - `FinalBoardDisplay(...)` (reuses `BoardGrid` with disabled cells).
  - `ui/theme/`
    - `Color.kt`
      - Defines the cohesive color palette for the app (primary, secondary, tertiary, surface, background, X-color, O-color, etc.).
    - `Theme.kt`
      - Defines `@Composable fun TicTacToeTheme(...)` wrapping `MaterialTheme`.
      - Sets typography, color scheme, and shapes.
      - Includes system UI controller integration via a `SystemUiSetup` composable or side effect hook.
    - `Type.kt`
      - Defines typography styles (e.g., titles, body text, labels).
  - `model/` (or `domain/`)
    - `Board.kt` (existing or to be created)
      - Represents immutable board state.
    - `Player.kt`
      - Sealed class or hierarchy for Human and AI players.
    - `AiLevel.kt`
      - Enum or sealed interface for AI difficulty (Easy/Medium/Hard).
    - `AiPlayerEasy.kt`, `AiPlayerMedium.kt`, `AiPlayerHard.kt` (if already present).
      - AI move selection logic encapsulated per difficulty.
    - `Move.kt`
      - Simple value class for row/column move representation.
  - `ui/components/` (optional, for reusable composables)
    - `PrimaryButton.kt` (if we factor out a shared button style).
    - `PlayerAvatar.kt` or simple decorative composables if needed.

- `app/src/main/res/`
  - `layout/` (probably minimal/unused due to Compose).
  - `values/`
    - `strings.xml` — all user-facing strings, including:
      - Screen titles.
      - Button labels.
      - Snackbar messages.
      - Player type labels.
    - `colors.xml` — optional if needed for XML/vector references.
  - `drawable/`
    - At least one vector/PNG asset:
      - e.g., `ic_tictactoe_board.xml` or `ic_welcome_illustration.xml`.
      - Optional: X/O icons used in board cells.

---

### 2. NAVIGATION ROUTES

**Route constants**

- In `navigation/AppNavigation.kt`:
  - `const val ROUTE_WELCOME = "welcome"`
  - `const val ROUTE_GAME = "game"`
  - `const val ROUTE_GAME_OVER = "game_over"`

**Navigation arguments (conceptual)**

- **From Welcome → Game (`ROUTE_GAME`)**
  - Arguments:
    - `player1Name: String`
    - `player2Name: String`
    - `player1Type: String` (e.g., "human", "easy_ai", "medium_ai", "hard_ai")
    - `player2Type: String`
    - Optional: round counters initialized to 0 (or managed locally in `GameScreen`).
  - Implementation:
    - Use encoded query parameters or `navArgument` definitions.
    - Example route pattern:
      - `"game?player1Name={p1Name}&player2Name={p2Name}&player1Type={p1Type}&player2Type={p2Type}"`

- **From Game → Game Over (`ROUTE_GAME_OVER`)**
  - Arguments:
    - `winnerName: String?` (empty string or special token to represent tie).
    - `player1Name: String`
    - `player2Name: String`
    - `player1Wins: Int`
    - `player2Wins: Int`
    - `ties: Int`
    - `finalBoard: String` (serialized representation of board, e.g., 9-char string "XOX...").
  - Implementation:
    - Encode board state into a compact string or JSON-safe encoding.
    - Pass via `navController.navigate` with interpolated arguments.

- **From Game Over → Game (`ROUTE_GAME`)**
  - Arguments:
    - Same player configuration as initial game:
      - `player1Name`, `player2Name`, `player1Type`, `player2Type`.
    - Optionally reuse updated scores, or let `GameScreen` manage score state across rounds by being popped back instead of recreated (see behavior below).

**Navigation flow and back stack behavior**

- **Welcome → Game**
  - `WelcomeScreen` calls `onStartGame(player1Config, player2Config)`.
  - AppNavigation:
    - Translates config into route with arguments and navigates to `ROUTE_GAME`.
    - May use `popUpTo(ROUTE_WELCOME)` with `inclusive = false` to retain Welcome for back navigation.

- **Game → Game Over**
  - When a win or tie is detected:
    - `GameScreen` calls `onGameOver(winnerName, p1Wins, p2Wins, ties, finalBoard)`.
    - AppNavigation navigates to `ROUTE_GAME_OVER` with encoded arguments.
    - It does not pop `ROUTE_GAME` so that going back from Game Over (Play again/back button) can return to Game with preserved or reset state as defined.

- **Game Over → Game (Play Again or Back)**
  - `GameOverScreen` offers `onPlayAgain()` and `onNavigateBack()`.
  - Both callbacks:
    - Typically call a shared navigation function that:
      - Pops the Game Over screen: `navController.popBackStack()`.
      - Option 1: Let `GameScreen` reinitialize for a new round (reset board/current player but keep scores).
      - Option 2: Navigate anew to `ROUTE_GAME` with the same configs if a complete fresh instance is needed.
    - Plan: Prefer **pop back to existing GameScreen** and signal it to start a new round by:
      - Maintaining scores as state in `GameScreen`.
      - Resetting board and current player when returning from Game Over (e.g., `LaunchedEffect` on `navBackStackEntry` or use a `rememberSaveable` flag).

- **Back button behavior**
  - **Game top app bar back**:
    - `onNavigateBack()` from `GameScreen`:
      - Pops back to `ROUTE_WELCOME`.
  - **Game Over top app bar back**:
    - Behaves identically to "Play Again":
      - Pops Game Over and returns to `GameScreen`.
  - **System back button**:
    - Follows same semantics as top app bar navigation set via `onBackPressedDispatcher` and NavController integration.

**Scaffold and TopAppBar**

- `MainActivity` owns:
  - `NavHostController`
  - `SnackbarHostState`
  - `AppScaffold`
- `AppScaffold`:
  - Queries the current route from `navController.currentBackStackEntryAsState()`.
  - Conditionally shows a `TopAppBar` for `ROUTE_GAME` and `ROUTE_GAME_OVER` only.
  - Defines an `onBackClick` lambda derived from `currentRoute` (delegates to navigation lambdas wired via `AppNavigation`).

---

### 3. DATA PASSING STRATEGY

**No global/static data (except route constants) and no ViewModel.**

Data flows through:
- Navigation arguments for screen-to-screen communication.
- Lambda callbacks for actions and events.
- Local composable state managed with `remember` / `rememberSaveable`.

**Conceptual data models**

- `data class PlayerConfig(val name: String, val type: PlayerType)`
  - `PlayerType` will be an enum: `HUMAN`, `EASY_AI`, `MEDIUM_AI`, `HARD_AI`.
- `data class GameResult(...)` for internal usage (not necessarily a nav argument type).

**WelcomeScreen parameters**

- `@Composable fun WelcomeScreen(`
  - `snackbarHostState: SnackbarHostState,`
  - `onStartGame: (player1Config: PlayerConfig, player2Config: PlayerConfig) -> Unit`
  - `modifier: Modifier = Modifier`
  - `)`

Behavior:
- On "Start Game":
  - Validates player names.
  - On validation success:
    - Constructs `PlayerConfig` objects for both players.
    - Invokes `onStartGame(player1Config, player2Config)` to request navigation.
- Snackbar usage:
  - Accessed via `snackbarHostState` to show validation errors.

**GameScreen parameters**

- `@Composable fun GameScreen(`
  - `player1Config: PlayerConfig,`
  - `player2Config: PlayerConfig,`
  - `snackbarHostState: SnackbarHostState,`
  - `onGameOver: (winnerName: String?, player1Wins: Int, player2Wins: Int, ties: Int, finalBoard: Board) -> Unit,`
  - `onNavigateBack: () -> Unit,`
  - `modifier: Modifier = Modifier`
  - `)`

Internal state:
- `board: Board` (immutable, replaced on each move).
- `currentPlayer: Player` (or representation like `Boolean` + mapping).
- `isAiTurn: Boolean`.
- Score counters:
  - `player1Wins: Int`
  - `player2Wins: Int`
  - `ties: Int`

Behavior:
- When a game ends:
  - `onGameOver` is invoked with the result and final board.
- When back arrow is tapped:
  - Calls `onNavigateBack()`; AppNavigation decides to `popBackStack()` to welcome.

**GameOverScreen parameters**

- `@Composable fun GameOverScreen(`
  - `winnerName: String?,`
  - `player1Name: String,`
  - `player2Name: String,`
  - `player1Wins: Int,`
  - `player2Wins: Int,`
  - `ties: Int,`
  - `finalBoard: Board,`
  - `onPlayAgain: () -> Unit,`
  - `onNavigateBack: () -> Unit,`
  - `modifier: Modifier = Modifier`
  - `)`

Behavior:
- Displays result message and score summary.
- Renders final board read-only (no clicks).
- "Play Again" button triggers `onPlayAgain()`:
  - Nav controller pops Game Over and returns to Game.
- TopAppBar back button uses `onNavigateBack()`:
  - Mirrors `onPlayAgain()` behavior.

---

### 4. SCREEN SPECIFICATIONS — WELCOME SCREEN

**Layout requirements**

- No `TopAppBar` visible.
- Uses `Column` with `verticalArrangement = SpaceBetween` to accommodate title, player sections, and bottom button in both portrait and landscape.
- Within content:
  - App illustration (optional) at top using `Image(painterResource(...))`.
  - Title / welcome message using `Text` with `MaterialTheme.typography.headlineMedium`.
  - Two `PlayerSetupCard`s stacked vertically or arranged side by side for large widths (responsive via `BoxWithConstraints` or `WindowSizeClass`-style approach using simple threshold).
  - `StartButton` anchored towards the bottom using padding and `Spacer(Modifier.weight(1f))`.

**Random default player names**

- A list of fun names in `WelcomeScreen.kt`:
  - Provided via constants referencing `strings.xml` entries or local `listOf()` of string resources.
  - Example: "Thunder X", "O-Master", "Strategist", etc.
- On `remember`, pick a random name for Player 1 and Player 2 (ensuring they are not identical if possible).

**Validation logic**

- On `StartButton` click:
  - If `player1Name` is blank:
    - Show snackbar message from `strings.xml` (e.g., `"Name cannot be empty"`).
    - Do not navigate.
  - Else if `player2Name` is blank:
    - Same snackbar behavior.
  - Else:
    - Call `onStartGame` with both configs.

**Composable breakdown**

- `WelcomeScreen(...)`
  - Owns local states:
    - `player1Name`, `player2Name`.
    - `player1Type`, `player2Type`.
  - Composes:
    - Title text.
    - `PlayerSetupCard` for each player.
    - `StartButton`.

- `PlayerSetupCard(`
  - `playerNumber: Int,`
  - `name: String,`
  - `onNameChange: (String) -> Unit,`
  - `playerType: PlayerType,`
  - `onTypeChange: (PlayerType) -> Unit`
  - Renders a `Card` with:
    - Label like "Player 1".
    - `TextField` for name.
    - `PlayerTypeDropdown` for type selection.

- `PlayerTypeDropdown(`
  - `selectedType: PlayerType,`
  - `onTypeChange: (PlayerType) -> Unit`
  - Uses `ExposedDropdownMenuBox` and `TextField` for selection.
  - Items:
    - Human
    - Easy AI
    - Medium AI
    - Hard AI

- `StartButton(onClick: () -> Unit)`
  - A primary button filling max width with appropriate padding.

**Landscape handling**

- Wrap content in `BoxWithConstraints`.
- If `maxWidth > maxHeight`:
  - Arrange player sections side by side using `Row`.
  - Place title/illustration at the top and the `StartButton` at the bottom or aligned center.
- Ensure scrollability using `Modifier.verticalScroll(rememberScrollState())` for small screens.

---

### 5. SCREEN SPECIFICATIONS — GAME SCREEN

**Layout (Portrait)**

- `Scaffold` provided by `AppScaffold` with `TopAppBar`:
  - Title: from `strings.xml` (e.g., "Game").
  - Back arrow: `Icons.AutoMirrored.Filled.ArrowBack`.
- `GameScreen` content:
  - `Column` with center alignment.
  - `CurrentPlayerBanner` at the top or bottom:
    - Displays text like "Claude's turn — playing X".
  - `BoardGrid` in the middle with aspect-ratio 1:1 to form a square.
  - Optional small status texts or hints near bottom.

**Layout (Landscape)**

- `Row` as root inside padding:
  - `CurrentPlayerBanner` on left or right.
  - `BoardGrid` centered with `Modifier.weight(1f).aspectRatio(1f)`.
  - Use `Modifier.fillMaxSize()` to center board in available space.

**Board behavior**

- Board representation:
  - `Board` model with:
    - Internal representation like `List<PlayerPiece?>` or `Array<Array<PlayerPiece?>>`.
    - Functions for:
      - `getCell(row, col)`
      - `applyMove(move: Move, piece: Piece): Board` returning a new board.
      - `checkWinner(): WinnerResult?`
      - `isFull(): Boolean`
  - `PlayerPiece` sealed class or enum with:
    - `X`, `O`, `EMPTY`.

- Game loop:
  - When a cell is tapped:
    - If `isAiTurn` is true:
      - Show snackbar (`"Please wait for AI"`).
      - Return early; no state change.
    - If selected cell is already non-empty:
      - Show snackbar error: `"Cell already occupied"`.
      - Return.
    - Else:
      - Create new `Board` by applying move.
      - Update board state.
      - Check win condition:
        - If winner:
          - Increment corresponding player's win counter.
          - Call `onGameOver` with winner data and final board.
        - Else if board full:
          - Increment `ties`.
          - Call `onGameOver` with tie state.
        - Else:
          - Switch current player.
          - If new current player is AI:
            - Set `isAiTurn = true`.

- AI behavior (Easy/Medium/Hard):
  - When `currentPlayer` is AI:
    - `LaunchedEffect(currentPlayer, board)`:
      - If `currentPlayer` is AI and game is not over:
        - `delay(500L..1000L)` (fixed value for simplicity).
        - Call `chooseMove(board)` on the appropriate AI player object.
        - Apply move as above.
        - Set `isAiTurn = false`.
  - AI implementation will rely on existing AI classes; `GameScreen` will own instances of AI players configured via `PlayerConfig.type`.

**State management**

- `remember` / `rememberSaveable` for:
  - `board: Board` (initialized to empty).
  - `currentPlayerIsPlayer1: Boolean`.
  - `isAiTurn: Boolean`.
  - Score counters: `player1Wins`, `player2Wins`, `ties`.

**Composable breakdown**

- `GameScreen(...)`
  - Orchestrates state and high-level UI.
  - Provides callbacks to `BoardGrid` and reads `SnackbarHostState`.

- `BoardGrid(`
  - `board: Board,`
  - `onCellClick: (row: Int, col: Int) -> Unit,`
  - `enabled: Boolean,`
  - `modifier: Modifier = Modifier`
  - Renders a 3x3 grid using `Column` of `Row`s or `LazyVerticalGrid(3)` (but static 3x3 is simple).

- `BoardCell(`
  - `piece: PlayerPiece?,`
  - `onClick: () -> Unit,`
  - `enabled: Boolean,`
  - `modifier: Modifier = Modifier`
  - Visual design:
    - `Surface` or `Button` with:
      - Background color equal to `MaterialTheme.colorScheme.surface`.
      - Border or shadow for visual separation.
      - If `piece == X`:
        - Show an icon or styled "X" in X-color.
      - If `piece == O`:
        - Show an icon or styled "O" in O-color.

- `CurrentPlayerBanner(`
  - `playerName: String,`
  - `piece: PlayerPiece`
  - Displays text like `"playerName's turn — playing X"`.

---

### 6. SCREEN SPECIFICATIONS — GAME OVER SCREEN

**Content**

- Uses `Scaffold` from `AppScaffold` with `TopAppBar` (back arrow acts like Play Again).
- Within `GameOverScreen` content:
  - `ResultHeadline(winnerName)`:
    - If `winnerName` is non-null/non-empty:
      - Show `"[winnerName] wins!"`.
    - Else:
      - Show `"It is a tie!"`.
  - `ScoreBoard(...)`:
    - Displays:
      - `"[Player 1 Name]: X wins"`.
      - `"[Player 2 Name]: Y wins"`.
      - `"Ties: Z"`.
  - `FinalBoardDisplay(board)`:
    - Reuses `BoardGrid`:
      - `enabled = false` so that cells are non-interactive.
  - `Play Again` button:
    - Calls `onPlayAgain()`.

**Back button behavior**

- TopAppBar back arrow:
  - Bound to `onNavigateBack()`, which will call the same logic as `onPlayAgain()` (navigate back to Game).
- System back:
  - Same behavior via NavController configuration.

**Composable breakdown**

- `GameOverScreen(...)`
  - Coordinates UI and callbacks.

- `ResultHeadline(`
  - `winnerName: String?`
  - Decides which result message to show.

- `ScoreBoard(`
  - `player1Name: String,`
  - `player1Wins: Int,`
  - `player2Name: String,`
  - `player2Wins: Int,`
  - `ties: Int`
  - Simple `Column` or `Card` summarizing scores.

- `FinalBoardDisplay(board: Board)`
  - Calls `BoardGrid(board = board, enabled = false, onCellClick = {})`.

---

### 7. GAME LOGIC INTEGRATION

**Existing models**

- `Board` (immutable):
  - Represents a 3x3 grid.
  - Provides:
    - `fun getCell(row: Int, col: Int): PlayerPiece?`
    - `fun applyMove(move: Move, piece: PlayerPiece): Board`
    - `fun checkWinner(): PlayerPiece?` or `WinnerResult`.
    - `fun isFull(): Boolean`.
- `Player` subclasses:
  - Human player representation (mainly used for labeling or type).
  - AI players with a `chooseMove(board: Board): Move` function.

**Game class absence**

- No `Game` class; logic is implemented inside `GameScreen` using:
  - Board states.
  - Player config and mapping to AI/human.
  - Helper functions in `GameScreen.kt` to encapsulate move application and winner checking.

**On cell tap**

- `onCellClick(row, col)`:
  - If `isAiTurn`:
    - Show AI-wait snackbar; return.
  - If cell occupied:
    - Show cell-occupied snackbar; return.
  - Compute `currentPiece` as X or O based on current player.
  - Call `board.applyMove(Move(row, col), currentPiece)` to get new board.
  - Update `board` state.
  - Check:
    - `winner = newBoard.checkWinner()`
    - `isFull = newBoard.isFull()`
  - If `winner != null`:
    - Update the appropriate player's win counter.
    - Call `onGameOver(winnerName, p1Wins, p2Wins, ties, newBoard)`.
  - Else if `isFull`:
    - Increment `ties`.
    - Call `onGameOver(null, p1Wins, p2Wins, ties, newBoard)`.
  - Else:
    - Toggle current player.
    - If new current player type is AI:
      - Set `isAiTurn = true`.

**AI move triggering**

- Use `LaunchedEffect(currentPlayer, board)`:
  - If `currentPlayer` is AI and game is not over:
    - `delay(750L)` (for example).
    - Call `aiPlayer.chooseMove(board)` for the AI corresponding to `currentPlayer`.
    - Apply move using the same logic as human moves.
    - After AI move:
      - If game not over, toggle player and set `isAiTurn = false`.
      - If game over, call `onGameOver`.

**isAiTurn flag**

- Managed by `GameScreen`:
  - Set to `true` when AI is about to make a move (just before `LaunchedEffect` or at the start of AI logic).
  - Set to `false` when AI move is finished and board/current player have been updated.
  - Used by UI to:
    - Disable `BoardGrid` clicks (`enabled = !isAiTurn`).
    - Show error snackbar if user attempts to tap during AI turn (guard inside `onCellClick`).

---

### 8. THEME

**Visual direction**

- Palette choice: **deep navy + amber + off-white**.
  - Primary: deep navy (`#0D1B2A`) used for:
    - TopAppBar background.
    - Primary buttons.
  - OnPrimary: near-white (`#FDFDFD`).
  - Secondary: warm amber (`#FFB703`) used as accents for active elements.
  - Tertiary: teal or soft aqua for subtle highlights.
  - Surface: slightly off-white (`#E0E1DD`) used for the board background.
  - Background: darker navy variant for overall app background.
  - X piece color: amber or bright orange (`#F4A261`).
  - O piece color: teal or sky blue (`#2A9D8F`).

**Implementation details**

- In `Color.kt`:
  - Define `val NavyPrimary`, `val AmberSecondary`, `val OffWhiteSurface`, `val XColor`, `val OColor`, etc.
- In `Theme.kt`:
  - Create a `ColorScheme` via `lightColorScheme(...)`.
  - Define `@Composable fun TicTacToeTheme(...)` that wraps `MaterialTheme`.
  - For now, app will use only a light theme.

**System UI colors (300-level requirement)**

- In `MainActivity`:
  - Call `WindowCompat.setDecorFitsSystemWindows(window, false)` to allow edge-to-edge content.
  - Use `accompanist-systemuicontroller`:
    - Inside `TicTacToeTheme`, provide a `SystemUiSetup`:
      - `val systemUiController = rememberSystemUiController()`.
      - `val useDarkIcons = ...` (true for light theme).
      - `SideEffect`:
        - `systemUiController.setStatusBarColor(color = primaryColor, darkIcons = useDarkIcons)`.
        - `systemUiController.setNavigationBarColor(color = primaryColor, darkIcons = useDarkIcons)`.

---

### 9. BACK BUTTON & APP BAR

**Back arrow icon**

- Use `Icons.AutoMirrored.Filled.ArrowBack`:
  - Import from `androidx.compose.material.icons.automirrored.filled.ArrowBack`.
  - Used in `TopAppBar` for navigation icon.

**TopAppBar visibility**

- `AppScaffold` receives current route:
  - On `ROUTE_WELCOME`:
    - No `TopAppBar` (or height = 0).
  - On `ROUTE_GAME`:
    - Show `TopAppBar` with title from `strings.xml` and back arrow that triggers `onGameBack()`.
  - On `ROUTE_GAME_OVER`:
    - Show `TopAppBar` with title from `strings.xml` and back arrow that triggers `onGameOverBack()` (which behaves like Play Again).

**Callback wiring**

- `MainActivity` or `AppNavigation` provides lambdas for back behavior:
  - For `ROUTE_GAME`:
    - `onBackClick` pops back stack to Welcome.
  - For `ROUTE_GAME_OVER`:
    - `onBackClick` reuses Play Again behavior (pop Game Over, return to Game).

---

### 10. CODE QUALITY REQUIREMENTS

**Documentation**

- Every public function and class will have KDoc:
  - Example:
    - `/** Brief description. @param paramName description @return description */`
  - Focus on explaining:
    - Purpose of screens.
    - Navigation contracts.
    - Game logic integration points.

**Strings**

- All user-visible text will come from `res/values/strings.xml`, including:
  - Screen titles.
  - Button labels ("Start Game", "Play Again", etc.).
  - Snackbar messages ("Name cannot be empty", "Cell already occupied", "Please wait for AI", etc.).
  - Player type options ("Human", "Easy AI", "Medium AI", "Hard AI").
  - Any descriptive or help text.

**No global/static state**

- Only `const val` route and nav-argument key constants allowed, defined inside `navigation/AppNavigation.kt` or companion objects if necessary.
- No singletons storing mutable game data.
- All state is either:
  - Local to composables via `remember`/`rememberSaveable`, or
  - Derived from navigation arguments.

**Duplication reduction**

- Common composables (like `BoardGrid`, `BoardCell`, primary buttons, score summary items) will be extracted to avoid copy-paste.
- Shared layout patterns (like card titles) may be refactored into small reusable composables if they appear in more than one screen.

**Linter and formatting**

- Use:
  - No unused imports.
  - No unused variables.
  - No `TODO` mentions in code or KDoc.
  - Reformat code regularly using IDE’s `Reformat Code`.
  - Avoid magic numbers (e.g., delay durations, paddings) by introducing named constants where appropriate.

**Comments**

- Where logic might be non-obvious to a CS2/3 student:
  - Add concise comments explaining rationale, especially around:
    - AI trigger logic.
    - Board serialization for navigation.
    - Edge-to-edge system UI handling.

---

### 11. IMAGES

**Image requirements**

- Include at least one drawable image:
  - `ic_tictactoe_logo.xml` vector drawable or imported asset:
    - Simple tic-tac-toe grid with X and O.
  - Used on `WelcomeScreen` above the title.

**Usage in Compose**

- Add in `WelcomeScreen`:
  - `Image(painter = painterResource(id = R.drawable.ic_tictactoe_logo), contentDescription = stringResource(id = R.string.cd_app_logo))`
  - `cd_app_logo` is a string resource for accessibility.

**Optional additional images**

- Icons for X and O:
  - Could either use styled text (large "X"/"O") or vector assets:
    - `ic_x_piece.xml`
    - `ic_o_piece.xml`
  - If used, referenced similarly with `painterResource`.

---

### PHASED IMPLEMENTATION ORDER (MAPPED TO REQUIREMENTS)

1. **Set up theme and strings (Step 1)**
   - Create `Color.kt`, `Theme.kt`, and `Type.kt`.
   - Configure `TicTacToeTheme`.
   - Define all necessary strings in `strings.xml`.
2. **Set up navigation and Scaffold (Step 2)**
   - Implement `AppNavigation.kt` with route constants and basic destinations.
   - Implement `AppScaffold.kt` with conditional `TopAppBar`.
   - Wire them together in `MainActivity`.
3. **Build `WelcomeScreen` (Step 3)**
   - Implement layout, player setup controls, validation, and Snackbar usage.
   - Connect `onStartGame` to navigation.
4. **Wire Welcome → Game with models (Step 4)**
   - Define `PlayerConfig` and `PlayerType`.
   - Implement argument passing and decoding for `GameScreen`.
   - Integrate `Board` and `Player` models.
5. **Build static `GameScreen` board UI (Step 5)**
   - Implement `BoardGrid`, `BoardCell`, and `CurrentPlayerBanner` design.
   - No game logic yet; board is static or minimally interactive.
6. **Integrate game logic into `GameScreen` (Step 6)**
   - Set up state, move handling, win detection, and AI turns with delay.
   - Manage `isAiTurn` correctly for user interaction blocking.
7. **Wire Game → Game Over (Step 7)**
   - Add navigation to `GameOverScreen` with required arguments.
   - Implement board serialization and deserialization for nav.
8. **Build `GameOverScreen` (Step 8)**
   - Implement result message, scores, and final board display (read-only).
9. **Wire Play Again navigation (Step 9)**
   - Implement `onPlayAgain` / `onNavigateBack` to return to `GameScreen` and start a new round.
10. **Apply OS status bar coloring (Step 10)**
    - Integrate system UI controller into theme or activity as planned.
11. **Verify AutoMirrored back arrow usage (Step 11)**
    - Ensure all back icons use `Icons.AutoMirrored.Filled.ArrowBack`.
12. **Code quality pass (Step 12)**
    - Add KDoc for all public classes and functions.
    - Ensure all strings are from `strings.xml`.
    - Run linter/inspection; fix warnings.
    - Reformat all code.

