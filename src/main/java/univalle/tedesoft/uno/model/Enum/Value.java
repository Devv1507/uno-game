package univalle.tedesoft.uno.model.Enum;
/**
 * Enum que representa los posibles valores que puede tener una carta en el juego UNO.
 * Incluye valores numericos (de ZERO a NINE), cartas de accion (SKIP, REVERSE, DRAW_TWO)
 * y cartas comodin (WILD, WILD_DRAW_FOUR).
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public enum Value {
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
    SKIP,
    DRAW_TWO,
    WILD,
    WILD_DRAW_FOUR
}
