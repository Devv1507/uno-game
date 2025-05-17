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

    /**
     * Constructor que crea una nueva excepción con el mensaje detallado especificado.
     * @param message El mensaje detallado.
     */
    public EmptyDeckException(String message) {
        super(message);
    }

    /**
     * Constructor que crea una nueva excepción con el mensaje detallado y la causa especificados.
     * @param message El mensaje detallado.
     * @param cause La causa de la excepción.
     */
    public EmptyDeckException(String message, Throwable cause) {
        super(message, cause);
    }
}
