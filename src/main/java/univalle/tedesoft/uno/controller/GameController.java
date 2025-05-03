package univalle.tedesoft.uno.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import univalle.tedesoft.uno.model.Cards.Card;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Players.HumanPlayer;
import univalle.tedesoft.uno.model.Players.MachinePlayer;
import univalle.tedesoft.uno.model.Players.Player;
import univalle.tedesoft.uno.model.State.GameState;
import univalle.tedesoft.uno.model.State.IGameState;
import univalle.tedesoft.uno.view.GameView;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameController {
    @FXML public Label machineCardsCountLabel;
    @FXML public HBox machineHandHBox;
    @FXML public ImageView deckImageView;
    @FXML public ImageView discardPileImageView;
    @FXML public Label messageLabel;
    @FXML public Label turnLabel;
    @FXML public HBox playerHandHBox;
    @FXML public Button passButton;
    @FXML public Button unoButton;
    @FXML public ProgressIndicator unoTimerIndicator;
    @FXML public Button restartButton;
    @FXML public Button aidButton;

    // --- Model ---
    private IGameState gameState;
    private HumanPlayer humanPlayer;
    private MachinePlayer machinePlayer;
    /**
     * Representa al jugador cuyo turno esta activo en el juego.
     * Se actualiza dinamicamente durante el juego a medida que los jugadores se turnan.
     */
    private Player currentPlayer;

    // --- View ---
    private GameView gameView;

    // --- Threads ---
    private ScheduledExecutorService executorService;

    @FXML
    public void initialize() {
        this.humanPlayer = new HumanPlayer("Pepito");
        this.machinePlayer = new MachinePlayer();

        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Establece la referencia a la GameView y arranca el juego si no se ha iniciado.
     * Llamado por GameView después de cargar el FXML.
     * @param gameView La instancia de GameView.
     */
    public void setGameView(GameView gameView) {
        this.gameView = gameView;
        // Ahora que la vista está lista, podemos configurar su estado inicial
        this.gameView.initializeUI(); // Configura imágenes por defecto, etc.

        // Si el estado del juego aún no existe, iniciar una nueva partida
        if (this.gameState == null) {
            this.startNewGame();
        }
    }

    /**
     * Inicia o reinicia una nueva partida.
     */
    private void startNewGame() {
        // Detener tareas programadas anteriores si existen
        if (this.executorService != null && !this.executorService.isShutdown()) {
            this.executorService.shutdownNow();
        }
        this.executorService = Executors.newSingleThreadScheduledExecutor();

        // Crear nuevo estado de juego y inicializarlo
        this.gameState = new GameState(this.humanPlayer, this.machinePlayer);
        this.gameState.onGameStart();
        this.currentPlayer = this.gameState.getCurrentPlayer();

        // Pedir a la vista que se reinicie y muestre el estado inicial
        this.gameView.resetUIForNewGame();
        this.gameView.displayInitialState(
                this.humanPlayer.getCards(),
                this.gameState.getTopDiscardCard(),
                this.gameState.getCurrentValidColor(), // Pasar color efectivo inicial
                this.machinePlayer.getNumeroCartas(),
                this.currentPlayer.getName()
        );
        this.updateInteractionBasedOnTurn();
        this.gameView.displayMessage("¡Tu turno, " + this.humanPlayer.getName() + "!");
        this.gameView.enableRestartButton(true);
    }

    // --- EventHandlers FXML ---

    /**
     * Maneja el clic en una carta de la mano del jugador.
     * @param mouseEvent El evento del mouse.
     */
    @FXML
    public void handlePlayCardClick(MouseEvent mouseEvent) {
        if (this.gameState.isGameOver() || this.currentPlayer != this.humanPlayer) {
            this.gameView.displayMessage(this.gameState.isGameOver() ? "El juego ha terminado." : "Espera tu turno.");
            return;
        }
        Card selectedCard = this.gameView.extractCardFromEvent(mouseEvent);
        if (selectedCard == null) {
            System.err.println("Error: No se pudo obtener la carta del evento de clic.");
            return;
        }
        // Limpiar resaltados
        this.gameView.clearPlayerHandHighlights();
        //
        if (this.gameState.isValidPlay(selectedCard)) {
            // El modelo se actualiza primero
            boolean gameEnded = this.gameState.playCard(this.humanPlayer, selectedCard);
            // Actualizar la vista completa después de la jugada
            this.updateViewAfterHumanPlay(selectedCard);
            // Evaluar si el juego ha terminado
            if (gameEnded) {
                this.handleGameOver();
                return;
            }

            this.checkAndUpdateUnoButtonVisuals(this.humanPlayer);
            // Si se jugó un comodín, el estado cambió y necesitamos elegir color
            if (selectedCard.getColor() == Color.WILD) {
                // El turno avanzará después de elegir el color
                this.promptHumanForColorChoice();
            } else {
                // Si no es comodín, el color válido se actualiza automáticamente
                this.handleTurnAdvancement();
            }
        } else {
            Card topCard = this.gameState.getTopDiscardCard();
            String playedCardDescription = this.gameState.getCardDescription(selectedCard);
            String topCardDescription = this.gameState.getCardDescription(topCard);
            this.gameView.displayInvalidPlayMessage(
                    selectedCard,
                    topCard,
                    playedCardDescription,
                    topCardDescription
            );
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

    /**
     * Maneja la acción del botón "Reiniciar".
     * @param actionEvent El evento de acción.
     */
    @FXML
    public void handleRestartButtonAction(ActionEvent actionEvent) {
        this.startNewGame();
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