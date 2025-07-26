package com.halime.checkers.model;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private Piece[][] board;

    public Board() {
        board = new Piece[8][8];
    }

    public void setupInitialBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = (row + 1) % 2; col < 8; col += 2) {
                boolean isBigShot = (row == 0 && col == 1); // Use col 1 instead of 2
                board[row][col] = new Piece(false, row, col, isBigShot);

                if (isBigShot) {
                    System.out.println("Black Big Shot placed at (" + row + "," + col + ")");
                }
            }
        }

        for (int row = 5; row < 8; row++) {
            for (int col = (row + 1) % 2; col < 8; col += 2) {
                boolean isBigShot = (row == 7 && col == 0); // Use col 0 instead of 1
                board[row][col] = new Piece(true, row, col, isBigShot);

                if (isBigShot) {
                    System.out.println("Red Big Shot placed at (" + row + "," + col + ")");
                }
            }
        }
    }



    public Piece getPiece(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        return null;
    }

    public void setPiece(int row, int col, Piece piece) {
        if (isValidPosition(row, col)) {
            board[row][col] = piece;
            if (piece != null) {
                piece.setRow(row);
                piece.setCol(col);
            }
        }
    }

    public void movePiece(Piece piece, int newRow, int newCol) {
        int oldRow = piece.getRow();
        int oldCol = piece.getCol();

        board[oldRow][oldCol] = null;

        // Set new position on the piece
        piece.setRow(newRow);
        piece.setCol(newCol);

        // Place it on board
        setPiece(newRow, newCol, piece);

        // Promote to King
        if ((piece.isRed() && newRow == 0) || (!piece.isRed() && newRow == 7)) {
            piece.setKing(true);
            System.out.println("Promoted to KING!");
        }
    }
    public List<int[]> getJumpMoves(Piece piece) {
        List<int[]> jumpMoves = new ArrayList<>();
        int row = piece.getRow();
        int col = piece.getCol();
        int dir = piece.isRed() ? -1 : 1;

        int[][] directions = piece.isKing() ?
                new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}} :
                new int[][]{{dir, -1}, {dir, 1}};

        for (int[] d : directions) {
            int middleRow = row + d[0];
            int middleCol = col + d[1];
            int targetRow = row + 2 * d[0];
            int targetCol = col + 2 * d[1];

            if (isValidPosition(targetRow, targetCol)) {
                Piece middle = getPiece(middleRow, middleCol);
                if (middle != null && middle.isRed() != piece.isRed() && getPiece(targetRow, targetCol) == null) {
                    jumpMoves.add(new int[]{targetRow, targetCol});
                }
            }
        }
        return jumpMoves;
    }



    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
