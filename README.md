# AICheckers

**Checkers game with AI** — A JavaFX desktop game featuring an AI opponent built with Minimax + Alpha-Beta pruning, adjustable difficulty, and a simple clean UI.

---

## Overview
AICheckers is a JavaFX-based checkers game where you can play against an AI opponent. The AI uses Minimax with Alpha-Beta pruning to evaluate moves at different difficulty levels. The game includes FXML-based UI screens for instructions, difficulty selection, and gameplay.

---

## Features
- AI opponent using Minimax + Alpha-Beta pruning  
- Multiple difficulty modes (AI depth levels)  
- “Big Shot” piece rule for instant win conditions  
- JavaFX UI with FXML, CSS, and image assets  
- End-game detection and restart option  

---

## Technologies
- Java, JavaFX, FXML, CSS  
- IntelliJ IDEA for development  

---

## How to Run
1. Clone the repository  
2. Open the project in IntelliJ IDEA  
3. Set the JavaFX SDK path and run the main class (extends `Application`)  

---

## Code Structure
- **controller/** — UI logic and scene transitions  
- **ai/** — Minimax + Alpha-Beta AI logic  
- **model/** — Board, Piece, and Move classes  
- **resources/** — FXML, CSS, and image assets  

---

## What I Learned
- Implementing AI using Minimax and Alpha-Beta pruning  
- Structuring a JavaFX app with FXML and MVC pattern  
- Managing multiple game scenes and difficulty logic  
- Debugging and testing AI decision-making  
