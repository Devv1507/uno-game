package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

/**
 * Representa una carta de accion tipo Skip en el juego UNO.
 * Esta carta hace que el siguiente jugador pierda su turno.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class SkipCard extends ActionCard{
    /**
     * Constructor de la clase SkipCard.
     *
     * @param color Color de la carta
     */
    public SkipCard(Color color) {
        super(color, Value.SKIP);
    }
}
