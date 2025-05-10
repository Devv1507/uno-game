package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

/**
 * Clase abstracta que representa una carta de accion en el juego UNO.
 * Hereda de la clase base Card y define un metodo abstracto que debera
 * ser implementado por las subclases para aplicar su efecto.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public abstract class ActionCard extends Card {
    /**
     * Constructor de la clase ActionCard.
     *
     * @param color Color de la carta
     * @param value Valor de la carta
     */
    public ActionCard(Color color, Value value) {
        super(color, value);
    }
}