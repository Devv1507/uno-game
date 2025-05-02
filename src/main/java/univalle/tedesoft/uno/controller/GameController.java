package univalle.tedesoft.uno.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import univalle.tedesoft.uno.Main;
import univalle.tedesoft.uno.model.Cards.Card;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;
import univalle.tedesoft.uno.model.Players.HumanPlayer;
import univalle.tedesoft.uno.model.Players.MachinePlayer;
import univalle.tedesoft.uno.model.Players.Player;
import univalle.tedesoft.uno.model.State.GameState;
import univalle.tedesoft.uno.model.State.IGameState;
import univalle.tedesoft.uno.view.GameView;


import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private GameView gameView;

    private HumanPlayer humanPlayer;
    private MachinePlayer machinePlayer;

    // Constantes para configuración de la UI
    private static final double CARD_HEIGHT = 100.0;
    private static final String CARD_IMAGE_PATH_PREFIX = "/univalle/tedesoft/uno/images/";
    private static final String CARD_IMAGE_EXTENSION = ".png";
    private static final String BACK_CARD_IMAGE_NAME = "deck_of_cards"; // Nombre del archivo para el reverso
    private static final String EMPTY_IMAGE_NAME = "card_uno"; // Un placeholder si es necesario

    /**
     * Representa al jugador cuyo turno esta activo en el juego.
     * Se actualiza dinamicamente durante el juego a medida que los jugadores se turnan.
     */
    private Player currentPlayer;
    // --- Utilities ---
    private ScheduledExecutorService executorService;


    @FXML
    public void initialize() {
        this.humanPlayer = new HumanPlayer("Pepito");
        this.machinePlayer = new MachinePlayer();

        // Configuramos una imagen por defecto para el mazo (el reverso)
        this.deckImageView.setImage(this.getCardImage(BACK_CARD_IMAGE_NAME));
        this.deckImageView.setFitHeight(CARD_HEIGHT);
        this.deckImageView.setPreserveRatio(true);

        // Configuramos una imagen por defecto o vacía para la pila de descarte
        this.discardPileImageView.setImage(this.getCardImage(EMPTY_IMAGE_NAME)); // O null si prefieres empezar sin nada
        this.discardPileImageView.setFitHeight(CARD_HEIGHT);
        this.discardPileImageView.setPreserveRatio(true);

        // Inicializamos el executorService aquí para que esté listo
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Establece la referencia a la GameView para que el controlador pueda interactuar con ella.
     * Debe ser llamado por GameView después de cargar el FXML.
     *
     * @param gameView La instancia de GameView.
     */
    public void setGameView(GameView gameView) {
        this.gameView = gameView;
        if (this.gameState == null) {
            this.startNewGame();
        }
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

        // Actualizar la UI para reflejar el estado inicial
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
        this.gameView.enableRestartButton(true);
    }

    /**
     * Renderiza la mano del jugador humano en el HBox correspondiente.
     * Cada carta es un ImageView clickeable.
     *
     * @param hand La lista de cartas del jugador humano.
     */
    public void renderPlayerHand(List<Card> hand) {
        this.playerHandHBox.getChildren().clear(); // Limpia la mano anterior
        for (Card card : hand) {
            ImageView cardView = this.createCardImageView(card);
            // EventHandler para cuando se haga clic en la carta
            cardView.setOnMouseClicked(this::handlePlayCardClick);
            // Guardamos la carta real en el ImageView para poder recuperarla en el evento
            cardView.setUserData(card);
            this.playerHandHBox.getChildren().add(cardView);
        }
    }

    /**
     * Renderiza representaciones visuales (cartas boca abajo) de la mano de la máquina.
     * @param cardCount El número de cartas que tiene la máquina.
     */
    public void renderMachineHand(int cardCount) {
        this.machineHandHBox.getChildren().clear(); // Limpia la vista anterior
        Image backImage = getCardImage(EMPTY_IMAGE_NAME); // Usa card_uno.png

        if (backImage == null) {
            return; // No podemos mostrar nada si la imagen base falta
        }

        for (int i = 0; i < cardCount; i++) {
            ImageView cardView = new ImageView(backImage);
            cardView.setFitHeight(CARD_HEIGHT); // Usa la misma altura que las cartas del jugador
            cardView.setPreserveRatio(true);
            cardView.setSmooth(true);
            // No necesita userData ni event handler
            this.machineHandHBox.getChildren().add(cardView);
        }
    }

    /**
     * Actualiza la imagen mostrada en la pila de descarte.
     *
     * @param topCard La carta que está ahora en la cima de la pila.
     */
    public void updateDiscardPile(Card topCard) {
        if (topCard != null) {
            ImageView cardView = this.createCardImageView(topCard);
            // La carta de descarte no necesita ser clickeable ni tener datos asociados
            this.discardPileImageView.setImage(cardView.getImage());
        } else {
            // Si no hay carta (inicio del juego o error), muestra una imagen vacía o el reverso
            this.discardPileImageView.setImage(this.getCardImage(EMPTY_IMAGE_NAME));
        }
        // Aseguramos el tamaño correcto consistentemente
        this.discardPileImageView.setFitHeight(CARD_HEIGHT);
        this.discardPileImageView.setPreserveRatio(true);
    }

    /**
     * Habilita o deshabilita la interacción del jugador con las cartas y el mazo.
     *
     * @param enabled true para habilitar, false para deshabilitar.
     */
    public void setPlayerInteractionEnabled(boolean enabled) {
        // Habilitar/deshabilitar clic en las cartas de la mano
        this.playerHandHBox.setDisable(!enabled);
        // Habilitar/deshabilitar clic en el mazo para robar
        this.deckImageView.setDisable(!enabled);
        this.aidButton.setDisable(!enabled);
    }

    /**
     * Muestra un diálogo para que el jugador elija un color después de jugar un comodín.
     */
    public void showColorChoiceDialog() {
        List<Color> choices = List.of(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE);
        // muestra las opciones de color
        List<String> choiceNames = choices.stream().map(Enum::name).toList();

        ChoiceDialog<String> dialog = new ChoiceDialog<>(choiceNames.get(0), choiceNames);
        dialog.setTitle("Elegir Color");
        dialog.setHeaderText("Has jugado un comodín.");
        dialog.setContentText("Elige el próximo color:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(colorName -> {
            Color chosenColor = Color.valueOf(colorName); // Convierte el String de vuelta a Enum
            this.gameState.onColorChosen(chosenColor);
            // Actualizar la UI para reflejar el color elegido (quizás un borde en la pila de descarte)
            this.updateDiscardPileColorIndicator(chosenColor);
            this.gameView.displayMessage("Color cambiado a " + chosenColor.name());
            // Una vez elegido el color, el turno puede avanzar
            this.handleTurnAdvancement();
        });

        // si el jugador cierra el dialogo sin elegir, se reintenta
        if (result.isEmpty()) {
            this.gameView.displayMessage("Debes elegir un color para continuar.");
            this.showColorChoiceDialog();
        }
    }

    /**
     * Actualiza un indicador visual del color elegido para el comodín.
     * Podría ser un borde alrededor de la pila de descarte.
     *
     * @param color El color elegido.
     */
    private void updateDiscardPileColorIndicator(Color color) {
        DropShadow borderGlow = new DropShadow();
        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(this.getColorFx(color));
        borderGlow.setWidth(30);
        borderGlow.setHeight(30);
        borderGlow.setSpread(0.7);

        this.discardPileImageView.setEffect(borderGlow);
    }

    /**
     * Convierte nuestro Enum Color a javafx.scene.paint.Color.
     *
     * @param unoColor El color del Enum de nuestro modelo.
     * @return El color correspondiente de JavaFX.
     */
    private javafx.scene.paint.Color getColorFx(Color unoColor) { // El tipo de retorno es el calificado
        switch (unoColor) {
            // Usa el nombre completamente calificado para las constantes de JavaFX Color
            case RED: return javafx.scene.paint.Color.RED;
            case YELLOW: return javafx.scene.paint.Color.YELLOW;
            case GREEN: return javafx.scene.paint.Color.LIMEGREEN; // O .GREEN
            case BLUE: return javafx.scene.paint.Color.BLUE;
            case WILD: return javafx.scene.paint.Color.GRAY; // Color neutral
            default: return javafx.scene.paint.Color.BLACK; // Color por defecto o error
        }
    }

    /**
     * Reinicia los componentes de la UI a su estado inicial para una nueva partida.
     */
    public void resetUI() {
        this.playerHandHBox.getChildren().clear();
        this.machineHandHBox.getChildren().clear(); // Limpiar también la máquina (si tuviera algo)
        this.messageLabel.setText("Iniciando nueva partida...");
        this.turnLabel.setText("Turno de:");
        this.machineCardsCountLabel.setText("Cartas Máquina: ?");

        // Restablecer imágenes del mazo y descarte
        this.deckImageView.setImage(getCardImage(BACK_CARD_IMAGE_NAME));
        this.discardPileImageView.setImage(getCardImage(EMPTY_IMAGE_NAME));
        this.discardPileImageView.setEffect(null); // Quitar cualquier efecto (como el borde de color)

        // Ocultar y deshabilitar botones relevantes
        this.unoButton.setVisible(false);
        this.unoTimerIndicator.setVisible(false);
        this.passButton.setDisable(true);
        this.aidButton.setDisable(true);
        // El botón de reinicio podría permanecer habilitado o deshabilitarse hasta que termine la partida
        this.restartButton.setDisable(false); // Dejémoslo habilitado para reiniciar en cualquier momento

        // Limpiar resaltado de cartas jugables si existiera
        this.clearPlayableCardHighlighting();
    }

    /**
     * Elimina cualquier efecto de resaltado de todas las cartas en la mano del jugador.
     */
    private void clearPlayableCardHighlighting() {
        for (Node node : this.playerHandHBox.getChildren()) {
            node.setEffect(null); // Quita cualquier efecto (DropShadow, etc.)
            node.setOpacity(1.0); // Restaura opacidad completa
        }
    }

    /**
     * Crea un ImageView para una carta específica.
     *
     * @param card La carta a representar.
     * @return Un ImageView configurado para la carta.
     */
    private ImageView createCardImageView(Card card) {
        Image img = getCardImageForCard(card);
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(CARD_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true); // Mejor calidad de imagen
        // Podríamos añadir un Tooltip para ver el nombre de la carta al pasar el ratón
        Tooltip.install(imageView, new Tooltip(this.gameState.getCardDescription(card)));
        return imageView;
    }

    /**
     * Obtiene el objeto Image para una carta dada, buscando en los recursos.
     *
     * @param card La carta.
     * @return El objeto Image.
     */
    private Image getCardImageForCard(Card card) {
        String imageName = getCardImageFilename(card);
        return getCardImage(imageName);
    }

    /**
     * Construye el nombre de archivo esperado para la imagen de una carta.
     *
     * @param card La carta.
     * @return El nombre del archivo (sin ruta ni extensión). Ej: "RED_5", "WILD", "BLUE_SKIP".
     */
    /**
     * Construye el nombre de archivo esperado para la imagen de una carta,
     * coincidiendo con los archivos en resources/univalle/tedesoft/uno/images/.
     *
     * @param card La carta.
     * @return El nombre del archivo (sin ruta ni extensión). Ej: "5_red", "skip_blue", "change_color".
     */
    private String getCardImageFilename(Card card) {
        if (card == null) {
            // Devuelve el nombre base para la imagen vacía/placeholder
            return EMPTY_IMAGE_NAME;
        }

        Value value = card.getValue();
        Color color = card.getColor(); // Tu Enum Color

        // Convertir el Enum Color a string en minúsculas (ej: "red", "blue")
        // excepto para WILD, donde no se usa el color en el nombre base.
        String colorString = "";
        if (color != Color.WILD) {
            colorString = color.name().toLowerCase();
        }

        switch (value) {
            // Cartas Numéricas (Formato: VALUE_color)
            case ZERO:  return "0_" + colorString;
            case ONE:   return "1_" + colorString;
            case TWO:   return "2_" + colorString;
            case THREE: return "3_" + colorString;
            case FOUR:  return "4_" + colorString;
            case FIVE:  return "5_" + colorString;
            case SIX:   return "6_" + colorString;
            case SEVEN: return "7_" + colorString;
            case EIGHT: return "8_" + colorString;
            case NINE:  return "9_" + colorString;

            // Cartas de Acción (Formato: action_color)
            case SKIP:    return "skip_" + colorString;
            // Asumiendo que '2_wild_draw_color.png' son las cartas DRAW_TWO (+2)
            case DRAW_TWO: return "2_wild_draw_" + colorString;

            // Cartas Comodín (Nombres específicos)
            case WILD:           return "change_color";     // Mapea a change_color.png
            case WILD_DRAW_FOUR: return "4_wild_draw";      // Mapea a 4_wild_draw.png

            default:
                // Fallback por si se añade un nuevo valor al Enum y no se actualiza aquí
                System.err.println("Advertencia: Valor de carta desconocido en getCardImageFilename: " + value);
                return EMPTY_IMAGE_NAME; // Usa el placeholder por defecto
        }
    }

    /**
     * Carga una imagen desde los recursos usando el nombre base.
     *
     * @param baseName Nombre del archivo sin ruta ni extensión (ej: "RED_5", "BACK").
     * @return El objeto Image cargado, o null si no se encuentra.
     */
    private Image getCardImage(String baseName) {
        String resourcePath = CARD_IMAGE_PATH_PREFIX + baseName + CARD_IMAGE_EXTENSION;
        try {
            InputStream stream = Main.class.getResourceAsStream(resourcePath);
            if (stream == null) {
                return null; // devolvemos null en caso de error
            }
            return new Image(stream);
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen: " + resourcePath);
            e.printStackTrace();
            return null; // Devuelve null si hay error
        }
    }

    /**
     * Extrae el objeto Card asociado a un ImageView que fue clickeado.
     * Asume que la Card fue almacenada usando setUserData().
     *
     * @param event El MouseEvent generado por el clic.
     * @return La Card asociada, o null si no se encuentra o el evento no es de un ImageView.
     */
    public Card extractCardFromEvent(MouseEvent event) {
        Object source = event.getSource();
        if (source instanceof ImageView) {
            ImageView clickedView = (ImageView) source;
            Object userData = clickedView.getUserData();
            if (userData instanceof Card) {
                return (Card) userData;
            } else {
                System.err.println("Advertencia: ImageView clickeado no tenía un objeto Card asociado.");
                return null;
            }
        }
        System.err.println("Advertencia: El evento de clic no provino de un ImageView.");
        return null;
    }

    /**
     * Handler para el evento de clic en una carta de la mano del jugador humano.
     * Intenta jugar la carta seleccionada llamando a la lógica en GameState.
     *
     * @param mouseEvent El evento del mouse que contiene información sobre el clic.
     */
    @FXML
    public void handlePlayCardClick(MouseEvent mouseEvent) {
        // Verificar si el juego ha terminado
        if (this.gameState.isGameOver()) {
            this.gameView.displayMessage("El juego ha terminado. Reinicia para jugar de nuevo.");
            return;
        }
        // Verificar si es el turno del jugador humano
        if (this.currentPlayer != this.humanPlayer) {
            this.gameView.displayMessage("Espera tu turno.");
            return;
        }
        // Obtener la carta seleccionada del ImageView clickeado
        Card selectedCard = this.gameView.getSelectedCardFromEvent(mouseEvent);
        if (selectedCard == null) {
            System.err.println("Error: No se pudo obtener la carta del ImageView");
            return;
        }
        this.clearPlayableCardHighlighting();

        // Validar si la jugada es legal
        if (this.gameState.isValidPlay(selectedCard)) {
            // Quitar efecto de la carta jugada ANTES de que desaparezca de la mano
            Node cardNode = findNodeForCard(this.playerHandHBox, selectedCard);
            if (cardNode != null) cardNode.setEffect(null);

            boolean gameEnded = this.gameState.playCard(this.humanPlayer, selectedCard);

            // Actualizar la vista DESPUÉS de que el modelo se haya actualizado
            this.updateViewAfterCardPlayed(selectedCard);

            // Verificar si el juego terminó DESPUÉS de actualizar la UI
            if (gameEnded) {
                this.handleGameOver();
                return;
            }


            // Verificar si el estado de UNO cambió para el jugador
            this.checkAndUpdateUnoButton(this.humanPlayer);

            // Si se jugó una carta Comodín, GameState indica que se debe elegir un color.
            if (selectedCard.getColor() == Color.WILD) {
                // El estado requiere elegir color, la vista debe pedirlo
                this.gameView.promptForColorChoice();
                // El avance de turno sucederá DESPUES de elegir el color
            } else {
                // Si no es una carta Comodín, proceder al siguiente turno inmediatamente
                this.gameState.onColorChosen(selectedCard.getColor());
                this.updateDiscardPileColorIndicator(selectedCard.getColor());
                this.handleTurnAdvancement();
            }
        } else {
            // La vista debe mostrar un mensaje indicando por qué la jugada fue inválida
            Card topCard = this.gameState.getTopDiscardCard();
            this.gameView.displayInvalidPlayMessage(selectedCard, topCard);
        }
    }


    @FXML
    public void handleMazoClick(MouseEvent mouseEvent) {
        if (this.gameState.isGameOver()) return;
        if (this.currentPlayer != this.humanPlayer) {
            this.gameView.displayMessage("Espera tu turno.");
            return;
        }

        // Limpiar resaltado de ayuda
        this.clearPlayableCardHighlighting();

        // Robar una carta
        Card drawnCard = this.gameState.drawTurnCard(this.humanPlayer);
        this.gameView.updatePlayerHand(this.humanPlayer.getCards());
        this.displayDrawCardMessage(drawnCard);

        // Si la carta robada es jugable, permitir al jugador jugarla
        if (this.gameState.isValidPlay(drawnCard)) {
            this.gameView.displayMessage("¡Robaste una carta jugable!");
        } else {
            // Si no puede jugarla, forzar pasar turno
            this.gameView.displayMessage("No puedes jugar la carta robada. Pasando turno...");
            this.handleTurnAdvancement();
        }
    }

    /** Muestra un mensaje indicando qué carta se robó. */
    public void displayDrawCardMessage(Card drawnCard) {
        if (drawnCard != null) {
            this.gameView.displayMessage("Robaste: " + this.gameState.getCardDescription(drawnCard));
        }
    }

    @FXML
    public void handleUnoButtonAction(ActionEvent actionEvent) {
        // TODO: Implementar lógica del botón UNO (declarar UNO, penalizaciones)
        this.gameView.displayMessage("Botón UNO presionado (lógica pendiente).");
        // Ocultar el botón después de presionarlo
        this.gameView.showUnoButton(false);
    }

    @FXML
    public void handleAidButtonAction(ActionEvent actionEvent) {
        if (this.currentPlayer != this.humanPlayer || this.gameState.isGameOver()) return;

        List<Card> playableCards = this.humanPlayer.getCards().stream()
                .filter(this.gameState::isValidPlay)
                .toList();

        if (playableCards.isEmpty()) {
            this.gameView.displayMessage("No tienes jugadas válidas. Debes robar del mazo.");
            highlightDeckForDrawing(); // Resaltar el mazo
        } else {
            this.gameView.highlightPlayableCards(playableCards);
            this.gameView.displayMessage("Cartas resaltadas son las que puedes jugar.");
        }
    }

    @FXML
    public void handleRestartButtonAction(ActionEvent actionEvent) {
        this.startNewGame();
    }

    @FXML
    public void handlePassButtonAction(ActionEvent actionEvent) {
        if (this.currentPlayer != this.humanPlayer || this.gameState.isGameOver()) return;

        this.clearPlayableCardHighlighting();
        this.gameView.displayMessage("Turno pasado.");
        this.handleTurnAdvancement();
    }

    /**
     * Actualiza todos los componentes relevantes de la UI después de que se juega una carta.
     */
    private void updateViewAfterCardPlayed(Card playedCard) {
        // Actualizar la mano del jugador
        this.gameView.updatePlayerHand(this.humanPlayer.getCards());

        // Actualizar la pila de descarte
        this.gameView.updateDiscardPile(
                this.gameState.getTopDiscardCard(),
                this.gameState.getCurrentValidColor()
        );
        // Actualizar contador de cartas de la máquina
        this.gameView.updateMachineHand(this.machinePlayer.getNumeroCartas());

        // Mostrar mensaje informativo
        this.gameView.displayCardPlayedMessage(playedCard);
    }

    /**
     * Maneja el fin del juego.
     */
    private void handleGameOver() {
        Player winner = this.gameState.getWinner();
        this.gameView.displayGameOver(winner.getName());
        this.gameView.disableGameInteractions();
        // Habilitar botón de reinicio u otras opciones post-juego
        this.gameView.enableRestartButton(true);
        if (this.executorService != null) {
            this.executorService.shutdownNow();
        }
    }

    /**
     * Avanza al turno del siguiente jugador.
     */
    private void handleTurnAdvancement() {
        this.clearPlayableCardHighlighting();
        this.deckImageView.setEffect(null);
        // Obtener el jugador actual desde el modelo
        this.currentPlayer = this.gameState.getCurrentPlayer();

        // Actualizar la UI para reflejar el cambio de turno
        this.gameView.updateTurnIndicator(this.currentPlayer.getName());
        boolean isHumanTurn = (this.currentPlayer == this.humanPlayer);
        this.gameView.enablePlayerInteraction(isHumanTurn);
        // habilitar el botón de pasar solo si el jugador no tiene jugadas obligatorias
        this.gameView.enablePassButton(isHumanTurn);
        this.gameView.displayMessage("Turno de " + this.currentPlayer.getName());

        // Comprobar estado UNO para el jugador que *inicia* su turno
        this.checkAndUpdateUnoButton(this.currentPlayer);

        // Si es el turno de la máquina, iniciar la lógica del turno de la máquina
        if (!isHumanTurn) {
            scheduleMachineTurn();
        }
    }

    /**
     * Programa que el turno de la máquina ocurra después de un pequeño retraso.
     */
    private void scheduleMachineTurn() {
        // Deshabilitar interacción mientras piensa la máquina
        this.gameView.enablePlayerInteraction(false);
        this.gameView.enablePassButton(false);
        this.gameView.displayMessage("Máquina pensando...");
        // Usar el servicio de ejecución para demorar ligeramente el turno de la máquina
        this.executorService.schedule(() -> {
            Platform.runLater(this::executeMachineTurn);
        }, 1500, TimeUnit.MILLISECONDS);  // Retraso de 1.5 segundos
    }

    /**
     * Ejecuta la lógica del turno del jugador máquina.
     */
    private void executeMachineTurn() {
        if (this.gameState.isGameOver()) return; // Comprobar de nuevo por si acaso

        // 1. Buscar una carta jugable
        Card cardToPlay = this.machinePlayer.chooseCardToPlay(this.gameState); // Necesita este método

        if (cardToPlay != null) {
            // 2a. Jugar la carta encontrada
            boolean gameEnded = this.gameState.playCard(this.machinePlayer, cardToPlay);
            // Si la máquina jugó comodín y eligió color, gameState ya actualizó currentValidColor.
            // El método playCard dentro de gameState debería manejar la lógica onMustChooseColor para la máquina.

            updateViewAfterMachinePlayed(cardToPlay); // Actualiza UI (contador, descarte)

            if (gameEnded) {
                handleGameOver();
                return;
            }
            // Comprobar UNO para la máquina
            checkAndUpdateUnoButton(this.machinePlayer);


        } else {
            // 2b. No hay carta jugable, robar una
            this.gameView.displayMessage("Máquina no tiene jugadas, robando...");
            Card drawnCard = this.gameState.drawTurnCard(this.machinePlayer); // Necesita método en GameState

            if (drawnCard == null) {
                this.gameView.displayMessage("Máquina no pudo robar (mazo vacío). Pasando.");
                // Avanzar turno (ya que no pudo hacer nada)
                handleTurnAdvancement();
                return;
            }

            this.gameView.updateMachineHand(this.machinePlayer.getNumeroCartas()); // Actualizar contador
            this.gameView.displayMessage("Máquina robó una carta.");

            // 3. Intentar jugar la carta robada
            if (this.gameState.isValidPlay(drawnCard)) {
                this.gameView.displayMessage("Máquina juega la carta robada: " + this.gameState.getCardDescription(drawnCard));
                boolean gameEnded = this.gameState.playCard(this.machinePlayer, drawnCard);
                updateViewAfterMachinePlayed(drawnCard); // Actualizar UI

                if (gameEnded) {
                    handleGameOver();
                    return;
                }
                // Comprobar UNO para la máquina
                checkAndUpdateUnoButton(this.machinePlayer);

            } else {
                this.gameView.displayMessage("Máquina no puede jugar la carta robada. Pasando turno.");
                // No se jugó nada, solo se robó. updateViewAfterMachinePlayed(null) podría ser útil si hace falta
                updateViewAfterMachinePlayed(null); // Para actualizar contadores si es necesario
            }
        }

        // 4. Avanzar al siguiente turno (humano)
        // El avance de turno se hace después de jugar o después de robar y no poder jugar.
        // Si se jugó comodín, onColorChosen dentro de playCard(machine) ya lo manejó.
        this.handleTurnAdvancement();
    }

    private void updateViewAfterMachinePlayed(Card playedCard) {
        // 1. Actualizar contador de cartas de la máquina
        this.gameView.updateMachineHand(this.machinePlayer.getNumeroCartas());

        // 2. Actualizar pila de descarte
        this.gameView.updateDiscardPile(
                this.gameState.getTopDiscardCard(),
                this.gameState.getCurrentValidColor()
        );
        // 3. Quitar efecto de color si la máquina jugó una carta normal
        if (playedCard != null && playedCard.getColor() != Color.WILD) {
            this.discardPileImageView.setEffect(null);
        }

        // 4. Mostrar mensaje
        if (playedCard != null) {
            this.gameView.displayMessage("Máquina jugó: " + this.gameState.getCardDescription(playedCard));
        } else {
            // Si playedCard es null, significa que la máquina robó y/o pasó
            // El mensaje específico debería venir de executeMachineTurn
        }

        // 5. Actualizar mano del jugador humano si la máquina le hizo robar
        this.gameView.updatePlayerHand(this.humanPlayer.getCards());
    }

    /**
     * Verifica el estado de UNO para un jugador y le indica a la Vista que actualice el botón.
     * @param player El jugador a verificar (usualmente el que acaba de terminar o comenzar su turno).
     */
    private void checkAndUpdateUnoButton(Player player) {
        boolean hasOneCard = player.getNumeroCartas() == 1;

        if (player == this.humanPlayer) {
            // Muestra u oculta el botón UNO para el humano
            this.gameView.showUnoButton(hasOneCard);
            if(hasOneCard) {
                this.gameView.displayMessage("¡Tienes una carta! ¡Presiona UNO!");
                // TODO: Iniciar temporizador para penalización si no presiona UNO
            }
        } else {
            // Máquina
            if (hasOneCard) {
                // La máquina "dice" UNO automáticamente (o con cierta probabilidad)
                this.gameView.displayMessage("¡Máquina dice UNO!");
                // gameState.processUnoDeclaration(this.machinePlayer, true); // Notificar al modelo
            }
        }
    }

    // --- Métodos de ayuda para efectos visuales ---

    /**
     * Resalta las cartas jugables en la mano del jugador.
     * @param playableCards Lista de cartas que son válidas para jugar.
     */
    public void highlightPlayableCards(List<Card> playableCards) {
        clearPlayableCardHighlighting(); // Limpia resaltados anteriores

        DropShadow playableGlow = new DropShadow();
        playableGlow.setColor(javafx.scene.paint.Color.LIGHTGREEN); // Verde claro para indicar jugable
        playableGlow.setWidth(20);
        playableGlow.setHeight(20);
        playableGlow.setSpread(0.6);

        for (Node node : this.playerHandHBox.getChildren()) {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                Object cardData = imageView.getUserData();
                if (cardData instanceof Card) {
                    Card card = (Card) cardData;
                    if (playableCards.contains(card)) {
                        imageView.setEffect(playableGlow); // Aplica efecto si la carta es jugable
                    } else {
                        imageView.setOpacity(0.6); // Atenúa las no jugables
                    }
                }
            }
        }
    }

    /**
     * Resalta el mazo para indicar que se debe robar.
     */
    private void highlightDeckForDrawing() {
        DropShadow deckGlow = new DropShadow();
        deckGlow.setColor(javafx.scene.paint.Color.YELLOW); // Amarillo para indicar acción requerida
        deckGlow.setWidth(25);
        deckGlow.setHeight(25);
        deckGlow.setSpread(0.7);
        this.deckImageView.setEffect(deckGlow);
    }

    /**
     * Encuentra el nodo ImageView correspondiente a una carta específica en un HBox.
     * @param container El HBox que contiene los ImageViews (ej. playerHandHBox).
     * @param cardToFind La carta cuyo ImageView se busca.
     * @return El Node (ImageView) encontrado, o null si no existe.
     */
    private Node findNodeForCard(HBox container, Card cardToFind) {
        for (Node node : container.getChildren()) {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                Object cardData = imageView.getUserData();
                if (cardData instanceof Card && cardData.equals(cardToFind)) {
                    return node;
                }
            }
        }
        return null; // No encontrado
    }

    /**
     * Aplica un efecto visual breve para indicar que una jugada fue inválida.
     * @param node El nodo (ImageView de la carta) al que aplicar el efecto.
     */
    private void applyInvalidPlayEffect(Node node) {
        // Ejemplo: un brillo rojo
        DropShadow errorGlow = new DropShadow();
        errorGlow.setColor(javafx.scene.paint.Color.RED);
        errorGlow.setWidth(20);
        errorGlow.setHeight(20);
        errorGlow.setSpread(0.6);
        node.setEffect(errorGlow);

        // Opcional: quitar el efecto después de un momento
         /*
         ScheduledExecutorService tempExecutor = Executors.newSingleThreadScheduledExecutor();
         tempExecutor.schedule(() -> {
             Platform.runLater(() -> node.setEffect(null));
             tempExecutor.shutdown();
         }, 500, TimeUnit.MILLISECONDS); // Quitar después de 0.5 segundos
         */
        // Nota: Si se usa el executor, cuidado con crear muchos si hay clics inválidos rápidos.
        // Mejor quitar el efecto al inicio del siguiente intento de jugada o al pasar turno.
    }
}
