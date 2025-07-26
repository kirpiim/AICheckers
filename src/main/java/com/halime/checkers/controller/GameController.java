package com.halime.checkers.controller;

import com.halime.checkers.model.Board;
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

import java.util.ArrayList;
import java.util.List;

public class GameController {

    @FXML private GridPane boardGrid;
    @FXML private Label turnLabel;
    @FXML private Button restartButton;

    private Board board;
    private Piece selectedPiece;
    private boolean isRedTurn = true;
    private final List<Piece> allPieces = new ArrayList<>();
    private Piece activeJumpingPiece = null;

    @FXML
    public void initialize() {
        board = new Board();
        board.setupInitialBoard();
        selectedPiece = null;
        isRedTurn = true;
        drawBoard();
        updateTurnLabel();

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
                tile.setFill((row + col) % 2 == 0 ? Color.BEIGE : Color.SADDLEBROWN);
                cell.getChildren().add(tile);

                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    allPieces.add(piece);
                    Circle checker = new Circle(30);
                    checker.setFill(piece.isRed() ? Color.RED : Color.BLACK);
                    if (piece.isBigShot()) {
                        checker.setStroke(Color.GOLD);
                        checker.setStrokeWidth(5);
                    } else {
                        checker.setStroke(Color.WHITE);
                        checker.setStrokeWidth(piece.isKing() ? 4 : 2);
                    }

                    cell.getChildren().add(checker);
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

        if (selectedPiece != null && clickedPiece == null) {
            List<int[]> validMoves = getValidMoves(selectedPiece);

            for (int[] move : validMoves) {
                if (move[0] == x && move[1] == y) {
                    int oldRow = selectedPiece.getRow();
                    int oldCol = selectedPiece.getCol();

                    int dx = x - oldRow;
                    int dy = y - oldCol;

                    // Check if this is a jump
                    boolean isJump = Math.abs(dx) == 2 && Math.abs(dy) == 2;

                    if (isJump) {
                        int midX = oldRow + dx / 2;
                        int midY = oldCol + dy / 2;
                        Piece middlePiece = board.getPiece(midX, midY);

                        if (middlePiece != null && middlePiece.isRed() != selectedPiece.isRed()) {
                            if (middlePiece.isBigShot()) {
                                board.setPiece(midX, midY, null); // Remove it
                                regenerateBigShot(middlePiece.isRed());
                            } else {
                                board.setPiece(midX, midY, null); // Normal capture
                            }
                        }
                    }

                    // Move the piece
                    board.movePiece(selectedPiece, x, y);

                    if (isJump) {
                        // Check for another jump
                        List<int[]> additionalJumps = board.getJumpMoves(selectedPiece);
                        if (!additionalJumps.isEmpty()) {
                            highlightJumpMoves(additionalJumps);  // show next jump
                            drawBoard();
                            return;  // Do NOT switch turn, wait for next click
                        }
                    }

                    // End turn
                    selectedPiece = null;
                    switchTurn();
                    drawBoard();
                    return;
                }
            }

        } else if (selectedPiece != null && clickedPiece != null && selectedPiece.isBigShot() && clickedPiece.isRed() == selectedPiece.isRed()) {
            int dx = clickedPiece.getRow() - selectedPiece.getRow();
            int dy = clickedPiece.getCol() - selectedPiece.getCol();

            // Only allow diagonally adjacent same-team pieces
            if (Math.abs(dx) == 1 && Math.abs(dy) == 1) {
                // Perform the Big Shot transfer
                selectedPiece.setBigShot(false);
                clickedPiece.setBigShot(true);

                selectedPiece = null;
                switchTurn();
                drawBoard();
                return;
            }

        } else if (clickedPiece != null && clickedPiece.isRed() == isRedTurn) {
            selectedPiece = clickedPiece;
            drawBoard();
        }
    }


    private void regenerateBigShot(boolean isRed) {
        int backRow = isRed ? 7 : 0; // Red's back row is 7, Black's is 0
        List<Integer> emptyCols = new ArrayList<>();

        for (int col = 0; col < 8; col++) {
            if (board.getPiece(backRow, col) == null) {
                emptyCols.add(col);
            }
        }

        if (!emptyCols.isEmpty()) {
            int col = emptyCols.get((int) (Math.random() * emptyCols.size()));
            Piece newBigShot = new Piece(isRed, backRow, col, true);
            board.setPiece(backRow, col, newBigShot);
            System.out.println("Big Shot regenerated at " + backRow + ", " + col);
        }
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

        int direction = piece.isRed() ? -1 : 1;
        int[][] steps = {{direction, -1}, {direction, 1}};
        int[][] jumps = {{direction * 2, -2}, {direction * 2, 2}};

        for (int i = 0; i < steps.length; i++) {
            int newRow = row + steps[i][0];
            int newCol = col + steps[i][1];
            if (isInBounds(newRow, newCol) && board.getPiece(newRow, newCol) == null) {
                moves.add(new int[]{newRow, newCol});
            }

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



}

