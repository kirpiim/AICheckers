package com.halime.checkers.controller.ai;

import com.halime.checkers.model.Board;
import com.halime.checkers.model.Move;
import com.halime.checkers.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class CheckersAI {

    private int searchDepth;

    public CheckersAI(int depth) {
        this.searchDepth = depth;
    }

    public Move getBestMove(Board board, int depth, boolean isRedTurn) {
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        int bestScore = isRedTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        Move bestMove = null;

        // Get all valid moves for this player
        List<Move> moves = board.getAllValidMoves(isRedTurn);

        // --- ENFORCE MANDATORY CAPTURE ---
        List<Move> captureMoves = new ArrayList<>();
        for (Move m : moves) {
            if (m.getCapturedPieces() != null && !m.getCapturedPieces().isEmpty()) {
                captureMoves.add(m);
            }
        }
        if (!captureMoves.isEmpty()) {
            moves = captureMoves; // Only consider captures
        }
        // ----------------------------------

        for (Move move : moves) {
            Board copy = board.copy();
            copy.makeMove(move);

            // Short-circuit: if this move produces a BigShot on the back rank, pick it immediately
            Piece moved = copy.getPiece(move.getEndRow(), move.getEndCol());
            if (moved != null && moved.isBigShot()) {
                boolean onBackRank = (moved.isRed() && moved.getRow() == 0) ||
                        (!moved.isRed() && moved.getRow() == 7);
                if (onBackRank) {
                    return move; // immediate winning/promotion move
                }
            }

            int score = minimax(copy, depth - 1, alpha, beta, !isRedTurn);

            if (isRedTurn) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestScore);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                beta = Math.min(beta, bestScore);
            }

            if (beta <= alpha) break; // alpha-beta cutoff
        }

        return bestMove;
    }


    private int minimax(Board board, int depth, int alpha, int beta, boolean isRedTurn) {
        if (depth == 0 || board.isGameOver()) {
            return evaluateBoard(board);
        }

        List<Move> moves = board.getAllValidMoves(isRedTurn);
        if (moves.isEmpty()) {
            return evaluateBoard(board);
        }

        if (isRedTurn) { // Maximizing player
            int maxEval = Integer.MIN_VALUE;
            for (Move move : moves) {
                Board copy = board.copy();
                copy.makeMove(move);
                int eval = minimax(copy, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // pruning
            }
            return maxEval;
        } else { // Minimizing player
            int minEval = Integer.MAX_VALUE;
            for (Move move : moves) {
                Board copy = board.copy();
                copy.makeMove(move);
                int eval = minimax(copy, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // pruning
            }
            return minEval;
        }
    }

    private Move getRandomMove(Board board, boolean isRedTurn) {
        List<Move> validMoves = board.getAllValidMoves(isRedTurn);

        if (validMoves == null || validMoves.isEmpty()) return null;

        return validMoves.get((int) (Math.random() * validMoves.size()));
    }

    public int evaluateBoard(Board board) {
        int score = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece == null) continue;

                // If a Big Shot is crowned king → immediate terminal evaluation
                if (piece.isBigShot() && piece.isKing()) {
                    return piece.isRed() ? Integer.MAX_VALUE / 4 : Integer.MIN_VALUE / 4;
                }

                // Base values (int)
                int value = piece.isKing() ? 800 : 300;  // king >> normal
                if (piece.isBigShot()) value += 400;     // big shot is valuable

                // Promotion proximity: red promotes at row 0, black promotes at row 7
                int stepsToPromote = piece.isRed() ? row : (7 - row);

                if (!piece.isKing()) {
                    if (stepsToPromote == 0) {
                        // Already on back rank (edge case) — big bonus
                        value += 1000;
                    } else if (stepsToPromote == 1) {
                        // One move away from promotion — very large bonus (makes AI prefer it)
                        value += 600;
                    } else {
                        // Small proximity bonus for being closer (closer -> higher)
                        value += Math.max(0, (4 - stepsToPromote)) * 30;
                    }
                }

                // Add to global score: positive for Red advantage; negative for Black advantage
                score += piece.isRed() ? value : -value;
            }
        }

        return score;
    }



}
