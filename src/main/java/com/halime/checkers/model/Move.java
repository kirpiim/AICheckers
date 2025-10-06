package com.halime.checkers.model;

import java.util.ArrayList;
import java.util.List;

public class Move {
    private List<int[]> steps; // each step is {row, col}
    private List<Piece> capturedPieces;

    // Constructor for a simple move
    public Move(int startRow, int startCol, int endRow, int endCol) {
        steps = new ArrayList<>();
        steps.add(new int[]{startRow, startCol});
        steps.add(new int[]{endRow, endCol});
        capturedPieces = new ArrayList<>();
    }

    // Copy constructor for chaining
    public Move(Move other) {
        this.steps = new ArrayList<>();
        for (int[] step : other.steps) {
            this.steps.add(new int[]{step[0], step[1]});
        }
        this.capturedPieces = new ArrayList<>(other.capturedPieces);
    }

    public void addStep(int row, int col) {
        steps.add(new int[]{row, col});
    }

    public int getStartRow() {
        return steps.get(0)[0];
    }

    public int getStartCol() {
        return steps.get(0)[1];
    }

    public int getEndRow() {
        return steps.get(steps.size() - 1)[0];
    }

    public int getEndCol() {
        return steps.get(steps.size() - 1)[1];
    }

    public List<int[]> getSteps() {
        return steps;
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
