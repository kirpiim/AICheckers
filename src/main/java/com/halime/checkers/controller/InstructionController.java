package com.halime.checkers.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class InstructionController {

    public Button playButton;
    @FXML
    private void handlePlayButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/halime/checkers/view/difficulty.fxml"));
            Scene difficultyScene = new Scene(loader.load());
            Stage stage = (Stage) playButton.getScene().getWindow();
            stage.setScene(difficultyScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}