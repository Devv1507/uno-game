package univalle.tedesoft.uno.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class GameController {
    @FXML
    public Label machineCardsCountLabel;
    @FXML
    public HBox machineHandHBox;
    @FXML
    public ImageView deckImageView;
    @FXML
    public ImageView discardPileImageView;
    @FXML
    public Label messageLabel;
    @FXML
    public Label turnLabel;
    @FXML
    public HBox playerHandHBox;
    @FXML
    public Button passButton;
    @FXML
    public Button unoButton;
    @FXML
    public ProgressIndicator unoTimerIndicator;
    @FXML
    public Button restartButton;
    @FXML
    public Button aidButton;


    public void handleMazoClick(MouseEvent mouseEvent) {
    }

    public void handleUnoButtonAction(ActionEvent actionEvent) {
    }

    public void handleAidButtonAction(ActionEvent actionEvent) {
    }

    public void handleRestartButtonAction(ActionEvent actionEvent) {
    }

    public void handlePassButtonAction(ActionEvent actionEvent) {
    }
}
