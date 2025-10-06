package com.halime.checkers.controller;

import com.halime.checkers.controller.ai.CheckersAI;
import com.halime.checkers.model.Board;
import com.halime.checkers.model.Move;
import com.halime.checkers.model.Piece;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.scene.shape.StrokeType;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class GameController {

    @FXML private GridPane boardGrid;
    @FXML private Label turnLabel;

    @FXML private AnchorPane rootPane;


    @FXML private Button playAgainButton;



    private Board board;
    private Piece selectedPiece;
    private boolean isRedTurn = true;
    private final List<Piece> allPieces = new ArrayList<>();
    private Piece activeJumpingPiece = null;
    private CheckersAI aiPlayer;
    private int aiDepth;

    public void setAiDepth(int depth) {
        this.aiDepth = depth;
        this.aiPlayer = new CheckersAI(depth); // rebuild AI with new depth
    }
    @FXML
    public void initialize() {

        // Clear any existing constraints (in case SceneBuilder added some)
        boardGrid.getRowConstraints().clear();
        boardGrid.getColumnConstraints().clear();
        // Hide at the start

        playAgainButton.setVisible(false);
        // Add exactly 8 row and 8 column constraints
        for (int i = 0; i < 8; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / 8); // 1/8th of height
            boardGrid.getRowConstraints().add(rc);

            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 8); // 1/8th of width
            boardGrid.getColumnConstraints().add(cc);
        }

        board = new Board();
        board.setupInitialBoard();
        selectedPiece = null;
        isRedTurn = true;

        drawBoard();
        updateTurnLabel();
        // Only initialize aiPlayer if it wasn’t already set by setAiDepth
        if (aiPlayer == null) {
            aiPlayer = new CheckersAI(aiDepth);
        }


    }

    @FXML
    private void restartGame() {
        // Hide game over UI

        playAgainButton.setVisible(false);

        gameOver = false; // reset game over flag

        // Re-enable the board
        boardGrid.setDisable(false);


        // Reset your board here
        drawBoard(); // or your restart logic
    }

    private void drawBoard() {
        boardGrid.getChildren().clear();
        allPieces.clear();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane cell = new StackPane();

                Rectangle tile = new Rectangle();
                // Bind to the cell (not boardGrid)
                tile.widthProperty().bind(cell.widthProperty());
                tile.heightProperty().bind(cell.heightProperty());
                tile.setFill((row + col) % 2 == 0 ? Color.web("#ffffff") : Color.web("#96a2b3"));

                cell.getChildren().add(tile);

                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    allPieces.add(piece);

                    String imagePath;
                    if (piece.isBigShot()) {
                        imagePath = piece.isRed() ? "/com/halime/checkers/images/red_bigshot.png"
                                : "/com/halime/checkers/images/blue_bigshot.png";
                    } else if (piece.isKing()) {
                        imagePath = piece.isRed() ? "/com/halime/checkers/images/red_king.png"
                                : "/com/halime/checkers/images/blue_king.png";
                    } else {
                        imagePath = piece.isRed() ? "/com/halime/checkers/images/red_piece.png"
                                : "/com/halime/checkers/images/blue_piece.png";
                    }

                    ImageView pieceView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));

                    // Bind to the cell, not boardGrid
                    pieceView.fitWidthProperty().bind(cell.widthProperty().multiply(0.8));
                    pieceView.fitHeightProperty().bind(cell.heightProperty().multiply(0.8));
                    pieceView.setPreserveRatio(true);

                    cell.getChildren().add(pieceView);
                }

                final int x = row;
                final int y = col;
                cell.setOnMouseClicked(e -> handleClick(x, y));
                boardGrid.add(cell, col, row);
            }
        }

        if (selectedPiece != null) {
            highlightValidMoves();
        }
    }




    private void handleClick(int x, int y) {
        Piece clickedPiece = board.getPiece(x, y);

        // --- A) Clicked an empty tile while a piece is selected ---
        // GameController.java (inside handleClick)
        if (selectedPiece != null && clickedPiece == null) {
            List<int[]> validMoves = getValidMoves(selectedPiece);

            for (int[] mv : validMoves) {
                if (mv[0] == x && mv[1] == y) {
                    // Build a Move object for this click
                    Move move = new Move(selectedPiece.getRow(), selectedPiece.getCol(), x, y);

                    // If this is a jump, attach the captured piece
                    int dx = x - selectedPiece.getRow();
                    int dy = y - selectedPiece.getCol();
                    boolean isJump = Math.abs(dx) == 2 && Math.abs(dy) == 2;
                    if (isJump) {
                        int midX = selectedPiece.getRow() + dx / 2;
                        int midY = selectedPiece.getCol() + dy / 2;
                        Piece middlePiece = board.getPiece(midX, midY);
                        if (middlePiece != null && middlePiece.isRed() != selectedPiece.isRed()) {
                            move.addCapturedPiece(middlePiece);
                        }
                    }

                    // Run the unified pipeline (same path AI uses)
                    executeMove(move, /*isAiMove=*/false);
                    return;
                }
            }

            // Not a valid destination -> just deselect
            selectedPiece = null;
            drawBoard();
            return;
        }


        // --- B) Big Shot transfer: only when clicking an ADJACENT same-color piece ---
        // ---- Replace BigShot-transfer block with this ----
        if (selectedPiece != null && selectedPiece.isBigShot() && clickedPiece != null
                && clickedPiece.isRed() == selectedPiece.isRed()) {

            int dx = clickedPiece.getRow() - selectedPiece.getRow();
            int dy = clickedPiece.getCol() - selectedPiece.getCol();

            if (Math.abs(dx) == 1 && Math.abs(dy) == 1) {
                // Perform transfer/swap of BigShot / King flags (keep positions unchanged)
                if (clickedPiece.isKing()) {
                    // selected (Big Shot) becomes a King (not Big Shot)
                    selectedPiece.setBigShot(false);
                    selectedPiece.setKing(true);

                    // target King becomes the Big Shot (not King)
                    clickedPiece.setBigShot(true);
                    clickedPiece.setKing(false);
                } else {
                    // Target is a regular piece → simple transfer
                    selectedPiece.setBigShot(false);
                    clickedPiece.setBigShot(true);
                }

                // --- NEW: if the piece that just became BigShot is on the back rank, it's an instant win ---
                if (clickedPiece.isBigShot()) {
                    boolean onBackRank = (clickedPiece.isRed() && clickedPiece.getRow() == 0)
                            || (!clickedPiece.isRed() && clickedPiece.getRow() == 7);
                    if (onBackRank) {
                        updateBoardUI();
                        endGame(clickedPiece.isRed(),
                                (clickedPiece.isRed() ? "Red" : "Blue") + " Big Shot reached back row by transfer!");
                        return; // stop further processing
                    }
                }

                // Normal flow: deselect, switch turn, redraw and (if appropriate) trigger AI
                selectedPiece = null;
                switchTurn();
                drawBoard();

                // If the next player is AI, trigger it
                if (!isRedTurn) {
                    triggerAI();
                }
                return;
            }

            // Not adjacent → treat like normal selection toggle
            if (clickedPiece == selectedPiece) {
                selectedPiece = null;
            } else {
                selectedPiece = clickedPiece;
            }
            drawBoard();
            return;
        }



        // --- C) Normal select/deselect (non-Big Shot or enemy piece click) ---
        if (clickedPiece != null && clickedPiece.isRed() == isRedTurn) {
            if (selectedPiece == clickedPiece) {
                selectedPiece = null; // deselect same piece
            } else {
                selectedPiece = clickedPiece; // select/switch
            }
            drawBoard();
            return;
        }

        // Optional: clicking an enemy piece while something is selected → deselect
        if (selectedPiece != null && clickedPiece != null && clickedPiece.isRed() != isRedTurn) {
            selectedPiece = null;
            drawBoard();
        }
    }




    private void triggerAI() {
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(ev -> {
            Move aiMove = aiPlayer.getBestMove(board, aiDepth, false); // already enforces mandatory capture
            if (aiMove != null) {
                executeMove(aiMove, true);
            }
        });
        pause.play();
    }





    public void regenerateBigShot(boolean isRed) {
        int startRow = isRed ? 7 : 0;
        int step = isRed ? -1 : 1;

        // Check up to 8 rows (from back row to front)
        for (int row = startRow; row >= 0 && row <= 7; row += step) {
            for (int col = 0; col < 8; col++) {
                Piece candidate = board.getPiece(row, col);
                boolean isDarkSquare = (row + col) % 2 != 0;

                if (candidate != null &&
                        candidate.isRed() == isRed &&
                        !candidate.isBigShot() &&
                        isDarkSquare) {

                    boolean wasKing = candidate.isKing();

                    // Make candidate the Big Shot; ensure no piece is both King + Big Shot
                    candidate.setBigShot(true);
                    if (candidate.isKing()) candidate.setKing(false);

                    // Instant-win ONLY for the edge case you reported:
                    // The respawn target WAS a King, it's on the back row, and it's the last friendly piece.
                    if (wasKing) {
                        boolean onBackRow = (isRed && row == 0) || (!isRed && row == 7);

                        int count = 0;
                        for (int r = 0; r < 8; r++) {
                            for (int c = 0; c < 8; c++) {
                                Piece p = board.getPiece(r, c);
                                if (p != null && p.isRed() == isRed) count++;
                            }
                        }

                        if (onBackRow && count == 1) {
                            updateBoardUI();
                            endGame(isRed, (isRed ? "Red" : "Blue") +
                                    " wins! Big Shot respawned on back row as the last piece.");
                            return;
                        }
                    }

                    System.out.println((isRed ? "Red" : "Blue") +
                            " Big Shot regenerated at (" + row + "," + col + ")");
                    return;
                }
            }
        }

        System.out.println("No eligible piece found on board to convert into Big Shot.");
    }




    private void highlightValidMoves() {
        removeHighlights();

        if (selectedPiece == null) return;

        List<int[]> moves = getValidMoves(selectedPiece);

        for (int[] move : moves) {
            for (Node node : boardGrid.getChildren()) {
                if (GridPane.getRowIndex(node) == move[0] &&
                        GridPane.getColumnIndex(node) == move[1]) {

                    if (node instanceof StackPane) {
                        StackPane cell = (StackPane) node;

                        Rectangle rect = new Rectangle();
                        rect.setFill(Color.color(0.5, 1.0, 0.5, 0.25)); // light green transparent fill
                        rect.setStroke(Color.LIMEGREEN);
                        rect.setStrokeWidth(3);
                        rect.setStrokeType(StrokeType.INSIDE);
                        rect.setMouseTransparent(true);

                        rect.widthProperty().bind(cell.widthProperty());
                        rect.heightProperty().bind(cell.heightProperty());

                        //  Pulsing stroke opacity (safe inside box)
                        FadeTransition pulse = new FadeTransition(Duration.seconds(1.2), rect);
                        pulse.setFromValue(0.3); // faint
                        pulse.setToValue(1.0);   // strong
                        pulse.setAutoReverse(true);
                        pulse.setCycleCount(Animation.INDEFINITE);
                        pulse.play();

                        cell.getChildren().add(rect);
                    }
                }
            }
        }
    }



    private List<int[]> getValidMoves(Piece piece) {
        if (piece.isBigShot()) {
            return getValidMovesForBigShot(piece);
        }
        return piece.isKing() ? getValidMovesForKing(piece) : getValidMovesForRegular(piece);
    }

    private List<int[]> getValidMovesForRegular(Piece piece) {
        List<int[]> moves = new ArrayList<>();
        int row = piece.getRow();
        int col = piece.getCol();

        // Red moves "up" (row decreasing), Black moves "down" (row increasing)
        int direction = piece.isRed() ? -1 : 1;

        // Only forward diagonals
        int[][] steps = {{direction, -1}, {direction, 1}};
        int[][] jumps = {{direction * 2, -2}, {direction * 2, 2}};

        for (int i = 0; i < steps.length; i++) {
            int newRow = row + steps[i][0];
            int newCol = col + steps[i][1];

            // Normal move
            if (isInBounds(newRow, newCol) && board.getPiece(newRow, newCol) == null) {
                moves.add(new int[]{newRow, newCol});
            }

            // Capture move
            int jumpRow = row + jumps[i][0];
            int jumpCol = col + jumps[i][1];
            if (isInBounds(jumpRow, jumpCol) && board.getPiece(jumpRow, jumpCol) == null) {
                int midRow = row + steps[i][0];
                int midCol = col + steps[i][1];
                Piece mid = board.getPiece(midRow, midCol);
                if (mid != null && mid.isRed() != piece.isRed()) {
                    moves.add(new int[]{jumpRow, jumpCol});
                }
            }
        }

        return moves;
    }

    private List<int[]> getValidMovesForKing(Piece piece) {
        List<int[]> moves = new ArrayList<>();
        int row = piece.getRow();
        int col = piece.getCol();
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (isInBounds(newRow, newCol) && board.getPiece(newRow, newCol) == null) {
                moves.add(new int[]{newRow, newCol});
            }

            int jumpRow = row + dir[0] * 2;
            int jumpCol = col + dir[1] * 2;
            if (isInBounds(jumpRow, jumpCol) && board.getPiece(jumpRow, jumpCol) == null) {
                Piece mid = board.getPiece(row + dir[0], col + dir[1]);
                if (mid != null && mid.isRed() != piece.isRed()) {
                    moves.add(new int[]{jumpRow, jumpCol});
                }
            }
        }

        return moves;
    }
    private List<int[]> getValidMovesForBigShot(Piece piece) {
        List<int[]> moves = new ArrayList<>();
        int row = piece.getRow();
        int col = piece.getCol();
        boolean isRed = piece.isRed();

        // All diagonal directions
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            // 1. Move to empty space
            if (isInBounds(newRow, newCol) && board.getPiece(newRow, newCol) == null) {
                moves.add(new int[]{newRow, newCol});
            }

            // 2. Travel to adjacent same-team piece to transfer Big Shot
            if (isInBounds(newRow, newCol)) {
                Piece target = board.getPiece(newRow, newCol);
                if (target != null && target.isRed() == isRed && !target.isBigShot()) {
                    // Add this move as a transfer option (use special flag later)
                    moves.add(new int[]{newRow, newCol});  // transfer move
                }
            }

            // 3. Optional: still allow jumping like a King
            int jumpRow = row + dir[0] * 2;
            int jumpCol = col + dir[1] * 2;
            if (isInBounds(jumpRow, jumpCol) && board.getPiece(jumpRow, jumpCol) == null) {
                Piece mid = board.getPiece(row + dir[0], col + dir[1]);
                if (mid != null && mid.isRed() != isRed) {
                    moves.add(new int[]{jumpRow, jumpCol});
                }
            }
        }

        return moves;
    }

    private boolean tryDoubleJump(Piece piece) {
        List<int[]> jumpMoves = board.getJumpMoves(piece); // You must already have a method like this
        if (!jumpMoves.isEmpty()) {
            highlightJumpMoves(jumpMoves); // Optional: show jump options
            selectedPiece = piece;         // Allow another move
            return true;
        }
        return false;
    }
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private void switchTurn() {
        isRedTurn = !isRedTurn;
        updateTurnLabel();
    }

    private void updateTurnLabel() {
        turnLabel.setText("Turn: " + (isRedTurn ? "Red" : "Blue"));
    }
    private void highlightJumpMoves(List<int[]> jumpMoves) {
        removeHighlights();

        for (int[] move : jumpMoves) {
            for (javafx.scene.Node node : boardGrid.getChildren()) {
                if (GridPane.getRowIndex(node) == move[0] &&
                        GridPane.getColumnIndex(node) == move[1]) {

                    if (node instanceof StackPane) {
                        StackPane cell = (StackPane) node;

                        Rectangle rect = new Rectangle();
                        rect.setFill(Color.TRANSPARENT);
                        rect.setStroke(Color.ORANGERED);
                        rect.setStrokeWidth(3);
                        rect.setStrokeType(StrokeType.INSIDE); //  full border shows
                        rect.setMouseTransparent(true);         //  clicks still go through

                        // auto-resize to cell
                        rect.widthProperty().bind(cell.widthProperty());
                        rect.heightProperty().bind(cell.heightProperty());

                        cell.getChildren().add(rect);
                    }
                }
            }
        }
    }


    private void executeMove(Move move, boolean isAiMove) {
        if (gameOver) return;

        // AI capturing move → animate every hop with 1s delay
        if (isAiMove && move.getCapturedPieces() != null && !move.getCapturedPieces().isEmpty()) {
            // Split the chain into single-hop moves
            List<Move> hops = new ArrayList<>();
            int curR = move.getStartRow();
            int curC = move.getStartCol();

            for (Piece cap : move.getCapturedPieces()) {
                int midR = cap.getRow();
                int midC = cap.getCol();
                int landR = 2 * midR - curR;   // landing is beyond the captured piece
                int landC = 2 * midC - curC;

                Move hop = new Move(curR, curC, landR, landC);
                hop.addCapturedPiece(cap);
                hops.add(hop);

                curR = landR;
                curC = landC;
            }

            // Schedule: 1 second before each hop is applied
            SequentialTransition seq = new SequentialTransition();
            for (Move hop : hops) {
                PauseTransition p = new PauseTransition(Duration.seconds(1));
                p.setOnFinished(e -> {
                    applySingleMove(hop);  // removes exactly one captured piece
                    drawBoard();
                });
                seq.getChildren().add(p);
            }
            seq.setOnFinished(e -> finishTurn());
            seq.play();
            return;
        }

        // Human move or non-capturing AI move → apply immediately
        Piece piece = board.getPiece(move.getStartRow(), move.getStartCol());
        if (piece == null) return;

        applySingleMove(move);
        if (gameOver) return;

        boolean didCapture = move.getCapturedPieces() != null && !move.getCapturedPieces().isEmpty();
        if (!isAiMove && didCapture) {
            // Human forced multi-jump (no delay; you already highlight)
            Piece movedPiece = board.getPiece(move.getEndRow(), move.getEndCol());
            List<int[]> jumpMoves = board.getJumpMoves(movedPiece);
            if (jumpMoves != null && !jumpMoves.isEmpty()) {
                highlightJumpMoves(jumpMoves);
                selectedPiece = movedPiece;
                return;
            }
        }

        finishTurn();
    }



    // Handles AI's first hop + chained jumps, all with delay
    private void startAIMultiJump(Piece piece, Move firstMove) {
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            applySingleMove(firstMove);
            drawBoard();
            continueAIMultiJump(board.getPiece(firstMove.getEndRow(), firstMove.getEndCol()));
        });
        pause.play();
    }




    // AI multi-jump continuation with 1s delay between hops
    private void continueAIMultiJump(Piece piece) {
        if (piece == null) { finishTurn(); return; }

        List<int[]> extraJumps = board.getJumpMoves(piece);
        if (extraJumps == null || extraJumps.isEmpty()) {
            finishTurn(); // chain ended
            return;
        }

        // Simple policy: take the first available jump
        int[] dest = extraJumps.get(0);
        int curRow = piece.getRow();
        int curCol = piece.getCol();
        int newRow = dest[0];
        int newCol = dest[1];

        // Build a single-hop Move so applySingleMove can remove the captured piece
        Move jump = new Move(curRow, curCol, newRow, newCol);
        int midRow = (curRow + newRow) / 2;
        int midCol = (curCol + newCol) / 2;
        Piece captured = board.getPiece(midRow, midCol);
        if (captured != null) jump.addCapturedPiece(captured);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            applySingleMove(jump);  // updates board, removes captured, handles promo/win
            drawBoard();
            continueAIMultiJump(piece); // recurse for further jumps
        });
        pause.play();
    }




    private void finishTurn() {
        selectedPiece = null;
        switchTurn();
        drawBoard();

        // 🔧 Add this block:
        if (board.isGameOver()) {
            boolean redHasMoves = !board.getAllValidMoves(true).isEmpty();
            boolean blackHasMoves = !board.getAllValidMoves(false).isEmpty();

            if (!redHasMoves) {
                endGame(false, "Red has no moves left!");
                return;
            }
            if (!blackHasMoves) {
                endGame(true, "Blue has no moves left!");
                return;
            }
        }

        // Trigger AI if it’s Black’s turn
        if (!isRedTurn) {
            triggerAI();
        }
    }



    private void applySingleMove(Move move) {
        Piece piece = board.makeMove(move);
        if (piece == null) return;

        // --- CAPTURE REMOVAL ---
        for (Piece captured : move.getCapturedPieces()) {
            if (captured == null) continue;
            board.removePiece(captured.getRow(), captured.getCol());
            if (captured.isBigShot()) {
                regenerateBigShot(captured.isRed());
            }
        }

        // --- PROMOTION ---
        // Inside applySingleMove, after capture removal
        boolean reachedBackRank =
                (piece.isRed()  && piece.getRow() == 0) ||
                        (!piece.isRed() && piece.getRow() == 7);

        if (reachedBackRank) {
            if (piece.isBigShot()) {
                //  BigShot reached back row so instant win
                updateBoardUI();
                endGame(piece.isRed(),
                        (piece.isRed() ? "Red" : "Blue") + " Big Shot reached back row!");
                return;
            } else if (!piece.isKing()) {
                piece.setKing(true);
                System.out.println((piece.isRed() ? "Red" : "Blue") + " promoted to KING!");
            }
        }


        // --- ENDGAME CHECKS ---
        // 1) Opponent has no pieces
        if (!board.hasPieces(!piece.isRed())) {
            updateBoardUI();
            endGame(piece.isRed(), (piece.isRed() ? "Red" : "Blue") + " wins! Opponent has no pieces.");
            return;
        }

        // 2) Opponent has no legal moves
        if (board.getAllValidMoves(!piece.isRed()).isEmpty()) {
            updateBoardUI();
            endGame(piece.isRed(), (piece.isRed() ? "Red" : "Blue") + " wins! Opponent is stuck.");
            return;
        }

        // 3) Fallback to your board.isGameOver() if you keep it
        if (board.isGameOver()) {
            updateBoardUI();
            endGame(piece.isRed(), "No moves left for opponent");
            return;
        }

        // Refresh board so king image appears immediately for human side too
        updateBoardUI();
    }








    private boolean gameOver = false;

    private void endGame(boolean redWon, String reason) {
        if (gameOver) return; // stop multiple triggers
        gameOver = true;

        String message;
        if (redWon) {
            message = "You win! (" + reason + ")";
        } else {
            message = "Game Over — Blue wins (" + reason + ")";
        }

        turnLabel.setText(message);

        playAgainButton.setVisible(true);
        boardGrid.setDisable(true);

    }


    @FXML
    private void handlePlayAgain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/halime/checkers/view/instruction.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeHighlights() {
        for (Node node : boardGrid.getChildren()) {
            if (node instanceof StackPane) {
                StackPane cell = (StackPane) node;
                // Remove all highlights but keep pieces
                cell.getChildren().removeIf(child -> child instanceof Rectangle && ((Rectangle) child).getStrokeWidth() == 3);
            }
        }
    }
    private void updateBoardUI() {
        drawBoard();
        updateTurnLabel();
    }
}

