package com.halime.checkers.model;

import java.util.ArrayList;
import java.util.List;

public class Move {
    private int startRow;
    private int startCol;
    private int endRow;
    private int endCol;
    private List<Piece> capturedPieces;

    public Move(int startRow, int startCol, int endRow, int endCol) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.capturedPieces = new ArrayList<>();
    }

    public int getStartRow() {
        return startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public int getEndRow() {
        return endRow;
    }

    public int getEndCol() {
        return endCol;
    }

    public List<Piece> getCapturedPieces() {
        return capturedPieces;
    }

    public void addCapturedPiece(Piece piece) {
        capturedPieces.add(piece);
    }
    public boolean isCapture() {
        return !capturedPieces.isEmpty();
    }
}
