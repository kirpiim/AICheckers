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

        List<Move> moves = board.getAllValidMoves(isRedTurn);

        for (Move move : moves) {
            Board copy = board.copy();
            copy.makeMove(move);

            int score = minimax(copy, depth - 1, alpha, beta, !isRedTurn);

            if (isRedTurn && score > bestScore) {
                bestScore = score;
                bestMove = move;
                alpha = Math.max(alpha, score);
            } else if (!isRedTurn && score < bestScore) {
                bestScore = score;
                bestMove = move;
                beta = Math.min(beta, score);
            }
        }
        return bestMove != null ? bestMove : moves.get(0); // fallback
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
                if (piece != null) {
                    // If a Big Shot is crowned king → instant win/loss
                    if (piece.isBigShot() && piece.isKing()) {
                        return piece.isRed() ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                    }

                    // Otherwise normal scoring
                    int value = piece.isKing() ? 3 : 1;
                    score += piece.isRed() ? value : -value;
                }
            }
        }
        return score;
    }

}
