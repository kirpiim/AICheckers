package com.halime.checkers.controller;

import com.halime.checkers.model.Board;
import com.halime.checkers.model.Piece;
import javafx.fxml.FXML;
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
    private Board board;
    private Piece selectedPiece;
    private boolean isRedTurn = true;

    @FXML
    public void initialize() {
        board = new Board();
        board.setupInitialBoard();
        drawBoard();
    }

    //  Redraws the board and places pieces
    private void drawBoard() {
        boardGrid.getChildren().clear();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane cell = new StackPane();
                Rectangle tile = new Rectangle(80, 80);
                tile.setFill((row + col) % 2 == 0 ? Color.BEIGE : Color.SADDLEBROWN);
                cell.getChildren().add(tile);

                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    Circle checker = new Circle(30);
                    checker.setFill(piece.isRed() ? Color.RED : Color.BLACK);
                    checker.setStroke(Color.WHITE);
                    checker.setStrokeWidth(piece.isKing() ? 4 : 2);
                    cell.getChildren().add(checker);
                }

                final int x = row;
                final int y = col;
                cell.setOnMouseClicked(e -> handleClick(x, y));
                boardGrid.add(cell, col, row);
            }
        }

        // Highlight possible moves
        if (selectedPiece != null) {
            highlightValidMoves();
        }
    }

    // Handles mouse clicks on the board
    private void handleClick(int x, int y) {
        Piece clickedPiece = board.getPiece(x, y);

        if (selectedPiece != null && clickedPiece == null) {
            int dx = x - selectedPiece.getRow();
            int dy = y - selectedPiece.getCol();

            // Normal move
            if (Math.abs(dx) == 1 && Math.abs(dy) == 1) {
                board.movePiece(selectedPiece, x, y);
                switchTurn();
            }

            // Jump move
            else if (Math.abs(dx) == 2 && Math.abs(dy) == 2) {
                int midX = selectedPiece.getRow() + dx / 2;
                int midY = selectedPiece.getCol() + dy / 2;
                Piece middlePiece = board.getPiece(midX, midY);

                if (middlePiece != null && middlePiece.isRed() != selectedPiece.isRed()) {
                    board.setPiece(midX, midY, null); // Remove captured piece
                    board.movePiece(selectedPiece, x, y);
                    switchTurn();
                }
            }

            selectedPiece = null;
            drawBoard();
        }
        else if (clickedPiece != null && clickedPiece.isRed() == isRedTurn) {
            selectedPiece = clickedPiece;
            drawBoard(); // Will highlight valid moves
        }
    }

    // Highlights the legal destinations for a selected piece
    private void highlightValidMoves() {
        List<int[]> moves = getValidMoves(selectedPiece);

        for (int[] move : moves) {
            int row = move[0];
            int col = move[1];

            for (javafx.scene.Node node : boardGrid.getChildren()) {
                if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
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

    //  Finds all valid move destinations for a piece
    private List<int[]> getValidMoves(Piece piece) {
        List<int[]> moves = new ArrayList<>();
        int row = piece.getRow();
        int col = piece.getCol();

        int[][] directions = piece.isKing() ?
                new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}} :
                (piece.isRed() ? new int[][]{{-1, -1}, {-1, 1}} : new int[][]{{1, -1}, {1, 1}});

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            // Normal move
            if (isInBounds(newRow, newCol) && board.getPiece(newRow, newCol) == null) {
                moves.add(new int[]{newRow, newCol});
            }

            // Jump move
            int jumpRow = row + dir[0] * 2;
            int jumpCol = col + dir[1] * 2;
            if (isInBounds(jumpRow, jumpCol) && board.getPiece(jumpRow, jumpCol) == null) {
                Piece middle = board.getPiece(row + dir[0], col + dir[1]);
                if (middle != null && middle.isRed() != piece.isRed()) {
                    moves.add(new int[]{jumpRow, jumpCol});
                }
            }
        }

        return moves;
    }

    //  Checks if the position is on the board
    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    //  Switch turns between red and black
    private void switchTurn() {
        isRedTurn = !isRedTurn;
    }
}
