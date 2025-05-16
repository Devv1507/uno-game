package univalle.tedesoft.uno.model.State;

import univalle.tedesoft.uno.exceptions.EmptyDeckException;
import univalle.tedesoft.uno.exceptions.InvalidPlayException;
import univalle.tedesoft.uno.model.Cards.Card;
import univalle.tedesoft.uno.model.Decks.Deck;
import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Players.Player;

/**
 * Contrato para reaccionar a los eventos principales del juego UNO.
 * El GameController implementará esta interfaz para actualizar la UI basada en estos eventos.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public interface IGameState {
    /**
     * Retorna el mazo del juego.
     * @return el mazo con todas las cartas que están disponibles para robar/tomar.
     */
    Deck getDeck();

    /**
     * Se llama una unica vez cuando el juego ha sido configurado
     * (mazo creado, cartas repartidas, primera carta volteada) y está listo para empezar.
     */
    void onGameStart();

    /**
     * Reparte la primera mano de cartas a los jugadores.
     */
    void dealInitialCards();

    /**
     * Se llama cuando un jugador ha jugado exitosamente una carta.
     * @param player El jugador que realizo la jugada.
     * @param card La carta especifica que fue jugada y ahora está en la cima.
     */
    boolean playCard(Player player, Card card) throws InvalidPlayException;

    /**
     * Se llama específicamente cuando un jugador es forzado a tomar cartas
     * debido a un efecto (+2, +4) o una penalización (no decir UNO).
     * @param player        El jugador que va a tomar cartas.
     * @param numberOfCards La cantidad de cartas que será forzado a tomar.
     */
    void forceDraw(Player player, int numberOfCards);

    /**
     * Se llama después de que un jugador ha elegido un color para un comodín,
     * o al inicio si la primera carta establece un color.
     * @param color El color que ahora está activo en el juego.
     */
    void onColorChosen(Color color);

    /**
     * Se llama cuando el mazo se ha agotado, por lo que se tiene que rellenar
     * y barajar usando las cartas de la pila de descarte.
     */
    void recyclingDeck();

    /**
     * Determina si la carta dada se puede jugar de acuerdo con las reglas del UNO.
     */
    boolean isValidPlay(Card card);

    /**
     * Comprueba si el juego ha terminado.
     * @return true si el juego ha terminado, si no false.
     */
    boolean isGameOver();

    /**
     * Devuelve la carta superior de la pila de descarte en el estado actual del juego.
     * @return el objeto que representa la carta superior de la pila de descarte.
     */
    Card getTopDiscardCard();

    /**
     * Recupera el color valido en la ronda actual.
     * @return el color valido actual del juego, que determina el color de las cartas jugables.
     */
    Color getCurrentValidColor();

    /**
     * Recupera el jugador actual cuyo turno esta activo.
     * @return el objeto que representa al jugador activo en el turno actual.
     */
    Player getCurrentPlayer();

    /**
     * Invocado cuando un jugador debe elegir un color para continuar el juego.
     * @param player El jugador que debe seleccionar un color.
     */
    void onMustChooseColor(Player player);

    /**
     * Retorna al ganador del juego una vez que este termina.
     * @return la instancia de Player que representa al ganador del juego,
     * o null si aún no hay un ganador o el juego no ha terminado.
     */
    Player getWinner();

    /**
     * Retorna una descripción textual de una carta dada, incluyendo su valor y color.
     * @param card La carta para la que se proporcionará la descripción.
     * @return un texto que contiene la descripción de la carta.
     */
    String getCardDescription(Card card);

    /**
     * Maneja la acción de un jugador que roba una carta del mazo durante su turno.
     * @param player El jugador que está robando la carta.
     * @return La Card robada, o null si el mazo y la pila de descarte están completamente vacíos.
     */
    Card drawTurnCard(Player player) throws EmptyDeckException;

    /**
     * Avanza el turno al siguiente jugador. Gestiona la transición entre el jugador actual y
     * y el siguiente en la secuencia lógica del juego 2v2. I.e. si un jugador usa una carta SKIP,
     * el siguiente jugador debe perder su turno.
     */
    void advanceTurn();

    /**
     * Aplica una penalización al jugador especificado por no haber declarado "UNO"
     * de acuerdo con las reglas del juego.
     * @param playerToPenalize El jugador que recibirá la penalización.
     */
    void penalizePlayerForUno(Player playerToPenalize);

    /**
     * Registra que el jugador especificado ha declarado "UNO" correctamente.
     * @param player El jugador que ha declarado "UNO".
     */
    void playerDeclaresUno(Player player);

    /**
     * Aplica cualquier penalización o efecto de robo pendiente al jugador humano en el juego.
     */
    void applyPendingDrawsToHuman();
}
