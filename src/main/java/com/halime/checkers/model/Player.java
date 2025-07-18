package com.halime.checkers.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Player {
    protected boolean isRed;
    protected List<Piece> pieces;
    protected Board board;

    public Player(boolean isRed, Board board) {
        this.isRed = isRed;
        this.board = board;
        this.pieces = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.isRed() == isRed) {
                    pieces.add(piece);
                }
            }
        }
    }

    public List<Piece> getPieces() {
        return pieces;
    }
}

