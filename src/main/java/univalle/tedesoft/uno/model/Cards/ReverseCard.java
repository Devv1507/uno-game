package univalle.tedesoft.uno.model.Cards;

import univalle.tedesoft.uno.model.Enum.Color;
import univalle.tedesoft.uno.model.Enum.Value;

/**
 * Representa una carta de accion tipo Reverse en el juego UNO.
 * Esta carta revierte el orden de turno en partidas con mas de dos jugadores.
 * En partidas de dos jugadores actua como una carta de salto.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class ReverseCard extends ActionCard{
    /**
     * Constructor de la clase ReverseCard.
     *
     * @param color Color de la carta
     */
    public ReverseCard(Color color) {
        super(color, Value.REVERSE);
    }
    /**
     * Aplica el efecto de la carta Reverse.
     * Este metodo debe ser complementado con la logica de juego para cambiar
     * el orden del turno o saltar al oponente.
     */
    public void appliedEffect() {
        System.out.println("¡El oponente revierte el sentido del juego!");
    }
}
