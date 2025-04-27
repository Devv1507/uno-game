package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

/**
 * Representa una carta de accion +2 (Draw Two) en el juego UNO.
 * Esta carta obliga al siguiente jugador a robar dos cartas y perder su turno.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class DrawTwoCard extends ActionCard {
    /**
     * Constructor de la clase DrawTwoCard.
     *
     * @param color Color de la carta (rojo, azul, verde o amarillo)
     */
    public DrawTwoCard(Color color) {
        super(color, Value.DRAW_TWO);
    }
    /**
     * Aplica el efecto de la carta Draw Two.
     * El siguiente jugador deberia robar dos cartas y perder su turno.
     * La logica completa debe ser implementada en el controlador del juego.
     */
    public void appliedEffect() {
        System.out.println("¡El oponente roba 2 cartas!");
    }
}