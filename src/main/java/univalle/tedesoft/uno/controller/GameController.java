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

    /**
     * Maneja el clic en el mazo para robar una carta.
     * @param mouseEvent El evento del mouse.
     */
    @FXML
    public void handleMazoClick(MouseEvent mouseEvent) {
        if (this.gameState.isGameOver() || this.currentPlayer != this.humanPlayer) {
            if (this.gameState.isGameOver()) {
                this.gameView.displayMessage("El juego ha terminado.");
            } else {
                this.gameView.displayMessage("Espera tu turno.");
            }
            return;
        }
        // Limpiar resaltados
        this.gameView.clearPlayerHandHighlights();
        // Quitar resaltado del mazo si lo había
        this.gameView.highlightDeck(false);
        // Robar carta del modelo
        Card drawnCard = this.gameState.drawTurnCard(this.humanPlayer);
        // Actualizar vista de la mano
        this.gameView.updatePlayerHand(this.humanPlayer.getCards(), this);

        if (drawnCard != null) {
            this.gameView.displayMessage("Robaste: " + this.gameState.getCardDescription(drawnCard));
            // Comprobar si la carta robada se puede jugar
            if (this.gameState.isValidPlay(drawnCard)) {
                this.gameView.displayMessage("¡Robaste una carta jugable! Puedes jugarla o pasar.");
                this.gameView.enablePassButton(true);
            } else {
                // Pasar turno automáticamente si no es jugable
                // TODO: evaluar si esto es válido de acuerdo a los requerimientos
                this.gameView.displayMessage("No puedes jugar la carta robada. Pasando turno...");
                this.handleTurnAdvancement();
            }
        } else {
            this.gameView.displayMessage("No hay más cartas para robar. Pasando turno...");
            this.handleTurnAdvancement();
        }
    }

    /**
     * Maneja la acción del botón "UNO!".
     * @param actionEvent El evento de acción.
     */
    @FXML
    public void handleUnoButtonAction(ActionEvent actionEvent) {
        if (this.gameState.isGameOver() || this.currentPlayer != this.humanPlayer) {
            return;
        }
        // TODO: Aquí iría la lógica para registrar que el jugador dijo "UNO"
        // Por ahora, solo ocultamos el botón y mostramos mensaje
        this.gameView.displayMessage("¡Declaraste UNO!");
        this.gameView.showUnoButton(false);
        //gameState.declareUno(this.humanPlayer); // TODO: Notificar al modelo
    }

    /**
     * Maneja la acción del botón de Ayuda ("?").
     * @param actionEvent El evento de acción.
     */
    @FXML
    public void handleAidButtonAction(ActionEvent actionEvent) {
        if (this.gameState.isGameOver() || this.currentPlayer != this.humanPlayer) {
            return;
        }

        List<Card> playableCards = this.humanPlayer.getCards().stream()
                .filter(this.gameState::isValidPlay)
                .toList();

        if (playableCards.isEmpty()) {
            // Si el jugador no tiene cartas jugables, debe tomar del mazo
            this.gameView.displayMessage("No tienes jugadas válidas. Debes robar del mazo.");
            this.gameView.highlightDeck(true);
        } else {
            // Si tiene cartas jugables, le damos una ayuda de cuales puede jugar
            this.gameView.highlightPlayableCards(playableCards);
            this.gameView.displayMessage("Cartas resaltadas son las que puedes jugar.");
        }
    }

    /**
     * Maneja la acción del botón "Reiniciar".
     * @param actionEvent El evento de acción.
     */
    @FXML
    public void handleRestartButtonAction(ActionEvent actionEvent) {
        this.startNewGame();
    }

    /**
     * Maneja la acción del botón "Pasar".
     * @param actionEvent El evento de acción.
     */
    @FXML
    public void handlePassButtonAction(ActionEvent actionEvent) {
        // Ignorar si no es turno del humano o el juego terminó
        if (this.gameState.isGameOver() || this.currentPlayer != this.humanPlayer) {
            return;
        }
        this.gameView.clearPlayerHandHighlights();
        this.gameView.displayMessage("Turno pasado.");
        this.handleTurnAdvancement();
    }

    // --- Lógica del Juego ---

    /**
     * Avanza al siguiente turno, actualiza la UI y maneja el turno de la máquina si corresponde.
     */
    private void handleTurnAdvancement() {
        // Limpiar ayudas visuales
        this.gameView.clearPlayerHandHighlights();
        this.gameView.highlightDeck(false);

        // Traer al jugador actual
        this.currentPlayer = this.gameState.getCurrentPlayer();
        // Actualizar indicador de turno en la vista
        this.gameView.updateTurnIndicator(this.currentPlayer.getName());
        this.gameView.displayMessage("Turno de " + this.currentPlayer.getName());
        // Habilitar/deshabilitar controles según de quién sea el turno
        this.updateInteractionBasedOnTurn();
        // Comprobar estado UNO para el jugador que inicia su turno
        this.checkAndUpdateUnoButtonVisuals(this.currentPlayer);

        // Si es el turno de la máquina, programar su ejecución
        if (this.currentPlayer == this.machinePlayer) {
            this.scheduleMachineTurn();
        }
    }

    /**
     * Pide a la vista que muestre el diálogo de elección de color y procesa la selección.
     */
    private void promptHumanForColorChoice() {
        Optional<String> result = this.gameView.promptForColorChoice(); // La vista mostrará el diálogo de elección
        result.ifPresentOrElse(
                colorName -> {
                    Color chosenColor = Color.valueOf(colorName);
                    // Notificar al modelo
                    this.gameState.onColorChosen(chosenColor);
                    // Actualizar la vista para reflejar el color elegido en el borde de la pila de descarte
                    this.gameView.updateDiscardPile(this.gameState.getTopDiscardCard(), chosenColor);
                    this.gameView.displayMessage("Color cambiado a " + chosenColor.name());
                    // Avanzar el turno
                    this.handleTurnAdvancement();
                },
                () -> {
                    // Si el usuario cerró el diálogo sin elegir, lo obligamos a elegir
                    this.gameView.displayMessage("Debes elegir un color para continuar >:c");
                    this.promptHumanForColorChoice();
                }
        );
    }

    /**
     * Programa la ejecución del turno de la máquina con un retraso.
     */
    private void scheduleMachineTurn() {
        // Usar el servicio de ejecución para demorar ligeramente el turno de la máquina
        this.executorService.schedule(() -> {
            Platform.runLater(() -> this.executeMachineTurn());
        }, 1500, TimeUnit.MILLISECONDS);
    }

    /**
     * Ejecuta la lógica del turno de la máquina.
     */
    private void executeMachineTurn() {
        if (this.gameState.isGameOver()) return;

        // 1. Máquina elige qué jugar (lógica en MachinePlayer/GameState)
        Card cardToPlay = this.machinePlayer.chooseCardToPlay(this.gameState);

        if (cardToPlay != null) {
            // 2a. Jugar la carta elegida
            this.gameView.displayMessage("Máquina juega: " + this.gameState.getCardDescription(cardToPlay));
            boolean gameEnded = this.gameState.playCard(this.machinePlayer, cardToPlay);

            // Actualizar vista DESPUÉS de que el modelo cambió
            this.updateViewAfterMachinePlay(cardToPlay);

            if (gameEnded) {
                this.handleGameOver();
                return;
            }

            this.checkAndUpdateUnoButtonVisuals(this.machinePlayer); // Comprobar UNO para la máquina

            // Si la máquina jugó comodín, el modelo actualizó currentValidColor y
            // la vista se actualizó en updateViewAfterMachinePlay. Avanzamos turno.
            this.handleTurnAdvancement();
        } else {
            // 2b. No hay carta jugable, robar una
            this.gameView.displayMessage("Máquina no tiene jugadas, robando...");
            Card drawnCard = this.gameState.drawTurnCard(this.machinePlayer);

            // Actualizar contador de cartas de la máquina en la vista
            this.gameView.updateMachineHand(this.machinePlayer.getNumeroCartas());

            if (drawnCard == null) {
                this.gameView.displayMessage("Máquina no pudo robar (mazo vacío). Pasando.");
                this.handleTurnAdvancement(); // Avanzar turno si no se pudo robar
                return;
            }

            this.gameView.displayMessage("Máquina robó una carta.");

            // 3. Intentar jugar la carta robada
            if (this.gameState.isValidPlay(drawnCard)) {
                this.gameView.displayMessage("Máquina juega la carta robada: " + this.gameState.getCardDescription(drawnCard));
                boolean gameEnded = this.gameState.playCard(this.machinePlayer, drawnCard);

                this.updateViewAfterMachinePlay(drawnCard); // Actualizar vista

                if (gameEnded) {
                    this.handleGameOver();
                    return;
                }
                this.checkAndUpdateUnoButtonVisuals(this.machinePlayer); // Comprobar UNO de nuevo
                this.handleTurnAdvancement(); // Avanzar turno después de jugar la robada

            } else {
                this.gameView.displayMessage("Máquina no puede jugar la carta robada. Pasando turno.");
                // No se jugó nada más, solo se robó. Avanzamos turno.
                this.handleTurnAdvancement();
            }
        }
    }

    /**
     * Maneja el final de la partida.
     * Deshabilita botones de interacción, renderiza el dialógo del ganador y habilita el botón de reinicio.
     */
    private void handleGameOver() {
        Player winner = this.gameState.getWinner();
        this.gameView.displayGameOver(winner.getName());
        this.gameView.disableGameInteractions();
        this.gameView.enableRestartButton(true);

        // Detener ejecuciones pendientes
        if (this.executorService != null) {
            this.executorService.shutdownNow();
        }
    }

    /**
     * Actualiza los componentes de la vista después de que el jugador humano juega una carta.
     * @param playedCard La carta que jugó el humano.
     */
    private void updateViewAfterHumanPlay(Card playedCard) {
        this.gameView.updatePlayerHand(this.humanPlayer.getCards(), this);
        this.gameView.updateDiscardPile(
                this.gameState.getTopDiscardCard(),
                this.gameState.getCurrentValidColor() // El color efectivo puede cambiar
        );
        // Si la máquina fue forzada a robar, su contador debe actualizarse
        this.gameView.updateMachineHand(this.machinePlayer.getNumeroCartas());
        String playedCardDescription = this.gameState.getCardDescription(playedCard);
        this.gameView.displayCardPlayedMessage(playedCard, playedCardDescription);
    }

    /**
     * Actualiza los componentes de la vista después de que la máquina juega una carta o roba.
     * @param playedCard La carta que jugó la máquina, o null si solo robó y/o pasó.
     */
    private void updateViewAfterMachinePlay(Card playedCard) {
        // Actualizar contador visual de la máquina
        this.gameView.updateMachineHand(this.machinePlayer.getNumeroCartas());
        // Actualizar la pila de descarte con la carta jugada (si la hubo) y el color efectivo
        this.gameView.updateDiscardPile(
                this.gameState.getTopDiscardCard(),
                this.gameState.getCurrentValidColor()
        );
        // Si el humano fue forzado a robar, actualizar su mano
        this.gameView.updatePlayerHand(this.humanPlayer.getCards(), this);
    }

    /**
     * Habilita o deshabilita los controles del jugador humano dependiendo de su turno.
     * Habilita el botón de 'Pasar' solo si es turno humano y no está obligado a tomar cartas.
     */
    private void updateInteractionBasedOnTurn() {
        boolean isHumanTurn = (this.currentPlayer == this.humanPlayer);
        this.gameView.enablePlayerInteraction(isHumanTurn);
        this.gameView.enablePassButton(isHumanTurn);
        // TODO: toca revisar como reutilizar este método para el caso de la máquina, que botones queremos deshabilitar o así
    }

    /**
     * Verifica si un jugador tiene una sola carta y actualiza la visibilidad
     * del botón UNO y muestra mensajes relevantes en la vista.
     * @param player El jugador a verificar.
     */
    private void checkAndUpdateUnoButtonVisuals(Player player) {
        boolean hasOneCard = player.getNumeroCartas() == 1;

        if (player == this.humanPlayer) {
            this.gameView.showUnoButton(hasOneCard); // Mostrar/ocultar botón para humano
            if (hasOneCard) {
                this.gameView.displayMessage("¡Tienes una carta! ¡Presiona UNO!");
                // TODO: Iniciar temporizador para penalización si no presiona UNO
                // this.gameView.showUnoPenaltyTimer(true);
                // startUnoTimer();
            } else {
                // Asegurarse de que el timer esté oculto si ya no tiene una carta
                // this.gameView.showUnoPenaltyTimer(false);
            }
        } else {
            // Lógica de la máquina cuando tiene la opción de decir 'UNO'
            if (hasOneCard) {
                this.gameView.displayMessage("¡Máquina dice UNO!"); // Mensaje para la máquina
                // gameState.declareUno(this.machinePlayer); // Lógica interna de la máquina (si aplica)
            }
        }
    }
}