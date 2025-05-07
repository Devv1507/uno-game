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
import java.util.Random;

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
     * Metodo de inicializacion del juego, se invoca una única vez cuando el juego ha sido configurado
     * y está listo para empezar. Inicializa el estado del juego: baraja el mazo, reparte las cartas,
     * coloca la primera carta en la pila de descarte y establece los valores iniciales.
     */
    @Override
    public void onGameStart() {
        // Limpiar el estado anterior para evitar acumular cartas
        this.humanPlayer.clearHand();
        this.machinePlayer.clearHand();
        this.gameOver = false;
        this.winner = null;
        this.skipNextTurn = false;

        // Preparar nuevo juego, barajar y repartir
        this.deck.shuffle();
        this.dealInitialCards();

        // Sacar una carta y colocarla en la pila de descarte
        Card firstCard = this.takeSingleCardFromDeckInternal();
        this.discardStack.discard(firstCard);

        // Establecer color y valor por defecto iniciales
        this.currentValidColor = firstCard.getColor();
        this.currentValidValue = firstCard.getValue();

        // Aplicar reglas especiales para la primera carta
        Value firstValue = firstCard.getValue();

        if (this.currentValidColor == Color.WILD) {
            // si la primera carta es un WILD se elige un color aleatorio
            Random random = new Random();
            Color[] playableColors = {Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE};
            int randomIndex = random.nextInt(playableColors.length);
            this.currentValidColor = playableColors[randomIndex];
            this.currentValidValue = null;
        }

        // por ahora, se considera que el humano siempre empieza
        this.currentPlayer = this.humanPlayer;
    }

    /**
     * Se llama cada vez que el turno pasa de un jugador a otro.(Funcion que pertenece al controlador de los eventos)
     * @param currentPlayer El jugador que ahora tiene el turno.
     */
    public void onTurnChanged(Player currentPlayer) {
    }

    /**
     * Reparte la mano inicial de cartas a ambos jugadores.
     *  Se invoca al inicio de la partida y distribuye una cantidad fija de cartas
     *  (5 cartas) a cada jugador.
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
     * @param player El jugador que realizo la jugada.
     * @param card   La carta especifica que fue jugada y ahora está en la cima.
     */
    @Override
    public boolean playCard(Player player, Card card) {
        player.playCard(card);
        this.discardStack.discard(card);
        if (card instanceof ActionCard) {
            this.applyCardEffect(card, player);
        }
        //this.applyCardEffect(card, player);
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
     * @param card La carta jugada.
     * @param playerWhoPlayed El jugador que jugó la carta.
     * @see #playCard
     */
    private void applyCardEffect(Card card, Player playerWhoPlayed) {
        Player opponent;
        if (playerWhoPlayed == this.humanPlayer) {
            opponent = this.machinePlayer;
        } else {
            opponent = this.humanPlayer;
        }
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
     * Determina quien es el siguiente jugador en el orden actual.
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
     * Fuerza a un jugador a tomar cartas ya sea debido a un efecto (+2, +4)
     * o una penalización (no decir UNO).
     * @param player        El jugador que va a tomar cartas.
     * @param numberOfCards La cantidad de cartas que será forzado a tomar.
     */
    @Override
    public void forceDraw(Player player, int numberOfCards) {
        int cardsActuallyDrawn = 0; // Contador por si no hay suficientes cartas TODO: esto no se me hace con sentido, ademas lo hago mas simple con los cambios.
        if (this.deck.getNumeroCartas() < numberOfCards) {
            this.recyclingDeck();
        }
        for (int i = 0; i < numberOfCards; i++) {
            Card card = this.deck.takeCard();
            if (card != null) {//TODO: Se borra si se contempla innecesaria.
                player.addCard(card);
                //cardsActuallyDrawn++;
            } else {
                break; // No hay más cartas
            }
            // Notificar por cada carta robada individualmente (si es necesario)
            // onPlayerDrewCard(player, card);  }
        }
        //return cardsActuallyDrawn;
    }

    /**
     * Se llama después de que un jugador ha elegido un color para un comodín,
     * o al inicio si la primera carta establece un color.
     * @param color El color que ahora está activo en el juego.
     */
    @Override
    public void onColorChosen(Color color) {
        this.currentValidColor = color;
        // ya no importa el valor anterior
        this.currentValidValue = null;
    }

    /**
     * Verifica si el estado UNO de un jugador ha cambiado y llama a onUnoStateChanged.
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
     * TODO: Explicacion: voy a cambiar este metodo, a uno que solo recicle el mazo, no tiene sentido verificar si esta vacio, es mejor saber si tiene la capacidad
     */
    @Override
    public void recyclingDeck() {
        ArrayList<Card> recycledCards = (ArrayList<Card>) this.discardStack.recycleDeck();
        /*
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
         */
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
     * Retorna el valor numérico o especial que actualmente está en efecto,
     * solo si la ultima carta no fue un comodín.
     * @return El valor válido actual, o null si se jugó un comodín.
     */
    public Value getCurrentValidValue() {
        return this.currentValidValue;
    }

    /**
     * Retorna la instancia del mazo principal de cartas.
     * @return El mazo de cartas.
     */
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

    @Override
    public Card drawTurnCard(Player player) {
        Card drawnCard = takeSingleCardFromDeckInternal();
        if (drawnCard != null) {
            player.addCard(drawnCard);
            // onPlayerDrewCard(player, drawnCard); // Posible notificación futura
        }
        // devolver la carta robada (o null si no se pudo robar)
        return drawnCard;
    }

    /**
     * Tomar una única carta del mazo,
     * manejando el reciclaje si es necesario.
     * @return La Card tomada, o null si no hay cartas disponibles.
     * TODO: innecesaria, ver si algo esta vacio es ineficiente, mejor saber si tiene la capacidad, si quieres tomar 2 cartas y solo pero solo tienes una, es una posible excepcion.
     */
    private Card takeSingleCardFromDeckInternal() {
        // Verificar si el mazo esta vacio ANTES de intentar tomar
        if (this.deck.getNumeroCartas() == 0) {
            this.recyclingDeck();
        }
        if (this.deck.getNumeroCartas() == 0) {
            System.err.println("Advertencia: El mazo y la pila de descarte están vacíos.");
            return null;
        }
        return this.deck.takeCard();
    }
}
