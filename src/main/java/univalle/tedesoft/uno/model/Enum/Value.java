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
    /** Valor numérico Cero. */
    ZERO,
    /** Valor numérico Uno. */
    ONE,
    /** Valor numérico Dos. */
    TWO,
    /** Valor numérico Tres. */
    THREE,
    /** Valor numérico Cuatro. */
    FOUR,
    /** Valor numérico Cinco. */
    FIVE,
    /** Valor numérico Seis. */
    SIX,
    /** Valor numérico Siete. */
    SEVEN,
    /** Valor numérico Ocho. */
    EIGHT,
    /** Valor numérico Nueve. */
    NINE,
    /** Acción de Saltarse el turno del siguiente jugador. */
    SKIP,
    /** Acción de obligar al siguiente jugador a tomar dos cartas. */
    DRAW_TWO,
    /** Acción de Comodín, permite cambiar el color. */
    WILD,
    /** Acción de Comodín, permite cambiar el color y obliga al siguiente jugador a tomar cuatro cartas. */
    WILD_DRAW_FOUR
}
