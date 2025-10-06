package com.halime.checkers.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class DifficultyController {

    @FXML private ToggleButton easyToggle;
    @FXML private ToggleButton mediumToggle;
    @FXML private ToggleButton hardToggle;
    @FXML private Button playNowButton;

    private ToggleGroup difficultyGroup;
    private int selectedDepth = 4; // default (medium)

    @FXML
    private void initialize() {
        // create toggle group
        difficultyGroup = new ToggleGroup();

        easyToggle.setToggleGroup(difficultyGroup);
        mediumToggle.setToggleGroup(difficultyGroup);
        hardToggle.setToggleGroup(difficultyGroup);

        // set default (medium)
        mediumToggle.setSelected(true);

        // listen for selection change
        difficultyGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == easyToggle) {
                selectedDepth = 2;
            } else if (newToggle == mediumToggle) {
                selectedDepth = 4;
            } else if (newToggle == hardToggle) {
                selectedDepth = 6;
            }
        });
    }

    @FXML
    private void handlePlayNowButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/halime/checkers/view/game.fxml"));
            Scene gameScene = new Scene(loader.load());

            // pass difficulty depth to GameController
            GameController controller = loader.getController();
            controller.setAiDepth(selectedDepth);

            Stage stage = (Stage) playNowButton.getScene().getWindow();
            stage.setScene(gameScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleMainMenu(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/halime/checkers/view/main_menu.fxml"));
            Scene menuScene = new Scene(loader.load());

            Stage stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
            stage.setScene(menuScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
