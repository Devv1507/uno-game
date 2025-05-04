package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

/**
 * Representa una carta comodin +4 (Wild Draw Four) en el juego UNO.
 * Esta carta permite al jugador cambiar el color actual del juego y
 * obliga al siguiente jugador a robar cuatro cartas.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class WildDrawFourCard extends ActionCard {
    /**
     * Constructor de la clase WildDrawFourCard.
     * Al igual que la carta Wild, no requiere color al ser creada.
     */
    public WildDrawFourCard() {
        super(Color.WILD, Value.WILD_DRAW_FOUR);
    }
}
