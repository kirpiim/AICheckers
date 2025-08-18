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

    public void removePiece(int row, int col) {
        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            board[row][col] = null;
        }
    }

    public Board copy() {
        Board newBoard = new Board();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = this.getPiece(row, col);
                if (piece != null) {

                    Piece cloned = new Piece(piece.isRed(), row, col, piece.isBigShot());
                    cloned.setKing(piece.isKing());
                    newBoard.setPiece(row, col, cloned);
                }
            }
        }
        return newBoard;
    }

    public void makeMove(Move move) {
        Piece piece = getPiece(move.getStartRow(), move.getStartCol());
        if (piece == null) return;

        // Remove from old position
        setPiece(move.getStartRow(), move.getStartCol(), null);

        // Place piece in new position
        setPiece(move.getEndRow(), move.getEndCol(), piece);

        // Handle captures
        for (Piece captured : move.getCapturedPieces()) {
            setPiece(captured.getRow(), captured.getCol(), null);
        }

        // Crown promotion
        if ((piece.isRed() && move.getEndRow() == 0) || (!piece.isRed() && move.getEndRow() == 7)) {
            piece.setKing(true);
        }
    }
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    // Get all valid moves for current player
    public List<Move> getAllValidMoves(boolean isRedTurn) {
        List<Move> moves = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = getPiece(row, col);
                if (piece != null && piece.isRed() == isRedTurn) {
                    moves.addAll(getValidMovesForPiece(piece));
                }
            }
        }
        return moves;
    }

    // Get valid moves for a single piece
    private List<Move> getValidMovesForPiece(Piece piece) {
        List<Move> moves = new ArrayList<>();
        int row = piece.getRow();
        int col = piece.getCol();
        int direction = piece.isRed() ? -1 : 1;

        // Normal diagonal moves
        checkAndAddMove(moves, piece, row + direction, col - 1);
        checkAndAddMove(moves, piece, row + direction, col + 1);

        // King can move backwards
        if (piece.isKing()) {
            checkAndAddMove(moves, piece, row - direction, col - 1);
            checkAndAddMove(moves, piece, row - direction, col + 1);
        }

        // Captures
        addCaptureMoves(moves, piece, row, col);

        return moves;
    }

    private void checkAndAddMove(List<Move> moves, Piece piece, int newRow, int newCol) {
        if (isValidPosition(newRow, newCol) && getPiece(newRow, newCol) == null) {
            moves.add(new Move(piece.getRow(), piece.getCol(), newRow, newCol));
        }
    }

    private void addCaptureMoves(List<Move> moves, Piece piece, int row, int col) {
        int[][] directions = {
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };

        for (int[] dir : directions) {
            int midRow = row + dir[0];
            int midCol = col + dir[1];
            int landingRow = row + dir[0] * 2;
            int landingCol = col + dir[1] * 2;

            if (isValidPosition(landingRow, landingCol) && getPiece(landingRow, landingCol) == null) {
                Piece middlePiece = getPiece(midRow, midCol);
                if (middlePiece != null && middlePiece.isRed() != piece.isRed()) {
                    Move captureMove = new Move(piece.getRow(), piece.getCol(), landingRow, landingCol);
                    captureMove.addCapturedPiece(middlePiece);
                    moves.add(captureMove);
                }
            }
        }
    }
    public boolean isGameOver() {
        // If either side has no pieces, game over
        if (!hasPieces(true) || !hasPieces(false)) return true;

        // If either side has no legal moves, game over
        if (getAllValidMoves(true).isEmpty()) return true;   // Red stuck
        if (getAllValidMoves(false).isEmpty()) return true;  // Black stuck

        // Big Shot auto-win rule: if a Big Shot is now a King, game ends
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.isBigShot() && p.isKing()) {
                    System.out.println("Big Shot became King! Game over.");
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasPieces(boolean isRed) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.isRed() == isRed) {
                    return true;
                }
            }
        }
        return false;
    }
}
