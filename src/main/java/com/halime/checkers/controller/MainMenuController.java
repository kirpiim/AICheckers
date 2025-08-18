package com.halime.checkers.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainMenuController {

    @FXML
    public Button startGameButton;

    @FXML
    public void startGame(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/halime/checkers/view/instruction.fxml"));
            Scene gameScene = new Scene(loader.load());
            Stage stage = (Stage) startGameButton.getScene().getWindow();
            stage.setScene(gameScene);
        } catch (Exception e) {
            e.printStackTrace(); // Make sure this prints if there's an error
        }
    }
}
