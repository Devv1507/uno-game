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
public class GameView extends Stage {

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

        if (this.controller != null) {
            this.controller.setGameView(this);
        } else {
            throw new IOException("No se pudo obtener el GameController desde el FXML");
        }

        this.setTitle("UNO! Game");
        this.setScene(scene);
    }

    /** {@inheritDoc} */
    public void displayInitialState(List<Card> playerHand, Card topDiscardCard, int machineCardCount, String initialPlayerName) {
        Platform.runLater(() -> {
            this.controller.renderPlayerHand(playerHand);
            this.controller.updateDiscardPile(topDiscardCard);
            this.controller.machineCardsCountLabel.setText("Cartas Máquina: " + machineCardCount);
            this.controller.renderMachineHand(machineCardCount);
            this.controller.turnLabel.setText("Turno de: " + initialPlayerName);
        });
    }

    /** {@inheritDoc} */
    public void updatePlayerHand(List<Card> hand) {
        Platform.runLater(() -> this.controller.renderPlayerHand(hand));
    }

    /** {@inheritDoc} */
    public void updateMachineHand(int cardCount) {
        Platform.runLater(() -> {
            this.controller.machineCardsCountLabel.setText("Cartas Máquina: " + cardCount);
            this.controller.renderMachineHand(cardCount);
        });
    }

    /** {@inheritDoc} */
    public void updateDiscardPile(Card card, Color effectiveColor) {
        Platform.runLater(() -> this.controller.updateDiscardPile(card));
    }

    /** {@inheritDoc} */
    public void updateDeckView() {
        // Implementación futura si se desea mostrar el mazo
    }

    /** {@inheritDoc} */
    public void updateTurnIndicator(String playerName) {
        Platform.runLater(() -> this.controller.turnLabel.setText("Turno de: " + playerName));
    }

    /** {@inheritDoc} */
    public void displayMessage(String message) {
        Platform.runLater(() -> this.controller.messageLabel.setText(message));
    }


    /** {@inheritDoc} */
    public void showUnoButton(boolean show) {
        Platform.runLater(() -> this.controller.unoButton.setVisible(show));
    }

    /** {@inheritDoc} */
    public void showUnoPenaltyTimer(boolean show) {
        Platform.runLater(() -> this.controller.unoTimerIndicator.setVisible(show));
    }

    /** {@inheritDoc} */
    public void enablePassButton(boolean enable) {
        Platform.runLater(() -> this.controller.passButton.setDisable(!enable));
    }

    /** {@inheritDoc} */
    public void enablePlayerInteraction(boolean enable) {
        Platform.runLater(() -> this.controller.setPlayerInteractionEnabled(enable));
    }

    /** {@inheritDoc} */
    public void promptForColorChoice() {
        Platform.runLater(this.controller::showColorChoiceDialog);
    }

    /** {@inheritDoc} */
    public void displayPlayerSkippedMessage(String playerName) {
        displayMessage(playerName + " fue saltado.");
    }

    /** {@inheritDoc} */
    public void displayDrawMessage(String playerName, int count) {
        displayMessage(playerName + " robó " + count + " carta(s).");
    }

    /** {@inheritDoc} */
    public void displayUnoDeclaredMessage(String playerName, boolean success) {
        String msg = success ? playerName + " declaró ¡UNO! correctamente." : playerName + " falló al declarar ¡UNO!";
        displayMessage(msg);
    }

    /** {@inheritDoc} */
    public void displayGameOver(String winnerName) {
        displayMessage("Juego terminado. Ganador: " + winnerName);
    }

    /** {@inheritDoc} */
    public void resetUIForNewGame() {
        Platform.runLater(this.controller::resetUI);
    }

    /** {@inheritDoc} */
    public Card getSelectedCardFromEvent(MouseEvent event) {
        return this.controller.extractCardFromEvent(event);
    }

    /** {@inheritDoc} */
    public void displayCardPlayedMessage(Card card) {
        displayMessage("Carta jugada: " + card.getColor() + " " + card.getValue());
    }

    /** {@inheritDoc} */
    public void displayInvalidPlayMessage(Card attempted, Card topCard) {
        displayMessage("No se puede jugar " + attempted.getColor() + " " + attempted.getValue()
                + ". Carta en cima: " + topCard.getColor() + " " + topCard.getValue());
    }

    /** {@inheritDoc} */
    public void disableGameInteractions() {
        enablePlayerInteraction(false);
        enablePassButton(false);
    }

    /** {@inheritDoc} */
    public void enableRestartButton(boolean enable) {
        Platform.runLater(() -> this.controller.restartButton.setDisable(!enable));
    }

    /**
     * Pide al controlador que resalte visualmente las cartas jugables.
     * @param playableCards La lista de cartas que se pueden jugar.
     */
    public void highlightPlayableCards(List<Card> playableCards) {
        Platform.runLater(() -> this.controller.highlightPlayableCards(playableCards));
    }

}