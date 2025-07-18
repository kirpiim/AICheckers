package com.halime.checkers.model;

public class Piece {
    private boolean isRed;
    private boolean isKing;
    private int row, col;

    public Piece(boolean isRed, int row, int col) {
        this.isRed = isRed;
        this.row = row;
        this.col = col;
        this.isKing = false;
    }

    public boolean isRed() {
        return isRed;
    }

    public boolean isKing() {
        return isKing;
    }

    public void makeKing() {
        isKing = true;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public String getColor() {
        return isRed ? "red" : "black";
    }
}
