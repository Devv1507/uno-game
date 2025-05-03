package univalle.tedesoft.uno.view;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import univalle.tedesoft.uno.Main;
import univalle.tedesoft.uno.controller.GameController;
import univalle.tedesoft.uno.model.Cards.Card;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Esta clase representa la vista principal del juego UNO.
 * Es responsable de toda la manipulación de la UI de JavaFX,
 * incluyendo la renderización de cartas, actualización de etiquetas,
 * manejo de efectos visuales y presentación de diálogos.
 * @author Juan Pablo Escamilla
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 */
public class GameView extends Stage {
    // Referencia al controlador
    private final GameController controller;
    // Constantes de UI
    private static final double CARD_HEIGHT = 100.0;
    private static final String CARD_IMAGE_PATH_PREFIX = "/univalle/tedesoft/uno/images/";
    private static final String CARD_IMAGE_EXTENSION = ".png";
    private static final String BACK_CARD_IMAGE_NAME = "deck_of_cards";
    private static final String EMPTY_IMAGE_NAME = "card_uno"; // Placeholder

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
        this.controller = loader.getController(); // Obtiene la instancia del controlador creada por FXML

        if (this.controller != null) {
            this.controller.setGameView(this); // Inyecta esta vista en el controlador
        } else {
            throw new IOException("No se pudo obtener el GameController desde el FXML");
        }

        this.setTitle("UNO! Game");
        this.setScene(scene);
    }

    /**
     * Inicializa la apariencia visual base de la interfaz de usuario.
     * Configura elementos visuales estáticos previo a iniciar una partida.
     * Establece las imágenes por defecto para el mazo y la pila de descarte,
     * asegurando que no tengan efectos visuales activos como brillos de color.
     */
    public void initializeUI() {
        Platform.runLater(() -> {
            // Configurar imagen inicial del mazo
            Image backImage = getCardImageByName(BACK_CARD_IMAGE_NAME);
            if (backImage != null) {
                this.controller.deckImageView.setImage(backImage);
                this.controller.deckImageView.setFitHeight(CARD_HEIGHT);
                this.controller.deckImageView.setPreserveRatio(true);
            }

            // Configurar imagen inicial (vacía) de la pila de descarte
            Image emptyImage = getCardImageByName(EMPTY_IMAGE_NAME);
            if (emptyImage != null) {
                this.controller.discardPileImageView.setImage(emptyImage);
                this.controller.discardPileImageView.setFitHeight(CARD_HEIGHT);
                this.controller.discardPileImageView.setPreserveRatio(true);
                this.controller.discardPileImageView.setEffect(null); // Sin efectos iniciales
            }
        });
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