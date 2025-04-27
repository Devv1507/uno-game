package univalle.tedesoft.uno.model.State;

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
    private final HumanPlayer humanPlayer;
    private final MachinePlayer machinePlayer;

    private Player currentPlayer;
    private Color currentValidColor;
    private Value currentValidValue;
    /**
     * Referencias al mazo de cartas y la pila de cartas descartadas
     */
    private final Deck deck;
    private final DiscardPile discardStack;

    private boolean skipNextTurn = false;
    private boolean gameOver = false;
    private Player winner = null;

    /**
     * Constructor de GameState
     * @param humanPlayer La instancia del jugador humano.
     * @param machinePlayer La instancia del jugador maquina.
     */
    public GameState(HumanPlayer humanPlayer, MachinePlayer machinePlayer) {
        this.humanPlayer = humanPlayer;
        this.machinePlayer = machinePlayer;
        this.deck = new Deck();
        this.discardStack = new DiscardPile();
    }

    /**
     * Se llama una unica vez cuando el juego ha sido configurado
     * (mazo creado, cartas repartidas, primera carta volteada) y está listo para empezar.
     */
    @Override
    public void onGameStart() {
        this.deck.shuffle();
        this.dealInitialCards();

        // Sacar una carta y colocarla en la pila de descarte
        Card firstCard = this.deck.takeCard();
        this.discardStack.discard(firstCard);

        // Establecer valores iniciales
        this.currentPlayer = this.humanPlayer;
        this.currentValidColor = firstCard.getColor();
        this.currentValidValue = firstCard.getValue();
        this.gameOver = false;
        this.winner = null;
        this.skipNextTurn = false;
    }

    /**
     * Se llama cada vez que el turno pasa de un jugador a otro.(Funcion que pertenece al controlador de los eventos)
     *
     * @param currentPlayer El jugador que ahora tiene el turno.
     */
    public void onTurnChanged(Player currentPlayer) {
    }

    /**
     * Se llama al inicio, para repartir la primera mano de cartas a ambos jugadores.
     */
    @Override
    public void dealInitialCards() {
        int initialHandSize = 5;
        for (int i = 0; i < initialHandSize; i++) {
            Card card1 = this.deck.takeCard();
            Card card2 = this.deck.takeCard();
            this.humanPlayer.addCard(card1);
            this.machinePlayer.addCard(card2);
        }
    }

    /**
     * Se llama cuando un jugador ha jugado exitosamente una carta.
     *
     * @param player El jugador que realizo la jugada.
     * @param card   La carta especifica que fue jugada y ahora está en la cima.
     */
    @Override
    public boolean playCard(Player player, Card card) {
        player.playCard(card);
        this.discardStack.discard(card);
        this.applyCardEffect(card, player);
        // comprobar si el jugador gano
        if (player.getNumeroCartas() == 0) {
            this.gameOver = true;
            this.winner = player;
            return true;
        }

        this.advanceTurn();
        return false;
    }

    /**
     * Determina y avanza al siguiente jugador, manejando los saltos.
     *
     * @see #playCard(Player, Card)
     */
    private void advanceTurn() {
        Player nextPlayer = this.getOpponent(this.currentPlayer);

        if (this.skipNextTurn) {
            // sigue siendo el turno del jugador actual
            this.skipNextTurn = false;
        } else {
            this.currentPlayer = nextPlayer;
        }
    }

    /**
     * Getter del oponente en un juego de 2 jugadores
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
     *
     * @param card            La carta jugada.
     * @param playerWhoPlayed El jugador que jugó la carta.
     * @see #playCard
     */
    private void applyCardEffect(Card card, Player playerWhoPlayed) {
        Player opponent = (playerWhoPlayed == this.humanPlayer) ? this.machinePlayer : this.humanPlayer;
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
     * Determina quien es el siguiente jugador en el orden actual.
     *
     * @return la instancia que representa el siguiente jugador.
     */
    private Player determineNextPlayer() {
        if (this.currentPlayer == this.humanPlayer) {
            return this.machinePlayer;
        } else {
            return this.humanPlayer;
        }
    }

    /**
     * Se llama específicamente cuando un jugador es forzado a tomar cartas
     * debido a un efecto (+2, +4) o una penalización (no decir UNO).
     *
     * @param player        El jugador que va a tomar cartas.
     * @param numberOfCards La cantidad de cartas que será forzado a tomar.
     */
    @Override
    public int forceDraw(Player player, int numberOfCards) {
        int cardsActuallyDrawn = 0; // Contador por si no hay suficientes cartas

        for (int i = 0; i < numberOfCards; i++) {
            // Verificar si el mazo esta vacio ANTES de intentar tomar
            if (this.deck.getNumeroCartas() == 0) {
                this.onEmptyDeck();
            }
            Card card = this.deck.takeCard();
            player.addCard(card);
            cardsActuallyDrawn++;
            // Notificar por cada carta robada individualmente (si es necesario)
            // onPlayerDrewCard(player, card);  }
        }
        return cardsActuallyDrawn;
    }

    /**
     * Se llama después de que un jugador ha elegido un color para un comodín,
     * o al inicio si la primera carta establece un color.
     *
     * @param color El color que ahora está activo en el juego.
     */
    public void onColorChosen(Color color) {
    }

    /**
     * Verifica si el estado UNO de un jugador ha cambiado y llama a onUnoStateChanged.
     *
     * @param player El jugador a verificar.
     */
    private void checkUnoState(Player player) {
        // TODO: Necesitaríamos saber el estado *anterior* para una notificación más precisa
        // de "entró en estado UNO" vs "salió de estado UNO".
        boolean hasOneCard = player.getNumeroCartas() == 1;
    }

    /**
     * Recicla las cartas de la pila de descarte de vuelta al mazo principal
     * cuando este se queda vacío.
     */
    @Override
    public void onEmptyDeck() {
        ArrayList<Card> recycledCards = (ArrayList<Card>) this.discardStack.recycleDeck();
        if (!recycledCards.isEmpty()) {
            for (Card card : recycledCards) {
                // Añade cada carta al final de la pila
                this.deck.getCards().add(card);
            }
            // Barajar el mazo despues de añadir las cartas recicladas
            this.deck.shuffle();
        } else {
            System.out.println("No hay suficientes cartas en la pila de descarte para reciclar.");
        }
    }


    /**
     * Verifica si una carta puede ser jugada legalmente sobre la carta * superior actual de la pila de descarte. * * @param card La carta que se intenta jugar.
     *
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
    @Override
    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    @Override
    public Color getCurrentValidColor() {
        return this.currentValidColor;
    }

    public Value getCurrentValidValue() {
        return this.currentValidValue;
    }

    public Deck getDeck() {
        return this.deck;
    }

    @Override
    public Card getTopDiscardCard() {
        return this.discardStack.SuperiorCard();
    }

    private String getCardDescription(Card card) {
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

    public boolean isGameOver() {
        return this.gameOver;
    }
}
