package univalle.tedesoft.uno.exceptions;

/**
 * Excepción lanzada cuando se intenta tomar una carta de un mazo (Deck)
 * que está completamente vacío y no hay cartas en la pila de descarte
 * para reciclar. Indica una situación potencialmente anómala en el juego.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */
public class EmptyDeckException extends Exception {

    public EmptyDeckException(String message) {
        super(message);
    }

    public EmptyDeckException(String message, Throwable cause) {
        super(message, cause);
    }
}
