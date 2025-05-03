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

    // --- Métodos de Actualización de la Vista Principal ---

    /**
     * Muestra el estado inicial completo del juego en la UI.
     */
    public void displayInitialState(List<Card> playerHand, Card topDiscardCard, Color effectiveColor, int machineCardCount, String initialPlayerName) {
        Platform.runLater(() -> {
            this.updatePlayerHand(playerHand, this.controller); // Renderiza mano inicial
            this.updateDiscardPile(topDiscardCard, effectiveColor); // Renderiza descarte inicial
            this.updateMachineHand(machineCardCount); // Renderiza mano máquina inicial
            this.updateTurnIndicator(initialPlayerName); // Muestra turno inicial
        });
    }

    /**
     * Renderiza (o re-renderiza) la mano del jugador humano en el HBox correspondiente.
     * @param hand La lista actualizada de cartas del jugador.
     * @param ctrl La instancia del controlador para asignar el manejador de clics.
     */
    public void updatePlayerHand(List<Card> hand, GameController ctrl) {
        Platform.runLater(() -> {
            this.controller.playerHandHBox.getChildren().clear(); // Limpiar vista anterior
            for (Card card : hand) {
                ImageView cardView = createCardImageView(card);
                // Asignar el handler del *controlador* al evento de clic
                cardView.setOnMouseClicked(ctrl::handlePlayCardClick);
                // Guardar la carta en el ImageView para recuperarla en el handler
                cardView.setUserData(card);
                this.controller.playerHandHBox.getChildren().add(cardView);
            }
        });
    }

    /**
     * Actualiza la representación visual de la mano de la máquina (contador y cartas boca abajo).
     * @param cardCount El número actual de cartas de la máquina.
     */
    public void updateMachineHand(int cardCount) {
        Platform.runLater(() -> {
            // Actualizar etiqueta del contador
            this.controller.machineCardsCountLabel.setText("Cartas Máquina: " + cardCount);

            // Renderizar cartas boca abajo (o placeholder)
            this.controller.machineHandHBox.getChildren().clear();
            Image backImage = getCardImageByName(EMPTY_IMAGE_NAME); // Usar placeholder

            if (backImage != null) {
                for (int i = 0; i < cardCount; i++) {
                    ImageView cardView = new ImageView(backImage);
                    cardView.setFitHeight(CARD_HEIGHT * 0.8); // Ligeramente más pequeñas quizás
                    cardView.setPreserveRatio(true);
                    cardView.setSmooth(true);
                    // Añadir un pequeño margen si se solapan mucho
                    HBox.setMargin(cardView, new Insets(0, -CARD_HEIGHT * 0.4, 0, 0)); // Solapamiento
                    this.controller.machineHandHBox.getChildren().add(cardView);
                }
            }
        });
    }

    /**
     * Actualiza la imagen mostrada en la pila de descarte y aplica un
     * indicador de color si es necesario (para comodines).
     * @param topCard        La carta que está ahora en la cima de la pila.
     * @param effectiveColor El color que está actualmente en vigor (importante tras un comodín).
     */
    public void updateDiscardPile(Card topCard, Color effectiveColor) {
        Platform.runLater(() -> {
            Image cardImage;
            if (topCard != null) {
                cardImage = getCardImageForCard(topCard);
            } else {
                cardImage = getCardImageByName(EMPTY_IMAGE_NAME); // Imagen vacía si no hay carta
            }

            if (cardImage != null) {
                this.controller.discardPileImageView.setImage(cardImage);
            } else {
                // Fallback si la imagen no se carga
                this.controller.discardPileImageView.setImage(getCardImageByName(EMPTY_IMAGE_NAME));
            }

            // Aplicar o quitar el efecto de color basado en `effectiveColor`
            if (topCard != null && effectiveColor != null &&  (topCard.getColor() == Color.WILD || topCard.getColor() != effectiveColor)) {
                // Aplicar brillo si es comodín o si el color efectivo difiere (caso raro)
                this.applyDiscardPileColorIndicator(effectiveColor);
            } else {
                // Quitar brillo si es una carta normal o no hay color efectivo
                this.controller.discardPileImageView.setEffect(null);
            }

            // Asegurar tamaño estándar
            this.controller.discardPileImageView.setFitHeight(CARD_HEIGHT);
            this.controller.discardPileImageView.setPreserveRatio(true);
        });
    }

    /**
     * Actualiza la etiqueta que indica de quién es el turno.
     * @param playerName El nombre del jugador actual.
     */
    public void updateTurnIndicator(String playerName) {
        Platform.runLater(() -> this.controller.turnLabel.setText("Turno de: " + playerName));
    }

    // --- Métodos de Control de la UI ---

    /**
     * Habilita o deshabilita la interacción del jugador con sus cartas, el mazo y botones relevantes
     * @param enable true para habilitar, false para deshabilitar.
     */
    public void enablePlayerInteraction(boolean enable) {
        Platform.runLater(() -> {
            this.controller.playerHandHBox.setDisable(!enable);
            this.controller.deckImageView.setDisable(!enable);
            this.controller.aidButton.setDisable(!enable);
        });
    }

    /**
     * Habilita o deshabilita el botón de "Pasar"
     * @param enable true para habilitar, false para deshabilitar.
     */
    public void enablePassButton(boolean enable) {
        Platform.runLater(() -> this.controller.passButton.setDisable(!enable));
    }

    /**
     * Muestra u oculta el botón "UNO!".
     * @param show true para mostrar, false para ocultar.
     */
    public void showUnoButton(boolean show) {
        Platform.runLater(() -> this.controller.unoButton.setVisible(show));
    }

    /**
     * Muestra u oculta el indicador de progreso/timer para la penalización de UNO.
     * @param show true para mostrar, false para ocultar.
     */
    public void showUnoPenaltyTimer(boolean show) {
        // TODO: implementar el temporizador de penalización
        Platform.runLater(() -> this.controller.unoTimerIndicator.setVisible(show));
    }

    /**
     * Habilita o deshabilita el botón de "Reiniciar".
     * @param enable true para habilitar, false para deshabilitar.
     */
    public void enableRestartButton(boolean enable) {
        Platform.runLater(() -> this.controller.restartButton.setDisable(!enable));
    }

    // --- Métodos para Dialogos ---

    /**
     * Muestra un mensaje informativo en la etiqueta de mensajes.
     * @param message El texto a mostrar.
     */
    public void displayMessage(String message) {
        Platform.runLater(() -> this.controller.messageLabel.setText(message));
    }

    /**
     * Muestra un mensaje indicando que la jugada intentada es inválida.
     * @param attempted     La carta que se intentó jugar.
     * @param topCard       La carta actual en la cima del descarte.
     * @param attemptedDesc Descripción textual de la carta intentada.
     * @param topCardDesc   Descripción textual de la carta en cima.
     */
    public void displayInvalidPlayMessage(Card attempted, Card topCard, String attemptedDesc, String topCardDesc) {
        this.displayMessage("Jugada inválida: No se puede jugar '" + attemptedDesc +
                "' sobre '" + topCardDesc + "'.");
    }

    /**
     * Muestra un mensaje indicando qué carta jugó un jugador.
     * @param card            La carta jugada.
     * @param cardDescription Descripción textual de la carta.
     */
    public void displayCardPlayedMessage(Card card, String cardDescription) {
        this.displayMessage("Se jugó: " + cardDescription);
    }

    /**
     * Muestra un mensaje de fin de juego indicando el ganador.
     * @param winnerName El nombre del jugador ganador.
     */
    public void displayGameOver(String winnerName) {
        this.displayMessage("¡Juego terminado! Ganador: " + winnerName + ". Reinicia para jugar de nuevo.");
    }
    
    /** {@inheritDoc} */
    @Override
    public void promptForColorChoice() {
        Platform.runLater(this.controller::showColorChoiceDialog);
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
    public void disableGameInteractions() {
        enablePlayerInteraction(false);
        enablePassButton(false);
    }
}