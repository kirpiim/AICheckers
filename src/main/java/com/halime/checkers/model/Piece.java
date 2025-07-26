package com.halime.checkers.model;

public class Piece {
    private boolean isRed;
    private boolean isKing;
    private int row, col;
    private boolean isBigShot;

    public Piece(boolean isRed, int row, int col) {
        this.isRed = isRed;
        this.row = row;
        this.col = col;
        this.isKing = false;
    }
    public Piece(boolean isRed, int row, int col, boolean isBigShot) {
        this.isRed = isRed;
        this.row = row;
        this.col = col;
        this.isKing = false;
        this.isBigShot = isBigShot;
    }
    public boolean isRed() {
        return isRed;
    }

    public boolean isKing() {
        return isKing;
    }
    public boolean isBigShot() {
        return isBigShot;
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
    public boolean isNormalPiece() {
        return !isBigShot;
    }

    public void setBigShot(boolean bigShot) {
        this.isBigShot = bigShot;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }
    public void setKing(boolean king){
        this.isKing = king;
    }

    public String getColor() {
        return isRed ? "red" : "black";
    }
}
