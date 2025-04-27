package univalle.tedesoft.uno.model.State;

import univalle.tedesoft.uno.model.Cards.Card;
import univalle.tedesoft.uno.model.Decks.Deck;
import univalle.tedesoft.uno.model.Decks.DiscardPile;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;
import univalle.tedesoft.uno.model.Players.HumanPlayer;
import univalle.tedesoft.uno.model.Players.MachinePlayer;
import univalle.tedesoft.uno.model.Players.Player;

import java.util.ArrayList;

/**
 * TODO: esta debe ser la clase principal que representa el estado del juego
 * Debe orquestar los turnos, el mazo, los jugadores y la pila de descarte.
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

    /**
     * Constructor de GameState, refleja la creacio * * @param humanPlayer La instancia del jugador humano.
     *
     * @param machinePlayer La instancia del jugador máquina.
     */
    public GameState(HumanPlayer humanPlayer, MachinePlayer machinePlayer) {
        this.humanPlayer = humanPlayer;
        this.machinePlayer = machinePlayer;

        this.deck = new Deck();
        this.discardStack = new DiscardPile();

        this.currentPlayer = null;
        this.currentValidColor = null;
        this.currentValidValue = null;
    }

    /**
     * Se llama una unica vez cuando el juego ha sido configurado(Funcion que pertenece al controlador de vistas) * (mazo creado, cartas repartidas, primera carta volteada) y está listo para empezar.
     */
    @Override
    public void onGameStart() {
        this.deck.shuffle();
        this.dealInitialCards();

        // Sacar una carta y colocarla en la pila de descarte
        Card firstCard = this.deck.takeCard();
        this.discardStack.discard(firstCard);

        // Establecer al jugador humano como el jugador inicial
        this.currentPlayer = this.humanPlayer;
        // Establecer el color y valor válidos iniciales
        this.currentValidColor = firstCard.getColor();
        this.currentValidValue = firstCard.getValue();

        System.out.println("Juego iniciado. Primera carta: " + currentValidValue + " " + currentValidColor);
        System.out.println("Turno de: " + currentPlayer.getName());
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
    public void onCardPlayed(Player player, Card card) {
        player.playCard(card);
        discardStack.discard(card);
    }

    /**
     * Se llama cuando un jugador toma una carta del mazo (ya sea por accion voluntaria
     * o por efecto de una carta especial, o penalización).
     *
     * @param player    El jugador que tomo la carta.
     * @param drawnCard La carta especifica que fue tomado.
     */
    public void onPlayerDrewCard(Player player, Card drawnCard) {
    }

    /**
     * Se llama cuando la mano de un jugador ha cambiado (se añadieron o quitaron cartas).
     * Esto puede ser resultado de jugar, tomar cartas, o ser penalizado.
     *
     * @param player El jugador cuya mano necesita ser actualizada en la vista.
     */
    public void onHandChanged(Player player) {
    }

    /**
     * Se llama específicamente cuando un jugador es forzado a tomar cartas
     * debido a un efecto (+2, +4) o una penalización (no decir UNO).
     *
     * @param player        El jugador que va a tomar cartas.
     * @param numberOfCards La cantidad de cartas que será forzado a tomar.
     */
    @Override
    public void onForceDraw(Player player, int numberOfCards) {
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
            if (cardsActuallyDrawn > 0) {
                this.onHandChanged(player);
            }
        }
    }

    /**
     * Se llama cuando un jugador es saltado (Skip, Reverse, o +2/+4).
     *
     * @param skippedPlayer El jugador cuyo turno ha sido saltado.
     */
    public void onPlayerSkipped(Player skippedPlayer) {
    }

    /**
     * Se llama tras jugar un comodín (Change Color),
     * indicando que el jugador que la jugó debe elegir un color.
     *
     * @param player El jugador que debe realizar la elección.
     */
    public void onMustChooseColor(Player player) {
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
     * Se llama cuando el estado de "tener una sola carta" de un jugador cambia.
     *
     * @param player     El jugador afectado.
     * @param hasOneCard true si el jugador AHORA tiene exactamente una carta,
     *                   false si tenía una y ahora tiene más (ej. por robar penalización).
     */
    public void onUnoStateChanged(Player player, boolean hasOneCard) {
    }

    /**
     * Se llama cuando un jugador (humano o máquina) intenta declarar "UNO".
     *
     * @param player  El jugador que declaró UNO.
     * @param success true si la declaración fue válida, false si fue inválida (tenía más de 1 carta).
     */
    public void onUnoDeclared(Player player, boolean success) {
    }

    /**
     * Se llama cuando el juego termina porque un jugador se ha quedado sin cartas.
     *
     * @param winner El jugador que ganó la partida.
     */
    public void onGameOver(Player winner) {
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
            // se relleno y barajo el mazo, se llama al metodo de notificacion
            this.onEmptyDeck();
        } else {
            System.out.println("No hay suficientes cartas en la pila de descarte para reciclar.");
        }
    }
}
