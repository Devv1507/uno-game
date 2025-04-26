package univalle.tedesoft.uno.model.State;

import univalle.tedesoft.uno.model.Cards.Card;
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
     * Se llama una unica vez cuando el juego ha sido configurado
     * (mazo creado, cartas repartidas, primera carta volteada) y está listo para empezar.
     * @param game La instancia del juego actual.
     */
    void onGameStart(GameState game);

    /**
     * Se llama cada vez que el turno pasa de un jugador a otro.
     * @param currentPlayer El jugador que ahora tiene el turno.
     */
    void onTurnChanged(Player currentPlayer);

    /**
     * Funcion que reparte la primera mano de cartas a los jugadores.
     * @param humanPlayer jugador humano.
     * @param machinePlayer jugador maquina controlado por el sistema.
     */
    void dealInitialCards(Player humanPlayer, Player machinePlayer );
    /**
     * Se llama cuando un jugador ha jugado exitosamente una carta.
     * @param player El jugador que realizo la jugada.
     * @param card La carta especifica que fue jugada y ahora está en la cima.
     */
    void onCardPlayed(Player player, Card card);

    /**
     * (Quiero borrar esto, posiblemente pertenezca a la vista)
     * Se llama cuando un jugador toma una carta del mazo (ya sea por accion voluntaria
     * o por efecto de una carta especial, o penalización).
     * @param player    El jugador que tomo la carta.
     * @param drawnCard La carta especifica que fue tomado.
     */
    void onPlayerDrewCard(Player player, Card drawnCard);

    /**
     * (Tambien es parte de la vista)
     * Se llama cuando la mano de un jugador ha cambiado (se añadieron o quitaron cartas).
     * Esto puede ser resultado de jugar, tomar cartas, o ser penalizado.
     * @param player El jugador cuya mano necesita ser actualizada en la vista.
     */
    void onHandChanged(Player player);

    /**
     * Se llama específicamente cuando un jugador es forzado a tomar cartas
     * debido a un efecto (+2, +4) o una penalización (no decir UNO).
     * @param player        El jugador que va a tomar cartas.
     * @param numberOfCards La cantidad de cartas que será forzado a tomar.
     */
    void onForceDraw(Player player, int numberOfCards);

    /**
     * (Siento que este tambien es un metodo del controlador, dado que solo el controlador puede quitar turnos
     * (Se puede plantar las sumas de cartas, mas el skip y el reverse solo con la logica implementada)
     * Se llama cuando un jugador es saltado (Skip, Reverse, o +2/+4).
     * @param skippedPlayer El jugador cuyo turno ha sido saltado.
     */
    void onPlayerSkipped(Player skippedPlayer);

    /**
     * (Para esto necesito la logica del juego, que se plantea en el controlador)
     * Se llama tras jugar un comodín (Change Color),
     * indicando que el jugador que la jugó debe elegir un color.
     * @param player El jugador que debe realizar la elección.
     */
    void onMustChooseColor(Player player);

    /**
     * Se llama después de que un jugador ha elegido un color para un comodín,
     * o al inicio si la primera carta establece un color.
     * @param color El color que ahora está activo en el juego.
     */
    void onColorChosen(Color color);

    /**
     * Se llama cuando el estado de "tener una sola carta" de un jugador cambia.
     * @param player     El jugador afectado.
     * @param hasOneCard true si el jugador AHORA tiene exactamente una carta,
     *                   false si tenía una y ahora tiene más (ej. por robar penalización).
     */
    void onUnoStateChanged(Player player, boolean hasOneCard);

    /**
     * Se llama cuando un jugador (humano o máquina) intenta declarar "UNO".
     * @param player  El jugador que declaró UNO.
     * @param success true si la declaración fue válida, false si fue inválida (tenía más de 1 carta).
     */
    void onUnoDeclared(Player player, boolean success);

    /**
     * Se llama cuando el juego termina porque un jugador se ha quedado sin cartas.
     * @param winner El jugador que ganó la partida.
     */
    void onGameOver(Player winner);

    /**
     * Se llama cuando el mazo se ha agotado, por lo que se tiene que rellenar
     * y barajar usando las cartas de la pila de descarte.
     */
    void onDeckShuffled();
}
