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
            return;
        }
        // Verificar si es el turno del jugador humano
        if (this.currentPlayer != this.humanPlayer) {
            return;
        }
        // Obtener la carta seleccionada del ImageView clickeado
        // TODO: Esto probablemente debería ir en la capa de vista
        Card selectedCard = this.gameView.getSelectedCardFromEvent(mouseEvent);
        if (selectedCard == null) {
            System.err.println("Error: No se pudo obtener la carta del ImageView");
            return;
        }

        // Validar si la jugada es legal
        if (this.gameState.isValidPlay(selectedCard)) {
            boolean gameEnded = this.gameState.playCard(this.humanPlayer, selectedCard);
            this.gameState.playCard(this.humanPlayer, selectedCard);

            // TODO: Se debe hacer un procesamiento Post-Jugada junto con GameView
            // La vista debe actualizar la mano del jugador (quitando la carta),
            // actualizar la pila de descarte (mostrando la nueva carta),
            // y actualizar el contador de cartas de la máquina (si cambió).
            this.updateViewAfterCardPlayed(selectedCard);

            // Verificar si el juego terminó DESPUÉS de actualizar la interfaz
            if (gameEnded) {
                this.handleGameOver();
                return; // Detener el procesamiento del turno
            }

            // Verificar si el estado de UNO cambió para el jugador
            this.checkAndUpdateUnoButton(this.humanPlayer);

            // Si se jugó una carta Comodín, GameState indica que se debe elegir un color.
            if (selectedCard.getColor() == Color.WILD) {
                // --- Sugerencia para la Vista ---
                // La vista debe mostrar un diálogo (modal o integrado) con los 4 colores.
                // Cuando el usuario elija, la vista debe llamar a controller.handleColorChosen(chosenColor).
                this.gameView.promptForColorChoice();
                // El avance de turno sucederá *después* de elegir el color en handleColorChosen
            } else {
                // Si no es una carta Comodín, proceder al siguiente turno inmediatamente
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
    }

    @FXML
    public void handleUnoButtonAction(ActionEvent actionEvent) {
    }

    @FXML
    public void handleAidButtonAction(ActionEvent actionEvent) {
    }

    @FXML
    public void handleRestartButtonAction(ActionEvent actionEvent) {
    }

    @FXML
    public void handlePassButtonAction(ActionEvent actionEvent) {
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

        // Mostrar mensaje informativo
        this.gameView.displayCardPlayedMessage(playedCard);
    }

    /**
     * Maneja el fin del juego.
     */
    private void handleGameOver() {
        Player winner = this.gameState.winner;
        this.gameView.displayGameOver(winner.getName());
        this.gameView.disableGameInteractions();
        // Habilitar botón de reinicio u otras opciones post-juego
        this.gameView.enableRestartButton(true);
    }

    /**
     * Avanza al turno del siguiente jugador.
     */
    private void handleTurnAdvancement() {
        // Obtener el jugador actual desde el modelo
        this.currentPlayer = this.gameState.getCurrentPlayer();

        // Actualizar la UI para reflejar el cambio de turno
        this.gameView.updateTurnIndicator(this.currentPlayer.getName());
        this.gameView.enablePassButton(this.currentPlayer == this.humanPlayer);
        this.gameView.enablePlayerInteraction(this.currentPlayer == this.humanPlayer);

        // Si es el turno de la máquina, iniciar la lógica del turno de la máquina
        if (this.currentPlayer == this.machinePlayer) {
            this.scheduleMachineTurn();
        }
    }

    /**
     * Programa que el turno de la máquina ocurra después de un pequeño retraso.
     */
    private void scheduleMachineTurn() {
        // Usar el servicio de ejecución para demorar ligeramente el turno de la máquina
        this.executorService.schedule(() -> {
            Platform.runLater(() -> this.executeMachineTurn());
        }, 1500, TimeUnit.MILLISECONDS);
    }

    /**
     * Ejecuta la lógica del turno del jugador máquina.
     */
    private void executeMachineTurn() {
        // Esto debería implementarse para manejar el turno de la máquina
        // Por ejemplo, la máquina podría necesitar elegir una carta para jugar
        // o robar una carta del mazo
        // ...
    }

    /**
     * Verifica el estado de UNO para un jugador y le indica a la Vista que actualice el botón.
     * @param player El jugador a verificar (usualmente el que acaba de terminar o comenzar su turno).
     */
    private void checkAndUpdateUnoButton(Player player) {
        boolean hasOneCard = player.getNumeroCartas() == 1;
        if (player == this.humanPlayer) {
            // --- Sugerencia para la Vista ---
            // La vista debe mostrar u ocultar el botón de UNO para el jugador humano.
            this.gameView.showUnoButton(hasOneCard);
        } else {
            // Para la máquina, podríamos simular la llamada de "UNO"
            if (hasOneCard) {
                System.out.println("Controller: La máquina está en estado UNO.");
                // Simular que la máquina declara UNO
                // TODO: Agregar lógica para que la máquina declare UNO y posibles penalizaciones
                // this.gameView.displayMessage("Computadora dice ¡UNO!");
                // gameState.processUnoDeclaration(this.machinePlayer, true);
            }
        }
    }
}
