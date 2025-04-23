package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

/**
 * Representa una carta numerica del juego UNO.
 * Hereda de la clase Card y no posee efectos especiales.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class NumberCard extends Card {
    /**
     * Constructor de la clase NumberCard.
     *
     * @param color Color de la carta
     * @param value Valor numerico de la carta (de ZERO a NINE)
     */
    public NumberCard(Color color, Value value) {
        super(color, value);  // value será por ejemplo Value.FIVE
    }
}