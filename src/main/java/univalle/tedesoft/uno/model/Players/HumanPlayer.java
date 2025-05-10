package univalle.tedesoft.uno.model.Players;

/**
 * Clase que representa al jugador humano.
 *
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class HumanPlayer extends Player {
    /**
     * Constructor para la clase HumanPlayer, recibe solo el nombre del jugador.
     * @param playerName nombre del jugador.
     */
    public HumanPlayer(String playerName) {
        this.name = playerName;
    }
}
