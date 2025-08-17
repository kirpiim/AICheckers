package com.halime.checkers.controller.ai;

import com.halime.checkers.model.Board;
import com.halime.checkers.model.Move;
import java.util.ArrayList;
import java.util.List;

public class CheckersAI {

    private int searchDepth;

    public CheckersAI(int depth) {
        this.searchDepth = depth;
    }

    public Move getBestMove(Board board, boolean isRedTurn) {
        // TODO: Implement Minimax with Alpha-Beta pruning
        // For now, just return a random move as placeholder
        return getRandomMove(board, isRedTurn);
    }

    private Move getRandomMove(Board board, boolean isRedTurn) {
        List<Move> validMoves = board.getAllValidMoves(isRedTurn);

        if (validMoves == null || validMoves.isEmpty()) return null;

        return validMoves.get((int) (Math.random() * validMoves.size()));
    }


}
