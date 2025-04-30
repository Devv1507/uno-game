package univalle.tedesoft.uno.view;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import univalle.tedesoft.uno.Main;
import univalle.tedesoft.uno.controller.GameController;
import univalle.tedesoft.uno.model.Cards.Card;
import univalle.tedesoft.uno.model.Enum.Color;

import java.io.IOException;
import java.util.List;

/**
 * Clase GameView que representa la vista principal del juego UNO.
 * Implementa la interfaz IGameView y actúa como contenedor de la UI de JavaFX.
 */
public class GameView extends Stage implements IGameView {

    private final GameController controller;

    /**
     * Clase interna para implementar el patrón Singleton.
     */
    private static class GameViewHolder {
        private static GameView INSTANCE;
    }

    /**
     * Devuelve la instancia única de GameView.
     * @return instancia singleton de GameView
     * @throws IOException si ocurre un error al cargar el archivo FXML
     */
    public static GameView getInstance() throws IOException {
        if (GameViewHolder.INSTANCE == null) {
            GameViewHolder.INSTANCE = new GameView();
        }
        return GameViewHolder.INSTANCE;
    }

    /**
     * Constructor privado que carga la vista desde el archivo FXML.
     * @throws IOException si falla la carga del FXML
     */
    private GameView() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("game-view.fxml"));
        Scene scene = new Scene(loader.load());
        this.controller = loader.getController();
        this.setTitle("UNO! Game");
        this.setScene(scene);
    }

    /**
     * Devuelve el controlador asociado a la vista.
     * @return instancia del controlador GameController
     */
    public GameController getController() {
        return this.controller;
    }

    /** {@inheritDoc} */
    @Override
    public void displayInitialState(List<Card> playerHand, Card topDiscardCard, int machineCardCount, String initialPlayerName) {
        Platform.runLater(() -> {
            this.controller.renderPlayerHand(playerHand);
            this.controller.updateDiscardPile(topDiscardCard);
            this.controller.machineCardsCountLabel.setText("Cartas Máquina: " + machineCardCount);
            this.controller.turnLabel.setText("Turno de: " + initialPlayerName);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void updatePlayerHand(List<Card> hand) {
        Platform.runLater(() -> this.controller.renderPlayerHand(hand));
    }

    /** {@inheritDoc} */
    @Override
    public void updateMachineHand(int cardCount) {
        Platform.runLater(() -> this.controller.machineCardsCountLabel.setText("Cartas Máquina: " + cardCount));
    }

    /** {@inheritDoc} */
    @Override
    public void updateDiscardPile(Card card, Color effectiveColor) {
        Platform.runLater(() -> this.controller.updateDiscardPile(card));
    }

    /** {@inheritDoc} */
    @Override
    public void updateDeckView() {
        // Implementación futura si se desea mostrar el mazo
    }

    /** {@inheritDoc} */
    @Override
    public void updateTurnIndicator(String playerName) {
        Platform.runLater(() -> this.controller.turnLabel.setText("Turno de: " + playerName));
    }

    /** {@inheritDoc} */
    @Override
    public void displayMessage(String message) {
        Platform.runLater(() -> this.controller.messageLabel.setText(message));
    }

    /** {@inheritDoc} */
    @Override
    public void showUnoButton(boolean show) {
        Platform.runLater(() -> this.controller.unoButton.setVisible(show));
    }

    /** {@inheritDoc} */
    @Override
    public void showUnoPenaltyTimer(boolean show) {
        Platform.runLater(() -> this.controller.unoTimerIndicator.setVisible(show));
    }

    /** {@inheritDoc} */
    @Override
    public void enablePassButton(boolean enable) {
        Platform.runLater(() -> this.controller.passButton.setDisable(!enable));
    }

    /** {@inheritDoc} */
    @Override
    public void enablePlayerInteraction(boolean enable) {
        Platform.runLater(() -> this.controller.setPlayerInteractionEnabled(enable));
    }

    /** {@inheritDoc} */
    @Override
    public void promptForColorChoice() {
        Platform.runLater(this.controller::showColorChoiceDialog);
    }

    /** {@inheritDoc} */
    @Override
    public void displayPlayerSkippedMessage(String playerName) {
        displayMessage(playerName + " fue saltado.");
    }

    /** {@inheritDoc} */
    @Override
    public void displayDrawMessage(String playerName, int count) {
        displayMessage(playerName + " robó " + count + " carta(s).");
    }

    /** {@inheritDoc} */
    @Override
    public void displayUnoDeclaredMessage(String playerName, boolean success) {
        String msg = success ? playerName + " declaró ¡UNO! correctamente." : playerName + " falló al declarar ¡UNO!";
        displayMessage(msg);
    }

    /** {@inheritDoc} */
    @Override
    public void displayGameOver(String winnerName) {
        displayMessage("Juego terminado. Ganador: " + winnerName);
    }

    /** {@inheritDoc} */
    @Override
    public void resetUIForNewGame() {
        Platform.runLater(this.controller::resetUI);
    }

    /** {@inheritDoc} */
    @Override
    public Card getSelectedCardFromEvent(MouseEvent event) {
        return this.controller.extractCardFromEvent(event);
    }

    /** {@inheritDoc} */
    @Override
    public void displayCardPlayedMessage(Card card) {
        displayMessage("Carta jugada: " + card.getColor() + " " + card.getValue());
    }

    /** {@inheritDoc} */
    @Override
    public void displayInvalidPlayMessage(Card attempted, Card topCard) {
        displayMessage("No se puede jugar " + attempted.getColor() + " " + attempted.getValue()
                + ". Carta en cima: " + topCard.getColor() + " " + topCard.getValue());
    }

    /** {@inheritDoc} */
    @Override
    public void disableGameInteractions() {
        enablePlayerInteraction(false);
        enablePassButton(false);
    }

    /** {@inheritDoc} */
    @Override
    public void enableRestartButton(boolean enable) {
        Platform.runLater(() -> this.controller.restartButton.setDisable(!enable));
    }
}