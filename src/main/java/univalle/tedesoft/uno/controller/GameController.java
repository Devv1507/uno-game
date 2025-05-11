package univalle.tedesoft.uno.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameController {
    @FXML public Label machineCardsCountLabel;
    @FXML public HBox machineHandHBox;
    @FXML public ImageView deckImageView;
    @FXML public ImageView discardPileImageView;
    @FXML public Label messageLabel;
    @FXML public Label turnLabel;
    @FXML public HBox playerHandHBox;
    @FXML public Button unoButton;
    @FXML public ProgressIndicator unoTimerIndicator;
    @FXML public Button restartButton;
    @FXML public Button aidButton;
    @FXML public Label playerNameLabel;
    @FXML public Button punishUnoButton;
    @FXML public VBox messageContainer;

    // --- Model ---
    private IGameState gameState;
    private HumanPlayer humanPlayer;
    private MachinePlayer machinePlayer;

    // --- Variables ---
    private String playerName;

    /**
     * Representa al jugador cuyo turno esta activo en el juego.
     * Se actualiza dinamicamente durante el juego a medida que los jugadores se turnan.
     */
    private Player currentPlayer;

    // --- View ---
    private GameView gameView;

    // --- Threads ---
    private ScheduledExecutorService executorService;
    // --- Timers para declarar UNO --
    private ScheduledFuture<?> humanUnoTimerTask;
    private ScheduledFuture<?> machineCatchUnoTimerTask;
    private final Random randomGenerator = new Random();
    // --- Constantes para controlar las ventanas de tiempo para declarar UNO ---
    private static final int UNO_PLAYER_RESPONSE_TIME_SECONDS = 4;
    private static final int MACHINE_CATCH_MIN_DELAY_MS = 2000; // 2 segundos
    private static final int MACHINE_CATCH_MAX_DELAY_MS = 4000; // 4 segundos
    private static final long MACHINE_TURN_THINK_DELAY_MS = 1500;
    private static final double CHANCE_MACHINE_FORGETS_UNO = 0.3; // 30% de probabilidad de que la máquina olvide

    // Bandera para controlar si el avance del turno es debido al timer de UNO del jugador
    private boolean advanceDueToUnoTimer = false;
    private boolean isChoosingColor = false;
    private boolean canPunishMachine = false; // Nueva bandera

    @FXML
    public void initialize() {
        // Inicializar el HumanPlayer con un nombre vacío
        this.humanPlayer = new HumanPlayer("");
        this.machinePlayer = new MachinePlayer();
        // No reiniciar al ejecutor si ya existe de una partida anterior y está activo.
        if (this.executorService == null || this.executorService.isShutdown()) {
            this.executorService = Executors.newSingleThreadScheduledExecutor();
        }
        
        // Si ya tenemos un nombre almacenado, actualizarlo
        if (this.playerName != null && !this.playerName.isEmpty()) {
            this.humanPlayer.setName(this.playerName);
            if (this.playerNameLabel != null) {
                this.playerNameLabel.setText("Jugador: " + this.playerName);
            }
        }
        // Asegurar que el botón de UNO y el timer están ocultos al principio
        if (this.unoButton != null) {
            this.unoButton.setVisible(false);
        }
        if (this.unoTimerIndicator != null) {
            this.unoTimerIndicator.setVisible(false);
        }
        // TODO: recordar mover esto a la lógica de GameView
        if (this.punishUnoButton != null) { // Inicializar estado del nuevo botón
            this.punishUnoButton.setVisible(false);
            this.punishUnoButton.setDisable(true);
        }
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
        this.cancelAllTimers();
        if (this.executorService != null && !this.executorService.isShutdown()) {
            this.executorService.shutdownNow(); // Detener tareas anteriores
        }
        this.executorService = Executors.newSingleThreadScheduledExecutor();

        // Asegurarnos de que el HumanPlayer mantenga su nombre
        if (this.playerName != null && !this.playerName.isEmpty()) {
            this.humanPlayer.setName(this.playerName);
        }

        // Crear nuevo estado de juego e inicializarlo
        this.gameState = new GameState(this.humanPlayer, this.machinePlayer);
        this.gameState.onGameStart();
        this.currentPlayer = this.gameState.getCurrentPlayer();

        // Pedir a la vista que se reinicie y muestre el estado inicial
        this.gameView.resetUIForNewGame();
        this.gameView.displayInitialState(
                this.humanPlayer.getCards(),
                this.gameState.getTopDiscardCard(),
                this.gameState.getCurrentValidColor(),
                this.machinePlayer.getNumeroCartas(),
                this.currentPlayer.getName()
        );
        this.updateInteractionBasedOnTurn();
        
        // Usar el nombre almacenado en playerName si existe
        String displayName = this.playerName != null && !this.playerName.isEmpty() ? 
            this.playerName : this.humanPlayer.getName();
        this.gameView.displayMessage("¡Tu turno, " + displayName + "!");
        this.gameView.enableRestartButton(true);

        // Asegurar que los estados de UNO estén limpios para la nueva partida
        this.humanPlayer.resetUnoStatus();
        this.machinePlayer.resetUnoStatus();
        this.updateUnoVisualsForHuman();
        this.advanceDueToUnoTimer = false;
        this.isChoosingColor = false;
        this.canPunishMachine = false; // Resetear bandera
        this.updatePunishUnoButtonVisuals(); // Actualizar estado del botón
    }

    /**
     * Establece el nombre del jugador humano y actualiza la interfaz.
     * @param playerName El nombre del jugador
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        // Actualizar el nombre en el HumanPlayer
        this.humanPlayer.setName(playerName);
        
        Platform.runLater(() -> {
            if (this.playerNameLabel != null) {
                this.playerNameLabel.setText("Jugador: " + playerName);
            }
            // Actualizar también el mensaje inicial
            this.gameView.displayMessage("¡Tu turno, " + playerName + "!");
        });
    }

    // --- EventHandlers FXML ---

    /**
     * Maneja el clic en una carta de la mano del jugador.
     * @param mouseEvent El evento del mouse.
     */
    @FXML
    public void handlePlayCardClick(MouseEvent mouseEvent) {
        // TODO: pasar este ternario a if-else clásico
        if (this.gameState.isGameOver() || this.currentPlayer != this.humanPlayer || this.isChoosingColor) {
            this.gameView.displayMessage(this.gameState.isGameOver() ? "El juego ha terminado." :
                    (this.isChoosingColor ? "Elige un color primero." : "Espera tu turno."));
            return;
        }
        this.canPunishMachine = false;
        // Si el jugador era candidato a UNO y juega otra carta sin decir UNO, se penaliza
        if (this.humanPlayer.isUnoCandidate()) {
            this.penalizeHumanForMissingUno("¡Debiste decir UNO antes de jugar otra carta!");
            this.humanPlayer.resetUnoStatus();
            this.processTurnAdvancement();
            return;
        }
        this.cancelHumanUnoTimer();

        Card selectedCard = this.gameView.extractCardFromEvent(mouseEvent);
        if (selectedCard == null) {
            System.err.println("Error: No se pudo obtener la carta del evento de clic.");
            return;
        }
        // Limpiar resaltados
        this.gameView.clearPlayerHandHighlights();
        // revisar si la jugada es valida
        if (this.gameState.isValidPlay(selectedCard)) {
            // consultar con el modelo el resultado de la jugada
            boolean gameEnded = this.gameState.playCard(this.humanPlayer, selectedCard);
            // Actualizar la vista completa después de la jugada
            this.updateViewAfterHumanPlay(selectedCard);
            // Evaluar si el juego ha terminado
            if (gameEnded) {
                this.handleGameOver();
                return;
            }

            // Lógica UNO para el jugador humano
            if (this.humanPlayer.isUnoCandidate()) {
                this.gameView.displayMessage("¡Tienes una carta! ¡Presiona UNO en " + UNO_PLAYER_RESPONSE_TIME_SECONDS + " segundos!");
                this.startHumanUnoTimer();
            } else {
                // Si no es candidato a UNO, el turno avanza.
                this.updateUnoVisualsForHuman();
                if (selectedCard.getColor() != Color.WILD) {
                    this.processTurnAdvancement();
                }
            }

            // Si se jugó un comodín, el estado cambió y necesitamos elegir color
            if (selectedCard.getColor() == Color.WILD) {
                // El turno avanzará después de elegir el color
                this.promptHumanForColorChoice();
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
        // TODO: lo mismo
        if (this.gameState.isGameOver() || this.currentPlayer != this.humanPlayer || this.isChoosingColor) {
            this.gameView.displayMessage(this.gameState.isGameOver() ? "El juego ha terminado." :
                    (this.isChoosingColor ? "Elige un color primero." : "Espera tu turno."));
            return;
        }
        this.canPunishMachine = false;
        // Penalizar si era candidato a UNO y roba en lugar de decir UNO
        if (this.humanPlayer.isUnoCandidate() && !this.humanPlayer.hasDeclaredUnoThisTurn()) {
            this.penalizeHumanForMissingUno("¡Debiste decir UNO antes de robar!");
            this.humanPlayer.resetUnoStatus();
            this.processTurnAdvancement();
            return;
        }
        this.cancelHumanUnoTimer(); // Cancelar cualquier timer de UNO pendiente
        this.updateUnoVisualsForHuman();

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
            } else {
                // Pasar turno automáticamente si no es jugable
                // TODO: evaluar si esto es válido de acuerdo a los requerimientos
                this.gameView.displayMessage("No puedes jugar la carta robada. Pasando turno...");
                this.processTurnAdvancement();
            }
        } else {
            this.gameView.displayMessage("No hay más cartas para robar. Pasando turno...");
            this.processTurnAdvancement();
        }
    }

    /**
     * Maneja la acción del botón "UNO!".
     * @param actionEvent El evento de acción.
     */
    @FXML
    public void handleUnoButtonAction(ActionEvent actionEvent) {
        if (this.gameState.isGameOver() || this.currentPlayer != this.humanPlayer || this.isChoosingColor) {
            return;
        }
        // Evaluar si es candidato para declarar UNO
        if (this.humanPlayer.isUnoCandidate() || (this.humanPlayer.getNumeroCartas() == 1 && !this.humanPlayer.hasDeclaredUnoThisTurn())) {
            this.gameState.playerDeclaresUno(this.humanPlayer);
            this.gameView.displayMessage(this.humanPlayer.getName() + " declaró UNO!");
            this.cancelHumanUnoTimer();
            this.cancelMachineCatchUnoTimer(); // Detener timer de la máquina si estaba intentando atrapar
            this.updateUnoVisualsForHuman();
            // Si el jugador declaró UNO y estaba pendiente una elección de color (por un comodín previo)
            // no avanzamos el turno aún.
            this.canPunishMachine = false; // Ya no se puede castigar a la máquina si el humano acaba de decir UNO
            this.updatePunishUnoButtonVisuals();

            if (!this.isChoosingColor) {
                this.processTurnAdvancement();
            }
        } else {
            this.gameView.displayMessage("No es el momento adecuado para declarar UNO.");
        }
    }

    /**
     * Maneja la acción del botón de Ayuda ("?").
     * @param actionEvent El evento de acción.
     */
    @FXML
    public void handleAidButtonAction(ActionEvent actionEvent) {
        if (this.gameState.isGameOver() || this.currentPlayer != this.humanPlayer || this.isChoosingColor) {
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
     * Maneja la acción del botón para castigar a la máquina por no decir "UNO".
     * @param actionEvent El evento de acción.
     */
    @FXML
    public void handlePunishUnoButtonAction(ActionEvent actionEvent) {
        if (this.gameState.isGameOver() || this.currentPlayer != this.humanPlayer || this.isChoosingColor) {
            this.gameView.displayMessage("No puedes castigar ahora.");
            return;
        }

        // Condiciones para castigar a la máquina:
        // 1. Es turno del humano (implícito por la guarda anterior).
        // 2. La máquina tiene 1 carta.
        // 3. La máquina es candidata a UNO (significa que jugó una carta que la dejó con 1).
        // 4. La máquina NO ha declarado UNO en su oportunidad.
        if (this.machinePlayer.getNumeroCartas() == 1 &&
                this.machinePlayer.isUnoCandidate() &&
                !this.machinePlayer.hasDeclaredUnoThisTurn()) {

            this.gameView.displayMessage("¡Atrapaste a la Máquina! No dijo UNO. Roba " + GameState.PENALTY_CARDS_FOR_UNO + " cartas.");
            this.gameState.penalizePlayerForUno(this.machinePlayer);
            this.gameView.updateMachineHand(this.machinePlayer.getNumeroCartas());

            this.canPunishMachine = false; // Oportunidad de castigo usada
            this.updatePunishUnoButtonVisuals(); // Ocultar el botón
            // El turno del humano continúa. No se avanza el turno aquí.
        } else {
            this.gameView.displayMessage("No es el momento de castigar a la máquina.");
            this.canPunishMachine = false; // No había nada que castigar, ocultar botón.
            this.updatePunishUnoButtonVisuals();
        }
    }

    // --- Lógica del Juego ---

    /**
     * Procesa el avance del turno y las lógicas asociadas.
     * Este es el punto central para cambiar de jugador, actualizando la UI y
     * manejando el turno de la máquina si corresponde.
     */
    private void processTurnAdvancement() {
        if (this.isChoosingColor) {
            return;
        }
        this.advanceDueToUnoTimer = false; // Resetear la bandera
        // Limpiar ayudas visuales
        this.cancelHumanUnoTimer();
        this.updateUnoVisualsForHuman();
        // Guardar quién era el jugador anterior para la lógica de "atrapar UNO"
        Player previousPlayer = this.currentPlayer;
        // Actualizar el jugador actual desde el GameState
        this.gameState.advanceTurn();
        // Actualizar indicador de turno en la vista con el nombre del jugador actual
        this.currentPlayer = this.gameState.getCurrentPlayer();
        this.currentPlayer.resetUnoStatus();
        // Actualizar mensaje y controles
        this.gameView.clearPlayerHandHighlights();
        this.gameView.highlightDeck(false);
        this.gameView.updateTurnIndicator(this.currentPlayer.getName());
        this.gameView.displayMessage("Turno de " + this.currentPlayer.getName());
        this.canPunishMachine = false; // Resetear el condicional de castigar

        // Lógica para que la máquina "atrape" al humano
        // Chequea si el humano TIENE 1 carta y NO ha declarado UNO en su turno.
        if (this.currentPlayer == this.machinePlayer) {
            this.cancelMachineCatchUnoTimer();
            if (previousPlayer == this.humanPlayer &&  this.humanPlayer.getNumeroCartas() == 1 &&  !this.humanPlayer.hasDeclaredUnoThisTurn()) {
                this.startMachineCatchUnoTimer();
            } else {
                this.cancelMachineCatchUnoTimer();
                this.scheduleMachineTurn();
            }
        } else {
            this.cancelMachineCatchUnoTimer();
            // Verificar si la máquina acaba de jugar y "olvidó" decir UNO
            if (previousPlayer == this.machinePlayer &&
                    this.machinePlayer.getNumeroCartas() == 1 &&
                    this.machinePlayer.isUnoCandidate() && // Es importante que sea candidata (jugó y quedó con 1)
                    !this.machinePlayer.hasDeclaredUnoThisTurn()) {
                this.canPunishMachine = true;
                this.gameView.displayMessage("¡La máquina tiene UNO y no lo ha dicho! ¡Puedes castigarla!");
            }
            this.updatePunishUnoButtonVisuals();
        }
        // Actualizar la interacción y los botones después de toda la lógica de cambio de turno
        this.updateInteractionBasedOnTurn();
    }

    /**
     * Pide a la vista que muestre el diálogo de elección de color y procesa la selección.
     */
    private void promptHumanForColorChoice() {
        this.isChoosingColor = true;
        this.gameView.enablePlayerInteraction(false);
        Optional<String> result = this.gameView.promptForColorChoice(); // La vista mostrará el diálogo de elección

        this.isChoosingColor = false;
        this.gameView.enablePlayerInteraction(true);
        this.updateInteractionBasedOnTurn(); // Actualizar botones ahora que el diálogo cerró
        result.ifPresentOrElse(
                colorName -> {
                    Color chosenColor = Color.valueOf(colorName);
                    // Notificar al modelo
                    this.gameState.onColorChosen(chosenColor);
                    // Actualizar la vista para reflejar el color elegido en el borde de la pila de descarte
                    this.gameView.updateDiscardPile(this.gameState.getTopDiscardCard(), chosenColor);
                    this.gameView.displayMessage("Color cambiado a " + chosenColor.name());
                    // Avanzar el turno
                    if (this.humanUnoTimerTask == null || this.humanUnoTimerTask.isDone() || this.humanPlayer.hasDeclaredUnoThisTurn()) {
                        this.processTurnAdvancement();
                    }
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
        if (this.currentPlayer != this.machinePlayer || this.gameState.isGameOver()) {
            return;
        }
        this.gameView.displayMessage("Máquina pensando...");
        // Usar el servicio de ejecución para demorar el turno de la máquina
        this.executorService.schedule(() -> {
            Platform.runLater(this::executeMachineTurnLogic); // Ejecutar en el hilo de JavaFX
        }, MACHINE_TURN_THINK_DELAY_MS, TimeUnit.MILLISECONDS); // Retraso de MACHINE_TURN_THINK_DELAY_MS segundos
    }


    /**
     * Ejecuta la lógica del turno de la máquina.
     */
    private void executeMachineTurnLogic() {
        if (this.gameState.isGameOver() || this.currentPlayer != this.machinePlayer) {
            return;
        }
        // 1. La máquina elige qué jugar
        Card cardToPlay = this.machinePlayer.chooseCardToPlay(this.gameState);
        if (cardToPlay != null) {
            // Jugar la carta elegida
            this.gameView.displayMessage("Máquina juega: " + this.gameState.getCardDescription(cardToPlay));
            boolean gameEnded = this.gameState.playCard(this.machinePlayer, cardToPlay);
            this.updateViewAfterMachinePlay(cardToPlay);

            if (gameEnded) {
                this.handleGameOver();
                return;
            }
            // Lógica para que la máquina "diga" o "olvide" UNO
            if (this.machinePlayer.isUnoCandidate()) {
                if (this.randomGenerator.nextDouble() > CHANCE_MACHINE_FORGETS_UNO) {
                    this.gameState.playerDeclaresUno(this.machinePlayer);
                    this.gameView.displayMessage("¡Máquina dice UNO!");
                } else {
                    this.gameView.displayMessage("Máquina tiene una carta... (parece que olvidó decir UNO)");
                }
            }
            // Si la máquina jugó comodín
            if (cardToPlay.getColor() == Color.WILD) {
                this.gameView.displayMessage("Máquina cambió el color a " + this.gameState.getCurrentValidColor().name());
            }
            this.processTurnAdvancement();
        } else {
            // 2. La máquina no encontró carta jugable, así que va a tomar una
            this.gameView.displayMessage("Máquina no tiene jugadas, robando...");
            Card drawnCard = this.gameState.drawTurnCard(this.machinePlayer);
            this.gameView.updateMachineHand(this.machinePlayer.getNumeroCartas()); // Actualizar contador de cartas de la máquina

            if (drawnCard == null) {
                // TODO: toca evaluar esto en una excepción
                this.gameView.displayMessage("Máquina no pudo robar (mazo vacío). Pasando.");
                this.processTurnAdvancement();
                return;
            }
            this.gameView.displayMessage("Máquina robó una carta.");
            // 3. Intentar jugar la carta robada
            if (this.gameState.isValidPlay(drawnCard)) {
                this.gameView.displayMessage("Máquina juega la carta robada: " + this.gameState.getCardDescription(drawnCard));
                boolean gameEnded = this.gameState.playCard(this.machinePlayer, drawnCard);
                this.updateViewAfterMachinePlay(drawnCard);

                if (gameEnded) {
                    this.handleGameOver();
                    return;
                }
                if (this.machinePlayer.isUnoCandidate()) {
                    this.gameState.playerDeclaresUno(this.machinePlayer);
                    this.gameView.displayMessage("¡Máquina dice UNO!");
                }
                if (drawnCard.getColor() == Color.WILD) {
                    this.gameView.displayMessage("Máquina cambió el color a " + this.gameState.getCurrentValidColor().name());
                }

            } else {
                // 4. No se jugó nada más, solo se robó. Avanzamos turno.
                this.gameView.displayMessage("Máquina no puede jugar la carta robada. Pasando turno.");
            }
            this.processTurnAdvancement();
        }
    }

    /**
     * Maneja el final de la partida.
     * Deshabilita botones de interacción, renderiza el dialógo del ganador y habilita el botón de reinicio.
     */
    private void handleGameOver() {
        Player winner = this.gameState.getWinner();
        // Se valida que hay un ganador
        if (winner != null) {
            this.gameView.displayGameOver(winner.getName());
        } else {
            this.gameView.displayGameOver("Juego terminado."); // Mensaje genérico si no hay ganador explícito
        }
        this.gameView.disableGameInteractions();
        this.gameView.enableRestartButton(true);
        this.updateUnoVisualsForHuman();
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
        boolean canInteractBase = (this.currentPlayer == this.humanPlayer &&
                !this.gameState.isGameOver() &&
                !this.isChoosingColor);
        this.gameView.enablePlayerInteraction(canInteractBase);
        this.updatePunishUnoButtonVisuals();
        // TODO: toca revisar este método
    }

    /**
     * Inicia un temporizador para que la máquina intente "atrapar" al jugador humano
     * si este último terminó su turno con una sola carta y no declaró "UNO".
     * El temporizador tiene una duración aleatoria dentro de un rango definido en MACHINE_CATCH_MIN_DELAY_MS.
     * @see #scheduleMachineTurn()
     * @see #penalizeHumanForMissingUno(String)
     * @see #cancelMachineCatchUnoTimer()
     */
    private void startMachineCatchUnoTimer() {
        this.cancelMachineCatchUnoTimer();
        long delay = MACHINE_CATCH_MIN_DELAY_MS + this.randomGenerator.nextInt((int) (MACHINE_CATCH_MAX_DELAY_MS - MACHINE_CATCH_MIN_DELAY_MS + 1));
        this.gameView.displayMessage("Máquina está observando si dijiste UNO...");

        this.machineCatchUnoTimerTask = this.executorService.schedule(() -> {
            Platform.runLater(() -> {
                // Verificar de nuevo si el humano AÚN no ha dicho UNO y sigue con 1 carta
                if (this.humanPlayer.getNumeroCartas() == 1 && !this.humanPlayer.hasDeclaredUnoThisTurn()) {
                    this.gameView.displayMessage("¡Máquina te atrapó! No dijiste UNO. Robas " + GameState.PENALTY_CARDS_FOR_UNO + " cartas.");
                    this.gameState.penalizePlayerForUno(this.humanPlayer); // Modelo penaliza
                    this.gameView.updatePlayerHand(this.humanPlayer.getCards(), this); // Vista actualiza
                }
                // Haya penalizado o no, la máquina ahora toma su turno.
                this.scheduleMachineTurn();
            });
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Aplica una penalización al jugador humano por no declarar "UNO" a tiempo o
     * por realizar otra acción cuando debería haberlo declarado. En este caso, lo
     * fuerza a tomar un número predefinido de cartas con base en PENALTY_CARDS_FOR_UNO.
     * @param message El mensaje específico que se mostrará en la UI
     *                para informar al jugador sobre la razón de la penalización.
     * @see GameState#penalizePlayerForUno(Player)
     * @see #cancelHumanUnoTimer()
     * @see #updateUnoVisualsForHuman()
     */
    private void penalizeHumanForMissingUno(String message) {
        this.gameView.displayMessage(message + " Robas " + GameState.PENALTY_CARDS_FOR_UNO + " cartas.");
        this.gameState.penalizePlayerForUno(this.humanPlayer);
        this.gameView.updatePlayerHand(this.humanPlayer.getCards(), this);
        this.cancelHumanUnoTimer();
        this.updateUnoVisualsForHuman(); // Ocultar botón y timer
    }

    /**
     * Actualiza la vista cuando el jugador humano tiene una ventana para declarar UNO.
     * Muestra el botón UNO, el temporizador y muestra mensajes relevantes en la vista.
     */
    private void updateUnoVisualsForHuman() {
        // El botón UNO se muestra si el jugador humano es candidato y aún no ha declarado UNO
        boolean showUnoButtonForHuman = this.humanPlayer.isUnoCandidate() && !this.humanPlayer.hasDeclaredUnoThisTurn();
        this.gameView.showUnoButton(showUnoButtonForHuman);
        this.gameView.showUnoPenaltyTimer(showUnoButtonForHuman); // Usar el mismo indicador
        if (!showUnoButtonForHuman && this.unoTimerIndicator != null) {
            // Si se oculta, resetear progreso
            this.unoTimerIndicator.setProgress(0);
        } else if (showUnoButtonForHuman && this.unoTimerIndicator != null) {
            // O una cuenta regresiva si se implementa
            this.unoTimerIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        }
    }

    /**
     * Cancela todos los temporizadores activos relacionados con la mecánica de "UNO".
     * @see #cancelHumanUnoTimer()
     * @see #cancelMachineCatchUnoTimer()
     */
    private void cancelAllTimers() {
        this.cancelHumanUnoTimer();
        this.cancelMachineCatchUnoTimer();
    }

    /**
     * Cancela el temporizador activo que da al jugador humano un tiempo límite para
     * presionar el botón "UNO" después de quedarse con una sola carta.
     * @see #humanUnoTimerTask
     */
    private void cancelHumanUnoTimer() {
        if (this.humanUnoTimerTask != null && !this.humanUnoTimerTask.isDone()) {
            this.humanUnoTimerTask.cancel(false);
        }
    }

    /**
     * Cancela el temporizador activo que permite a la máquina "atrapar" al jugador humano
     * si este no declara "UNO" a tiempo después de quedarse con una sola carta.
     * @see #machineCatchUnoTimerTask
     */
    private void cancelMachineCatchUnoTimer() {
        if (this.machineCatchUnoTimerTask != null && !this.machineCatchUnoTimerTask.isDone()) {
            this.machineCatchUnoTimerTask.cancel(false);
        }
    }

    /**
     * Inicia un temporizador que otorga al jugador humano un período de tiempo (definido por
     * UNO_PLAYER_RESPONSE_TIME_SECONDS) para declarar "UNO".
     * Si el temporizador expira y el jugador no declaro "UNO", se le penaliza.
     * Después de la penalización (o si el jugador declara "UNO" a tiempo), se procede con el
     * avance del turno del juego, a menos que el jugador esté en medio de una elección de color para un comodín.
     * @see #cancelHumanUnoTimer()
     * @see #updateUnoVisualsForHuman()
     * @see #penalizeHumanForMissingUno(String)
     * @see #processTurnAdvancement()
     */
    private void startHumanUnoTimer() {
        this.cancelHumanUnoTimer(); // Asegurar que no haya timers duplicados
        this.updateUnoVisualsForHuman(); // Mostrar botón y timer

        this.humanUnoTimerTask = this.executorService.schedule(() -> {
            Platform.runLater(() -> {
                // Solo penalizar y avanzar si el timer realmente expiró
                // y el jugador aún es candidato y no ha declarado UNO.
                if (!this.gameState.isGameOver() && this.humanPlayer.isUnoCandidate() && !this.humanPlayer.hasDeclaredUnoThisTurn()) {
                    this.penalizeHumanForMissingUno(this.humanPlayer.getName() + " no dijo UNO a tiempo.");
                    this.humanPlayer.resetUnoStatus();

                    this.advanceDueToUnoTimer = true;
                    if (!this.isChoosingColor) {
                        this.processTurnAdvancement();
                    }
                }
            });
        }, UNO_PLAYER_RESPONSE_TIME_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Actualiza la visibilidad y el estado de habilitación del botón para castigar a la máquina.
     * El botón es visible y está habilitado si {@code canPunishMachine} es verdadero
     * y es el turno del jugador humano y el juego no ha terminado.
     */
    private void updatePunishUnoButtonVisuals() {
        Platform.runLater(() -> {
            boolean showButton = this.canPunishMachine &&
                    this.currentPlayer == this.humanPlayer &&
                    !this.gameState.isGameOver() &&
                    !this.isChoosingColor;
            if (this.punishUnoButton != null) {
                this.punishUnoButton.setVisible(showButton);
                this.punishUnoButton.setDisable(!showButton);
            }
        });
    }
}