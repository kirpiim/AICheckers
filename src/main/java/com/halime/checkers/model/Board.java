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
        if (piece == null) return;
        int oldRow = piece.getRow();
        int oldCol = piece.getCol();
        setPiece(oldRow, oldCol, null);      // clears old cell
        setPiece(newRow, newCol, piece);     // setPiece updates row/col on the Piece
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

    // Move by coordinates from a Move object; no capture removal, no promotion here.
// applySingleMove in GameController will handle capture removal and promotion.

    public Piece makeMove(Move move) {
        Piece piece = getPiece(move.getStartRow(), move.getStartCol());
        if (piece == null) return null;

        setPiece(move.getStartRow(), move.getStartCol(), null);
        setPiece(move.getEndRow(), move.getEndCol(), piece);

        // ✅ Update piece’s row/col
        piece.setRow(move.getEndRow());
        piece.setCol(move.getEndCol());

        return piece;
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

    // Entry point
    private void addCaptureMoves(List<Move> moves, Piece piece, int row, int col) {
        // Start recursive search with a fresh base move
        addCaptureMoves(moves, piece, row, col, new Move(row, col, row, col));
    }

    // Recursive version that carries the chain
    private void addCaptureMoves(List<Move> moves, Piece piece, int row, int col, Move currentChain) {
        int[][] directions = piece.isKing() ?
                new int[][]{{-1,-1}, {-1,1}, {1,-1}, {1,1}} :
                new int[][]{ {piece.isRed() ? -1 : 1, -1}, {piece.isRed() ? -1 : 1, 1} };

        boolean extended = false;

        for (int[] dir : directions) {
            int midRow = row + dir[0];
            int midCol = col + dir[1];
            int landingRow = row + 2 * dir[0];
            int landingCol = col + 2 * dir[1];

            if (isValidPosition(landingRow, landingCol) && getPiece(landingRow, landingCol) == null) {
                Piece middlePiece = getPiece(midRow, midCol);
                if (middlePiece != null && middlePiece.isRed() != piece.isRed()) {
                    // Build new extended move chain
                    Move newChain = new Move(currentChain);
                    newChain.addStep(landingRow, landingCol);
                    newChain.addCapturedPiece(middlePiece);

                    // --- Simulate move ---
                    Piece temp = getPiece(row, col);
                    Piece captured = getPiece(midRow, midCol);

                    setPiece(row, col, null);
                    setPiece(midRow, midCol, null);
                    setPiece(landingRow, landingCol, temp);

                    // Recursive search for further jumps
                    addCaptureMoves(moves, temp, landingRow, landingCol, newChain);

                    // --- Undo ---
                    setPiece(row, col, temp);
                    setPiece(midRow, midCol, captured);
                    setPiece(landingRow, landingCol, null);

                    extended = true;
                }
            }
        }

        // If no further extensions, add the chain if it's more than the starting stub
        if (!extended && currentChain.getCapturedPieces().size() > 0) {
            moves.add(currentChain);
        }
    }



    public boolean isGameOver() {
        // If either side has no pieces, game over
        if (!hasPieces(true) || !hasPieces(false)) return true;

        // If either side has no legal moves, game over
        if (getAllValidMoves(true).isEmpty()) return true;
        if (getAllValidMoves(false).isEmpty()) return true;

        return false;
    }


    public boolean hasPieces(boolean isRed) {
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
