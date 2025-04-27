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

    private IGameState gameState;
    private IGameView gameView;

    private HumanPlayer humanPlayer;
    private MachinePlayer machinePlayer;

    /**
     * Representa al jugador cuyo turno esta activo en el juego.
     * Se actualiza dinamicamente durante el juego a medida que los jugadores se turnan.
     */
    private Player currentPlayer;
    // --- Utilities ---
    private ScheduledExecutorService executorService;


    @Override
    public void initialize() {
        this.humanPlayer = new HumanPlayer("Pepito");
        this.machinePlayer = new MachinePlayer();

        this.messageLabel.setText("Bienvenido a UNO");
        this.unoButton.setVisible(false);
        this.unoTimerIndicator.setVisible(false);
        this.passButton.setDisable(true);
//        this.deckImageView.setImage(this.getCardBackImage());

        this.startNewGame();
    }

    /**
     *
     */
    private void startNewGame() {
        if (this.executorService != null && !this.executorService.isShutdown()) {
            this.executorService.shutdownNow();
        }
        this.executorService = Executors.newSingleThreadScheduledExecutor();

        this.gameState = new GameState(this.humanPlayer, this.machinePlayer);
        this.gameState.onGameStart();

        this.currentPlayer = this.gameState.getCurrentPlayer();

        // TODO: actualizar GameView para reflejar el estado inicial en la UI
        // Indicar a la vista que muestre la configuración inicial
        this.gameView.resetUIForNewGame();
        this.gameView.displayInitialState(
                this.humanPlayer.getCards(),
                this.gameState.getTopDiscardCard(),
                this.machinePlayer.getNumeroCartas(),
                this.currentPlayer.getName()
        );
        this.gameView.enablePassButton(this.currentPlayer == this.humanPlayer);
        this.gameView.showUnoButton(false);
        this.gameView.enablePlayerInteraction(this.currentPlayer == this.humanPlayer);
        this.gameView.displayMessage("¡Tu turno, " + this.humanPlayer.getName() + "!");
    }

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
