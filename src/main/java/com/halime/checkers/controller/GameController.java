package com.halime.checkers.controller;

import com.halime.checkers.controller.ai.CheckersAI;
import com.halime.checkers.model.Board;
import com.halime.checkers.model.Move;
import com.halime.checkers.model.Piece;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import java.util.Objects;

public class GameController {

    @FXML private GridPane boardGrid;
    @FXML private Label turnLabel;
    @FXML private Button restartButton;

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

        restartButton.setOnAction(e -> {
            board.setupInitialBoard();
            selectedPiece = null;
            isRedTurn = true;
            drawBoard();
            updateTurnLabel();
        });
    }

    private void drawBoard() {
        boardGrid.getChildren().clear();
        allPieces.clear(); // Reset and repopulate

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane cell = new StackPane();
                Rectangle tile = new Rectangle(80, 80);
                tile.setFill((row + col) % 2 == 0 ? Color.web("#ffffff") : Color.web("#96a2b3"));
                cell.getChildren().add(tile);

                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    allPieces.add(piece);

                    // Pick correct image based on piece type
                    String imagePath;
                    if (piece.isBigShot()) {
                        imagePath = piece.isRed() ? "/com/halime/checkers/images/red_bigshot.png" : "/com/halime/checkers/images/blue_bigshot.png";
                    } else if (piece.isKing()) {
                        imagePath = piece.isRed() ? "/com/halime/checkers/images/red_king.png" : "/com/halime/checkers/images/blue_king.png";
                    } else {
                        imagePath = piece.isRed() ? "/com/halime/checkers/images/red_piece.png" : "/com/halime/checkers/images/blue_piece.png";
                    }

                    ImageView pieceView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
                    pieceView.setFitWidth(70);
                    pieceView.setFitHeight(70);
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
        if (selectedPiece != null && clickedPiece == null) {
            List<int[]> validMoves = getValidMoves(selectedPiece);

            for (int[] move : validMoves) {
                if (move[0] == x && move[1] == y) {
                    int oldRow = selectedPiece.getRow();
                    int oldCol = selectedPiece.getCol();

                    int dx = x - oldRow;
                    int dy = y - oldCol;

                    boolean isJump = Math.abs(dx) == 2 && Math.abs(dy) == 2;
                    if (isJump) {
                        int midX = oldRow + dx / 2;
                        int midY = oldCol + dy / 2;
                        Piece middlePiece = board.getPiece(midX, midY);
                        if (middlePiece != null && middlePiece.isRed() != selectedPiece.isRed()) {
                            if (middlePiece.isBigShot()) {
                                board.setPiece(midX, midY, null);
                                regenerateBigShot(middlePiece.isRed());
                            } else {
                                board.setPiece(midX, midY, null);
                            }
                        }
                    }

                    board.movePiece(selectedPiece, x, y);

                    if (isJump) {
                        List<int[]> additionalJumps = board.getJumpMoves(selectedPiece);
                        if (!additionalJumps.isEmpty()) {
                            highlightJumpMoves(additionalJumps);
                            drawBoard();
                            return; // wait for next jump click
                        }
                    }

                    selectedPiece = null;
                    switchTurn();
                    drawBoard();

                    if (!isRedTurn) {
                        triggerAI();
                    }
                    return;
                }
            }

            // ❌ Not a valid destination → just deselect
            selectedPiece = null;
            drawBoard();
            return;
        }

        // --- B) Big Shot transfer: only when clicking an ADJACENT same-color piece ---
        if (selectedPiece != null && selectedPiece.isBigShot() && clickedPiece != null
                && clickedPiece.isRed() == selectedPiece.isRed()) {

            int dx = clickedPiece.getRow() - selectedPiece.getRow();
            int dy = clickedPiece.getCol() - selectedPiece.getCol();

            if (Math.abs(dx) == 1 && Math.abs(dy) == 1) {
                // Perform the Big Shot transfer
                selectedPiece.setBigShot(false);
                clickedPiece.setBigShot(true);

                selectedPiece = null;
                switchTurn();
                drawBoard();
                triggerAI(); // AI after transfer
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
            Move aiMove = aiPlayer.getBestMove(board, aiDepth, /*isRedTurn=*/false);
            if (aiMove != null) {
                executeMove(aiMove, /*isAiMove*/ true);  // <- important
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

                if (candidate != null) {
                    System.out.println("Checking piece at (" + row + "," + col + ") | isRed=" + candidate.isRed() + ", isBigShot=" + candidate.isBigShot() + ", isDark=" + isDarkSquare);

                    if (candidate.isRed() == isRed && !candidate.isBigShot() && isDarkSquare) {
                        candidate.setBigShot(true);
                        System.out.println((isRed ? "Red" : "Black") + " Big Shot regenerated at (" + row + "," + col + ")");
                        return;
                    }
                }
            }
        }

        System.out.println("No eligible piece found on board to convert into Big Shot.");
    }



    private void highlightValidMoves() {
        List<int[]> moves = getValidMoves(selectedPiece);
        for (int[] move : moves) {
            for (javafx.scene.Node node : boardGrid.getChildren()) {
                if (GridPane.getRowIndex(node) == move[0] && GridPane.getColumnIndex(node) == move[1]) {
                    StackPane cell = (StackPane) node;
                    Rectangle highlight = new Rectangle(80, 80);
                    highlight.setFill(Color.TRANSPARENT);
                    highlight.setStroke(Color.LIMEGREEN);
                    highlight.setStrokeWidth(3);
                    cell.getChildren().add(highlight);
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
        turnLabel.setText("Turn: " + (isRedTurn ? "Red" : "Black"));
    }
    private void highlightJumpMoves(List<int[]> jumpMoves) {
        for (int[] move : jumpMoves) {
            for (javafx.scene.Node node : boardGrid.getChildren()) {
                if (GridPane.getRowIndex(node) == move[0] && GridPane.getColumnIndex(node) == move[1]) {
                    StackPane cell = (StackPane) node;
                    Rectangle highlight = new Rectangle(80, 80);
                    highlight.setFill(Color.TRANSPARENT);
                    highlight.setStroke(Color.ORANGERED);
                    highlight.setStrokeWidth(3);
                    cell.getChildren().add(highlight);
                }
            }
        }
    }

    private void executeMove(Move move, boolean isAiMove) {
        Piece piece = board.getPiece(move.getStartRow(), move.getStartCol());
        if (piece == null) return;

        // Apply the first move
        applySingleMove(piece, move.getEndRow(), move.getEndCol(), move.getCapturedPieces());

        boolean didCapture = move.getCapturedPieces() != null && !move.getCapturedPieces().isEmpty();

        if (didCapture) {
            if (isAiMove) {
                // --- AI: auto-chain with delays ---
                continueAIMultiJump(piece);
                return; // defer finishing the turn until AI is done
            } else {
                // --- Player: keep interactive double-jump UX ---
                List<int[]> jumpMoves = board.getJumpMoves(piece);
                if (jumpMoves != null && !jumpMoves.isEmpty()) {
                    highlightJumpMoves(jumpMoves);
                    selectedPiece = piece;   // let the player click the next jump
                    return;                  // don't switch turn yet
                }
            }
        }

        // Finish the turn
        finishTurn();
    }

    // Helper for AI multi-jumps with delay
    private void continueAIMultiJump(Piece piece) {
        List<int[]> extraJumps = board.getJumpMoves(piece);
        if (extraJumps == null || extraJumps.isEmpty()) {
            // No more jumps -> finish turn
            finishTurn();
            return;
        }

        // Take the first available jump (simple AI)
        int[] dest = extraJumps.get(0);
        int curRow = piece.getRow();
        int curCol = piece.getCol();
        int newRow = dest[0];
        int newCol = dest[1];

        int midRow = (curRow + newRow) / 2;
        int midCol = (curCol + newCol) / 2;
        Piece captured = board.getPiece(midRow, midCol);

        List<Piece> capturedList = new ArrayList<>();
        if (captured != null) capturedList.add(captured);

        // Delay before executing next jump
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            applySingleMove(piece, newRow, newCol, capturedList);
            drawBoard();
            continueAIMultiJump(piece); // recursive chain with delay
        });
        pause.play();
    }

    private void finishTurn() {
        selectedPiece = null;
        switchTurn();
        drawBoard();

        // Trigger AI if it’s Black’s turn
        if (!isRedTurn) {
            triggerAI();
        }
    }


    private void applySingleMove(Piece piece, int endRow, int endCol, List<Piece> capturedPieces) {
        board.movePiece(piece, endRow, endCol);
        // Immediately check if Big Shot or other game over happened
        if (board.isGameOver()) {
            endGame(piece.isRed(), "Big Shot crowned");
            return;
        }
        if (capturedPieces != null) {
            for (Piece captured : capturedPieces) {
                if (captured == null) continue;
                board.removePiece(captured.getRow(), captured.getCol());
                if (captured.isBigShot()) {
                    regenerateBigShot(captured.isRed());
                }
            }
        }
        updateBoardUI();
    }
    private void endGame(boolean redWon, String reason) {
        String winner = redWon ? "Red" : "Black";
        turnLabel.setText("Game Over! " + winner + " wins (" + reason + ")");

        // Disable further clicks on the board
        boardGrid.setDisable(true);
        restartButton.setDisable(false);
    }

    private void updateBoardUI() {
        drawBoard();
        updateTurnLabel();
    }
}

