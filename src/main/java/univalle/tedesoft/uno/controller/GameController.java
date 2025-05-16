package univalle.tedesoft.uno.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import univalle.tedesoft.uno.exceptions.EmptyDeckException;
import univalle.tedesoft.uno.exceptions.InvalidPlayException;
import univalle.tedesoft.uno.model.Cards.Card;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Players.HumanPlayer;
import univalle.tedesoft.uno.model.Players.MachinePlayer;
import univalle.tedesoft.uno.model.Players.Player;
import univalle.tedesoft.uno.model.State.GameState;
import univalle.tedesoft.uno.model.State.IGameState;
import univalle.tedesoft.uno.threads.HumanUnoTimerRunnable;
import univalle.tedesoft.uno.threads.MachineDeclareUnoRunnable;
import univalle.tedesoft.uno.threads.MachinePlayerRunnable;
import univalle.tedesoft.uno.view.GameView;

import java.util.List;
import java.util.Optional;
import java.util.Random;
/**
 * Clase encargada de controlar la pantalla principal donde se desarrolla el juego
 * y donde se da la conexion entre los modelos y la capa de vistas.
 * @author Juan Pablo Escamilla
 * @author David Esteban Valencia
 * @author Santiago Guerrero
 */
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
     * Representa al jugador cuyo turno está activo en el juego.
     * Se actualiza dinámicamente durante el juego a medida que los jugadores se turnan.
     */
    private Player currentPlayer;
    private boolean isChoosingColor = false; // --- Banderas para controlar lógica de decisión ---
    private boolean canPunishMachine = false;

    // --- View ---
    private GameView gameView;

    // --- Threads ---
    private Thread machineTurnThread;
    private Thread humanUnoTimerThread;
    private Thread machineDeclareUnoThread;

    // --- Constantes para controlar las ventanas de tiempo para declarar UNO ---
    private static final int CATCH_MIN_DELAY_MS = 2000; // 2 segundos
    private static final int CATCH_MAX_DELAY_MS = 4000; // 4 segundos
    private static final long MACHINE_TURN_THINK_DELAY_MS = 1500;

    @FXML
    public void initialize() {
        // Inicializar el HumanPlayer con un nombre vacío
        this.humanPlayer = new HumanPlayer("");
        this.machinePlayer = new MachinePlayer();

        // Si ya tenemos un nombre almacenado, actualizarlo
        if (this.playerName != null && !this.playerName.isEmpty()) {
            this.humanPlayer.setName(this.playerName);
            if (this.playerNameLabel != null) {
                this.playerNameLabel.setText("Jugador: " + this.playerName);
            }
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
        // Detener tareas de hilos anteriores si existen
        this.cancelAllTimers();
        // Interrumpir el hilo del turno de la máquina si está activo
        if (this.machineTurnThread != null && this.machineTurnThread.isAlive()) {
            this.machineTurnThread.interrupt();
        }

        // Asegurarnos de que el HumanPlayer mantenga su nombre
        if (this.playerName != null && !this.playerName.isEmpty()) {
            this.humanPlayer.setName(this.playerName);
        }

        // Crear nuevo estado de juego e inicializarlo
        this.gameState = new GameState(this.humanPlayer, this.machinePlayer);
        try {
            this.gameState.onGameStart();
        } catch (IllegalStateException e) {
            this.gameView.displayMessage("Error crítico al iniciar: " + e.getMessage() + ". Reinicia el juego.");
            this.gameView.disableGameInteractions();
            this.gameView.enableRestartButton(true);
            return;
        }
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
        if (this.humanPlayer.getName() != null && !this.humanPlayer.getName().isEmpty() && this.currentPlayer == this.humanPlayer) {
            this.gameView.displayMessage("¡Tu turno, " + this.humanPlayer.getName() + "!");
        }

        this.updateInteractionBasedOnTurn();
        this.gameView.enableRestartButton(true);

        // Asegurar que los estados de UNO estén limpios para la nueva partida
        this.humanPlayer.resetUnoStatus();
        this.machinePlayer.resetUnoStatus();
        this.updateUnoVisualsForHuman();
        this.isChoosingColor = false;
        this.setCanPunishMachine(false);
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
        if (this.gameState.isGameOver()) {
            this.gameView.displayMessage("El juego ha terminado.");
            return;
        }
        if (this.currentPlayer != this.humanPlayer) {
            this.gameView.displayMessage("Espera tu turno.");
            return;
        }
        if (this.isChoosingColor) {
            this.gameView.displayMessage("Elige un color primero.");
            return;
        }
        // Si el humano juega/roba en lugar de castigar a la máquina
        if (this.canPunishMachine) {
            this.setCanPunishMachine(false);
            if (this.machinePlayer.isUnoCandidate() && !this.machinePlayer.hasDeclaredUnoThisTurn()) {
                this.gameState.playerDeclaresUno(this.machinePlayer); // Máquina "dijo" UNO implícitamente
            }
        }
        // Si el jugador era candidato a UNO y juega otra carta sin decir UNO, se penaliza
        if (this.humanPlayer.isUnoCandidate()) {
            this.penalizeHumanForMissingUno("¡Debiste decir UNO antes de jugar otra carta!");
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
        try {
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
                this.gameView.displayMessage("¡Tienes una carta! ¡Presiona UNO rápido!");
                this.startHumanUnoTimer();
            } else {
                // Si no es candidato a UNO, el turno avanza.
                this.updateUnoVisualsForHuman();
            }
            // Si se jugó un comodín, el estado cambió y necesitamos elegir color
            if (selectedCard.getColor() == Color.WILD) {
                // El turno avanzará después de elegir el color
                this.promptHumanForColorChoice();
            } else {
                if (!this.humanPlayer.isUnoCandidate()) {
                    this.processTurnAdvancement();
                }
            }
        } catch (InvalidPlayException exception) {
            Card topCard = exception.getTopDiscardCard();
            Card attemptedCard = exception.getAttemptedCard();
            String playedCardDescription = this.gameState.getCardDescription(attemptedCard);
            String topCardDescription = this.gameState.getCardDescription(topCard);
            this.gameView.displayInvalidPlayMessage(
                    attemptedCard,
                    topCard,
                    playedCardDescription,
                    topCardDescription
            );
        }
    }

    /**
     * Maneja el clic en el mazo para robar una carta.
     */
    @FXML
    public void handleDeckClick() {
        if (this.gameState.isGameOver()) {
            this.gameView.displayMessage("El juego ha terminado.");
            return;
        }
        if (this.currentPlayer != this.humanPlayer) {
            this.gameView.displayMessage("Espera tu turno.");
            return;
        }
        if (this.isChoosingColor) {
            this.gameView.displayMessage("Elige un color primero.");
            return;
        }
        // Si el humano juega en lugar de castigar
        if (this.canPunishMachine) {
            this.setCanPunishMachine(false);
            if (this.machinePlayer.isUnoCandidate() && !this.machinePlayer.hasDeclaredUnoThisTurn()) {
                this.gameState.playerDeclaresUno(this.machinePlayer); // Máquina "dijo" UNO implícitamente
            }
        }
        // Penalizar si era candidato a UNO y roba en lugar de decir UNO
        if (this.humanPlayer.isUnoCandidate() && !this.humanPlayer.hasDeclaredUnoThisTurn()) {
            this.penalizeHumanForMissingUno("¡Debiste decir UNO antes de robar!");
            this.processTurnAdvancement();
            return;
        }
        this.cancelHumanUnoTimer(); // Cancelar cualquier timer de UNO pendiente
        this.updateUnoVisualsForHuman();

        // Limpiar resaltados
        this.gameView.clearPlayerHandHighlights();
        // Quitar resaltado del mazo si lo había
        this.gameView.highlightDeck(false);
        // Se intenta sacar una carta
        Card drawnCard = tryDrawCard(this.humanPlayer, "Mazo vacío. Reciclando cartas de la pila de descarte...");
        if (drawnCard == null) {
            this.gameView.displayMessage("No hay cartas disponibles para robar, incluso después de reciclar x.x");
        } else {
            this.gameView.displayMessage("Sacaste: " + this.gameState.getCardDescription(drawnCard));
        }
        this.gameView.updatePlayerHand(this.humanPlayer.getCards(), this); // Actualizar mano en la UI
        this.processTurnAdvancement();
    }

    /**
     * Maneja la acción del botón "¡UNO!".
     */
    @FXML
    public void handleUnoButtonAction() {
        if (this.gameState.isGameOver() || this.currentPlayer != this.humanPlayer || this.isChoosingColor) {
            return;
        }
        // Evaluar si es candidato para declarar UNO
        if (this.humanPlayer.isUnoCandidate() || (this.humanPlayer.getNumeroCartas() == 1 && !this.humanPlayer.hasDeclaredUnoThisTurn())) {
            this.gameState.playerDeclaresUno(this.humanPlayer);
            this.gameView.displayMessage(this.humanPlayer.getName() + " declaró UNO!");
            this.cancelHumanUnoTimer();
            this.updateUnoVisualsForHuman();
            // Actualizar la vista de las cartas del jugador
            this.gameView.updatePlayerHand(this.humanPlayer.getCards(), this);
            this.setCanPunishMachine(false); // no se puede castigar a la máquina si el jugador prefirió decir UNO

            if (!this.isChoosingColor) {
                this.processTurnAdvancement();
            }
        } else {
            this.gameView.displayMessage("No es el momento adecuado para declarar UNO.");
        }
    }

    /**
     * Maneja la acción del botón de Ayuda.
     */
    @FXML
    public void handleAidButtonAction() {
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
            // Si tiene cartas jugables, le damos una ayuda de cuáles puede jugar
            this.gameView.highlightPlayableCards(playableCards);
            this.gameView.displayMessage("Cartas resaltadas son las que puedes jugar.");
        }
    }

    /**
     * Maneja la acción del botón "Reiniciar".
     */
    @FXML
    public void handleRestartButtonAction() {
        this.startNewGame();
    }

    /**
     * Maneja la acción del botón para castigar a la máquina por no decir "UNO".
     * Condiciones para castigar a la máquina:
     * 1. La máquina tiene 1 carta.
     * 2. La máquina es candidata a UNO (significa que jugó una carta que la dejó con 1).
     * 3. La máquina NO ha declarado UNO en su oportunidad.
     */
    @FXML
    public void handlePunishUnoButtonAction() {
        if (!canPunishMachine) {
            this.gameView.displayMessage("No es el momento de castigar a la máquina.");
            return;
        }
        try {
            if (this.shouldPenalizeMachineForUno()) {
                this.gameView.displayMessage("¡Atrapaste a la Máquina! Roba " + GameState.PENALTY_CARDS_FOR_UNO + " cartas.");
                this.gameState.penalizePlayerForUno(this.machinePlayer);
                this.gameView.updateMachineHand(this.machinePlayer.getNumeroCartas());

                // Esto asegura que la máquina no diga UNO después de ser penalizada
                this.cancelMachineDeclareUnoTimer();
            } else {
                this.gameView.displayMessage("No es el momento de castigar a la máquina.");
            }
        } finally {
            this.proceedWithGameAfterMachineUnoWindow(); // continuar el juego y evaluar
        }
    }

    // --- Lógica del Juego ---

    public Label getMachineCardsCountLabel() {
        return machineCardsCountLabel;
    }

    /**
     * Verifica si se cumplen las condiciones para penalizar a la máquina por no decir UNO.
     * Estas condiciones son: (1) La máquina tiene 1 carta; (2) La máquina es candidata a
     * UNO; (3) La máquina NO ha declarado UNO en su oportunidad.
     * @return true si la máquina debe ser penalizada, false en caso contrario
     */
    private boolean shouldPenalizeMachineForUno() {
        boolean shouldPenalizeMachine = this.machinePlayer.getNumeroCartas() == 1 &&
                this.machinePlayer.isUnoCandidate() &&
                !this.machinePlayer.hasDeclaredUnoThisTurn();
        return shouldPenalizeMachine;
    }

    /**
     * Procesa el avance del turno y las lógicas asociadas.
     * Este es el punto central para cambiar de jugador, actualizando la UI y
     * manejando el turno de la máquina si corresponde.
     */
     public void processTurnAdvancement() {
        if (this.isChoosingColor) {
            return;
        }
        // Limpiar ayudas visuales
        this.cancelHumanUnoTimer();
        this.updateUnoVisualsForHuman();
        // Guardar quién era el jugador anterior para la lógica de "atrapar UNO"
        Player previousPlayer = this.currentPlayer;

        this.gameState.advanceTurn(); // Actualizar el jugador actual desde el GameState
        this.currentPlayer = this.gameState.getCurrentPlayer();
        if (this.currentPlayer != previousPlayer) {
            this.currentPlayer.resetUnoStatus();
        }
        // Actualizar mensaje y controles
        this.gameView.clearPlayerHandHighlights();
        this.gameView.highlightDeck(false);
        this.gameView.updateTurnIndicator(this.currentPlayer.getName());
        this.gameView.displayMessage("Turno de " + this.currentPlayer.getName());

        if (this.currentPlayer == this.machinePlayer) {
            // Turno normal de la máquina
            this.setCanPunishMachine(false);
            this.scheduleMachineTurn();
        } else {
            // Verificar si la máquina acaba de jugar y "olvidó" decir UNO
            this.setCanPunishMachine(this.shouldPunishMachine(previousPlayer));
            // Actualizar la interacción y los botones después de toda la lógica de cambio de turno
            this.updateInteractionBasedOnTurn();
        }
    }

    /**
     * Determina si el jugador máquina debe ser castigado según el estado del juego.
     * @param previousPlayer el jugador que jugó antes del turno actual.
     * @return verdadero si el jugador máquina debe ser castigado; falso en caso contrario
     */
    private boolean shouldPunishMachine(Player previousPlayer) {
        boolean isPreviousMachine = previousPlayer == this.machinePlayer;
        boolean hasOneCard = this.machinePlayer.getNumeroCartas() == 1;
        boolean isUnoCandidate = this.machinePlayer.isUnoCandidate();
        boolean hasNotDeclaredUno = !this.machinePlayer.hasDeclaredUnoThisTurn();

        return isPreviousMachine && hasOneCard && isUnoCandidate && hasNotDeclaredUno;
    }

    /**
     * Pide a la vista que muestre el diálogo de elección de color y procesa la selección.
     */
    private void promptHumanForColorChoice() {
        this.isChoosingColor = true;
        this.gameView.enablePlayerInteraction(false);

        // Pedir a la vista que muestre el diálogo de elección
        Optional<Color> result = this.gameView.promptForColorChoice();

        this.isChoosingColor = false;
        this.gameView.enablePlayerInteraction(true);
        this.updateInteractionBasedOnTurn(); // Actualizar botones ahora que el diálogo cerró
        result.ifPresentOrElse(
                targetColor -> {
                    // Notificar al modelo
                    this.gameState.onColorChosen(targetColor);
                    // Actualizar la vista para reflejar el color elegido en el borde de la pila de descarte
                    this.gameView.updateDiscardPile(this.gameState.getTopDiscardCard(), targetColor);
                    // Obtener el nombre del color en español del GameState
                    String spanishColorName = this.gameState.getSpanishColorName(targetColor);
                    this.gameView.displayMessage("Color cambiado a " + spanishColorName);
                    // El turno NO debe avanzar aquí si el jugador humano tiene la chance de declarar UNO
                    if (!this.humanPlayer.isUnoCandidate() || this.humanPlayer.hasDeclaredUnoThisTurn()) {
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
    public void scheduleMachineTurn() {
        if (this.currentPlayer != this.machinePlayer || this.gameState.isGameOver()) {
            return;
        }
        this.gameView.displayMessage("Máquina pensando...");
        // Interrumpir el hilo anterior si existiera y estuviera vivo (por si acaso)
        if (this.machineTurnThread != null && this.machineTurnThread.isAlive()) {
            this.machineTurnThread.interrupt();
            try {
                this.machineTurnThread.join(100); // 100 ms de delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        MachinePlayerRunnable machineRunnable = new MachinePlayerRunnable(this, MACHINE_TURN_THINK_DELAY_MS);
        this.machineTurnThread = new Thread(machineRunnable);
        this.machineTurnThread.setName("MachinePlayerTurnThread-" + System.currentTimeMillis());
        this.machineTurnThread.setDaemon(true); // Permite que la JVM cierre si solo quedan hilos daemon
        this.machineTurnThread.start();
    }

    /**
     * Maneja las acciones posteriores a que la máquina haya jugado una carta.
     * Esto incluye actualizar la UI, verificar si el juego terminó,
     * manejar la lógica de UNO para la máquina y avanzar el turno.
     *
     * @param playedCard La carta que jugó la máquina.
     * @param gameEnded  True si la jugada de la máquina terminó el juego.
     */
    private void handleMachinePlayedCard(Card playedCard, boolean gameEnded) {
        if (gameEnded) {
            this.handleGameOver();
            return;
        }

        // Actualizar la vista de la pila de descarte.
        this.gameView.updateDiscardPile(this.gameState.getTopDiscardCard(), this.gameState.getCurrentValidColor());

        // Si la máquina jugó un comodín, mostrar mensaje de cambio de color.
        if (playedCard.getColor() == Color.WILD) {
            String validColorName = this.gameState.getSpanishColorName(this.gameState.getCurrentValidColor());
            this.gameView.displayMessage("Máquina cambió el color a " + validColorName);
        }

        if (this.machinePlayer.isUnoCandidate()) {
            // La máquina está en situación de UNO, se programa su intento de declarar UNO
            this.setCanPunishMachine(true);
            this.startMachineDeclareUnoTimer();
        } else {
            this.processTurnAdvancement();
        }
    }


    /**
     * Ejecuta la lógica del turno de la máquina.
     */
    public void executeMachineTurnLogic() {
        if (this.gameState.isGameOver() || this.currentPlayer != this.machinePlayer) {
            return;
        }
        // 1. La máquina elige qué jugar
        Card cardToPlay = this.machinePlayer.chooseCardToPlay(this.gameState);
        if (cardToPlay != null) {
            // Jugar la carta elegida
            try {
                this.gameView.displayMessage("Máquina juega: " + this.gameState.getCardDescription(cardToPlay));
                boolean gameEnded = this.gameState.playCard(this.machinePlayer, cardToPlay);
                this.updateViewAfterMachinePlay(); // Actualiza UI (contador, descarte)
                this.handleMachinePlayedCard(cardToPlay, gameEnded); // Lógica post-jugada (UNO, avance)
            } catch (InvalidPlayException e) {
                // Esto sería un bug en la lógica de la máquina si elige una carta inválida.
                this.gameView.displayMessage("Error: Máquina intentó una jugada inválida. Forzando robo.");
                // Forzar a la máquina a robar como penalización/corrección
                this.tryDrawCard(this.machinePlayer, null);
                this.updateViewAfterMachinePlay();
                this.processTurnAdvancement();
            }
        } else {
            // 2. La máquina no encontró carta jugable, así que va a tomar una
            this.gameView.displayMessage("La máquina no tiene jugadas, robando...");
            Card drawnCard = null;
            try {
                drawnCard = this.gameState.drawTurnCard(this.machinePlayer);
            } catch (EmptyDeckException e) {
                this.gameView.displayMessage("Máquina: Mazo vacío. Reciclando...");
                this.gameState.recyclingDeck();
            }
            this.updateViewAfterMachinePlay();
            if (drawnCard != null) {
                this.gameView.displayMessage("La máquina robó una carta");
            }
            this.processTurnAdvancement();
        }
    }

    /**
     * Intenta robar una carta para el jugador especificado, manejando el reciclaje
     * del mazo en caso de que esté vacío.
     * @param player el jugador que intenta robar la carta
     * @param recycleMessage mensaje a mostrar cuando se necesita reciclar el mazo (puede ser null)
     * @return la carta robada, o null si no fue posible robar después de intentar reciclar
     * @see EmptyDeckException
     */
    private Card tryDrawCard(Player player, String recycleMessage) {
        try {
            // primer intento de sacar una carta
            return this.gameState.drawTurnCard(player);
        } catch (EmptyDeckException e) {
            if (recycleMessage != null) {
                this.gameView.displayMessage(recycleMessage);
            }
            // en caso de que se acabe el mazo, se recicla
            this.gameState.recyclingDeck();
            if (this.gameState.getDeck().getNumeroCartas() > 0) {
                try {
                    // segundo y último intento de sacar una carta
                    return this.gameState.drawTurnCard(player);
                } catch (EmptyDeckException ignored) {
                    // No se pudo robar incluso tras reciclar
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * Inicia un temporizador para que la máquina declare "UNO" después de un breve retraso aleatorio.
     * Este retraso simula un tiempo de reacción y da una pequeña ventana para que el jugador
     * humano pueda intentar "atrapar" a la máquina.
     */
    private void startMachineDeclareUnoTimer() {
        this.cancelMachineDeclareUnoTimer(); // Ahora interrumpe el Thread

        // Retraso aleatorio para declarar UNO
        Random randomGenerator = new Random();
        long delay = 2000 + randomGenerator.nextInt(2000); // entre 2 a 4 segundos

        // Crear y empezar el nuevo Thread
        MachineDeclareUnoRunnable runnable = new MachineDeclareUnoRunnable(this, delay);
        this.machineDeclareUnoThread = new Thread(runnable);
        this.machineDeclareUnoThread.setName("MachineDeclareUnoThread-" + System.currentTimeMillis());
        this.machineDeclareUnoThread.setDaemon(true);
        this.machineDeclareUnoThread.start();
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
        // Interrumpir el hilo de la máquina si estuviera activo
        if (this.machineTurnThread != null && this.machineTurnThread.isAlive()) {
            this.machineTurnThread.interrupt();
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
     */
    private void updateViewAfterMachinePlay() {
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
        boolean isHumanTurnAndGameCanContinue = (this.currentPlayer == this.humanPlayer &&
                !this.gameState.isGameOver() &&
                !this.isChoosingColor);
        if (this.canPunishMachine) {
            // Si el humano tiene la oportunidad de castigar a la máquina,
            // se deshabilita el juego normal de cartas y el robo del mazo.
            this.gameView.enablePlayerInteraction(false);
        } else {
            this.gameView.enablePlayerInteraction(isHumanTurnAndGameCanContinue);
        }
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
    public void penalizeHumanForMissingUno(String message) {
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
     * @see #cancelMachineDeclareUnoTimer()
     */
    private void cancelAllTimers() {
        this.cancelHumanUnoTimer();
        this.cancelMachineDeclareUnoTimer();
    }

    /**
     * Cancela el temporizador activo que da al jugador humano un tiempo límite para
     * presionar el botón "UNO" después de quedarse con una sola carta.
     * @see #humanUnoTimerThread
     */
    private void cancelHumanUnoTimer() {
        // Interrumpir el Thread si está vivo
        if (this.humanUnoTimerThread != null && this.humanUnoTimerThread.isAlive()) {
            this.humanUnoTimerThread.interrupt();
            try {
                this.humanUnoTimerThread.join(100); // Opcional
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Cancela el temporizador activo que está programado para que la máquina declare "UNO".
     */
    private void cancelMachineDeclareUnoTimer() {
        // Interrumpir el Thread si está vivo
        if (this.machineDeclareUnoThread != null && this.machineDeclareUnoThread.isAlive()) {
            this.machineDeclareUnoThread.interrupt();
            try {
                this.machineDeclareUnoThread.join(100); // Opcional
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
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
        this.cancelHumanUnoTimer(); // Ahora interrumpe el Thread
        this.updateUnoVisualsForHuman(); // Mostrar botón y timer

        Random randomGenerator = new Random();
        long delayTime = CATCH_MIN_DELAY_MS + (randomGenerator.nextInt(CATCH_MAX_DELAY_MS - CATCH_MIN_DELAY_MS + 1));

        // Crear y empezar el nuevo Thread
        HumanUnoTimerRunnable runnable = new HumanUnoTimerRunnable(this, delayTime);
        this.humanUnoTimerThread = new Thread(runnable);
        this.humanUnoTimerThread.setName("HumanUnoTimerThread-" + System.currentTimeMillis());
        this.humanUnoTimerThread.setDaemon(true);
        this.humanUnoTimerThread.start();
    }

    // --- Getters para MachinePlayerRunnable ---
    /**
     * Devuelve el estado actual del juego.
     * Necesario para que MachinePlayerRunnable pueda verificar condiciones.
     * @return la instancia de IGameState.
     */
    public IGameState getGameState() {
        return this.gameState;
    }

    /**
     * Devuelve el jugador actual.
     * Necesario para que MachinePlayerRunnable pueda verificar condiciones.
     * @return la instancia del Player actual.
     */
    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    /**
     * Devuelve la instancia del jugador máquina.
     * Necesario para que MachinePlayerRunnable pueda verificar condiciones.
     * @return la instancia de MachinePlayer.
     */
    public MachinePlayer getMachinePlayer() {
        return this.machinePlayer;
    }

    /**
     * Devuelve la instancia del jugador humano.
     * Necesario para que MachineCatchUnoRunnable pueda verificar condiciones.
     * @return la instancia de humanPlayer.
     */
    public HumanPlayer getHumanPlayer() {
        return this.humanPlayer;
    }

    /**
     * Devuelve la instancia de vista.
     * Necesario para implementar MachineCatchUnoRunnable.
     * @return la instancia de gameView
     */
    public GameView getGameView() {
        return this.gameView;
    }

    /**
     * Devuelve el booleano isChoosingColor.
     * Necesario para implementar HumanTimerRunnable.
     * @return isChoosingColor
     */
    public boolean getIsChoosingColor() {
        return this.isChoosingColor;
    }

    /**
     * Setter para el booleano isChoosingColor.
     * Necesario para implementar MachineDeclareUnoRunnable.
     * @param canPunish booleano que cambia según el flujo del juego y ciertas condiciones específicas.
     */
    public void setCanPunishMachine(boolean canPunish) {
        this.canPunishMachine = canPunish;
        Platform.runLater(() -> { // Asegurar que las actualizaciones de UI estén en el hilo de JavaFX
            this.gameView.updatePunishUnoButtonVisuals(
                    this.canPunishMachine,
                    (this.gameState != null && this.gameState.isGameOver()),
                    this.isChoosingColor
            );
            this.updateInteractionBasedOnTurn(); // actualizar las interacciones del jugador
        });
    }

    /**
     * Procede con el juego después de que la ventana de oportunidad para castigar
     * a la máquina (o para que la máquina declare UNO) haya concluido.
     * Aplica efectos de carta pendientes y avanza el turno.
     */
    public void proceedWithGameAfterMachineUnoWindow() {
        this.setCanPunishMachine(false);
        // Aplicar cualquier efecto de robo pendiente que la máquina haya causado
        this.gameState.applyPendingDrawsToHuman();
        this.updateViewAfterMachinePlay(); // actualizar la vista
        this.processTurnAdvancement();
    }
}