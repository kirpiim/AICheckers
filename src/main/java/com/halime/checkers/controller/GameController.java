package com.halime.checkers.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    private GridPane boardGrid;

    private final int TILE_SIZE = 50;
    private final int ROWS = 8;
    private final int COLS = 8;

    private StackPane selectedCell = null;
    private Circle selectedPiece = null;
    private Color currentTurn = Color.RED;
    private List<StackPane> highlightedCells = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        drawBoard();
    }

    private void drawBoard() {
        boardGrid.getChildren().clear();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Color tileColor = (row + col) % 2 == 0 ? Color.BEIGE : Color.SADDLEBROWN;

                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                tile.setFill(tileColor);

                StackPane cell = new StackPane(tile);
                int finalRow = row;
                int finalCol = col;

                cell.setOnMouseClicked(e -> handleCellClick(finalRow, finalCol, cell));

                boardGrid.add(cell, col, row);

                // Place pieces at start
                if (tileColor == Color.SADDLEBROWN) {
                    if (row < 3) {
                        Circle blackPiece = createPiece(Color.BLACK);
                        cell.getChildren().add(blackPiece);
                    } else if (row > 4) {
                        Circle redPiece = createPiece(Color.RED);
                        cell.getChildren().add(redPiece);
                    }
                }
            }
        }
    }

    private Circle createPiece(Color color) {
        Circle piece = new Circle(TILE_SIZE * 0.4);
        piece.setFill(color);
        piece.setStroke(Color.GOLD);
        piece.setStrokeWidth(2);
        return piece;
    }

    private void handleCellClick(int row, int col, StackPane clickedCell) {
        if (selectedPiece == null) {
            // Selecting a piece
            if (clickedCell.getChildren().size() > 1 && clickedCell.getChildren().get(1) instanceof Circle) {
                Circle piece = (Circle) clickedCell.getChildren().get(1);
                if (piece.getFill().equals(currentTurn)) {
                    selectedCell = clickedCell;
                    selectedPiece = piece;
                    highlightMoves(row, col);
                }
            }
        } else {
            // Clicked on a highlighted cell to move
            if (highlightedCells.contains(clickedCell)) {
                movePieceTo(clickedCell);
                switchTurn();
            }
            clearHighlights();
            selectedPiece = null;
            selectedCell = null;
        }
    }

    private void highlightMoves(int row, int col) {
        clearHighlights();
        int direction = (currentTurn == Color.RED) ? -1 : 1;
        boolean jumpAvailable = false;

        // Try jumps first
        for (int d = -1; d <= 1; d += 2) {
            int midRow = row + direction;
            int midCol = col + d;
            int jumpRow = row + 2 * direction;
            int jumpCol = col + 2 * d;

            if (isInBounds(midRow, midCol) && isInBounds(jumpRow, jumpCol)) {
                StackPane midCell = getCell(midRow, midCol);
                StackPane jumpCell = getCell(jumpRow, jumpCol);

                if (midCell.getChildren().size() > 1 && jumpCell.getChildren().size() == 1) {
                    Circle midPiece = (Circle) midCell.getChildren().get(1);
                    if (!midPiece.getFill().equals(currentTurn)) {
                        Rectangle highlight = new Rectangle(TILE_SIZE, TILE_SIZE);
                        highlight.setFill(Color.YELLOW);
                        highlight.setOpacity(0.4);
                        jumpCell.getChildren().add(highlight);
                        highlightedCells.add(jumpCell);
                        jumpAvailable = true;
                    }
                }
            }
        }

        // If no jump, allow normal diagonal move
        if (!jumpAvailable) {
            for (int d = -1; d <= 1; d += 2) {
                int newRow = row + direction;
                int newCol = col + d;

                if (isInBounds(newRow, newCol)) {
                    StackPane targetCell = getCell(newRow, newCol);
                    if (targetCell.getChildren().size() == 1) {
                        Rectangle highlight = new Rectangle(TILE_SIZE, TILE_SIZE);
                        highlight.setFill(Color.YELLOW);
                        highlight.setOpacity(0.4);
                        targetCell.getChildren().add(highlight);
                        highlightedCells.add(targetCell);
                    }
                }
            }
        }
    }

    private void clearHighlights() {
        for (StackPane cell : highlightedCells) {
            if (cell.getChildren().size() > 1 &&
                    cell.getChildren().get(cell.getChildren().size() - 1) instanceof Rectangle) {
                cell.getChildren().remove(cell.getChildren().size() - 1);
            }
        }
        highlightedCells.clear();
    }

    private void movePieceTo(StackPane destinationCell) {
        int fromRow = GridPane.getRowIndex(selectedCell);
        int fromCol = GridPane.getColumnIndex(selectedCell);
        int toRow = GridPane.getRowIndex(destinationCell);
        int toCol = GridPane.getColumnIndex(destinationCell);

        // Handle jumping
        if (Math.abs(toRow - fromRow) == 2 && Math.abs(toCol - fromCol) == 2) {
            int midRow = (fromRow + toRow) / 2;
            int midCol = (fromCol + toCol) / 2;
            StackPane midCell = getCell(midRow, midCol);
            if (midCell.getChildren().size() > 1) {
                midCell.getChildren().remove(1); // remove the jumped piece
            }
        }

        selectedCell.getChildren().remove(selectedPiece);
        destinationCell.getChildren().add(selectedPiece);
    }

    private void switchTurn() {
        currentTurn = (currentTurn == Color.RED) ? Color.BLACK : Color.RED;
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    private StackPane getCell(int row, int col) {
        for (javafx.scene.Node node : boardGrid.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return (StackPane) node;
            }
        }
        return null;
    }
}
