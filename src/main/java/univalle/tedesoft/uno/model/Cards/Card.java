package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

/**
 * Clase abstracta que representa una carta generica del juego UNO.
 * Contiene atributos comunes como el color, el valor y un indicador
 * de si la carta ya fue usada o no.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public abstract class Card {
    /** Color de la carta (puede ser rojo, azul, verde, amarillo o comodin) */
    public Color color;
    /** Valor de la carta (numero, accion o comodin) */
    public Value value;
    /** Indica si la carta ya fue utilizada en el juego */
    public Boolean isUsed = false;
    /**
     * Constructor de la clase Card.
     *
     * @param color Color de la carta
     * @param value Valor de la carta
     */
    public Card(Color color, Value value) {
        this.color = color;
        this.value = value;
    }
    /**
     * Devuelve el color de la carta.
     *
     * @return Color de la carta
     */
    public Color getColor() {
        return color;
    }
    /**
     * Devuelve el valor de la carta.
     *
     * @return Valor de la carta
     */
    public Value getValue() {
        return value;
    }
}
