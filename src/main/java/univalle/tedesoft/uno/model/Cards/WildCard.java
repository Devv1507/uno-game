package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

/**
 * Representa una carta comodin (Wild) en el juego UNO.
 * Esta carta permite al jugador cambiar el color actual del juego.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class WildCard extends ActionCard {
    /**
     * Constructor de la clase WildCard.
     * No requiere color especifico al ser creada, ya que el color
     * se selecciona por el jugador al momento de jugarla.
     */
    public WildCard() {
        super(Color.WILD, Value.WILD);
    }
}
