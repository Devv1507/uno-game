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

    // --- Métodos para Feedback Visual ---

    /**
     * Muestra el diálogo para que el jugador elija un color después de jugar un comodín.
     * @return Un Optional<String> con el nombre del color elegido (e.g. "RED"), o Optional.empty() si el diálogo fue cancelado.
     */
    public Optional<String> promptForColorChoice() {
        List<Color> choices = List.of(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE);
        List<String> choiceNames = choices.stream()
                .map(Enum::name) // Obtiene "RED", "YELLOW", etc.
                .collect(Collectors.toList());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choiceNames.get(0), choiceNames);
        dialog.setTitle("Elegir Color");
        dialog.setHeaderText("Has jugado un comodín.");
        dialog.setContentText("Elige el próximo color:");

        // Mostrar el diálogo y esperar la respuesta del usuario
        return dialog.showAndWait();
    }

    /**
     * Resalta visualmente las cartas jugables en la mano del jugador y atenúa las no jugables.
     * @param playableCards La lista de cartas que son válidas para jugar.
     */
    public void highlightPlayableCards(List<Card> playableCards) {
        Platform.runLater(() -> {
            this.clearPlayerHandHighlights(); // Limpia efectos anteriores

            DropShadow playableGlow = new DropShadow();
            playableGlow.setColor(javafx.scene.paint.Color.LIGHTGREEN);
            playableGlow.setWidth(20);
            playableGlow.setHeight(20);
            playableGlow.setSpread(0.6);

            for (Node node : this.controller.playerHandHBox.getChildren()) {
                if (node instanceof ImageView) {
                    ImageView imageView = (ImageView) node;
                    Object cardData = imageView.getUserData();
                    if (cardData instanceof Card) {
                        Card card = (Card) cardData;
                        if (playableCards.contains(card)) {
                            imageView.setEffect(playableGlow); // Aplica brillo a jugables
                            imageView.setOpacity(1.0);       // Opacidad normal
                        } else {
                            imageView.setEffect(null);       // Sin brillo
                            imageView.setOpacity(0.6);       // Atenuar no jugables
                        }
                    }
                }
            }
        });
    }

    /**
     * Elimina cualquier efecto de resaltado de las cartas en la mano del jugador.
     */
    public void clearPlayerHandHighlights() {
        Platform.runLater(() -> {
            for (Node node : this.controller.playerHandHBox.getChildren()) {
                node.setEffect(null); // Quita cualquier efecto (DropShadow, etc.)
                node.setOpacity(1.0); // Restaura opacidad completa
            }
        });
    }

    /**
     * Resalta o quita el resaltado del mazo para indicar si se debe robar.
     * @param highlight true para resaltar, false para quitar el resaltado.
     */
    public void highlightDeck(boolean highlight) {
        Platform.runLater(() -> {
            if (highlight) {
                DropShadow deckGlow = new DropShadow();
                deckGlow.setColor(javafx.scene.paint.Color.YELLOW);
                deckGlow.setWidth(25);
                deckGlow.setHeight(25);
                deckGlow.setSpread(0.7);
                this.controller.deckImageView.setEffect(deckGlow);
            } else {
                this.controller.deckImageView.setEffect(null);
            }
        });
    }

    /**
     * Reinicia todos los componentes visuales de la UI a su estado inicial para una nueva partida.
     */
    public void resetUIForNewGame() {
        Platform.runLater(() -> {
            this.controller.playerHandHBox.getChildren().clear();
            this.controller.machineHandHBox.getChildren().clear();
            this.controller.messageLabel.setText("Iniciando nueva partida...");
            this.controller.turnLabel.setText("Turno de:");
            this.controller.machineCardsCountLabel.setText("Cartas Máquina: ?");

            // Restablecer imágenes por defecto
            this.initializeUI();

            // Ocultar y deshabilitar botones relevantes
            this.controller.unoButton.setVisible(false);
            this.controller.unoTimerIndicator.setVisible(false);
            this.controller.passButton.setDisable(true);
            this.controller.aidButton.setDisable(true);
            this.controller.restartButton.setDisable(false);

            // Limpiar resaltados
            this.clearPlayerHandHighlights();
            this.highlightDeck(false);
        });
    }

    /**
     * Crea un ImageView configurado para una carta específica, cargando su imagen.
     * @param card La carta a representar.
     * @return Un ImageView configurado.
     */
    private ImageView createCardImageView(Card card) {
        Image img = getCardImageForCard(card);
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(CARD_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true); // Mejor calidad visual

        // Añadir Tooltip con la descripción de la carta
        String description = this.getCardDescription(card); // Usar helper local
        Tooltip.install(imageView, new Tooltip(description));

        return imageView;
    }

    /**
     * Obtiene el objeto Image para una carta dada.
     * @param card La carta.
     * @return El objeto Image, o null si hay error.
     */
    private Image getCardImageForCard(Card card) {
        String imageName = this.getCardImageFilename(card);
        return getCardImageByName(imageName);
    }

    /**
     * Obtiene un objeto Image por su nombre de archivo base (sin ruta ni extensión).
     * @param baseName Nombre base (ej: "RED_5", "deck_of_cards").
     * @return El objeto Image, o null si no se encuentra o hay error.
     */
    private Image getCardImageByName(String baseName) {
        String resourcePath = CARD_IMAGE_PATH_PREFIX + baseName + CARD_IMAGE_EXTENSION;
        try {
            InputStream stream = Main.class.getResourceAsStream(resourcePath);
            if (stream == null) {
                // si no se encuentra el recurso, se define una imagen placeholder o null
                if (!baseName.equals(EMPTY_IMAGE_NAME)) {
                    return getCardImageByName(EMPTY_IMAGE_NAME);
                }
                return null;
            }
            Image image = new Image(stream);
            return image;
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen: " + resourcePath);
            // Devolver una imagen placeholder o null
            if (!baseName.equals(EMPTY_IMAGE_NAME)) {
                return getCardImageByName(EMPTY_IMAGE_NAME);
            }
            return null;
        }
    }

    /**
     * Construye el nombre de archivo esperado para la imagen de una carta.
     * @param card La carta que se necesita renderizar.
     * @return El nombre del archivo (sin extensión), e.g: "5_red", "skip_blue", "change_color".
     */
    private String getCardImageFilename(Card card) {
        if (card == null) {
            return EMPTY_IMAGE_NAME;
        }

        Value value = card.getValue();
        Color color = card.getColor();
        String colorString;
        if (color != Color.WILD) {
            // Si el color NO es WILD, obtener su nombre en minúsculas
            colorString = color.name().toLowerCase();
        } else {
            colorString = "";
        }

        return switch (value) {
            case ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE ->
                    value.ordinal() + "_" + colorString; // Asume 0_red, 1_blue...
            case SKIP -> "skip_" + colorString;
            case DRAW_TWO -> "2_wild_draw_" + colorString; // Asume '2_wild_draw_color.png'
            case WILD -> "change_color";
            case WILD_DRAW_FOUR -> "4_wild_draw";
            default -> {
                System.err.println("Advertencia: Valor de carta desconocido para imagen: " + value);
                yield EMPTY_IMAGE_NAME;
            }
        };
    }

    /**
     * Aplica un efecto visual (borde brillante) a la pila de descarte para indicar el color elegido para un comodín.
     * @param color El color elegido.
     */
    private void applyDiscardPileColorIndicator(Color color) {
        DropShadow borderGlow = new DropShadow();
        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(getColorFx(color));
        borderGlow.setWidth(30);
        borderGlow.setHeight(30);
        borderGlow.setSpread(0.7);

        this.controller.discardPileImageView.setEffect(borderGlow);
    }

    /**
     * Convierte nuestro Enum Color del modelo a javafx.scene.paint.Color para la UI.
     * @param unoColor El color del Enum del modelo.
     * @return El color correspondiente de JavaFX.
     */
    private javafx.scene.paint.Color getColorFx(Color unoColor) {
        if (unoColor == null) {
            return javafx.scene.paint.Color.BLACK;
        }
        return switch (unoColor) {
            case RED -> javafx.scene.paint.Color.RED;
            case YELLOW -> javafx.scene.paint.Color.YELLOW;
            case GREEN -> javafx.scene.paint.Color.LIMEGREEN;
            case BLUE -> javafx.scene.paint.Color.DEEPSKYBLUE;
            case WILD -> javafx.scene.paint.Color.LIGHTGRAY;
        };
    }

    /**
     * Extrae el objeto Card almacenado en el UserData de un ImageView
     * que disparó un evento de mouse
     * @param event El MouseEvent.
     * @return La Card asociada, o null si no se encuentra.
     */
    public Card extractCardFromEvent(MouseEvent event) {
        Object source = event.getSource();
        if (source instanceof ImageView clickedView) {
            Object userData = clickedView.getUserData();
            if (userData instanceof Card card) {
                return card;
            }
        }
        System.err.println("Advertencia: El evento de clic no provino de un ImageView con una Card.");
        return null;
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