package univalle.tedesoft.uno.model.State;

import univalle.tedesoft.uno.exceptions.EmptyDeckException;
import univalle.tedesoft.uno.exceptions.InvalidPlayException;
import univalle.tedesoft.uno.model.Cards.*;
import univalle.tedesoft.uno.model.Decks.Deck;
import univalle.tedesoft.uno.model.Decks.DiscardPile;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;
import univalle.tedesoft.uno.model.Players.HumanPlayer;
import univalle.tedesoft.uno.model.Players.MachinePlayer;
import univalle.tedesoft.uno.model.Players.Player;

import java.util.ArrayList;

/**
 * Orquesta los turnos, el mazo, los jugadores y la pila de descarte.
 *
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class GameState implements IGameState {
    /**
     * Jugadores
     */
    private final HumanPlayer humanPlayer;
    private final MachinePlayer machinePlayer;
    /**
     * CurrentPLayer es el jugador de turno
     * CurrentValidColor es el color de la carta actual sobre la mesa
     * CurrentValidValue es el valor de la carta actual sobre la mesa
     */
    private Player currentPlayer;
    private Color currentValidColor;
    private Value currentValidValue;
    /**
     * Referencias al mazo de cartas y la pila de cartas descartadas
     */
    private final Deck deck;
    private final DiscardPile discardStack;
    /**
     * Estas son situaciones que ocurren dentro del juego.
     * skipNextTurn significa que el actual jugador pierde su turno por un efecto de carta false hasta que esto sucede.
     * gameOver Algun jugador ya no posee cartas por lo que el juego se acabo false hasta que esto se cumpla.
     * winner jugador que ya no posee cartas.
     */
    private boolean skipNextTurn = false;
    private boolean gameOver = false;
    private Player winner = null;
    /**
     * Constante para definir el número de cartas que se penalizarán por no cantar "UNO" a tiempo.
     */
    public static final int PENALTY_CARDS_FOR_UNO = 2;
    /**
     * Constante para la mano inicial.
     */
    private static final int INITIAL_HAND_SIZE = 5;

    /**
     * Constructor de GameState, recibe los dos jugadores participantes y los inicializa por constructor.
     * @param humanPlayer La instancia del jugador humano.
     * @param machinePlayer La instancia del jugador maquina.
     */
    public GameState(HumanPlayer humanPlayer, MachinePlayer machinePlayer) {
        this.humanPlayer = humanPlayer;
        this.machinePlayer = machinePlayer;
        //Se crea un Deck y una pila de descarte
        this.deck = new Deck();
        this.discardStack = new DiscardPile();
    }

    /**
     * Metodo de inicializacion del juego, se invoca una única vez cuando el juego ha sido configurado
     * y está listo para empezar. Inicializa el estado del juego: baraja el mazo, reparte las cartas,
     * coloca la primera carta en la pila de descarte y establece los valores iniciales.
     */
    @Override
    public void onGameStart() {
        // Limpiar el estado anterior
        this.humanPlayer.clearHand();
        this.humanPlayer.resetUnoStatus();
        this.machinePlayer.clearHand();
        this.machinePlayer.resetUnoStatus();

        this.gameOver = false;
        this.winner = null;
        this.skipNextTurn = false;

        // Preparar nuevo juego, barajar y repartir
        this.deck.shuffle();
        this.dealInitialCards();

        // Sacar una carta y colocarla en la pila de descarte
        Card firstCardToDiscard = null;
        do {
            try {
                firstCardToDiscard = this.deck.takeCard();
            } catch (EmptyDeckException e) {
                // Si ocurre, esto sería un error irrecuperable en la configuración del juego
                throw new IllegalStateException("No se pudo obtener una carta inicial para la pila de descarte", e);
            }
            // Reciclar si el mazo se agota buscando una carta numérica
            if (firstCardToDiscard instanceof ActionCard && this.deck.getNumeroCartas() == 0) {
                this.recyclingDeck();
            }
            // Si la carta es de acción, se devuelve al fondo del mazo y se saca otra.
            if (firstCardToDiscard instanceof ActionCard) {
                this.deck.getCards().addLast(firstCardToDiscard);
                this.deck.shuffle();
                firstCardToDiscard = null;
            }
        } while (firstCardToDiscard == null);
        this.discardStack.discard(firstCardToDiscard);

        // Establecer color y valor por defecto iniciales
        this.currentValidColor = firstCardToDiscard.getColor();
        this.currentValidValue = firstCardToDiscard.getValue();
        // El humano siempre empieza
        this.currentPlayer = this.humanPlayer;
    }

    /**
     * Reparte la mano inicial de cartas a ambos jugadores.
     *  Se invoca al inicio de la partida y distribuye una cantidad fija de cartas
     *  (5 cartas) a cada jugador.
     */
    @Override
    public void dealInitialCards() {
        int cardsDealt = 0;
        int attemptsCount = 0;

        while (cardsDealt < INITIAL_HAND_SIZE && attemptsCount < 2) {
            try {
                // Reparte las cartas restantes
                for (int i = cardsDealt; i < INITIAL_HAND_SIZE; i++) {
                    // Carta para el jugador humano
                    Card humanTakenCard = this.deck.takeCard();
                    this.humanPlayer.addCard(humanTakenCard);

                    // Carta para la máquina
                    Card machineTakenCard = this.deck.takeCard();
                    this.machinePlayer.addCard(machineTakenCard);

                    cardsDealt++;
                }
                // Si llegamos aquí, todas las cartas fueron repartidas correctamente
                return;
            } catch (EmptyDeckException e) {
                attemptsCount++;
                if (attemptsCount < 2) {
                    // Reciclar el mazo y continuar
                    this.recyclingDeck();
                    if (this.deck.getCards().isEmpty()) {
                        // No hay cartas disponibles, salir del bucle
                        break;
                    }
                } else {
                    // Demasiados intentos, evitar bucle infinito
                    break;
                }
            }
        }
    }

    /**
     * Se llama cuando un jugador ha jugado exitosamente una carta.
     * @param player El jugador que realizo la jugada.
     * @param card   La carta especifica que fue jugada y ahora está en la cima.
     */
    @Override
    public boolean playCard(Player player, Card card) throws InvalidPlayException {
        if (!this.isValidPlay(card)) {
            String message = "Jugada inválida: La carta '" + getCardDescription(card) +
                    "' no se puede jugar sobre '" + getCardDescription(this.getTopDiscardCard());
            throw new InvalidPlayException(message, card, this.getTopDiscardCard());
        }
        player.removeCardOfCards(card);
        this.discardStack.discard(card);
        // Resetear el estado UNO del jugador antes de evaluar la nueva situación
        player.resetUnoStatus();

        if (card instanceof ActionCard) {
            this.applyCardEffect(card, player);
        }
        if (card.getColor() != Color.WILD) {
            this.currentValidColor = card.getColor();
            this.currentValidValue = card.getValue();
        } else {
            this.currentValidValue = null;
        }

        // comprobar si el jugador gano o queda en estado UNO
        if (player.getNumeroCartas() == 0) {
            this.gameOver = true;
            this.winner = player;
            return true;
        } else if (player.getNumeroCartas() == 1) {
            player.setUnoCandidate(true); // candidato para declarar UNO
        }
        return false;
    }

    /**
     * Determina y avanza al siguiente jugador, manejando los saltos.
     * @see #playCard(Player, Card)
     */
    public void advanceTurn() {
        Player nextPlayer = this.getOpponent(this.currentPlayer);

        if (this.skipNextTurn) {
            // sigue siendo el turno del jugador actual dado que el siguiente se saltó
            this.skipNextTurn = false;
        } else {
            this.currentPlayer = nextPlayer;
        }
    }

    /**
     * Getter del oponente en un juego de 2 jugadores
     * @param player jugador que esta en este momento en turno.
     * @return player jugador oponente al player que se ingreso.
     */
    private Player getOpponent(Player player) {
        if (player == this.humanPlayer) {
            return this.machinePlayer;
        } else {
            return this.humanPlayer;
        }
    }

    /**
     * Aplica los efectos especiales de las cartas de acción y comodines
     * @param card La carta jugada.
     * @param playerWhoPlayed El jugador que jugó la carta.
     * @see #playCard
     */
    private void applyCardEffect(Card card, Player playerWhoPlayed) {
        Player opponent;
        opponent= this.getOpponent(playerWhoPlayed);
        if (card instanceof DrawTwoCard) {
            this.forceDraw(opponent, 2);
            this.skipNextTurn = true; // El oponente pierde el turno
        } else if (card instanceof WildDrawFourCard) {
            this.forceDraw(opponent, 4);
            this.skipNextTurn = true; // El oponente pierde el turno
            this.onMustChooseColor(playerWhoPlayed);
        } else if (card instanceof SkipCard) {
            this.skipNextTurn = true; // El oponente pierde el turno
        } else if (card instanceof WildCard) {
            // La elección de color se maneja fuera, notificamos la necesidad
            this.onMustChooseColor(playerWhoPlayed);
        }
    }

    /**
     * Maneja la situación cuando un jugador debe elegir un color después de jugar una carta comodín.
     * Si el jugador es humano, se depende de un control externo para obtener la elección del jugador.
     * Si el jugador es la máquina, selecciona un color automáticamente.
     * @param playerWhoPlayed El jugador que acaba de jugar una carta comodín y necesita elegir un color.
     */
    @Override
    public void onMustChooseColor(Player playerWhoPlayed) {
        if (playerWhoPlayed == this.humanPlayer) {
            // El controlador debería haber sido notificado para pedirle al usuario humano
            // (por ahora vamos a dejarlo vacío y que el flujo ya esté controlado desde el Controller después)
        } else if (playerWhoPlayed == this.machinePlayer) {
            // La máquina elige un color automáticamente
            Color chosenColor = this.machinePlayer.chooseColor();
            this.onColorChosen(chosenColor);
        }
    }


    /**
     * Fuerza a un jugador a tomar cartas ya sea debido a un efecto (+2, +4)
     * o una penalización (no decir UNO).
     * @param player        El jugador que va a tomar cartas.
     * @param numberOfCards La cantidad de cartas que será forzado a tomar.
     */
    @Override
    public void forceDraw(Player player, int numberOfCards) {
        int cardsDrawn = 0;
        int attempts = 0;

        while (cardsDrawn < numberOfCards && attempts < 2) {
            try {
                // Roba las cartas restantes
                for (int i = cardsDrawn; i < numberOfCards; i++) {
                    Card cardToDraw = this.deck.takeCard();
                    player.addCard(cardToDraw);
                    cardsDrawn++;
                }
            } catch (EmptyDeckException e) {
                attempts++;
                if (attempts < 2) {
                    // Reciclar el mazo y continuar
                    this.recyclingDeck();
                    // Verificar si hay cartas después del reciclaje
                    if (this.deck.getCards().isEmpty()) {
                        // No hay cartas disponibles, salir del bucle
                        break;
                    }
                } else {
                    // Demasiados intentos, evitar bucle infinito
                    break;
                }
            }
        }
    }

    /**
     * Se llama después de que un jugador ha elegido un color para un comodín,
     * o al inicio si la primera carta establece un color.
     * @param color El color que ahora está activo en el juego.
     */
    @Override
    public void onColorChosen(Color color) {
        this.currentValidColor = color;
        // ya no importa el valor anterior, solo el color importa tras usar WILD
        this.currentValidValue = null;
    }

    /**
     * Registra que un jugador ha declarado "UNO".
     * @param player El jugador que declara "UNO".
     */
    @Override
    public void playerDeclaresUno(Player player) {
        if (player.isUnoCandidate()) {
            player.setHasDeclaredUnoThisTurn(true);
            player.setUnoCandidate(false);
        }
    }

    /**
     * Penaliza a un jugador por no declarar "UNO" correctamente.
     * El jugador roba un número determinado de cartas.
     * @param playerToPenalize El jugador que será penalizado.
     */
    @Override
    public void penalizePlayerForUno(Player playerToPenalize) {
        this.forceDraw(playerToPenalize, PENALTY_CARDS_FOR_UNO);
        playerToPenalize.resetUnoStatus();
    }

    /**
     * Recicla las cartas de la pila de descarte de vuelta al mazo principal
     * cuando este se queda vacío.
     */
    @Override
    public void recyclingDeck() {
        ArrayList<Card> recycledCards = (ArrayList<Card>) this.discardStack.recycleDeck();
        for (Card card : recycledCards) {
            // Añade cada carta al final de la pila
            this.deck.getCards().add(card);
        }
        this.deck.shuffle();
    }


    /**
     * Verifica si una carta puede ser jugada legalmente sobre la carta * superior actual de la pila de descarte.
     * @param card La carta que se intenta jugar.
     * @return true si la jugada es válida, false en caso contrario.
     */
    @Override
    public boolean isValidPlay(Card card) {
        if (card == null) {
            return false;
        }
        // Comodines siempre son jugables (Wild, WildDrawFour)
        if (card.getColor() == Color.WILD) {
            return true;
        }
        // Si la carta anterior fue un comodín, solo importa el color elegido
        if (this.currentValidValue == null) {
            return card.getColor() == this.currentValidColor;
        }

        // Reglas normales: Coincidir en color o en valor
        if (card.getColor() == this.currentValidColor || card.getValue() == this.currentValidValue) {
            return true;
        } else {
            return false;
        }
    }

    // --- Getters  ---
    /**
     * Retorna el jugador cuyo turno está activo actualmente en el juego.
     * @return El jugador actual.
     */
    @Override
    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    /**
     * Retorna el color que actualmente está en efecto para determinar jugadas válidas.
     * @return El color valido actual.
     */
    @Override
    public Color getCurrentValidColor() {
        return this.currentValidColor;
    }

    /**
     * Retorna la instancia del mazo principal de cartas.
     * @return El mazo de cartas.
     */
    @Override
    public Deck getDeck() {
        return this.deck;
    }

    /**
     * Retorna la carta que actualmente se encuentra en la cima de la pila de descarte.
     * @return La carta superior de la pila de descarte.
     */
    @Override
    public Card getTopDiscardCard() {
        return this.discardStack.SuperiorCard();
    }

    /**
     * Retorna el player asociado a winner.
     * @return winner, player con 0 cartas.
     */
    @Override
    public Player getWinner() {
        return this.winner;
    }

    /**
     * Genera una representación en texto legible de una carta específica,
     * combinando su color (si no es un comodín) y su valor.
     * @param card La carta de la cual generar la descripción.
     * @return Una cadena que describe la carta.
     */
    @Override
    public String getCardDescription(Card card) {
        String colorPart;
        if (card.getColor() == Color.WILD) {
            colorPart = "";
        } else {
            colorPart = card.getColor().name() + " ";
        }
        String valuePart = card.getValue().name().replace("_", " ");
        String cardDescription = colorPart + valuePart;
        return cardDescription;
    }

    /**
     * Verifica si el juego ha terminado.
     * @return true si el juego ha finalizado, false en caso contrario.
     */
    public boolean isGameOver() {
        return this.gameOver;
    }

    /**
     * Metodo con el objetivo de tomar una carta del mazo cuando no hay cartas jugables en el mazo.
     * @param player El jugador que está robando la carta.
     * @return drawnCard carta que se roba del mazo principal(Deck)
     */
    @Override
    public Card drawTurnCard(Player player) throws EmptyDeckException {
        Card drawnCard = this.deck.takeCard(); // Puede lanzar EmptyDeckException
        player.addCard(drawnCard);
        player.resetUnoStatus(); // Al robar, ya no es candidato inmediato a UNO por la jugada anterior
        return drawnCard;
    }
}
